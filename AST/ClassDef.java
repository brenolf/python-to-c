package AST;

import java.util.*;

public class ClassDef extends CompoundStmt {
	
	public ClassDef(String name, ArrayList<Atom> list, TestList tl, Suite body){
		this.name = name;
		this.list = list;
		this.tl = tl;
		this.body = body;

		functions = new ArrayList<FuncDef>();
		attr = new ArrayList<ExprStmt>();
		init = null;

		for(int i = body.size() - 1; i >= 0; i--){
			Stmt s = body.get(i);

			if(s instanceof FuncDef){
				if(((FuncDef) s).getName().equals("__init__"))
					init = (FuncDef) s;
				else
					functions.add((FuncDef) s);

				
				body.remove(i);

			} else if(s instanceof SimpleStmt){
				SimpleStmt ss = (SimpleStmt) s;

				for(int j = 0, l = ss.size(); j < l; j++){
					if(!(ss.get(j) instanceof ExprStmt))
						continue;

					ExprStmt es = (ExprStmt) ss.get(j);
					String op = es.getAugassign();

					if(op != null && op.equals("=") && !es.getNoDeclaration())
						attr.add(es);
				}
			}
		}

		if(body.size() == 0)
			body = null;
	}

	public void genC(Pw pw){
		pw.print("// Classe \"" + name + "\"\n");

		pw.print("struct class_" + name + " {\n");
		pw.add();

		pw.print("// Atributos\n");

		for(ExprStmt es : attr){
			es.setIsInsideClass(true);
			es.genC(pw);
			es.setIsInsideClass(false);
		}

		pw.printi("\n");
		pw.print("// Metodos\n");

		for(FuncDef f : functions){
			pw.print(f.getType() + " (*" + name + "_" + f.getName() + ") ");
			f.printParamString(pw);
			pw.printi(";\n");
		}

		pw.sub();

		pw.print("};\n");

		pw.print("\n\n// Metodos da Classe " + name + "\n");

		
		for(FuncDef f : functions){
			f.setName(name + "_" + f.getName());
			f.genC(pw);
			pw.printi("\n");
		}

		printConstructor(pw);
	}

	private void printConstructor(Pw pw){
		if(body == null && init == null)
			return;

		Suite initBody = null;

		pw.print("\nstruct class_" + name + "* " + name);

		if(init != null){
			initBody = init.getBody();
			init.printParamString(pw);
		} else 
			pw.printi("()");

		pw.print("{\n");
		pw.add();
		pw.print("struct class_" + name + "* CLASS = (struct class_" + name  + "*) malloc(sizeof(struct class_" + name  + "));\n");

		if(body != null)
			body.genC(pw);

		for(FuncDef f : functions)
			pw.print("CLASS->" + f.getName() + " = " + f.getName() + ";\n");

		pw.print("return CLASS;\n");
		pw.sub();
		pw.print("}\n\n");
	}

	private boolean assignmentsOff = true;
	private String name;
	private ArrayList<Atom> list;
	private TestList tl;
	private Suite body;
	private ArrayList<FuncDef> functions;
	private ArrayList<ExprStmt> attr;
	private FuncDef init;
}