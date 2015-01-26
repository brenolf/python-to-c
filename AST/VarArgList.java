package AST;

import java.util.*;
import AuxComp.TypeManager;

public class VarArgList {
	
	public VarArgList(ArrayList<Pair> pairs, String asterisk, String doubleAsterisk, String className){
		this.pairs = pairs;
		this.asterisk = asterisk;
		this.doubleAsterisk = doubleAsterisk;
		this.className = className;
	}

	public int getSelfPosition(){
		for(int i = 0, l = pairs.size(); i < l; i++)
			if(pairs.get(i).fpdef.get(0).getName().equals("self"))
				return i;

		return -1;
	}

	public void changeParamType(int index, String type){
		Pair p = pairs.get(index);

		for(int j = 0, m = p.fpdef.size(); j < m; j++){
			NameAtom param = p.fpdef.get(j);
			param.setType(type);
		}
	}
	
	public void genC(Pw pw){
		
		for(int i = 0, l = pairs.size(); i < l; i++){
			Pair p = pairs.get(i);

			for(int j = 0, m = p.fpdef.size(); j < m; j++){
				NameAtom param = p.fpdef.get(j);

				String name = param.getName();
				String tipo = "";

				if(name.equals("self") && className != null)
					tipo = "struct class_" + className + "* self";
				
				else {
					tipo = TypeManager.getCType(param.getType(), name);
					tipo = param.getType().equals("VOID") ? ("void* " + tipo) : tipo;
				}

				pw.printi(tipo + ((j + 1 >= m) ? "" : ", "));
			}

			//for(Test t : p.tests)
			//	t.genC(pw);

			if(i + 1 < l)
				pw.printi(", ");
		}

		if(asterisk != null)
			pw.printi(", *" + asterisk);

		if(doubleAsterisk != null)
			pw.printi(", **" + doubleAsterisk);
	}

	public ArrayList<Pair> getPairs(){
		return this.pairs;
	}

	private ArrayList<Pair> pairs;
	private String asterisk, doubleAsterisk;
	private String className;
}