package AST;

import java.util.*;

public class Expr {

	public Expr(ArrayList<XorExpr> xors){
		this.xors = xors;
	}

	public void genC(Pw pw){
		
		for(int i = 0, l = xors.size(); i < l; i++){
			xors.get(i).genC(pw);
			pw.printi((i + 1 >= l) ? "" : " | ");
		}
	}

	private ArrayList<XorExpr> xors;
}