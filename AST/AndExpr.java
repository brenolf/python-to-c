package AST;

import java.util.*;

public class AndExpr {

	public AndExpr(ArrayList<ArithExpr> ariths){
		this.ariths = ariths;
	}

	public void genC(Pw pw){
		for(int i = 0, l = ariths.size(); i < l; i++){
			ariths.get(i).genC(pw);
			pw.printi((i + 1 >= l) ? "" : " & ");
		}
	}

	private ArrayList<ArithExpr> ariths;
}