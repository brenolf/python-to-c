def imprimeLista(argInt, argStr, argDbl, argByFuncCall, argByVar):
	minhaLista = [1, 2, 3]

	for i in minhaLista:
		print i

	a = argInt + 10
	b = argStr + 'str'
	c = argDbl + 10.10

	d = argDbl + argByVar

def strRet():
	return 'hello world'

def doubleRet():
	return 10.10

def intRet():
	return 10

frac = 2.56 ** -1
pergunta = " + 1 = ?"
qst = ~ -2
qst = 1

lista = [1, 2, 3]

imprimeLista(1, 'str', 1.0, 'argByFuncCall', 10.10)

if not 'hello' != 'hello':
	print "soma", 1 + 1 + 3 + 4

print "pergunta ", qst, ") ", frac, pergunta

class classe():
	x = 10
	y = 10

	def num(self, x):
		return 10 + x

	def soma(self):
		return self.x + self.y + self.num(1)

instancia = classe()

a = instancia.num(100)
