import AST.*;
import Lexer.*;
import AuxComp.*;
import java.io.*;

import java.util.ArrayList;
import java.util.HashMap;

public class Compiler {
	private static CompilerError ce;
	public static Lexer lexer;
	private SymbolTable symbolTable;
	private int currentLevel;
	private String className; // Nome da classe que esta sendo explorada
	private String lastType; // Guarda o ultimo tipo, aí não preciso descer a arvore da AST pra procurar
	private boolean isTarget; // Pra eu saber onde tá da arvore
	private boolean isTargetTest; // se é targetlist
	private boolean declare; // sempre declara se for 1
	private boolean isListMaker; // se esta explorando o listmaker
	private boolean isForAtom; // se esta explorando o atom do for
	private boolean isFuncDef; // se esta no suite do funcdef
	private boolean isTest; // se esta no test
	private boolean exprStmtNoDeclare; // se variavel repetida apareceu em um exprstmt [onde nao sera gerado a declaracao]
	private ArrayList<Integer> listsSizes; // tipo do atom mais baixo explorado no dfs
	private ArrayList<String> forDeclarationVariables; // variaveis declaradas no for para por seu tipo
	private ArrayList<String> classesNames; // nomes das classes
	private ArrayList<ArrayList<StringPair>> classesVars; // variaveis das classes em classesNames e seus tipos
	private TrioCollection functions; // funcoes, seus pais [null para main ou nome da classe] e seus atributos tipados
	private ArrayList<FuncDef> funcPointers; // ponteiros para as funcoes para mudar o tipo de parametro futuramente [em chamadas]
	private ArrayList<NameAtom> funcParams; // parametros da funcao sendo explorada no momento
	private ArrayList<NameAtom> exprFuncParams; // variaveis na expressao sendo avaliada
	private Expression analyser; // analisador de expressao
	private String caller; // a funcao que esta para ser chamada
	private String returnType; // tipo da estrutura 'return'

	private boolean DEBUG = false;

	public boolean is(Symbol...list){
		return lexer.matches(list);
	}
	
	public void error(String msg) {
		ce.error(msg, lexer);
	}
	
	public void errorif(String msg, Symbol...list){
		if(!is(list))
			error(msg);
	}

	public boolean classHasAttr(String cn, String attr, StringBuilder sb){
		int index = classesNames.indexOf(cn);
		ArrayList<StringPair> aux = classesVars.get(index);

		for(int i = 0, l = aux.size(); i < l; i++){
			if(aux.get(i).get(0).equals(attr)){
				if(sb != null)
					sb.append(aux.get(i).get(1));
				return true;
			}
		}
		return false;
	}

	public FuncDef getFunctionPointer(String name, String parent){
		for(FuncDef f : funcPointers){
			boolean sameParent = (parent == null && f.getParent() == null);

			if(parent != null && f.getParent() != null)
				sameParent |= parent.equals(f.getParent());

			if(!f.getName().equals(name) || !sameParent)
				continue;

			return f;
		}

		return null;
	}

	//Não pega tipo, declara nova variavel mesmo
	public void putNew(String key, Object o, int level){
		symbolTable.put(key,o,level);
	}

	//pega tipo
	public void put(String key, Object o, int level){
		if(symbolTable.getLevel(key,level)!=null){
			NameAtom a = (NameAtom) symbolTable.get(key,level);
			NameAtom b = (NameAtom) o;
			//Se os tipos são diferentes, no mesmo nivel
			if(!a.getType().equals(b.getType())&&!a.getType().equals("VOID")&&!b.getType().equals("VOID")){
				error("Type error: "+b.getName()+" is a "+b.getType()+" but it should be a "+ a.getType());
			}
			//Se não tinha tipo, mas agora tem, substitui
			if(a.getType().equals("VOID")){
				symbolTable.put(key,o,level);
			}
		}else{
			symbolTable.put(key,o,level);
		}
	}

	public FileInput compile(char[] p_input, PrintWriter outError, boolean quiet) {
		ce = new CompilerError();
		ce.setQuiet(quiet);
		symbolTable = new SymbolTable();
		currentLevel = 0;
		isTarget = false;
		isTargetTest = false;
		declare = false;
		lastType = "VOID";
		className = null;
		lexer = new Lexer(p_input, ce);
		lexer.nextToken();
		listsSizes = new ArrayList<Integer>();
		classesNames = new ArrayList<String>();
		forDeclarationVariables = new ArrayList<String>();
		funcParams = new ArrayList<NameAtom>();
		exprFuncParams = new ArrayList<NameAtom>();
		analyser = new Expression();
		classesVars = new ArrayList<ArrayList<StringPair>>();
		functions = new TrioCollection();
		funcPointers = new ArrayList<FuncDef>();


		return file_input();
	}
	
	public FileInput file_input(){
		// file_input: (NEWLINE | stmt)* EOF
		ArrayList<Stmt> statements = new ArrayList<Stmt>();

		while(is(Symbol.NEWLINE, Symbol.ID, Symbol.PRINT, Symbol.BREAK, 
			Symbol.CONTINUE, Symbol.RETURN, Symbol.IF, Symbol.WHILE, Symbol.FOR, 
			Symbol.TRY, Symbol.WITH, Symbol.DEF, Symbol.CLASS)){

			if(is(Symbol.NEWLINE))
				lexer.nextToken();
			else
				statements.add(stmt());
		}

		errorif("Syntax error: EOF expected", Symbol.EOF);

		return new FileInput(statements);
	}

	public Stmt stmt(){
		/*stmt: simple_stmt | compound_stmt*/
		
		if(is(Symbol.ID, Symbol.PRINT, Symbol.BREAK, Symbol.CONTINUE, Symbol.RETURN))
			return simple_stmt();

		else if(is(Symbol.IF, Symbol.WHILE, Symbol.FOR, Symbol.TRY, Symbol.WITH, Symbol.DEF, Symbol.CLASS))
			return compound_stmt();

		return null;
	}
	
	public SimpleStmt simple_stmt(){
		/*  simple_stmt: small_stmt (';' small_stmt)* [';'] (NEWLINE | ENDMARKER) */
		ArrayList<SmallStmt> statements = new ArrayList<SmallStmt>();

		statements.add(small_stmt());
		
		while(is(Symbol.SEMICOLON)){
			lexer.nextToken();
			
			if(is(Symbol.ID, Symbol.PRINT, Symbol.BREAK, Symbol.CONTINUE, Symbol.RETURN))
				statements.add(small_stmt());
		}
		
		errorif("Syntax error: NEWLINE or EOF expected", Symbol.NEWLINE, Symbol.EOF);
		lexer.nextToken();

		return new SimpleStmt(statements);
	}
	
