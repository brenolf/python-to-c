package AST;

import java.util.*;

public class ContinueStmt extends FlowStmt {
	
	public ContinueStmt(){
	}
	
	public void genC(Pw pw){
		
		pw.print("continue;");
	}
}