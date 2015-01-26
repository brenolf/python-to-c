package AuxComp;

import java.util.ArrayList;

public class TrioCollection {

    public TrioCollection(){
    	vet = new ArrayList<Trio>();
	}

	public int size(){
		return vet.size();
	}

	public Trio at(int index){
		return vet.get(index);
	}

	public int get(String name){
		int i = 0;

		for(Trio t : vet){
			if(compare(t.getName(), name))
				return i;
			i++;
		}

		return -1;
	}

	public int get(String name, String parent){
		int i = 0;

		for(Trio t : vet){
			if(compare(t.getName(), name) && compare(t.getParent(), parent))
				return i;
			i++;
		}

		return -1;
	}

	public boolean contains(String name){
		return get(name) >= 0;
	}

	public boolean contains(String name, String parent){
		return get(name, parent) >= 0;
	}

	public void add(String name, String parent){
		vet.add(new Trio(name, parent, new ArrayList<String>()));
	}

	public void addVar(int index, String var){
		vet.get(index).add(var);
	}

	public int matches(int index, ArrayList<String> params, boolean ignoreSelf){
		return vet.get(index).matches(params, ignoreSelf);
	}

	private boolean compare(String a, String b){
		if(a == null && b == null)
			return true;

		if(a == null ^ b == null)
			return false;

		return a.equals(b);
	}

	private ArrayList<Trio> vet;
}