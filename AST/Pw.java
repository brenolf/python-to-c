package AST;

import java.lang.System;

import java.io.*;


public class Pw {

	public void add() {
		currentIndent++;
	}

	public void sub() {
		currentIndent--;
	}

	public void set( PrintWriter out ) {
		this.out = out;
		currentIndent = 0;
	}

	public void set( int indent ) {
		currentIndent = indent;
	}

	public void printi( String s ) {
		out.print(s);
	}

	public void print( String s ) {
		tabs();
		out.print(s);
	}

	public void println( String s ) {
		out.println(s);
	}

	public void tabs(){
		String s = "";

		for(int i = 0; i < currentIndent; i++)
			s += "\t";

		out.print(s);
	}

	public int currentIndent = 0;
	public PrintWriter out;

}