	public CompoundStmt compound_stmt(){
		/*  compound_stmt: if_stmt | while_stmt | for_stmt | try_stmt | with_stmt | funcdef | classdef */
		if(is(Symbol.IF))
			return if_stmt();

		else if(is(Symbol.WHILE))
			return while_stmt();

		else if(is(Symbol.FOR))
			return for_stmt();

		else if(is(Symbol.TRY))
			return try_stmt();

		else if(is(Symbol.WITH))
			return with_stmt();

		else if(is(Symbol.DEF))
			return funcdef();

		else if(is(Symbol.CLASS))
			return classdef();

		else
			error("Syntax error: 'if', 'while', 'for', 'try', 'with', 'class' or 'def' expected");

		return null;
	}
	
	public SmallStmt small_stmt(){
		/*  small_stmt: (expr_stmt | print_stmt  | flow_stmt)   */
		if(is(Symbol.ID))
			return expr_stmt();

		else if(is(Symbol.PRINT))
			return print_stmt();

		else if(is(Symbol.BREAK, Symbol.CONTINUE, Symbol.RETURN))
			return flow_stmt();

		else
			error("Syntax error: ID, 'print', 'break', 'continue' or 'return' expected");

		return null;
	}
	
	public ExprStmt expr_stmt(){
		/*  expr_stmt: targetlist ((augassign testlist) | trailer*) */
		TestList tests = null;
		ArrayList<Trailer> trailers = new ArrayList<Trailer>();

		exprStmtNoDeclare = false;

		isTarget = true;
		TargetList left = targetlist();
		isTarget = false;
		String operator = null;

		boolean leftHasPrimitiveTypes = left.hasType("INT", "DOUBLE", "CHAR", "STRING", "STRING_LIST", "INT_LIST", "DOUBLE_LIST");

		exprFuncParams.clear();

		if(is(Symbol.ASSIGN, Symbol.PLUSASSIGN, Symbol.MINUSASSIGN, Symbol.MULTIASSIGN, Symbol.DIVASSIGN, Symbol.MODASSIGN, 
			Symbol.ANDASSIGN, Symbol.ORASSIGN, Symbol.XORASSIGN)){
			operator = augassign();

			if(left.hasType("STRING", "STRING_LIST", "INT_LIST", "DOUBLE_LIST") && !operator.equals("=") && !operator.equals("+="))
				error("Scope error: Operator \"" + operator + "\" is not allowed to Strings and lists.");

			tests = testlist();
		} else {
			if(leftHasPrimitiveTypes && is(Symbol.LEFTPAR, Symbol.DOT))
				error("Scope error: Primitive type or list protoypes/functions are not allowed.");

			caller = left.get(left.size() - 1).getName();

			int counter = 0;
			ReferenceTrio data = null;
			NameAtom na = (NameAtom) left.get(0);
			String name = na.getName();
			boolean isFunctionCall = is(Symbol.LEFTPAR);

			while(is(Symbol.LEFTPAR, Symbol.DOT)){
				if(data != null && ((counter == 1 && !data.function) || (counter == 2)))
					error("Scope error: \"" + name + "\" has no more than one level of methods/attributes");

				data = new ReferenceTrio(null, null, true);

				trailers.add(trailer(na, data, isFunctionCall, name));
				counter++;
			}
				
		}

		StringBuilder failureVar = new StringBuilder();
		StringBuilder failureType = new StringBuilder();


		if(operator != null){
			// paramtype

			if(!left.compareTypes(lastType, failureVar, failureType))
				error("Type error: Wrong type operation on variable \"" + failureVar + "\" (" + failureType + "), got " + lastType);

			if(analyser.isList() && !operator.equals("="))
				error("Type error: Operations in lists are not allowed");

			if(analyser.isVoid())
				lastType = "VOID";

			else
				lastType = analyser.isNumeric() ? "DOUBLE" : analyser.firstNonVoidType;

			if(operator.equals("=") && className != null && !isFuncDef){
				int index = classesNames.size() - 1;
				StringPair sp = new StringPair(left.get(0).getName(), lastType);
				classesVars.get(index).add(sp);
			}

			left.setTypes(lastType);

			if(TypeManager.isList(lastType)){
				left.setSizes(listsSizes);
				listsSizes.clear();
			}

			int max = left.size();
			for(int i = 0; i < max; i++)
				put(left.get(i).getName(), left.get(i), currentLevel);
		}

		boolean isInsideClass = (className != null);

		return new ExprStmt(left, operator, tests, trailers, isInsideClass, exprStmtNoDeclare);
	}
	
	public PrintStmt print_stmt(){
		/*  print_stmt: 'print' ( [ test (',' test)* [','] ] | '>>' test [ (',' test)+ [','] ] )	*/
		errorif("Syntax error: 'print' expected", Symbol.PRINT);
		lexer.nextToken();

		ArrayList<Test> tests = new ArrayList<Test>();
		ArrayList<String> format = new ArrayList<String>();
		int begin = 0;
		StringBuilder sb = null;

		if(is(Symbol.SHELL)){
			lexer.nextToken();

			tests.add(test());

			if(is(Symbol.COMMA)){
				lexer.nextToken();
				errorif("Syntax error: '+', '-', '~', '`', 'not', ID, NUM or STRING expected", Symbol.PLUS, 
					Symbol.MINUS, Symbol.INVERTION, Symbol.GRAVEACCENT, Symbol.ID,
					Symbol.NUM, Symbol.STRING, Symbol.NOT, Symbol.LEFTCURBRACKET);

				sb = new StringBuilder();
				tests.add(test(sb));

				while(is(Symbol.COMMA)){
					lexer.nextToken();

					if(is(Symbol.PLUS, Symbol.MINUS, Symbol.INVERTION, Symbol.GRAVEACCENT, 
						Symbol.ID, Symbol.NUM, Symbol.STRING, Symbol.NOT, Symbol.LEFTCURBRACKET)){
						sb = new StringBuilder();
						tests.add(test(sb));
					}
				}
			}
		} else if(is(Symbol.PLUS, Symbol.MINUS, Symbol.INVERTION, Symbol.GRAVEACCENT, 
			Symbol.ID, Symbol.NUM, Symbol.STRING, Symbol.NOT, Symbol.LEFTCURBRACKET)) {

			sb = new StringBuilder();
			tests.add(test(sb));

			format.add(sb.toString());

			while(is(Symbol.COMMA)){
				lexer.nextToken();

				if(is(Symbol.PLUS, Symbol.MINUS, Symbol.INVERTION, Symbol.GRAVEACCENT, 
					Symbol.ID, Symbol.NUM, Symbol.STRING, Symbol.NOT, Symbol.LEFTCURBRACKET)){
					sb = new StringBuilder();
					tests.add(test(sb));
					format.add(sb.toString());
				}
			}
		} else
			error("Syntax error: '>>' or test expected");

		return new PrintStmt(tests, format);
	}
	
