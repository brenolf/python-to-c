package AST;

import java.util.*;
import AuxComp.*;

public class NameAtom extends Atom {

	public NameAtom(String name, boolean insideClass){
		this.name = name;
		this.type = "VOID";
		this.size = 0;
		this.insideClass = insideClass;
		typeId = TypeManager.getTypeId(this.type);
		this.declared = insideClass;
	}

	public void setType(String type){
		this.type = type;
		typeId = TypeManager.getTypeId(this.type);
	}

	public void genC(Pw pw){
		pw.printi(declared ? (insideClass ? "CLASS->" : "") + name : TypeManager.getCType(type, name));
	}

	public void genC(Pw pw, boolean option){
		if(option)
			pw.printi((insideClass ? "CLASS->" : "") + name);
		else
			genC(pw);
	}

	public String getName(){
		return name;
	}

	public String getType(){
		return type;
	}

	public void setIsFunction(){
		this.declared = true;
	}

	public void setDeclared(boolean declared){
		this.declared = declared;
	}

	public void setSize(int size){
		this.size = size;
	}

	public int getSize(){
		return this.size;
	}

	public void setInsideClass(boolean insideClass){
		this.insideClass = insideClass;
		this.declared = insideClass;
	}

	private String name;
	private String type;
	private boolean insideClass;
	private int typeId;
	private int size;
	private boolean declared;
}