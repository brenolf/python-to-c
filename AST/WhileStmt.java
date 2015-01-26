package AST;

import java.util.*;

public class WhileStmt extends CompoundStmt {
	
	public WhileStmt(Test condition, Suite body, Suite elseBody){
		this.condition = condition;
		this.body = body;
		this.elseBody = elseBody;
	}
	
	public void genC(Pw pw){
		
		pw.print("while(");
		condition.genC(pw);
		pw.printi("){\n");
		pw.add();
		body.genC(pw);
		pw.sub();
		pw.print("}\n");

		if(elseBody != null){
			pw.print("if(!(");
			condition.genC(pw);
			pw.printi(")) {\n");
			pw.add();
			elseBody.genC(pw);
			pw.sub();
			pw.print("}\n");
		}
	}

	private Test condition;
	private Suite body, elseBody;
}