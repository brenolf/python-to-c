package AST;

import java.util.*;

public class AndTest {

	public AndTest(ArrayList<NotTest> nots){
		this.nots = nots;
	}
	
	public void genC(Pw pw){
		for(int i = 0, l = nots.size(); i < l; i++){
			nots.get(i).genC(pw);
			pw.printi((i + 1 >= l) ? "" : " && ");
		}
	}

	private ArrayList<NotTest> nots;
}