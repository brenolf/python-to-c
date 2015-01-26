package AST;

import java.util.*;

public class Comparision extends NotTest {

	public Comparision(Expr left, ArrayList<String> operators, ArrayList<Boolean> strcmp, ArrayList<Expr> statements){
		this.left = left;
		this.operators = operators;
		this.statements = statements;
		this.strcmp = strcmp;
	}
	
	public void genC(Pw pw){
		
		boolean lookBack = false;

		if(strcmp.size() == 0 || !strcmp.get(0).booleanValue())
			left.genC(pw);
		else
			lookBack = true;

		for(int i = 0, l = operators.size(); i < l; i++){

			if(strcmp.get(i).booleanValue()){
				pw.printi("strcmp(");

				if(lookBack)
					left.genC(pw);
				else
					statements.get(i - 1).genC(pw);

				pw.printi(", ");

				statements.get(i).genC(pw);

				pw.printi(") " + operators.get(i) + " 0");

				continue;
			}

			pw.printi(" " + operators.get(i) + " ");
			statements.get(i).genC(pw);
		}
	}

	private Expr left;
	private ArrayList<String> operators;
	private ArrayList<Expr> statements;
	private ArrayList<Boolean> strcmp;
}