	public FlowStmt flow_stmt(){
		/*  flow_stmt: break_stmt | continue_stmt | return_stmt */
		if(is(Symbol.BREAK))
			return break_stmt();

		else if(is(Symbol.CONTINUE))
			return continue_stmt();

		else if(is(Symbol.RETURN))
			return return_stmt();

		else
			error("Syntax error: 'break', 'continue' or 'return' expected");

		return null;
	}
	
	public TargetList targetlist(){
		/*  targetlist = target ("," target)* [","] */
		ArrayList<Target> targets = new ArrayList<Target>();

		targets.add(target());

		while(is(Symbol.COMMA)){
			lexer.nextToken();

			if(is(Symbol.ID))
				targets.add(target());
		}

		return new TargetList(targets);
	}
	
	public String augassign(){
		/*  augassign: ('=' | '+=' | '-=' | '*=' | '/=' | '%=' | '&=' | '|=' | '^=') */
		errorif("Syntax error: Assignment operator expected", Symbol.ASSIGN, Symbol.PLUSASSIGN, 
			Symbol.MINUSASSIGN, Symbol.MULTIASSIGN, Symbol.DIVASSIGN, Symbol.MODASSIGN, 
			Symbol.ANDASSIGN, Symbol.ORASSIGN, Symbol.XORASSIGN);

		String operator = lexer.getTokenValue();

		lexer.nextToken();

		return operator;
	}
	
	public Target target(){
		/*  target = ID */
		errorif("Syntax error: ID expected", Symbol.ID);
		String name = lexer.getVar();
		lexer.nextToken();

		Target target = null;

		if(!functions.contains(name)){
			target = (Target) symbolTable.getLevel(name, currentLevel);
			exprStmtNoDeclare = (target != null);
		}

		if(target == null){
			target = new Target(name, className != null && !isFuncDef);
			exprStmtNoDeclare = functions.contains(name);
		}

		return target;
	}
	
	public BreakStmt break_stmt(){
		/*  break_stmt: 'break' */
		errorif("Syntax error: 'break' expected", Symbol.BREAK);
		lexer.nextToken();

		return new BreakStmt();
	}
	
	public ContinueStmt continue_stmt(){
		/*  continue_stmt: 'continue' */
		errorif("Syntax error: 'continue' expected", Symbol.CONTINUE);
		lexer.nextToken();

		return new ContinueStmt();
	}
	
	public ReturnStmt return_stmt(){
		/*  return_stmt: 'return' [testlist] */
		errorif("Syntax error: 'return' expected", Symbol.RETURN);
		lexer.nextToken();

		TestList tests = null;

		if(is(Symbol.PLUS, Symbol.MINUS, Symbol.INVERTION, Symbol.GRAVEACCENT, 
			Symbol.ID, Symbol.NUM, Symbol.STRING, Symbol.NOT, Symbol.LEFTCURBRACKET))
			tests = testlist();

		if(analyser.isVoid() && tests != null)
			error("Type error: could not define types in return expression");

		returnType = analyser.firstNonVoidType;

		return new ReturnStmt(tests, analyser.firstNonVoidType);
	}
	
	public IfStmt if_stmt(){
		/*  if_stmt: 'if' test ':' suite ('elif' test ':' suite)* ['else' ':' suite] */
		//currentLevel++;
		Test condition = null;
		Suite body = null, elseBody = null;

		ArrayList<Test> elifCondition = new ArrayList<Test>();
		ArrayList<Suite> elifBody = new ArrayList<Suite>();

		errorif("Syntax error: 'if' expected", Symbol.IF);
		lexer.nextToken();

		condition = test();

		errorif("Syntax error: ':' expected", Symbol.COLON);
		lexer.nextToken();

		body = suite();
		//symbolTable.removeIdent(currentLevel);


		while(is(Symbol.ELIF)){
			lexer.nextToken();

			elifCondition.add(test());

			errorif("Syntax error: ':' expected", Symbol.COLON);
			lexer.nextToken();

			elifBody.add(suite());
			//symbolTable.removeIdent(currentLevel);
		}

		if(is(Symbol.ELSE)){
			lexer.nextToken();

			errorif("Syntax error: ':' expected", Symbol.COLON);
			lexer.nextToken();

			elseBody = suite();
			//symbolTable.removeIdent(currentLevel);
		}
		//currentLevel--;

		return new IfStmt(condition, body, elifCondition, elifBody, elseBody);
	}
	
	public WhileStmt while_stmt(){
		/*  while_stmt: 'while' test ':' suite ['else' ':' suite] */
		//currentLevel++;
		Test condition = null;
		Suite body = null, elseBody = null;

		errorif("Syntax error: 'while' expected", Symbol.WHILE);
		lexer.nextToken();

		condition = test();

		errorif("Syntax error: ':' expected", Symbol.COLON);
		lexer.nextToken();		

		body = suite();
		//symbolTable.removeIdent(currentLevel);
		if(is(Symbol.ELSE)){
			lexer.nextToken();

			errorif("Syntax error: ':' expected", Symbol.COLON);
			lexer.nextToken();		

			elseBody = suite();
		}
		//symbolTable.removeIdent(currentLevel);
		//currentLevel--;


		return new WhileStmt(condition, body, elseBody);
	}
	
