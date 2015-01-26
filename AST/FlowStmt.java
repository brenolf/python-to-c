package AST;

import java.util.*;

public abstract class FlowStmt extends SmallStmt {
	public abstract void genC(Pw pw);
}