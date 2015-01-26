package AST;

import java.util.*;

public class Test {

	public Test(OrTest orTest, OrTest condition, Test elseBody){
		this.orTest = orTest;
		this.condition = condition;
		this.elseBody = elseBody;
	}
	
	public void genC(Pw pw){
		
		if(condition == null)
			orTest.genC(pw);

		else {
			pw.print("if(");
			condition.genC(pw);
			pw.printi("){");
			pw.add();
			orTest.genC(pw);
			pw.sub();
			pw.print("} else {");
			pw.add();
			elseBody.genC(pw);
			pw.sub();
			pw.print("}");
		}
	}

	private OrTest orTest, condition;
	private Test elseBody;
}