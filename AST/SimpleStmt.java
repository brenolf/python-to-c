package AST;

import java.util.*;

public class SimpleStmt extends Stmt {
	
	public SimpleStmt(ArrayList<SmallStmt> statements){
		this.statements = statements;
	}
	
	public void genC(Pw pw){
		for(SmallStmt s : statements)
			s.genC(pw);
	}

	public int size(){
		return statements.size();
	}

	public SmallStmt get(int i){
		return statements.get(i);
	}

	ArrayList<SmallStmt> statements;
}