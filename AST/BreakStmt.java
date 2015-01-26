package AST;

import java.util.*;

public class BreakStmt extends FlowStmt {
	
	public BreakStmt(){
	}
	
	public void genC(Pw pw){
		
		pw.print("break;\n");
	}
}