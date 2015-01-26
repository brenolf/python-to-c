package AST;

import java.util.*;

public class IfStmt extends CompoundStmt {
	
	public IfStmt(Test condition, Suite body, ArrayList<Test> elifCondition, ArrayList<Suite> elifBody, Suite elseBody){

		this.condition = condition;
		this.body = body;
		this.elifCondition = elifCondition;
		this.elifBody = elifBody;
		this.elseBody = elseBody;

	}
	
	public void genC(Pw pw){
		
		pw.print("if(");
		condition.genC(pw);
		pw.printi("){\n");
		pw.add();
		body.genC(pw);
		pw.sub();
		pw.print("}");

		int l = elifCondition.size();

		if(l == 0 && elseBody == null)
			pw.printi("\n");

		for(int i = 0; i < l; i++){
			pw.printi(" else if(");
			elifCondition.get(i).genC(pw);
			pw.printi("){\n");
			pw.add();
			elifBody.get(i).genC(pw);
			pw.sub();
			pw.print("}");

			pw.print((i + 1 >= l) ? "\n" : " ");
		}

		if(elseBody != null){
			pw.printi(" else {\n");
			pw.add();
			elseBody.genC(pw);
			pw.sub();
			pw.print("}\n");
		}
	}

	private Test condition;
	private Suite body;

	private ArrayList<Test> elifCondition;
	private ArrayList<Suite> elifBody;

	private Suite elseBody;
}