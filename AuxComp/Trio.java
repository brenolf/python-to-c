package AuxComp;

import java.util.ArrayList;

public class Trio {

	public Trio(String name, String parent, ArrayList<String> var){
		this.name = name;
		this.parent = parent;
		this.var = var;
	}

	public void add(String str){
		var.add(str);
	}

	public int size(){
		return var.size();
	}

	public String getParent(){
		return this.parent;
	}

	public String getName(){
		return this.name;
	}

	public String getVar(int index){
		return this.var.get(index);
	}

	public void setVar(int index, String str){
		this.var.set(index, str);
	}

	public int matches(ArrayList<String> params, boolean ignoreSelf){
		int max = size();

		if(params.size() != (max - (ignoreSelf ? 1 : 0)))
			return -1;

		int i = 0;

		for(String s : params){
			String t = var.get(i).toUpperCase();

			if(ignoreSelf && t.equals("self"))
				continue;

			if(!s.toUpperCase().equals(t))
				return i;

			i++;
		}

		return -2;
	}

	private String name, parent;
	private ArrayList<String> var;
}