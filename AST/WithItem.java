package AST;

import java.util.*;

public class WithItem {
	
	public WithItem(Test test, Expr as){
		this.test = test;
		this.as = as;
	}

	public void genC(Pw pw){
		
		if(as != null)
			as.genC(pw);

		pw.printi(" = ");

		test.genC(pw);

		pw.printi(";\n");
	}

	private Test test;
	private Expr as;
}