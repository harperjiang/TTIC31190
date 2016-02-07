from hw2.WordMatrix import WordMatrix
import random
import codecs

v = codecs.open('vocab-15k.txt', encoding='utf-8').readlines()
vc = codecs.open('vocab-10k.txt', encoding='utf-8').readlines()
inpt = codecs.open('wiki-0.1percent.txt', encoding='utf-8').readlines()

wm = WordMatrix(4)
wm.init(v, vc)
wm.train(inpt)


sampleWords = ['people', 'flew', 'transported', 'quickly', 'good', 'python', 'apple', 'red', 'chicago', 'language']
keylist = list(wm.vWords.keys())
'''
for w in sampleWords:
    print(w + ':' + str(wm.eval(w, 10)))
    
for i in range(6):
    word = random.choice(keylist)
    print(word + ':' + str(wm.eval(word, 10)))
'''
pmim = wm.pmi()

for w in sampleWords:
    print(w + ':' + str(pmim.eval(w, 10)))
    
for i in range(6):
    word = random.choice(keylist)
    print(word + ':' + str(pmim.eval(word, 10)))