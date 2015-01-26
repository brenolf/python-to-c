package AST;

import java.util.*;
import AuxComp.TypeManager;

public class NumberAtom extends Atom {

	public NumberAtom(double number, String type){
		this.number = number;
		this.type = type;
	}

	public void genC(Pw pw){
		
		if(TypeManager.getTypeId(type) == TypeManager.getTypeId("INT"))
			pw.printi(getInt() + "");
		
		else
			pw.printi(number + "");
	}

	public void genC(Pw pw, boolean option){
		
	}

	public int getInt(){
		return ((int) number);
	}

	public String getType(){
		return type.toUpperCase();
	}

	private double number;
	private String type;
}