package Lexer;

public enum Symbol {

	EOF(0), 
	ID(1),
	NUM(2),      
	PLUS(3),         
	MINUS(4),        
	MULT(5),         
	DIV(6),          
	MOD(7),
	ASSIGN(8),      	
	PLUSASSIGN(9),
	MINUSASSIGN(10),
	MULTIASSIGN(11),
	DIVASSIGN(12),
	MODASSIGN(13),
	ANDASSIGN(14),
	ORASSIGN(15),
	XORASSIGN(16),
	LEFTPAR(17),     
	RIGHTPAR(18),    
	SEMICOLON(19),   
	COLON(20),       
	COMMA(21),    
	LT(22),     
	LE(23),    
	GT(24),       
	GE(25),        
	NEQ(26),      
	NEQC(27),      
	EQ(28),   
	IN(29),
	NOT(30),
	IS(31),
	XOR(32),
	OR(33),
	AND(34),
	INVERTION(35), 
	FLOORDIV(36), 
	PRINT(37),
	BREAK(38),
	CONTINUE(39),
	RETURN(40),
	IF(41),
	ELIF(42),
	ELSE(43),
	WHILE(44),
	FOR(45),
	RANGE(46),
	DEF(47),
	CLASS(48),
	LEFTCURBRACKET(49),
	RIGHTCURBRACKET(50), 
	SELF(51),

	ASTERISK(52),
	DOUBLEASTERISK(53),
	PIPE(54),
	AMPERSAND(55),
	BACKSLASH(56),
	SHELL(57),
	AS(58),
	EXCEPT(59),
	FINALLY(60),
	TRY(61),
	WITH(62),	
	DEDENT(63),
	INDENT(64),
	NEWLINE(65),
	GRAVEACCENT(66),
	STRING(67),
	YIELD(68),
	DOT(69),
	INT(70),
	NOTIN(71),
	ISNOT(72),

	LastSymbol(99);


	Symbol(int s){
		this.s = s;
	}

	public int get(){
		return this.s;
	}

	private int s;
    
}