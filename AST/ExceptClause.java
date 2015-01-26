package AST;

import java.util.*;

public class ExceptClause {
	
	public ExceptClause(ArrayList<Test> tests){
		this.tests = tests;
	}
	
	public void genC(Pw pw){
		
		if(tests.size() >= 1)
			tests.get(0).genC(pw);

		if(tests.size() >= 2)
			tests.get(1).genC(pw);
	}

	private ArrayList<Test> tests;
}