	public ForStmt for_stmt(){
		/*  for_stmt: for' exprlist 'in' atom ':' suite ['else' ':' suite] |
	  		'for' exprlist 'in' 'range' '(' NUMBER ',' NUMBER ')' ':' suite ['else' ':' suite]  */
	  	
		ExprList exprs = null;
		Atom list[] = {null, null};
		Suite body = null, elseBody = null;

		String tipo = "INT";

		forDeclarationVariables.clear();

		errorif("Syntax error: 'for' expected", Symbol.FOR);
		lexer.nextToken();
		//currentLevel++;
		declare=true;
		exprs = exprlist();
		declare=false;
		
		errorif("Syntax error: 'in' expected", Symbol.IN);
		lexer.nextToken();

		if(is(Symbol.RANGE)){
			if(exprs.size() != 1)
				error("Scope error: Too many variables to iterate in range");

			lexer.nextToken();

			errorif("Syntax error: '(' expected", Symbol.LEFTPAR);
			lexer.nextToken();

			errorif("Syntax error: NUMBER expected", Symbol.NUM);
			tipo = lexer.getNumberType();

			if(!tipo.equals("INT"))
				error("Type error: range() integer start argument expected, got float.");

			list[0] = new NumberAtom(lexer.getNumber(), tipo);
			lexer.nextToken();

			errorif("Syntax error: ',' expected", Symbol.COMMA);
			lexer.nextToken();

			errorif("Syntax error: NUMBER expected", Symbol.NUM);
			tipo = lexer.getNumberType();

			if(!tipo.equals("INT"))
				error("Type error: range() integer end argument expected, got float.");

			list[1] = new NumberAtom(lexer.getNumber(), tipo);
			lexer.nextToken();

			errorif("Syntax error: ')' expected", Symbol.RIGHTPAR);
			lexer.nextToken();

		} else {
			isForAtom = true;
			list[0] = atom();
			isForAtom = false;

			if(!(list[0] instanceof Listmaker) && !(list[0] instanceof StringAtom) && !TypeManager.isList(lastType))
				error("Type error: Object is not iterable");

			if(list[0] instanceof StringAtom)
				tipo = "STRING";

			else if(TypeManager.isList(lastType))
				tipo = TypeManager.getListType(lastType);

			if(list[0] instanceof NameAtom && 
				symbolTable.get(((NameAtom)list[0]).getName(), currentLevel) == null){
					error("Name error: "+((NameAtom)list[0]).getName() + " does not exist");
			}
		}

		ArrayList<NameAtom> variables = new ArrayList<NameAtom>();

		for(String name : forDeclarationVariables){
			NameAtom nameatom = (NameAtom) symbolTable.get(name, currentLevel);
			nameatom.setType(tipo);
			variables.add(nameatom);
			// put(name, nameatom, currentLevel);
		}

		errorif("Syntax error: ':' expected", Symbol.COLON);
		lexer.nextToken();		

		body = suite();
		
		if(is(Symbol.ELSE)){
			lexer.nextToken();

			errorif("Syntax error: ':' expected", Symbol.COLON);
			lexer.nextToken();		

			elseBody = suite();
			//symbolTable.removeIdent(currentLevel);
		}

		//currentLevel--;

		return new ForStmt(exprs, variables, list, body, elseBody);	
	}
	
	public TryStmt try_stmt(){
		/*  try_stmt: 'try' ':' suite
		   ((except_clause ':' suite)+
			['else' ':' suite]
			['finally' ':' suite] |
		   'finally' ':' suite)
		 */
	//currentLevel++;

		Suite body = null, elseBody = null, finallyBody = null;
		ArrayList<ExceptClause> exceptCondition = new ArrayList<ExceptClause>();
		ArrayList<Suite> exceptBody = new ArrayList<Suite>();

		errorif("Syntax error: 'try' expected", Symbol.TRY);
		lexer.nextToken();

		errorif("Syntax error: ':' expected", Symbol.COLON);
		lexer.nextToken();		

		body = suite();
		//symbolTable.removeIdent(currentLevel);

		if(is(Symbol.FINALLY)){
			lexer.nextToken();

			errorif("Syntax error: ':' expected", Symbol.COLON);
			lexer.nextToken();

			finallyBody = suite();
			//symbolTable.removeIdent(currentLevel);
		} else if(is(Symbol.EXCEPT)){

			while(is(Symbol.EXCEPT)){
				exceptCondition.add(except_clause());

				errorif("Syntax error: ':' expected", Symbol.COLON);
				lexer.nextToken();

				exceptBody.add(suite());
				//symbolTable.removeIdent(currentLevel);
			}

			if(is(Symbol.ELSE)){
				lexer.nextToken();

				errorif("Syntax error: ':' expected", Symbol.COLON);
				lexer.nextToken();

				elseBody = suite();
				//symbolTable.removeIdent(currentLevel);
			}

			if(is(Symbol.FINALLY)){
				lexer.nextToken();

				errorif("Syntax error: ':' expected", Symbol.COLON);
				lexer.nextToken();

				finallyBody = suite();
				//symbolTable.removeIdent(currentLevel);
			}	
		} else
			error("Syntax error: 'except' or 'finally' expected");
		//currentLevel--;

		return new TryStmt(body, exceptCondition, exceptBody, elseBody, finallyBody);
	}

	public WithStmt with_stmt(){
		/*  with_stmt: 'with' with_item (',' with_item)*  ':' suite */
		//currentLevel++;
		ArrayList<WithItem> items = new ArrayList<WithItem>();

		errorif("Syntax error: WITH expected", Symbol.WITH);
		lexer.nextToken();

		items.add(with_item());

		while(is(Symbol.COMMA)){
			lexer.nextToken();
			items.add(with_item());
		}

		errorif("Syntax error: ':' expected", Symbol.COLON);
		lexer.nextToken();

		Suite body = suite();
		//symbolTable.removeIdent(currentLevel);
		//currentLevel--;

		return new WithStmt(items, body);
	}

	public FuncDef funcdef(){
		/*  funcdef: 'def' ID parameters ':' suite */

		if(isFuncDef)
			error("Scope error: Nested functions are not allowed");

		currentLevel++;
		returnType = null;

		errorif("Syntax error: 'def' expected", Symbol.DEF);
		lexer.nextToken();

		errorif("Syntax error: ID expected", Symbol.ID);
		String name = lexer.getVar();

		lexer.nextToken();

		VarArgList params = parameters();

		functions.add(name, className);
		FuncDef function = new FuncDef(name, className, params);
		funcPointers.add(function);

		boolean hasSelf = false;
		int index = functions.get(name, className);
		for(NameAtom n : funcParams){
			if(n.getName().equals("self"))
				hasSelf = true;

			functions.addVar(index, n.getType());
		}

		if(!hasSelf && className != null)
			error("Scope error: methods must have a 'self' attribute");

		errorif("Syntax error: ':' expected", Symbol.COLON);
		lexer.nextToken();

		isFuncDef = true;
		Suite body = suite();
		isFuncDef = false;
		
		symbolTable.removeIdent(currentLevel);
		
		currentLevel--;

		NameAtom a = new NameAtom(name, className != null && !isFuncDef);
		a.setIsFunction();
		a.setType("VOID");
		put(name, a, currentLevel);

		funcParams.clear();

		function.setAttributes(body, returnType);

		return function;
	}

