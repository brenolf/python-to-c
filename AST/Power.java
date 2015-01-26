package AST;

import java.util.*;

public class Power extends Factor {
	public Power(Atom atom, ArrayList<Trailer> trailers, Factor factor, boolean isDirectInstance){
		this.atom = atom;
		this.trailers = trailers;
		this.factor = factor;
		this.isDirectInstance = isDirectInstance;
	}

	private void getBody(Pw pw){
		atom.genC(pw);

		for(Trailer t : trailers)
			t.genC(pw);

		if(isDirectInstance)
			pw.printi("()");
	}

	private void getBody(Pw pw, boolean option){
		if(atom instanceof Listmaker)
			atom.genC(pw, option);
		else
			atom.genC(pw);

		for(Trailer t : trailers)
			t.genC(pw);

		if(isDirectInstance)
			pw.printi("()");
	}

	public void genC(Pw pw){
		
		if(factor != null){
			pw.printi("(int) pow(");
			getBody(pw);
			pw.printi(", ");
			factor.genC(pw);
			pw.printi(")");
		} else
			getBody(pw);
	}

	private Atom atom;
	private ArrayList<Trailer> trailers;
	private Factor factor;
	private boolean isDirectInstance;
}