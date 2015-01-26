package AST;

import java.util.*;

public class WithStmt extends CompoundStmt {
	
	public WithStmt(ArrayList<WithItem> items, Suite body){
		this.items = items;
		this.body = body;
	}
	
	public void genC(Pw pw){
		
		pw.print("// With statement\n");

		for(WithItem w : items)
			w.genC(pw);
		
		pw.add();
		body.genC(pw);
		pw.sub();
	}

	private ArrayList<WithItem> items;
	private Suite body;
}