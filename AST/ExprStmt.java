package AST;

import java.util.*;
import AuxComp.TypeManager;

public class ExprStmt extends SmallStmt {
	
	public ExprStmt(TargetList left, String augassign, TestList tests, ArrayList<Trailer> trailers, boolean isInsideClass, boolean noDeclaration){
		this.left = left;
		this.augassign = augassign;
		this.tests = tests;
		this.trailers = trailers;
		this.isInsideClass = isInsideClass;
		this.noDeclaration = noDeclaration;
	}
	
	public void genC(Pw pw){
		
		pw.tabs();
		StringBuilder sb = new StringBuilder();
		boolean hasStrings = left.hasType(sb, "STRING");
		boolean ok = false, malloc = false;
		String type = sb.toString();

		if(isInsideClass)
			left.setInsideClass(false);

		if(hasStrings && augassign != null && augassign.equals("=")){
			left.genC(pw, noDeclaration);
			pw.printi(" = malloc(sizeof(char) * 1000);\n");

			noDeclaration = true;
			malloc = true;

			pw.print("strcpy(");
			left.genC(pw, noDeclaration);
			pw.printi(", \"\");\n");
		}

		if(hasStrings && augassign != null && (augassign.equals("+=") || augassign.equals("="))){
			if(!malloc)
				pw.printi("strcat_aux(");
			else
				pw.print("strcat_aux(");

			left.genC(pw, noDeclaration);
			pw.printi(", ");
			tests.genC(pw);
			pw.printi(");\n");
			ok = true;
		} else 
			left.genC(pw, noDeclaration);

		if(!isInsideClass){
			if(augassign != null && !ok){
				pw.printi(" " + augassign + " ");
				tests.genC(pw);
				pw.printi(";\n");
			} else if(trailers.size() > 0) {
				for(Trailer t : trailers)
					t.genC(pw);
				pw.printi(";\n");
			}
		} else
			pw.printi(";\n");

		if(isInsideClass)
			left.setInsideClass(true);
	}

	public TargetList getLeft(){
		return this.left;
	}

	public void setIsInsideClass(boolean isInsideClass){
		this.isInsideClass = isInsideClass;
	}

	public void setNoDeclaration(boolean noDeclaration){
		this.noDeclaration = noDeclaration;
	}

	public String getAugassign(){
		return this.augassign;
	}

	public boolean getNoDeclaration(){
		return this.noDeclaration;
	}

	private TargetList left;
	private String augassign;
	private TestList tests;
	private ArrayList<Trailer> trailers;
	private boolean isInsideClass;
	private boolean noDeclaration;
}