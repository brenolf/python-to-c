package AST;

import java.util.*;


public class TargetList {
	
	public TargetList(ArrayList<Target> targets){
		this.targets = targets;
	}
	
	public void genC(Pw pw){
		for(Target t : targets)
			t.genC(pw);
	}

	public void genC(Pw pw, boolean option){
		for(Target t : targets)
			t.genC(pw, option);
	}

	public boolean compareTypes(String lastType, StringBuilder failureVar, StringBuilder failureType){
		
		for(Target t : targets){
			//System.out.println("tipo: "+t.getType()+" ");
			if(!t.getType().equals(lastType) && !t.getType().toUpperCase().equals("VOID")){
				failureVar = failureVar.append(t.getName());
				failureVar = failureType.append(t.getType());
				return false;
			}
		}
		
		failureVar = null;
		return true;
	}

	public boolean hasType(String...type){
		StringBuilder sb = new StringBuilder();
		return hasType(sb, type);
	}

	public boolean hasType(StringBuilder sb, String...type){
		for(String s : type)
			s = s.toUpperCase();
		
		boolean found = true;

		for(Target t : targets){
			found = false;

			for(String s : type)
				if(t.getType().toUpperCase().equals(s))
					found = true;

			if(!found)
				return false;

			else if(!sb.equals(""))
				sb.append(t.getType());
		}


		return true;
	}

	public void setTypes(String s){
		for(Target t : targets)
			t.setType(s);
	}

	public void setSizes(ArrayList<Integer> sizes){
		int i = 0;

		for(Target t : targets)
			t.setSize(sizes.get(i).intValue());
	}

	public int size(){
		return targets.size();
	}

	public Target get(int i){
		return targets.get(i);
	}

	public void setInsideClass(boolean insideClass){
		for(Target t : targets)
			t.setInsideClass(insideClass);
	}

	private ArrayList<Target> targets;
}