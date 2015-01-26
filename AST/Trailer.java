package AST;

import java.util.*;

public abstract class Trailer {
	public abstract void genC(Pw pw);
	public abstract void genC(Pw pw, boolean option);
}