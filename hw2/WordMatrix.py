from math import sqrt, log
import heapq

class WordMatrix:
    
    windowSize = 4
    vWords = {}
    vcIndex = {}
    
    startSym = '<s>'
    stopSym = '</s>'
    
    
    def __init__(self, ws):
        self.windowSize = ws
        return

    '''
    Init the WordMatrix with v and vc
    '''
    def init(self, v, vc):
        for word in v:
            word = word.strip()
            self.vWords[word] = {}
        counter = 0
        for word in vc:
            word = word.strip()
            self.vcIndex[word] = counter
            counter += 1
        pass
    
    '''
    Train the WordMatrix with input data
    '''
    def train(self, inpt_file):
        inpt_file.seek(0)
        for line in inpt_file.readlines():
            tokens = line.split()
            for i in range(len(tokens)):
                token = tokens[i]
                if i < self.windowSize:
                    self._inc(token, self.startSym)
                for d in range(i):
                    self._inc(token, tokens[d])
                
                if i >= len(tokens) - self.windowSize:
                    self._inc(token, self.stopSym)
                for d in range(i + 1, len(tokens)):
                    self._inc(token, tokens[d])
        return
    
    '''
    Calculate PMI and return a new WordMatrix
    '''
    def pmi(self):
        pmim = WordMatrix(0)
        denom = 0
        for l in self.vWords.values():
            for c in l.values():
                denom += c
        
        vBuffer = {}
        vcBuffer = {}
        
        for vWord in self.vWords.keys():
            for vcWord in self.vWords[vWord].keys():
                vValue = vBuffer.get(vWord)
                if vValue == None:
                    # For all the vc words
                    vValue = sum(self.vWords[vWord].values())
                    vBuffer[vWord] = vValue
                
                vcValue = vcBuffer.get(vcWord)
                if vcValue == None:
                    vcValue = 0
                    for iv in self.vWords.keys():
                        vcItemVal = self.vWords[iv].get(vcWord)
                        if vcItemVal != None:
                            vcValue += vcItemVal
                    vcBuffer[vcWord] = vcValue
                
                pmival = 0
                
                if vValue == 0 or vcValue == 0:
                    pmival = 0
                elif self._get(vWord,vcWord) == 0:
                    pmival = 0
                else:
                    pmival = log(float(denom) * self._get(vWord, vcWord) / (vValue * vcValue))
                if pmival < 0:
                    pmival = 0
                
                pmim._set(vWord, vcWord, pmival)
        return pmim
    
    
    '''
    Find the nearest neighbor of given word
    '''
    def eval(self, refWord, topn):
        heap = []
        for word in self.vWords:
            if refWord == word:
                continue
            item = (self.similarity(refWord, word), word)
            if item[0] == None:
                continue
            if len(heap) >= topn:
                heapq.heappushpop(heap, item)
            else:
                heapq.heappush(heap, item)
        return heap
    
    '''
    Calculate the similarity of two given words
    '''
    def similarity(self, worda, wordb):
        if self.vWords.get(worda) == None or self.vWords.get(wordb) == None:
            return None
        va = self.vWords[worda]
        vb = self.vWords[wordb]
        if len(va) == 0 or len(vb) == 0:
            return None
        words = set(va.keys()).union(vb)
        dotprod = 0
        vasum = 0
        vbsum = 0
        for word in words:
            vaval = va.get(word)
            if vaval == None:
                vaval = 0
            vbval = vb.get(word)
            if vbval == None:
                vbval = 0
            dotprod += vaval * vbval
            vasum += vaval * vaval
            vbsum += vbval * vbval
        if vasum == 0 or vbsum == 0:
            return None
        return (dotprod + 0.0) / (sqrt(vasum) * sqrt(vbsum))
    '''
    Private method for counting word count
    '''
    def _inc(self, vWord, vcWord):
        if self.vWords.get(vWord) != None:
            cnt = self.vWords[vWord].get(vcWord)
            if None == cnt:
                cnt = 0
            self.vWords[vWord][vcWord] = cnt + 1

    def _set(self, vWord, vcWord, val):
        if self.vWords.get(vWord) == None:
            self.vWords[vWord] = {}    
        self.vWords[vWord][vcWord] = val
        
    def _get(self, vWord, vcWord):
        if self.vWords.get(vWord) == None:
            return 0
        if self.vWords[vWord].get(vcWord) == None:
            return 0
        return self.vWords[vWord][vcWord]
