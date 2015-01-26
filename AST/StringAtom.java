package AST;

import java.util.*;

public class StringAtom extends Atom {

	public StringAtom(String str){
		this.str = str;
	}

	public void genC(Pw pw){
		
		pw.printi("\"" + str + "\"");
	}

	public void genC(Pw pw, boolean option){
		
	}

	public String getType(){
		return "STRING";
	}

	public int length(){
		return str.length();
	}

	private String str;
}