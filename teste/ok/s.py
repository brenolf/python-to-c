class A:
	def sum(x, self):
		return 10

	def m(self):
		print ' 7 '
		if 1 > 0 :
			print ' 0 '
		if 1 >= 0 :
			print ' 1 '
		if 1 != 0 :
			print ' 2 '
		if 0 < 1 :
			print ' 3 '
		if 0 <= 1 :
			print ' 4 '
		if 0 == 0 :
			print ' 5 '
		if 0 >= 0 :
			print ' 6 '
		if 0 <= 0 :
			print ' 7 '
		if 1 == 0 :
			print ' 18 '
		if 0 > 1 :
			print ' 10 '
		if 0 >= 1 :
			print ' 11 '
		if 0 != 0 :
			print ' 12 '
		if 1 < 0 :
			print ' 13 '
		if 1 <= 0 :
			print ' 14 '
a = A
a.m()
a.sum(1)

print '';
print 'ok-Ger01'
print 'The output should be : '
print '7 0 1 2 3 4 5 6 7'
