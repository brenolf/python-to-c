package AuxComp;


public class ReferenceTrio {

    public ReferenceTrio(String instance, String isInstanceOf, boolean function){
    	this.instance = instance;
    	this.isInstanceOf = (instance == null) ? isInstanceOf : null;
    	this.function = function;
	}

	public ReferenceTrio(){
		this(null, null, false);
	}

	public String instance, isInstanceOf;
	public boolean function;
}