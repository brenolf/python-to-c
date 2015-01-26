package Lexer;

import java.util.Hashtable;
import AuxComp.*;

public class Lexer {
	private Symbol token;
	private String var, str;
	private double number;
	private int tokenPos;
	private int lineNumber;
	private char[] input;
	private static CompilerError ce;
	private static Hashtable<String, Symbol> keywordsTable;

	private static final boolean DEBUG = false;

	private int indentation = 0, indentcounter = 0, dedents = 0;
	private boolean newline = false, indentation_kept = false, isFloat = false, reference_obj = false;


	private final String DIGIT = "1", LETTER = "2";

	static {
		keywordsTable = new Hashtable<String, Symbol>();

		for(String s : Word.reserved)
			keywordsTable.put(Word.valueOf(s).get(), Symbol.valueOf(s));
	}

	public Lexer(char[] p_input, CompilerError ce){
		input = p_input;
		lineNumber = 1;
		tokenPos = 0;
		token = null;
		this.ce = ce;
	}

	public void error(String msg){
		ce.error(msg, this);
	}

	public String getVar(){
		return this.var;
	}

	public String getStr(){
		return this.str;
	}

	public double getNumber(){
		return this.number;
	}

	public String getNumberType(){
		return isFloat ? "DOUBLE" : "INT";
	}

	public int getTokenPos(){
		return this.tokenPos;
	}

	public int getLine(){
		return this.lineNumber + (matches(Symbol.NEWLINE) ? -1 : 0);
	}
	
	public Symbol getToken(){
		return this.token;
	}
	
	public boolean matches(Symbol...list){
		for(Symbol s : list){
			if(token == s)
				return true;
		}
		
		return false;
	}

	private void debug(){
		if(DEBUG)
			//System.out.println(getTokenValue());
			System.out.println(this.token.get());
	}

	private boolean is(char...list){
		for(char c : list){
			if(input[tokenPos] == c)
				return true;
		}
		return false;
	}

	private boolean is(String type){
		if(type.equals(DIGIT))
			return Character.isDigit(input[tokenPos]);

		else if(type.equals(LETTER))
			return Character.isLetter(input[tokenPos]);

		else
			return false;
	}

	public String getTokenValue(){
		return Word.valueOf(token.name()).get();
	}

	public boolean is4spaces(){
		int i = tokenPos;
		return i + 3 < input.length && input[i] == ' ' && input[i + 1] == ' ' 
		&& input[i + 2] == ' ' && input[i + 3] == ' ';
	}

	public void nextToken() {
		boolean has_reference_obj = reference_obj;

		if(dedents > 0){
			token = Symbol.DEDENT;
			dedents--;
			debug();
			return;
		}

		while(tokenPos < input.length && is('\r'))
			tokenPos++;

		if(is4spaces()){
			tokenPos += 3;
			input[tokenPos] = '\t';
		} else {
			while(tokenPos < input.length && is(' '))
				tokenPos++;
		}

		if(is('#')){
			while(!is('\n', '\0'))
				tokenPos++;

			nextToken();
			return;
		}

		if(tokenPos >= input.length - 1){
			if(indentation > 0){
				dedents = indentation;
				indentation = 0;
				nextToken();
				return;
			}

			token = Symbol.EOF;
			debug();

			return;
		}

		String aux = "";

		if(!is('\t', '\n') && indentation > 0 && newline && !indentation_kept){
			dedents = indentation;
			indentation = 0;
			nextToken();
			return;
		}

		indentation_kept = false;

		if(is('\n')){

			token = Symbol.NEWLINE;
			tokenPos++;
			lineNumber++;

			if(newline){
				nextToken();
				return;
			}

		} else if (is('\t')){

			if(newline){
				token = Symbol.INDENT;
				indentcounter = 0;

				while(is('\t') || is4spaces()){
					if(is4spaces())
						tokenPos += 3;

					tokenPos++;
					indentcounter++;
				}

				if(is('#', '\r', '\0', '\n')){
					nextToken();
					return;
				}

				if(indentcounter < indentation){
					dedents = indentation - indentcounter;
					indentation -= dedents;

					dedents--;
					token = Symbol.DEDENT;

				} else if(indentcounter > indentation)
				indentation = indentcounter;

				else{
					indentation_kept = true;
					nextToken();
					return;
				}

			} else {
				tokenPos++;
				nextToken();
				return;
			}

		} else if(is(DIGIT) || (is('.') && !reference_obj)){

			isFloat = false;
			boolean headless = true;

			while(is(DIGIT)){
				aux += input[tokenPos];
				tokenPos++;
				headless = false;
			}

			if(is('.')){
				isFloat = true;
				tokenPos++;

				aux += '.';

				if(!is(DIGIT) && headless)
					error("Lexicographic error: Invalid float number");

				while(is(DIGIT)){
					aux += input[tokenPos];
					tokenPos++;
				}
			} else if(is(LETTER))
			error("Lexicographic error: Invalid number");

			if(aux.length() > 10)
				error("Lexicographic error: Invalid number");

			number = Double.parseDouble(aux);

			if(number > Integer.MAX_VALUE)
				error("Lexicographic error: Invalid number");

			token = Symbol.NUM;

		} else if(is(LETTER)) {

			while(is(LETTER) || is(DIGIT) || is('_')){
				aux += input[tokenPos];
				tokenPos++;
			}

			token = (Symbol) keywordsTable.get(aux);
			
			if (token == null || token == Symbol.SELF || token == Symbol.INT) {
				token = Symbol.ID;
				var = aux;
				reference_obj = true;
			}

		} else if(is('_')) {

			int _counter = 1;
			tokenPos++;
			aux = "_";

			while(input[tokenPos] != '\0' && _counter < "__init__".length()){
				_counter++;
				aux += input[tokenPos++];
			}

			if(!aux.equals("__init__"))
				error("Lexicographic error: Symbol \"" + aux + "\" not found");

			token = Symbol.ID;
			var = aux;
			reference_obj = true;

		} else if(is('\'', '"')) {

			char stopper = input[tokenPos];

			tokenPos++;

			while(!is(stopper) || (is(stopper) && input[tokenPos - 1] == '\\')){
				aux += input[tokenPos];
				tokenPos++;
			}

			tokenPos++;

			str = aux;
			token = Symbol.STRING;

		} else {
			String s = "";
			boolean found = false;

			for(int i = 0, l = Word.symbols.length; i < l; i++){
				s = Word.valueOf(Word.symbols[i]).get();

				if(s.charAt(0) == input[tokenPos]){

					if(s.length() == 2 && s.charAt(1) != input[tokenPos + 1])
						continue;

					token = Symbol.valueOf(Word.symbols[i]);
					found = true;
					tokenPos += s.length();

					break;
				}
			}

			if(!found)
				error("Lexicographic error: Symbol \"" + input[tokenPos] + "\" not found");
		}

		newline = (token == Symbol.NEWLINE);

		reference_obj ^= has_reference_obj;

		debug();
	}
}