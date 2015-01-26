package AST;

import java.util.*;

public class TestList1 extends Atom {
	
	public TestList1(ArrayList<Test> tests){
		this.tests = tests;
	}
	
	public void genC(Pw pw){
		
		for(int i = 0, l = tests.size(); i < l; i++){
			tests.get(i).genC(pw);

			if(i + 1 < l)
				pw.printi(", ");
		}
	}

	public void genC(Pw pw, boolean option){
	}

	public String getType(){
		return "Testlist";
	}



	ArrayList<Test> tests;
}