package AST;

import java.util.*;

public abstract class CompoundStmt extends Stmt {
	public abstract void genC(Pw pw);
}