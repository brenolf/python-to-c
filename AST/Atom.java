package AST;

import java.util.*;

public abstract class Atom {
	public abstract void genC(Pw pw);
	public abstract void genC(Pw pw, boolean option);
	public abstract String getType();
}