	public ClassDef classdef(){
		/*  'class' NAME ['(' ([atom [',' atom]* | [testlist]) ] ')'] ':' suite */

		if(className != null)
			error("Scope error: Nested classes are not allowed");

		errorif("Syntax error: 'class' expected", Symbol.CLASS);
		lexer.nextToken();
		Atom a = null;
		TestList tl = null;

		errorif("Syntax error: ID expected", Symbol.ID);
		String name = lexer.getVar();
		className = name;
		classesVars.add(new ArrayList<StringPair>());
		NameAtom b = new NameAtom(name, className != null && !isFuncDef);

		classesNames.add(name);

		b.setType("VOID");
		put(name,b,currentLevel);
		currentLevel++;

		lexer.nextToken();

		ArrayList<Atom> list = new ArrayList<Atom>();

		if(is(Symbol.LEFTPAR)){

			lexer.nextToken();

			if(is(Symbol.GRAVEACCENT, Symbol.ID, Symbol.NUM, Symbol.STRING, Symbol.LEFTCURBRACKET, Symbol.LEFTPAR)){
				a = atom();
				list.add(a);
				if(a instanceof NameAtom)
					put(((NameAtom)a).getName(),a,currentLevel);

				while(is(Symbol.COMMA)){
					lexer.nextToken();
					errorif("Syntax error: GRAVEACCENT, NAME, NUMBER, STRING, '(' or '[' expected", Symbol.GRAVEACCENT, Symbol.ID, Symbol.NUM, Symbol.STRING, Symbol.LEFTCURBRACKET, Symbol.LEFTPAR);
					a = atom();
					list.add(a);
					if(a instanceof NameAtom)
						put(((NameAtom)a).getName(),a,currentLevel);
				}
			} else if(is(Symbol.PLUS, Symbol.MINUS, Symbol.INVERTION, Symbol.GRAVEACCENT, 
			Symbol.ID, Symbol.NUM, Symbol.STRING, Symbol.NOT, Symbol.LEFTCURBRACKET))
				tl = testlist();

			errorif("Syntax error: ')' expected", Symbol.RIGHTPAR);
			lexer.nextToken();
		}

		errorif("Syntax error: ':' expected", Symbol.COLON);
		lexer.nextToken();

		Suite body = suite();
		symbolTable.removeIdent(currentLevel);
		currentLevel--;

		className = null;
		return new ClassDef(name, list, tl, body);
	}

	public Suite suite(){
		/*  suite: simple_stmt | NEWLINE INDENT stmt+ DEDENT */
		ArrayList<Stmt> statements = new ArrayList<Stmt>();

		if(is(Symbol.ID, Symbol.PRINT, Symbol.BREAK, Symbol.CONTINUE, Symbol.RETURN))
			statements.add(simple_stmt());

		else if(is(Symbol.NEWLINE)){
			lexer.nextToken();

			errorif("Syntax error: INDENT expected", Symbol.INDENT);
			lexer.nextToken();

			do{
				statements.add(stmt());
			} while(is(Symbol.NEWLINE, Symbol.ID, Symbol.PRINT, Symbol.BREAK, 
				Symbol.CONTINUE, Symbol.RETURN, Symbol.IF, Symbol.WHILE, Symbol.FOR, 
				Symbol.TRY, Symbol.WITH, Symbol.DEF));

			errorif("Syntax error: DEDENT expected", Symbol.DEDENT);
			lexer.nextToken();
		} else
		error("Syntax error: ID, 'print', 'break', 'continue', 'return' or NEWLINE expected");

		return new Suite(statements);
	}

	public Test test(){
		StringBuilder sb = new StringBuilder();
		return test(sb);
	}

	public Test test(StringBuilder sb){
		isTest = true;
		analyser.reset();
		Test t = realTest();

		// System.out.println(analyser.trace());

		analyser.evaluate();
		isTest = false;

		String newType = analyser.firstNonVoidType;

		if(analyser.isNumeric())
			newType = "DOUBLE";

		else if(analyser.isIllegal())
			error("Type error: expression has a void-returning function");

		else if(analyser.hasNotOnlyClasses())
			error("Type error: You cannot mix classes and other types");

		else if(analyser.hasNotOnlyString())
			error("Type error: You cannot mix strings and other types");

		else if(analyser.hasNotOnlyLists())
			error("Type error: You cannot mix lists and other types");

		else if(analyser.illegalListOperation())
			error("Scope error: Operations in lists are not allowed");

		if(isFuncDef && !analyser.isVoid()){
			for(NameAtom p : exprFuncParams)
				if(p != null && p.getType() != null && p.getType().equals("VOID"))
					p.setType(newType);
		}

		if(newType != null)
			sb.append(newType);
		else
			sb = null;

		return t;
	}

	public Test realTest(){
		/*  test: or_test ['if' or_test 'else' test] */

		OrTest orTest = or_test();
		OrTest condition = null;
		Test elseBody = null;

		if(is(Symbol.IF)){
			lexer.nextToken();

			condition = or_test();

			errorif("Syntax error: 'else' expected", Symbol.ELSE);
			lexer.nextToken();

			elseBody = test();
		}

		return new Test(orTest, condition, elseBody);
	}

	public OrTest or_test(){
		/*  or_test: and_test ('or' and_test)* */
		ArrayList<AndTest> list = new ArrayList<AndTest>();
		list.add(and_test());

		while(is(Symbol.OR)){
			analyser.addOpr(lexer.getToken());
			lexer.nextToken();
			list.add(and_test());
		}

		return new OrTest(list);
	}

	public AndTest and_test(){
		/*  and_test: not_test ('and' not_test)* */
		ArrayList<NotTest> list = new ArrayList<NotTest>();
		list.add(not_test());

		while(is(Symbol.AND)){
			analyser.addOpr(lexer.getToken());
			lexer.nextToken();
			list.add(not_test());
		}

		return new AndTest(list);
	}

	public NotTest not_test(){
		/*  not_test: 'not' not_test | comparison */

		NotTest statement =  null;

		if(is(Symbol.NOT)){
			analyser.addOpr(lexer.getToken());
			lexer.nextToken();
			statement = new NotStatement(not_test());
		} else
			statement = comparison();

		return statement;
	}

	public Comparision comparison(){
		/*  comparison: expr (comp_op expr)* */

		Expr left = expr();
		String operator;
		ArrayList<String> operators = new ArrayList<String>();
		ArrayList<Expr> statements = new ArrayList<Expr>();
		ArrayList<Boolean> strcmp = new ArrayList<Boolean>();

		while(comp_op()){
			operator = "";

			Symbol aux = lexer.getToken();
			Symbol analyserAux = aux;
			lexer.nextToken();

			operator += Word.valueOf(aux.name()).get();

			if((aux == Symbol.IS && is(Symbol.NOT)) || (aux == Symbol.NOT && is(Symbol.IN))){

				if((aux == Symbol.IS && is(Symbol.NOT)))
					analyserAux = Symbol.ISNOT;
				else
					analyserAux = Symbol.NOTIN;

				operator += lexer.getToken().name();
				lexer.nextToken();
			}

			analyser.addOpr(analyserAux);
			operators.add(operator);

			String sequence[][] = {{"STRING"}, {"EQ", "NEQ", "NEQC"}};

			strcmp.add(analyser.sliceIs(-2, analyser.end(), sequence));
			statements.add(expr());
		}

		return new Comparision(left, operators, strcmp, statements);
	}

