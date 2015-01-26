package AST;

import java.util.*;

public class ArgList extends Trailer {

	public ArgList(ArrayList<Test> list, int selfpos, String selfname){
		this.list = list;
		this.selfpos = selfpos;
		this.selfname = selfname;
	}

	public void genC(Pw pw){
		pw.printi("(");

		int i = 0, l = list.size();
		boolean ok = false;

		for(i = 0; i < l; i++){
			if(selfpos == i && !ok) {
				pw.printi(selfname);
				i--;
				ok = true;
			} else
				list.get(i).genC(pw);

			pw.printi(i + 1 < l ? ", " : "");
		}

		if(selfpos == i){
			if(l != 0)
				pw.printi(", ");

			pw.printi(selfname);
		}

		pw.printi(")");
	}

	public void genC(Pw pw, boolean option){
	}

	private ArrayList<Test> list;
	private int selfpos;
	private String selfname;
}