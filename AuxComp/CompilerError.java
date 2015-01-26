package AuxComp;

import Lexer.Lexer;

public class CompilerError {

	public CompilerError() {
		this.quiet = false;
	}

	public void setQuiet(boolean quiet){
		this.quiet = quiet;
	}

	public void error(String msg, Lexer lexer){
		int colon = msg.indexOf(':');
		String error = msg.substring(0, colon + 1);
		String rest = msg.substring(colon + 1);

		msg = "\n\n\tProblem on line " + lexer.getLine() + ", last read: " + lexer.getToken()
		+ "\n\t" + ANSI_RED + error + ANSI_RESET
		+ rest + "\n\n";

		if(quiet){
			System.out.println(msg);
			System.exit(1);
		} else
			throw new RuntimeException(msg);
	}

	private boolean quiet;

	private static final String ANSI_RESET = "\u001B[0m";
	private static final String ANSI_BLACK = "\u001B[30m";
	private static final String ANSI_RED = "\u001B[31m";
	private static final String ANSI_GREEN = "\u001B[32m";
	private static final String ANSI_YELLOW = "\u001B[33m";
	private static final String ANSI_BLUE = "\u001B[34m";
	private static final String ANSI_PURPLE = "\u001B[35m";
	private static final String ANSI_CYAN = "\u001B[36m";
	private static final String ANSI_WHITE = "\u001B[37m";
}