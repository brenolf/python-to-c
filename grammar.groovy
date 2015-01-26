// # comment

// # Initial symbol
file_input: (NEWLINE | stmt)* ENDMARKER

stmt: simple_stmt | compound_stmt
simple_stmt: small_stmt (';' small_stmt)* [';'] (NEWLINE | ENDMARKER)
small_stmt: (expr_stmt | print_stmt | flow_stmt)

// Adapatacao da gramatica orignal para nao verificar testlist em expr_stmt
expr_stmt: targetlist ((augassign testlist) | trailer*)
augassign: ('=' | '+=' | '-=' | '*=' | '/=' | '%=' | '&=' | '|=' | '^=')
targetlist: target ("," target)* [","]
target: NAME

print_stmt: 'print' ( [ test (',' test)* [','] ] | '>>' test [ (',' test)+ [','] ] )
flow_stmt: break_stmt | continue_stmt | return_stmt 
break_stmt: 'break'
continue_stmt: 'continue'
return_stmt: 'return' [testlist]

compound_stmt: if_stmt | while_stmt | for_stmt | try_stmt | with_stmt | funcdef | classdef
if_stmt: 'if' test ':' suite ('elif' test ':' suite)* ['else' ':' suite]
while_stmt: 'while' test ':' suite ['else' ':' suite]
for_stmt: 'for' exprlist 'in' atom ':' suite ['else' ':' suite] |
	  		'for' exprlist 'in' 'range' '(' NUMBER ',' NUMBER ')' ':' suite ['else' ':' suite] 

try_stmt: ('try' ':' suite
		   ((except_clause ':' suite)+
			['else' ':' suite]
			['finally' ':' suite] |
		   'finally' ':' suite))
with_stmt: 'with' with_item (',' with_item)*  ':' suite
with_item: test ['as' expr]
exprlist: expr (',' expr)* [',']

except_clause: 'except' [test [('as' | ',') test]]
suite: simple_stmt | NEWLINE INDENT stmt+ DEDENT

funcdef: 'def' NAME parameters ':' suite
parameters: '(' [varargslist] ')'
varargslist: ((fpdef ['=' test] ',')*
			  ('*' NAME [',' '**' NAME] | '**' NAME) |
			  fpdef ['=' test] (',' fpdef ['=' test])* [','])
fpdef: target | '(' fplist ')'
fplist: fpdef (',' fpdef)* [',']

classdef: 'class' NAME ['(' ([atom [',' atom]* | [testlist]) ] ')'] ':' suite

test: or_test ['if' or_test 'else' test]
or_test: and_test ('or' and_test)*
and_test: not_test ('and' not_test)*
not_test: 'not' not_test | comparison
comparison: expr (comp_op expr)*
comp_op: '<'|'>'|'=='|'>='|'<='|'<>'|'!='|'in'|'not' 'in'|'is'|'is' 'not'
expr: xor_expr ('|' xor_expr)*
xor_expr: and_expr ('^' and_expr)*
and_expr: arith_expr ('&' arith_expr)*
arith_expr: term (('+'|'-') term)*
term: factor (('*'|'/'|'%'|'//') factor)*
factor: ('+'|'-'|'~') factor | power
power: atom trailer* ['**' factor]
atom: (/*'(' [yield_expr|testlist_comp] ')' |*/ '[' [listmaker] ']' | '`' testlist1 '`' | NAME | NUMBER | STRING+)

// Adaptado da gramatica original, sem: | '[' subscriptlist ']' e arglist obrigatorio
trailer: '(' arglist ')' | '.' NAME
// Adaptado da gramatica original
arglist: (test ',')*
// argument: test [comp_for] | test '=' test

/*
	Dependencias para expansao da gramatica em '[' subscriptlist ']':
	subscriptlist: subscript (',' subscript)* [',']
	subscript: '.' '.' '.' | test | [test] ':' [test] [sliceop]
	sliceop: ':' [test]

	arglist: (argument ',')* (argument [',']
             |'*' test (',' argument)* [',' '**' test] 
             |'**' test)
*/

listmaker: test (',' test)* 

testlist: test (',' test)* [',']
testlist1: test (',' test)*

/*
yield_expr: 'yield' [testlist]
testlist_comp: test ( comp_for | (',' test)* [','] )
*/

// Adaptado da gramatica original: comp_for: 'for' exprlist 'in' or_test [comp_iter]
// comp_for: 'for' exprlist 'in' or_test [comp_for]

/*
	Dependencias para expansao da gramatica em comp_for:

	comp_for: 'for' exprlist 'in' or_test [comp_iter]
	comp_iter: comp_for | comp_if
	comp_if: 'if' old_test [comp_iter]
	old_test: or_test | old_lambdef
	old_lambdef: 'lambda' [varargslist] ':' old_test
*/