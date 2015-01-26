package AuxComp;

public class StringPair {

    public StringPair(String atr1, String atr2){
    	this.atr1 = atr1;
    	this.atr2 = atr2;
	}

	public String get(int index){
		if(index == 0)
			return atr1;
		return atr2;
	}

	private String atr1, atr2;
}