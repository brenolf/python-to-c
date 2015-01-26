package AST;

import java.util.*;

public class DotName extends Trailer {

	public DotName(String str, String className, boolean isFunction){
		this.str = str;
		this.className = (className == null || !isFunction) ? "" : className + "_";
	}

	public void genC(Pw pw){
		pw.printi("->" + className + str);
	}

	public void genC(Pw pw, boolean option){
		genC(pw);
	}

	private String str;
	private String className;
}