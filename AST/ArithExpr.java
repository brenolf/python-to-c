package AST;

import java.util.*;
import AuxComp.TypeManager;

public class ArithExpr {

	public ArithExpr(ArrayList<Term> terms, ArrayList<String> operators, boolean hasStrings){
		this.terms = terms;
		this.operators = operators;
		this.hasStrings = hasStrings;
	}

	public void genC(Pw pw){
		
		int omax = operators.size();

		for(int i = 0, l = terms.size(); i < l; i++){	
			Term t = terms.get(i);

			if(hasStrings && i + 1 < l)
				pw.printi("strcat_aux(");

			t.genC(pw);

			if(hasStrings && i + 1 < l)
				pw.printi(", ");
			
			if(omax > i && !hasStrings)
				pw.printi(" " + operators.get(i) + " ");
		}

		if(hasStrings){
			for(int i = 0, l = terms.size() - 1; i < l; i++)
				pw.printi(")");
		}
	}

	private ArrayList<Term> terms;
	private ArrayList<String> operators;
	private boolean hasStrings;
}