	public Expr expr(){
		/*  expr: xor_expr ('|' xor_expr)* */
		ArrayList<XorExpr> xors = new ArrayList<XorExpr>();

		xors.add(xor_expr());

		while(is(Symbol.PIPE)){
			analyser.addOpr(lexer.getToken());
			lexer.nextToken();
			xors.add(xor_expr());
		}

		return new Expr(xors);
	}

	public ExprList exprlist(){
		/*  exprlist: expr (',' expr)* [','] */
		ArrayList<Expr> exprs = new ArrayList<Expr>();

		exprs.add(expr());

		while(is(Symbol.COMMA)){
			lexer.nextToken();

			if(is(Symbol.PLUS, Symbol.MINUS, Symbol.INVERTION, Symbol.GRAVEACCENT, Symbol.ID, 
				Symbol.NUM, Symbol.STRING))
				exprs.add(expr());
		}

		return new ExprList(exprs);
	}

	public boolean comp_op(){
		/*  comp_op: '<'|'>'|'=='|'>='|'<='|'<>'|'!='|'in'|'not' 'in'|'is'|'is' 'not' */
		return is(Symbol.LT, Symbol.GT, Symbol.EQ, Symbol.GE, Symbol.LE, 
			Symbol.NEQ, Symbol.NEQC, Symbol.IN, Symbol.IS, Symbol.NOT);
	}

	public XorExpr xor_expr(){
		/*  xor_expr: and_expr ('^' and_expr)* */
		ArrayList<AndExpr> ands = new ArrayList<AndExpr>();

		ands.add(and_expr());

		while(is(Symbol.XOR)){
			analyser.addOpr(lexer.getToken());
			lexer.nextToken();
			ands.add(and_expr());
		}

		return new XorExpr(ands);
	}

	public AndExpr and_expr(){
		/*  and_expr: arith_expr ('&' arith_expr)* */
		ArrayList<ArithExpr> ariths = new ArrayList<ArithExpr>();

		ariths.add(arith_expr());

		while(is(Symbol.AMPERSAND)){
			analyser.addOpr(lexer.getToken());
			lexer.nextToken();
			ariths.add(arith_expr());
		}

		return new AndExpr(ariths);
	}

	public ArithExpr arith_expr(){
		/*  arith_expr: term (('+'|'-') term)* */
		ArrayList<Term> terms = new ArrayList<Term>();
		ArrayList<String> operators = new ArrayList<String>();

		terms.add(term());

		while(is(Symbol.PLUS, Symbol.MINUS)){
			analyser.addOpr(lexer.getToken());
			operators.add(Word.valueOf(lexer.getToken().name()).get());
			lexer.nextToken();
			terms.add(term());
		}

		boolean hasStrings = analyser.hasStrings();

		return new ArithExpr(terms, operators, hasStrings);
	}

	public Term term(){
		/*  term: factor (('*'|'/'|'%'|'//') factor)* */
		ArrayList<Factor> factors = new ArrayList<Factor>();
		ArrayList<String> operators = new ArrayList<String>();

		factors.add(factor());

		while(is(Symbol.ASTERISK, Symbol.DIV, Symbol.MOD, Symbol.FLOORDIV)){
			analyser.addOpr(lexer.getToken());
			operators.add(Word.valueOf(lexer.getToken().name()).get());
			lexer.nextToken();
			factors.add(factor());
		}

		return new Term(factors, operators);
	}

	public Factor factor(){
		/*  factor: ('+'|'-'|'~') factor | power */

		if(is(Symbol.PLUS, Symbol.MINUS, Symbol.INVERTION)){
			analyser.addOpr(lexer.getToken());
			String signal = lexer.getTokenValue();
			lexer.nextToken();
			return new FactorStatement(signal, factor());

		} else if(is(Symbol.GRAVEACCENT, Symbol.ID, Symbol.NUM, Symbol.STRING, Symbol.LEFTCURBRACKET, Symbol.LEFTPAR))
			return power();

		else
			error("Syntax error: '+', '-', '~', '`', '[', ID, NUM or STRING expected");

		return null;
	}

	public Power power(){
		// power: atom trailer* ['**' factor]

		Atom a = atom();
		ArrayList<Trailer> t = new ArrayList<Trailer>();
		Factor f = null;
		String instance = null;
		String isInstanceOf = null;
		String name = null;

		String tipo = null;
		boolean isSelf = (className != null && a instanceof NameAtom && ((NameAtom)a).getName().equals("self"));
		boolean isDirectInstance = false;

		if(a instanceof NameAtom){
			NameAtom nameatom = (NameAtom) a;
			name = nameatom.getName();

			if(declare){
				forDeclarationVariables.add(name);
				put(name, a, currentLevel);
			} else {
				if(!isTarget && symbolTable.get(name, currentLevel) == null){
					if(!isSelf && !(isTest && functions.contains(name, className)))
						error("Name error: The variable " + name + " is not declared");
				} else if(!isTarget){
					NameAtom b = (NameAtom) symbolTable.get(name,currentLevel);
					lastType = b.getType();
					tipo = lastType;
				} else if(isTarget){			
					put(name, a, currentLevel);
				} else if(isTargetTest && symbolTable.getLevel(name, currentLevel) != null){
					if(!(nameatom.getType().equals(lastType) && !lastType.equals("VOID") && !nameatom.getType().equals("VOID")))
						error("Type error: Expected " + lastType + ", " + nameatom.getType() + " got");
				}

				// vartype

				if(isTest){
					if(classesNames.contains(name)){
						if(!is(Symbol.NEWLINE, Symbol.EOF, Symbol.LEFTPAR))
							error("Type error: instance must be the only statement in an expression");
						else {
							instance = name;

							if(is(Symbol.NEWLINE, Symbol.EOF)){
								analyser.addVar(instance, Expression.CLASS);						
								isDirectInstance = true;
							}
						}

					} else if(functions.contains(name, className)){
						
						if(!is(Symbol.LEFTPAR))
							error("Scope error: Cannot make a pointer reference of function \"" + name + "\"");

					} else {
						NameAtom obj = (NameAtom) symbolTable.get(name, currentLevel);
						exprFuncParams.add(obj);

						if(isSelf){
							isInstanceOf = className;
							name = "self";
						} else if(classesNames.contains(obj.getType())){
							isInstanceOf = obj.getType();
							name = obj.getName();
						} else if(tipo != null)
							analyser.addVar(tipo);
					}
				}
			}
		}

		if(is(Symbol.LEFTPAR, Symbol.DOT))
			caller = ((NameAtom) a).getName();

		else if(isInstanceOf != null)
			analyser.addVar(isInstanceOf, Expression.CLASS);

		if(is(Symbol.LEFTPAR) && isInstanceOf != null)
			error("Type error: Cannot call object \"" + name + "\" as a function");

		int counter = 0;
		ReferenceTrio data = null;

		while(is(Symbol.LEFTPAR, Symbol.DOT)){
			if(instance != null && is(Symbol.DOT))
				error("Scope error: Cannot make a static reference of neither an attribute nor a method of a class");

			if(data != null && isInstanceOf != null && ((counter == 1 && !data.function) || (counter == 2)))
				error("Scope error: \"" + name + "\" has no more than one level of methods/attributes");

			data = new ReferenceTrio(instance, isInstanceOf, true);
			t.add(trailer(data, name));
			counter++;
		}

		if(is(Symbol.DOUBLEASTERISK)){
			analyser.addOpr(Symbol.DOUBLEASTERISK);
			lexer.nextToken();
			f = factor();
		}

		instance = null;
		isInstanceOf = null;

		return new Power(a, t, f, isDirectInstance);
	}

