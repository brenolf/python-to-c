package AST;

import java.util.*;

public class Suite {
	
	public Suite(ArrayList<Stmt> statements){
		this.statements = statements;
	}
	
	public void genC(Pw pw){
		
		for(Stmt s : statements)
			s.genC(pw);
	}

	public int size(){
		return statements.size();
	}

	public Stmt get(int i){
		return statements.get(i);
	}

	public void remove(int i){
		statements.remove(i);
	}

	private ArrayList<Stmt> statements;
}