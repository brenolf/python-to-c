# Python to C

This project is a very simple Java implementation of a compiler for a more simple python grammar (shown in grammar.groovy) which takes a python source code and outputs (if everything is correct) a corresponding C code for that.

Any doubt about the project, feel free to contact me.

### Adapted grammar

This is the grammar used for the compiler. It is an adaptation of the original python grammar.

```groovy
file_input: (NEWLINE | stmt)* ENDMARKER

stmt: simple_stmt | compound_stmt
simple_stmt: small_stmt (';' small_stmt)* [';'] (NEWLINE | ENDMARKER)
small_stmt: (expr_stmt | print_stmt | flow_stmt)

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

trailer: '(' arglist ')' | '.' NAME
arglist: (test ',')*

listmaker: test (',' test)* 

testlist: test (',' test)* [',']
testlist1: test (',' test)*
```

### Example

The following python code:

```python
def imprimeLista(argInt, argStr, argDbl, argByFuncCall, argByVar):
	minhaLista = [1, 2, 3]

	for i in minhaLista:
		print i

	a = argInt + 10
	b = argStr + 'str'
	c = argDbl + 10.10

	d = argDbl + argByVar

def intRet():
	return 10

lista = [1, 2, 3]

imprimeLista(1, 'str', 1.0, 'argByFuncCall', 10.10)

if not 'hello' != 'hello':
	print "soma", 1 + 1 + 3 + 4

class classe():
	x = 10
	y = 10

	def num(self, x):
		return 10 + x

	def soma(self):
		return self.x + self.y + self.num(1)

instancia = classe()

a = instancia.num(100)
```

Would be compiled to the following C code:

```c
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <signal.h>
#include <math.h>
char * strcat_aux(char * a, char * b){
	int an = strlen(a);
	int bn = strlen(b);
	char * x = (char *)malloc(sizeof(char)*(an+bn+1));
	x[0]='\0';
	strcat(x,a);
	strcat(x,b);
	return x;
}

// Prototipos de funcoes
int intRet();
void imprimeLista(int argInt, char* argStr, double argDbl, char* argByFuncCall, double argByVar);

// Classes
// Classe "classe"
struct class_classe {
	// Atributos
	int y;
	int x;

	// Metodos
	int (*classe_soma) (struct class_classe* self);
	int (*classe_num) (struct class_classe* self, int x);
};

// Metodos da Classe classe
int classe_soma(struct class_classe* self){
	return self->x + self->y + self->classe_num(self, 1);
}

int classe_num(struct class_classe* self, int x){
	return 10 + x;
}

struct class_classe* classe(){
	struct class_classe* CLASS = (struct class_classe*) malloc(sizeof(struct class_classe));
	CLASS->x = 10;
	CLASS->y = 10;
	CLASS->classe_soma = classe_soma;
	CLASS->classe_num = classe_num;
	return CLASS;
}

int main(){
	int lista[] = {1, 2, 3};
	imprimeLista(1, "str", 1.0, "argByFuncCall", 10.1);
	if(!(strcmp("hello", "hello") != 0)){
		printf("%s %d\n", "soma", 1 + 1 + 3 + 4);
	}
	struct class_classe* instancia = classe();
	int a = instancia->classe_num(instancia, 100);

	return 0;
}

int intRet(){
	return 10;
}
void imprimeLista(int argInt, char* argStr, double argDbl, char* argByFuncCall, double argByVar){
	int minhaLista[] = {1, 2, 3};
	int _i;
	
	for(_i = 0; _i < 3; _i++){
		int i = minhaLista[_i];
		printf("%d\n", i);
	}
	int a = argInt + 10;
	char* b = malloc(sizeof(char) * 1000);
	strcpy(b, "");
	strcat_aux(b, strcat_aux(argStr, "str"));
	double c = argDbl + 10.1;
	double d = argDbl + argByVar;
}
```

### Development

This project was made for a compiler design course. Therefore, there is no reason to maintain this project other than for academic purposes.

### Keywords

Compiler, python, c, compilador, compiladores, state machine.

License
----

MIT