	public Trailer trailer(NameAtom ref, ReferenceTrio data, boolean isFunctionCall, String self){
		String name = ref.getName();
		NameAtom obj = (NameAtom) symbolTable.get(name, currentLevel);
		String tipo = obj.getType();

		data.instance = null;
		data.isInstanceOf = tipo;

		if(isFunctionCall)
			data.isInstanceOf = null;

		if(is(Symbol.LEFTPAR))
			return trailer(data, self);

		if(!classesNames.contains(tipo))
			error("Name error: \"" + tipo + "\" is not a class");

		return trailer(data, self);
	}

	public Trailer trailer(ReferenceTrio data, String self){
		// trailer: '(' arglist ')' | '.' NAME // Adaptado da gramatica original, sem: | '[' subscriptlist ']'

		Trailer statement = null;

		if(is(Symbol.LEFTPAR)){

			lexer.nextToken();

			if(data.instance != null && !is(Symbol.RIGHTPAR))
				error("Scope error: Instances take no arguments");

			statement = arglist(data.instance, data.isInstanceOf, self);

			errorif("Syntax error: ')' expected", Symbol.RIGHTPAR);
			lexer.nextToken();

		} else if(is(Symbol.DOT)) {

			lexer.nextToken();
			errorif("Syntax error: ID expected", Symbol.ID);

			String name = lexer.getVar();
			lexer.nextToken();

			String ref = data.isInstanceOf == null ? className : data.isInstanceOf;

			statement = new DotName(name, ref, is(Symbol.LEFTPAR));

			caller = name;

			StringBuilder sb = new StringBuilder();

			data.function = true;

			if(!classHasAttr(ref, name, sb) && !functions.contains(name, ref))
				error("Attribute error: The class \"" + ref + "\" has neither an attribute nor a function named \"" + name + "\"");

			if(classHasAttr(ref, name, null) && is(Symbol.LEFTPAR))
				error("Type error: Cannot call attribute \"" + name + "\" as a function");

			if(!functions.contains(name, ref)){
				analyser.addVar(sb.toString());
				lastType = sb.toString();
				data.function = new Boolean(false);

			} else {
				if(is(Symbol.DOT))
					error("Scope error: \"" + data.isInstanceOf + "\" has no more than one level of methods/attributes");
				
				if(!is(Symbol.LEFTPAR))
					error("Scope error: Cannot make a pointer reference of function \"" + name + "\"");
			}

		} else 
			error("Syntax error: '(' or '.' expected");

		return statement;
	}

	public ArgList arglist(String instance, String isInstanceOf, String self){
		// arglist: (test ',')*
		ArrayList<Test> list = new ArrayList<Test>();
		ArrayList<String> var = new ArrayList<String>();

		while(is(Symbol.PLUS, Symbol.MINUS, Symbol.INVERTION, Symbol.GRAVEACCENT, 
					Symbol.ID, Symbol.NUM, Symbol.STRING, Symbol.NOT)){

			StringBuilder sb = new StringBuilder();
			list.add(test(sb));

			var.add(sb.toString());

			if(is(Symbol.COMMA))
				lexer.nextToken();
			else
				break;
		}

		if(instance != null){
			lastType = instance;
			analyser.addVar(lastType, Expression.CLASS);
			return new ArgList(list, -1, null);
		}

		String ref = isInstanceOf == null ? className : isInstanceOf;

		boolean ignoreSelf = isInstanceOf != null;

		int index = functions.get(caller, ref);
		int selfpos = getFunctionPointer(caller, ref).getSelfPosition();
		int status = functions.matches(index, var, ignoreSelf);

		if(status == -1){
			int narg = functions.at(index).size() - (ignoreSelf ? 1 : 0);
			error("Type error: The function \"" + caller + "\" takes " + narg + " argument" + (narg == 1 ? "" : "s") + ", " + var.size() + " given");
		}

		FuncDef function = getFunctionPointer(caller, ref);

		while(status != -2){
			String typeA = functions.at(index).getVar(status);
			String typeB = var.get(status);

			if(typeA.equals("VOID")){
				function.changeParamType(status, typeB);
				functions.at(index).setVar(status, typeB);
				
				status = functions.matches(index, var, ignoreSelf);
				continue;
			}

			error("Type error: Argument " + (status + 1) + " of function \"" + caller + "\" should be " + typeA + ", got " + typeB);
		}

		if(!isFuncDef){
			lastType = function.getOriginalType();
			analyser.addVar(lastType, Expression.FUNCTION);
		}

		return new ArgList(list, selfpos, self);
	}

	public Atom atom(){
		/*  atom: ('[' [listmaker] ']' | '`' testlist1 '`' | NAME | NUMBER | STRING+) */

		Atom statement = null;
		Symbol a = lexer.getToken();

		if(is(Symbol.GRAVEACCENT)){
			lexer.nextToken();
			statement = testlist1();
			errorif("Syntax error: GRAVEACCENT expected", Symbol.GRAVEACCENT);
			lexer.nextToken();

			return statement;
		} else if(is(Symbol.LEFTCURBRACKET)){
			lexer.nextToken();
			statement = listmaker();
			errorif("Syntax error: ']' expected", Symbol.RIGHTCURBRACKET);
			lexer.nextToken();
		} else if(is(Symbol.ID)){ //nesse caso, vai ser tratado em power
			statement = new NameAtom(lexer.getVar(), className != null && !isFuncDef);
			lexer.nextToken();

			if(isForAtom){
				NameAtom nameatom = (NameAtom) symbolTable.get(((NameAtom) statement).getName(), currentLevel);
				NameAtom old = (NameAtom) statement;
				old.setType(nameatom.getType());
				old.setSize(nameatom.getSize());
			}

		} else if(is(Symbol.NUM)){
			lastType = lexer.getNumberType(); // mudou: era NUM
			statement = new NumberAtom(lexer.getNumber(), lastType);
			lexer.nextToken();

			analyser.addVar(lastType);
		} else if(is(Symbol.STRING)){
			String aux = "";
			lastType="STRING";

			analyser.addVar(lastType);

			while(is(Symbol.STRING)){
				aux += lexer.getStr();
				lexer.nextToken();
			}

			statement = new StringAtom(aux);
		} else
			error("Syntax error: `TestList1`, [list], ID, NUM or STRING expected");

		return statement;
	}

