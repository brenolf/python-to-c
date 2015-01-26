package AST;

import java.util.*;

public class ExprList {
	
	public ExprList(ArrayList<Expr> exprs){
		this.exprs = exprs;
	}
	
	public void genC(Pw pw){
		
		for(int i = 0, l = exprs.size(); i < l; i++){
			exprs.get(i).genC(pw);

			if(i + 1 < l)
				pw.printi(", ");
		}
	}

	public int size(){
		return exprs.size();
	}

	public Expr get(int i){
		return exprs.get(i);
	}

	private ArrayList<Expr> exprs;
}