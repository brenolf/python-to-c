package AST;

import java.util.*;

public class XorExpr {

	public XorExpr(ArrayList<AndExpr> ands){
		this.ands = ands;
	}

	public void genC(Pw pw){
		
		for(int i = 0, l = ands.size(); i < l; i++){
			ands.get(i).genC(pw);
			pw.printi((i + 1 >= l) ? "" : " ^ ");
		}
	}

	private ArrayList<AndExpr> ands;
}