	public TestList testlist(){
		/*  testlist: test (',' test)* [','] */
		ArrayList<Test> tests = new ArrayList<Test>();

		tests.add(test());

		while(is(Symbol.COMMA)){
			lexer.nextToken();

			if(is(Symbol.PLUS, Symbol.MINUS, Symbol.INVERTION, Symbol.GRAVEACCENT, 
				Symbol.ID, Symbol.NUM, Symbol.STRING, Symbol.NOT, Symbol.LEFTCURBRACKET)){
				tests.add(test());
			}
		}

		return new TestList(tests);
	}

	public TestList1 testlist1(){
		/*  testlist1: test (',' test)* */
		ArrayList<Test> tests = new ArrayList<Test>();

		tests.add(test());

		while(is(Symbol.COMMA)){
			lexer.nextToken();
			tests.add(test());
		}

		return new TestList1(tests);
	}

	public Listmaker listmaker(){
		/*  listmaker: test (',' test)*  */
		if(isListMaker)
			error("Scope error: Nested lists are not allowed");

		isListMaker = true;

		ArrayList<Test> tests = new ArrayList<Test>();

		StringBuilder sb = new StringBuilder();

		tests.add(test(sb));

		int size = 1;

		while(is(Symbol.COMMA)){
			size++;

			StringBuilder got = new StringBuilder();

			lexer.nextToken();
			tests.add(test(got));

			if(!got.toString().equals(sb.toString()))
				error("Type error: elements should be " + sb + ", got " + got);
		}

		lastType = sb.toString() + "_LIST";
		listsSizes.add(size);

		analyser.reset();
		analyser.addVar(lastType);

		isListMaker = false;

		return new Listmaker(tests, TypeManager.getTypeId(sb.toString()));
	}

	public ExceptClause except_clause(){
		/*  except_clause: 'except' [test [('as' | ',') test]] */

		ArrayList<Test> tests = new ArrayList<Test>();

		errorif("Syntax error: EXCEPT expected", Symbol.EXCEPT);
		lexer.nextToken();

		if(is(Symbol.NOT, Symbol.PLUS, Symbol.MINUS, Symbol.INVERTION, Symbol.GRAVEACCENT, 
			Symbol.ID, Symbol.NUM, Symbol.STRING, Symbol.LEFTCURBRACKET)){
			lexer.nextToken();
			tests.add(test());

			if(is(Symbol.AS, Symbol.COMMA)) {
				lexer.nextToken();
				tests.add(test());
			}
		}

		return new ExceptClause(tests);
	}

	public WithItem with_item(){
		/*  with_item: test ['as' expr] */

		Test left = test();
		Expr as = null;

		if(is(Symbol.AS)){
			lexer.nextToken();
			as = expr();
		}

		return new WithItem(left, as);
	}

	public VarArgList parameters(){
		/*  parameters: '(' [varargslist] ')' */

		VarArgList params = null;

		errorif("Syntax error: '(' expected", Symbol.LEFTPAR);
		lexer.nextToken();
		
		if(is(Symbol.ID, Symbol.LEFTPAR, Symbol.ASTERISK, Symbol.DOUBLEASTERISK))
			params = varargslist();
		
		errorif("Syntax error: ')' expected", Symbol.RIGHTPAR);
		lexer.nextToken();

		return params;
	}
	
	public VarArgList varargslist(){
		/*  varargslist:
		(fpdef ['=' test] ',')* ('*' ID [',' '**' ID] | '**' ID) | 
		fpdef ['=' test] (',' fpdef ['=' test])* [',']
		*/

		ArrayList<Pair> pairs = new ArrayList<Pair>();
		String asterisk = null, doubleAsterisk = null;

		boolean first_case = false;
		int pairs_count = 0;

		while(is(Symbol.ID, Symbol.LEFTPAR)){
			pairs_count++;
			pairs.add(new Pair(new ArrayList<Target>(), new ArrayList<Test>()));

			pairs.get(pairs_count - 1).fpdef.addAll(fpdef());
			
			if(is(Symbol.ASSIGN)){
				lexer.nextToken();
				pairs.get(pairs_count - 1).tests.add(test());
			}
			
			if(is(Symbol.COMMA)){
				lexer.nextToken();

				if(is(Symbol.ASTERISK, Symbol.DOUBLEASTERISK))
					first_case = true;
				
				if(!is(Symbol.ID, Symbol.LEFTPAR))
					break;
			}
		}

		if(first_case){
			if(is(Symbol.ASTERISK)){
				lexer.nextToken();

				errorif("Syntax error: ID expected", Symbol.ID);

				asterisk = lexer.getVar();

				lexer.nextToken();

				if(is(Symbol.COMMA)){
					lexer.nextToken();

					errorif("Syntax error: '**' expected", Symbol.DOUBLEASTERISK);
					lexer.nextToken();

					errorif("Syntax error: ID expected", Symbol.ID);

					doubleAsterisk = lexer.getVar();

					lexer.nextToken();
				}
			} else if(is(Symbol.DOUBLEASTERISK)) {
				lexer.nextToken();

				errorif("Syntax error: ID expected", Symbol.ID);

				doubleAsterisk = lexer.getVar();

				lexer.nextToken();
			}
		}

		return new VarArgList(pairs, asterisk, doubleAsterisk, className);
	}
	
	public ArrayList<Target> fplist(){
		// fplist: fpdef (',' fpdef)* [',']	
		ArrayList<Target> names = new ArrayList<Target>();

		names.addAll(fpdef());
		
		while(is(Symbol.COMMA)){
			lexer.nextToken();
			
			if(is(Symbol.LEFTPAR, Symbol.ID))
				names.addAll(fpdef());
		}

		return names;
	}

	public ArrayList<Target> fpdef(){
		// fpdef: target | '(' fplist ')'
		ArrayList<Target> names = new ArrayList<Target>();

		if(is(Symbol.LEFTPAR)){
			lexer.nextToken();
			names.addAll(fplist());
			errorif("Syntax error: ')' expected", Symbol.RIGHTPAR);
			lexer.nextToken();
		} else {
			Target t = target();
			t.setType("VOID");
			names.add(t);
			put(t.getName(), t, currentLevel);

			funcParams.add(t);
		}
		
		return names;
	}
}
