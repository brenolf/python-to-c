package AST;

import java.util.*;

public class TryStmt extends CompoundStmt {
	
	public TryStmt(Suite body, ArrayList<ExceptClause> exceptCondition, ArrayList<Suite> exceptBody, Suite elseBody, Suite finallyBody){
		this.body = body;
		this.exceptCondition = exceptCondition;
		this.exceptBody = exceptBody;
		this.elseBody = elseBody;
		this.finallyBody = finallyBody;
	}

	public int genPrototype(Pw pw, int depth){
		int i = depth;
		this.begin = i;

		for(int l = exceptCondition.size(); i < l; i++)
			pw.print("void GLOBAL_TRY_FUNC_" + i + "(int signal);\n");

		this.end = i;

		return i;
	}

	public int genFunctions(Pw pw, int depth){
		int i = depth;

		for(Suite e : exceptBody){
			pw.print("void GLOBAL_TRY_FUNC_" + (i++) + "(int signal){\n");
			pw.add();
			e.genC(pw);
			pw.sub();
			pw.print("}\n\n");
		}

		return i;
	}
	
	public void genC(Pw pw){
		
		
		for(int i = this.begin; i < this.end; i++)
			pw.print("GLOBAL_SH_PUSH(GLOBAL_TRY_FUNC_" + i + ");\n");
		pw.printi("\n");

		for(ExceptClause e : exceptCondition)
			e.genC(pw);

		body.genC(pw);
		finallyBody.genC(pw);

		for(int i = this.begin; i < this.end; i++)
			pw.print("GLOBAL_SH_POP();\n");
		pw.printi("\n");
	}

	private Suite body, elseBody, finallyBody;
	private ArrayList<ExceptClause> exceptCondition;
	private ArrayList<Suite> exceptBody;
	private int begin = 0, end = 0;
}