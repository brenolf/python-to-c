package AST;

import java.util.*;
import AuxComp.TypeManager;

public class FuncDef extends CompoundStmt {
	
	public FuncDef(String name, String parent, VarArgList params){
		this.name = name;
		this.parent = parent;
		this.params = params;
	}

	public void setAttributes(Suite body, String type){
		this.body = body;
		this.type = (type == null) ? "VOID" : type;
	}

	public void setName(String name){
		this.name = name;
	}

	public String getName(){
		return this.name;
	}

	public void printParamString(Pw pw){
		pw.printi("(");

		if(params != null)
			params.genC(pw);

		pw.printi(")");
	}

	public void genPrototype(Pw pw){
		String tipo = TypeManager.getCType(type, name);
		tipo = type.equals("VOID") ? ("void " + tipo) : tipo; 
		pw.print(tipo);
		printParamString(pw);
	}

	public String getType(){
		return TypeManager.getRawType(type);
	}

	public String getOriginalType(){
		return type;
	}
	
	public void genC(Pw pw){
		
		genPrototype(pw);
		pw.printi("{\n");

		pw.add();
		if(body != null)
			body.genC(pw);
		pw.sub();

		pw.printi("\n");
		pw.print("}\n");
	}

	public Suite getBody(){
		return this.body;
	}

	public VarArgList getParams(){
		return this.params;
	}

	public void setParent(String parent){
		this.parent = parent;
	}

	public String getParent(){
		return this.parent;
	}

	public void changeParamType(int index, String type){
		params.changeParamType(index, type);
	}

	public int getSelfPosition(){
		return params.getSelfPosition();
	}

	private String name;
	private VarArgList params;
	private Suite body;
	private String type;
	private String parent;
}