package AST;

import java.util.*;
import AuxComp.TypeManager;

public class Listmaker extends Atom {
	
	public Listmaker(ArrayList<Test> tests, int type){
		this.tests = tests;
		this.type = type;
	}
	
	public void genC(Pw pw){
		pw.printi("{");

		for(int i = 0, l = tests.size(); i < l; i++){
			tests.get(i).genC(pw);

			if(i + 1 < l)
				pw.printi(", ");
		}

		pw.printi("}");
	}

	public void genC(Pw pw, boolean option){
		genC(pw);
	}

	public String getType(){
		return TypeManager.title[type] + "_LIST";
	}

	public int size(){
		return tests.size();
	}

	ArrayList<Test> tests;
	int type;
}