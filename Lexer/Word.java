package Lexer;

public enum Word {
	
	ASSIGN("="),
	ANDASSIGN("&="),
	DIVASSIGN("/="),
	MULTIASSIGN("*="),
	ORASSIGN("|="),
	MODASSIGN("%="),
	XORASSIGN("^="),
	PLUSASSIGN("+="),
	MINUSASSIGN("-="),	
	
	RIGHTPAR(")"),
	LEFTPAR("("),	
	RIGHTCURBRACKET("]"),
	LEFTCURBRACKET("["),
	COLON(":"),
	COMMA(","),
	SHELL(">>"),
	SEMICOLON(";"),
	
	BREAK("break"),
	CONTINUE("continue"),
	PRINT("print"),	
	RETURN("return"),
	
	ELIF("elif"),
	ELSE("else"),
	FOR("for"),
	IF("if"),
	IN("in"),
	WHILE("while"),     
	
	AS("as"),
	DEF("def"),
	EXCEPT("except"),
	FINALLY("finally"),
	TRY("try"),
	WITH("with"),
	CLASS("class"),
	SELF("self"),
	RANGE("range"),
	YIELD("yield"),

	
	ASTERISK("*"),
	DOUBLEASTERISK("**"),
	OR("or"),
	AND("and"),
	NOT("not"),
	IS("is"),
	LT("<"),
	GT(">"),
	EQ("=="),
	LE("<="),
	GE(">="),
	NEQ("<>"),
	NEQC("!="),
	PIPE("|"),
	XOR("^"),
	AMPERSAND("&"),
	PLUS("+"),
	MINUS("-"),
	DIV("/"),
	BACKSLASH("\\"),
	MOD("%"),
	FLOORDIV("//"),
	INVERTION("~"),
	GRAVEACCENT("`"),
	DOT("."),
	
	EOF("EOF"),
	ID("ID"),
	STRING("STRING"),
	NUM("NUM"),
	DEDENT("DEDENT"),
	INDENT("INDENT"),
	NEWLINE("NEWLINE"),
	INT("int");

	
	Word (String s){
		this.s = s;
	}

	public String get(){
		return this.s;
	}

	public char getChar(){
		return this.s.charAt(0);
	}

	private String s;

	public static final String reserved[] = {"BREAK", "CONTINUE", "PRINT", "RETURN", "ELIF", "ELSE", "FOR", "IF", "IN", "WHILE", "AS", "DEF", "EXCEPT", "FINALLY", "TRY", "WITH", "OR", "AND", "NOT", "IS", "CLASS", "SELF", "RANGE", "YIELD", "INT"};
	public static final String symbols[] = {"EQ", "LE", "GE", "NEQ", "NEQC", "DOUBLEASTERISK", "FLOORDIV", "ANDASSIGN", "DIVASSIGN", "MULTIASSIGN", "ORASSIGN", "MODASSIGN", "XORASSIGN", "PLUSASSIGN", "MINUSASSIGN", "SHELL", "ASTERISK", "LT", "GT", "PIPE", "XOR", "AMPERSAND", "PLUS", "MINUS", "DIV", "BACKSLASH", "MOD", "INVERTION", "GRAVEACCENT", "ASSIGN", "RIGHTPAR", "LEFTPAR", "RIGHTCURBRACKET", "LEFTCURBRACKET", "COLON", "COMMA", "SEMICOLON", "DOT"};
	
}