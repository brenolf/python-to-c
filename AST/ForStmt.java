
package AST;

import java.util.*;

public class ForStmt extends CompoundStmt {
	
	public ForStmt(ExprList exprs, ArrayList<NameAtom> variables, Atom list[], Suite body, Suite elseBody){
		this.exprs = exprs;
		this.list = list;
		this.body = body;
		this.variables = variables;
		this.elseBody = elseBody;
	}
	
	public void genC(Pw pw){
		int len = -1;

		int start = 0, end = 0;

		boolean isRange = (list[1] != null);

		boolean isIntList = false, isDoubleList = false, isStringList = false, isReference = false;

		if(list[0] instanceof Listmaker || list[0] instanceof NameAtom){
			isIntList = !isRange && list[0].getType().equals("INT_LIST");
			isDoubleList = !isRange && list[0].getType().equals("DOUBLE_LIST");
		}

		isStringList = !isIntList && !isDoubleList && !isRange;
		isReference = list[0] instanceof NameAtom;

		String tipo = isIntList ? "int" : (isDoubleList ? "double" : "char");
		String var = isReference ? ((NameAtom) list[0]).getName() : "_list";

		if(isRange){
			start = ((NumberAtom) list[0]).getInt();
			end = ((NumberAtom) list[1]).getInt();
		} else {
			if(list[0] instanceof Listmaker)
				end = ((Listmaker) list[0]).size();

			else if(list[0] instanceof NameAtom)
				end = ((NameAtom) list[0]).getSize();

			else 
				end = ((StringAtom) list[0]).length();
		}

		for(int i = 0; i < variables.size(); i++)
			pw.print("int _" + variables.get(i).getName() + ";\n");

		pw.print("\n");

		pw.print("for(");

		for(int i = 0, l = variables.size(); i < l; i++)
			pw.printi("_" + variables.get(i).getName() + " = " + start + (i + 1 < l ? ", " : "; "));

		for(int i = 0, l = variables.size(); i < l; i++)
			pw.printi("_" + variables.get(i).getName() + " < " + end + (i + 1 < l ? ", " : "; "));

		for(int i = 0, l = variables.size(); i < l; i++)
			pw.printi("_" + variables.get(i).getName() + "++" + (i + 1 < l ? ", " : ""));

		pw.printi("){\n");
		pw.add();

		if(!isRange && !isReference){
			pw.print(tipo + " " + var + "[] = ");
			list[0].genC(pw);
			pw.printi(";\n");
		}

		for(int i = 0; i < variables.size(); i++){
			if(isRange){
				pw.print("int " + variables.get(i).getName() + " = _" + variables.get(i).getName() + ";\n");
			} else {
				if(isIntList || isDoubleList)
					pw.print(tipo + " " + variables.get(i).getName() + " = " + var + "[_i];\n");

				else
					pw.print("char " + variables.get(i).getName() + "[] = {" + var + "[_i], '\\0'};\n");
			}
		}
		
		body.genC(pw);

		pw.sub();
		pw.print("}\n");

		if(elseBody != null){
			pw.print("if(");

			for(int i = 0, l = exprs.size(); i < l; i++){
				pw.printi("_");
				exprs.get(i).genC(pw);
				pw.printi(" == " + end + " - 1" + (i + 1 < l ? " && " : ""));
			}

			pw.printi("){");

			pw.add();
			elseBody.genC(pw);
			pw.sub();
			
			pw.print("}\n");
		}

	}

	private ExprList exprs;
	private Atom list[];
	ArrayList<NameAtom> variables;
	private Suite body, elseBody;
}
