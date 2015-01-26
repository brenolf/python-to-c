package AST;

import java.util.*;

public class TestList {
	
	public TestList(ArrayList<Test> tests){
		this.tests = tests;
	}
	
	public void genC(Pw pw){
		
		for(int i = 0, l = tests.size(); i < l; i++){
			tests.get(i).genC(pw);

			if(i + 1 < l)
				pw.printi(", ");
		}
	}

	public int size(){
		return tests.size();
	}

	public Test get(int index){
		return tests.get(index);
	}

	ArrayList<Test> tests;
}