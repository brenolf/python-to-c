package AuxComp;

import java.util.*;
import Lexer.Symbol;
import Lexer.Word;

public class Expression {

	public Expression(){
		queue = new ArrayList<String>();
		types = new ArrayList<Integer>();
		this.reset();
	}

	public void landmark(){
		this.lm = queue.size();
	}

	public void wipe(){
		queue.subList(lm, queue.size()).clear();
	}

	public int steps(){
		return queue.size() - lm;
	}

	public void pop(){
		this.queue.remove(queue.size() - 1);
		this.types.remove(types.size() - 1);
	}

	public int end(){
		return queue.size();
	}

	public void addVar(String var){
		addVar(var, VARIABLE);
	}

	public void addVar(String var, int type){
		this.types.add(type);
		this.queue.add(var);
	}

	public void addOpr(Symbol opr){
		this.types.add(OPERATOR);
		this.queue.add(((Symbol) opr).name());
		this.opr++;
	}

	private boolean in(int index, String...list){
		if(types.get(index) == VARIABLE)
			return false;

		String t = queue.get(index);

		for(String s : list){
			if(s.equals(t))
				return true;
		}

		return false;
	}

	public void evaluate(){

		for(int i = 0, l = queue.size(); i < l; i++){
			String q = queue.get(i);
			int t = types.get(i);

			if(i + 1 < l && in(i + 1, "LT", "GT", "EQ", "LE", "GE", "NEQ", "NEQC")){
				queue.set(i, "INT");
				q = "INT";
				queue.remove(i + 2);
				queue.remove(i + 1);
				types.remove(i + 2);
				types.remove(i + 1);
				l -= 2;
			}

			if(t == OPERATOR || t == NEUTRAL)
				continue;

			if(!q.equals("VOID") && firstNonVoidType == null)
				firstNonVoidType = q;

			if(TypeManager.isList(q))
				hasList = true;

			else if(t == CLASS)
				hasClass = true;

			else if(q.equals("INT"))
				hasInt = true;

			else if(q.equals("DOUBLE"))
				hasDouble = true;

			else if(q.equals("STRING"))
				hasString = true;

			else if(q.equals("VOID")){
				hasVoid = true;
				badVoid |= (t == FUNCTION);
			}
		}
	}

	public void reset(){
		queue.clear();
		types.clear();

		hasInt = false;
		hasDouble = false;
		hasList = false;
		hasClass = false;
		hasString = false;
		hasVoid = false;
		badVoid = false;

		lm = 0;
		opr = 0;

		firstNonVoidType = null;
	}

	public boolean sliceIs(int from, int to, String options[][]){
		if(from < 0)
			from += queue.size();

		if(to < 0)
			to += queue.size();

		for(int i = from, k = 0; i < to; i++, k++){
			boolean ok = false;
			String str = queue.get(i);

			for(int j = 0; j < options[k].length; j++)
				if(options[k][j].equals(str))
					ok = true;

			if(!ok)
				return false;
		}

		return true;
	}

	public boolean isNumeric(){
		return hasInt && hasDouble && !hasString && !hasList && !badVoid;
	}

	public boolean hasNotOnlyString(){
		return hasString && (hasList || hasDouble || hasInt || badVoid);
	}

	public boolean hasNotOnlyLists(){
		return hasList && (hasString || hasDouble || hasInt || badVoid);
	}

	public boolean hasNotOnlyClasses(){
		return hasClass && (hasList || hasString || hasDouble || hasInt || badVoid);
	}

	public boolean isList(){
		return hasList && !hasString && !hasDouble && !hasInt && !badVoid;
	}

	public boolean isVoid(){
		return hasVoid && !hasList && !hasString && !hasDouble && !hasInt;
	}

	public boolean isIllegal(){
		return badVoid;
	}

	public boolean illegalListOperation(){
		return hasList && opr > 0;
	}

	public boolean hasMixedLists(){
		if(hasNotOnlyLists())
			return false;

		String cases[] = {"STRING_LIST", "INT_LIST", "DOUBLE_LIST"};
		byte has[] = {0, 0, 0};
		int j = 0;

		for(String type : queue){
			if(types.get(j++) != VARIABLE)
				continue;

			for(int i = 0; i < cases.length; i++){
				if(type.toUpperCase().equals(cases[i]))
					has[i] = 1;
			}
		}

		return (has[0] + has[1] + has[2]) >= 2;
	}

	public boolean hasStrings(){
		int i = 0;

		for(String t : queue){
			if(types.get(i++) != VARIABLE)
				continue;

			if(t.equals("STRING")){
				return true;
			}
		}
		return false;
	}

	public String trace(){
		String ts = "";
		String names[] = {"N", "F", "V", "O", "C"};

		for(int i = 0, l = types.size(); i < l; i++)
			ts += names[types.get(i)] + (i + 1 < l ? ", " : "");

		return queue.toString() + "\n[" + ts + "]\n\n";
	}

	private ArrayList<Integer> types;
	private ArrayList<String> queue;
	public String firstNonVoidType;
	private boolean hasInt, hasDouble, hasList, hasString, hasVoid, badVoid, hasClass;
	private int lm = 0, opr = 0;

	public static final int NEUTRAL = 0, FUNCTION = 1, VARIABLE = 2, OPERATOR = 3, CLASS = 4;
}