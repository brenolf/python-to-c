a = 1
b = 2
c = 3

nf1 = 1.99
nf2 = 2.87

print nf1 + nf2

class novaClasse():
    x = 10
    y = 10

    def func1(self):
        return 1 + 1

    def func2(self):
        return self.x + self.y

if a > 0:
    if b > 0:
        print "hello world"
    else:
        print "hello" ' ' 'world' '\"escape\"'
    
    c = 10

print 'this is', ' a ', ' long print ', 1 + 1 + 2.5, 2, '!', nf2, a, 'end' + " " + '!'

def square(xx, a):
    return xx * 10

if b < 1 and c != 4:
    if a > 2 or b == 2:
        if nf1 < 0:
            if nf2 != 9 and not "hello" == "world":
                print "hello"
        else:
            print "world"

a = 2 & 1 | 3
a = 10

def line(a, b = 1):
    a = 'hello'
    for i in range (2,3):
        for j in [1, 2, 3]:
            if i == j:
                print "soma: ", i + j

for i in [1, 2, 3]:
    if 3 == 3:
        if 2 == 2:
            if 1 == 1:
                a ^= i
    a = 5
    c = 1

    while a * c < 10:
        c += 1
        if c == 2:
            # a nice comment
            a *= 2

print "\"dedent\""

# another comment

z = 1
while z:
    z += 1
    z *= 2
    z = z + 1 + 2 + 3
    
    if z > 100:
        break
    

for yy in 'string':
    if yy == 'i':
        print 'o'
    else:
        print yy


print "That's all folks!"
