package AST;

import java.util.*;

public class ReturnStmt extends FlowStmt {
	
	public ReturnStmt(TestList tests, String type){
		this.tests = tests;
		this.type = type;
	}
	
	public void genC(Pw pw){
		
		if(type == null || type.equals("void") || tests == null)
			pw.print("return;");	
		else {
			pw.print("return ");
			tests.genC(pw);
			pw.printi(";");
		}
	}

	public String getType(){
		return type;
	}

	private TestList tests;
	private String type;
}