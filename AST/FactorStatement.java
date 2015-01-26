package AST;

import java.util.*;

public class FactorStatement extends Factor {

	public FactorStatement(String signal, Factor statement){
		this.signal = signal;
		this.statement = statement;
	}

	public void genC(Pw pw){
		
		pw.printi(signal);
		statement.genC(pw);
	}

	public String getSignal(){
		return signal;
	}

	private String signal;
	private Factor statement;
}