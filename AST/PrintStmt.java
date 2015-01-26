package AST;

import java.util.*;
import AuxComp.TypeManager;

public class PrintStmt extends SmallStmt {
	
	public PrintStmt(ArrayList<Test> tests, ArrayList<String> format){
		this.tests = tests;
		this.format = format;
	}
	
	public void genC(Pw pw){
		
		pw.print("printf(");

		String formatStr = "";

		int l = format.size();
		for(int i = 0; i < l; i++){
			formatStr += TypeManager.getFormatStr(format.get(i));
			formatStr += (i == l - 1) ? "\\n" : " ";
		}

		pw.printi("\"" + formatStr + "\", ");

		l = tests.size();
		for(int i = 0; i < l; i++){
			tests.get(i).genC(pw);
			pw.printi(i + 1 < l ? ", " : "");
		}

		pw.printi(");\n");
	}

	private ArrayList<Test> tests;
	private ArrayList<String> format;
}