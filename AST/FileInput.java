package AST;

import java.util.*;

public class FileInput {
	
	public FileInput(ArrayList<Stmt> statements){
		this.statements = statements;
	}
	
	public void genC(Pw pw){
		
		boolean exception = false;

		// Separando FuncDef / ClassDef / TryStmt
		functions = new ArrayList<FuncDef>();
		classes = new ArrayList<ClassDef>();

		for(int i = statements.size() - 1; i >= 0; i--){
			Stmt s = statements.get(i);

			if(s instanceof FuncDef){
				functions.add((FuncDef) s);
				statements.remove(i);
			} else if (s instanceof TryStmt)
				exception = true;
			else if (s instanceof ClassDef){
				classes.add((ClassDef) s);
				statements.remove(i);
			}
		}

		// includes
		String includes[] = {"stdio", "stdlib", "string", "signal", "math"};

		for(int i = 0, l = includes.length; i < l; i++)
			pw.printi("#include <" + includes[i] + ".h>\n");

		pw.printi("char * strcat_aux(char * a, char * b){\n"
						+ "\tint an = strlen(a);\n"
						+ "\tint bn = strlen(b);\n"
						+ "\tchar * x = (char *)malloc(sizeof(char)*(an+bn+1));\n"
						+ "\tx[0]='\\0';\n"
						+ "\tstrcat(x,a);\n"
						+ "\tstrcat(x,b);\n"
						+ "\treturn x;\n"
					+ "}");
				// Tratamento de excecao
		int depth = 0;

		if(exception){
			pw.print("\n\n// Prototipos de excecoes\n");

			for(Stmt s : statements)
				if(s instanceof TryStmt)
					depth = ((TryStmt) s).genPrototype(pw, depth);

			pw.printi("\n\n");

			pw.printi("\ntypedef struct {"
			+ "\n\tvoid (*exec)(int signal);"
			+ "\n} GLOBAL_SH_STRUCT;"
			+ "\n\nGLOBAL_SH_STRUCT *GLOBAL_SH_VET;"
			+ "\nint GLOBAL_SH_SIZE = 0;"
			+ "\n\nvoid GLOBAL_SH_PUSH(void (*exec)(int signal)){"
			+ "\n\tGLOBAL_SH_STRUCT new = (GLOBAL_SH_STRUCT) {exec};"
			+ "\n\tGLOBAL_SH_SIZE++;"
			+ "\n\tGLOBAL_SH_VET = (GLOBAL_SH_STRUCT*)"
			+ "\n\t\trealloc(GLOBAL_SH_VET, GLOBAL_SH_SIZE * sizeof(GLOBAL_SH_STRUCT));"
			+ "\n\tGLOBAL_SH_VET[GLOBAL_SH_SIZE - 1] = new;"
			+ "\n}"
			+ "\n\nGLOBAL_SH_STRUCT GLOBAL_SH_POP(){"
			+ "\n\treturn GLOBAL_SH_VET[--GLOBAL_SH_SIZE];"
			+ "\n}"
			+ "\n\nvoid GLOBAL_SIGNAL_CALLBACK_HANDLER(int signal){"
			+ "\n\tGLOBAL_SH_STRUCT caller = GLOBAL_SH_POP();"
			+ "\n\tcaller.exec(signal);"
			+ "\n}\n\n");
		}

		// pw.printi("\n// Funcao generica de concatenacao de array disponivel em <http://rosettacode.org/wiki/Array_concatenation#C>\n\n"
		// + "#define _CONCAT(TYPE, A, An, B, Bn) (TYPE *)"
		// + "_concat((const void *)(A), (An), (const void *)(B), (Bn), sizeof(TYPE));\n\n"
		// + "void *_concat(const void *a, size_t an, const void *b, size_t bn, size_t s){\n"
		// + "\tchar *p = malloc(s * (an + bn));\n"
		// + "\tmemcpy(p, a, an * s);\n"
		// + "\tmemcpy(p + an * s, b, bn * s);\n"
		// + "\treturn p;\n"
		// + "}\n\n");

		pw.print("\n\n// Prototipos de funcoes\n");

		// Prototipos
		for(FuncDef f : functions){
			f.genPrototype(pw);
			pw.printi(";\n");
		}

		pw.printi("\n");

		pw.print("\n\n// Classes\n");

		// Prototipos
		for(ClassDef c : classes)
			c.genC(pw);

		pw.printi("\n");

		// Corpo
		pw.print("\nint main(){\n");

		pw.add();

		if(exception){
			String signals[] = {"SIGINT", "SIGBUS", "SIGFPE", "SIGSEGV", "SIGPIPE", "SIGSTKFLT", "SIGSYS"};

			for(int i = 0, l = signals.length; i < l; i++)
				pw.print("signal(" + signals[i] + ", GLOBAL_SIGNAL_CALLBACK_HANDLER);\n");

			pw.printi("\n");
		}

		for(Stmt s : statements)
			s.genC(pw);

		pw.printi("\n");
		pw.print("return 0;\n");

		pw.sub();
		pw.print("}\n\n");

		// Corpo das funcoes
		for(FuncDef f : functions)
			f.genC(pw);

		depth = 0;

		if(exception){
			for(Stmt s : statements)
				if(s instanceof TryStmt)
					depth = ((TryStmt) s).genFunctions(pw, depth);
		}
	}

	private ArrayList<Stmt> statements;
	private ArrayList<FuncDef> functions;
	private ArrayList<ClassDef> classes;
}