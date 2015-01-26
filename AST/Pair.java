package AST;

import java.util.*;

public class Pair {
	
	public Pair(ArrayList<Target> fpdef, ArrayList<Test> tests){
		this.fpdef = fpdef;
		this.tests = tests;
	}

	public void setType(String type){
		for(Target t : fpdef)
			t.setType(type);
	}

	public ArrayList<Target> fpdef;
	public ArrayList<Test> tests;
}