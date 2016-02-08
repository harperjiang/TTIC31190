from hw2.WordMatrix import WordMatrix
import random
import codecs

def format_similar(lst):
    return ' '.join(k[1] for k in lst)

sampleWords = ['people', 'flew', 'transported', 'quickly', 'good', 'python', 'apple', 'red', 'chicago', 'language']

def eval_wordmatrix(name,matrix):
    print(name)
    for w in sampleWords:
        print(w + ':' + format_similar(matrix.eval(w, 10)))
        
    keylist = list(matrix.vWords.keys())
    for i in range(6):
        word = random.choice(keylist)
        print(word + ':' + format_similar(matrix.eval(word, 10)))
    return

v = list(codecs.open('vocab-15k.txt', encoding='utf-8').readlines())
vc10k = list(codecs.open('vocab-10k.txt', encoding='utf-8').readlines())
vc3k = list(codecs.open('vocab-3k.txt', encoding='utf-8').readlines())
vcr3k = list(codecs.open('vocab-rare3k.txt', encoding='utf-8').readlines())

inpt = codecs.open('wiki-0.1percent.txt', encoding='utf-8')

wm_10k = WordMatrix(4)
wm_10k.init(v, vc10k)
wm_10k.train(inpt)

eval_wordmatrix('Result for word count 10k', wm_10k)
pmim_10k = wm_10k.pmi()
eval_wordmatrix('Result for PPMI 10k', pmim_10k)

wm_3k = WordMatrix(4) 
wm_3k.init(v, vc3k)
wm_3k.train(inpt)
pmim_3k = wm_3k.pmi()
eval_wordmatrix('Result for PPMI 3k', pmim_3k)

wm_r3k = WordMatrix(4) 
wm_r3k.init(v, vcr3k)
wm_r3k.train(inpt)
pmim_r3k = wm_r3k.pmi()
eval_wordmatrix('Result for PPMI rare 3k', pmim_r3k)

wm_w1 = WordMatrix(1) 
wm_w1.init(v, vc10k)
wm_w1.train(inpt)
pmim_w1 = wm_w1.pmi()
eval_wordmatrix('Result for PPMI 10k, window 1', pmim_w1)

wm_w10 = WordMatrix(10) 
wm_w10.init(v, vc10k)
wm_w10.train(inpt)
pmim_w10 = wm_w10.pmi()
eval_wordmatrix('Result for PPMI 10k, window 10', pmim_w10)


