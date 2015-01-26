package AST;

import java.util.*;

public class NotStatement extends NotTest {

	public NotStatement(NotTest statement){
		this.statement = statement;
	}
	
	public void genC(Pw pw){
		
		pw.printi("!(");
		statement.genC(pw);
		pw.printi(")");
	}

	private NotTest statement;
}