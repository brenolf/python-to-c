package AST;

import java.util.*;

public class Term {

	public Term(ArrayList<Factor> factors, ArrayList<String> operators){
		this.factors = factors;
		this.operators = operators;
	}

	public void genC(Pw pw){
		
		int i = 0;

		for(Factor f : factors){
			f.genC(pw);

			if(operators.size() > i){
				String op = operators.get(i++);

				if(op.equals("//"))
					op = "/";

				pw.printi(" " + op + " ");
			}
		}
	}

	private ArrayList<Factor> factors;
	private ArrayList<String> operators;
}