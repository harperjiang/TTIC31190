'''
Created on Jan 20, 2016

@author: harper
'''
import codecs

'''
Calculate the score of a given data
'''
def score(model, words, label):
    score = 0
    for word in words:
        score += model['params'][word][label]
    return score

'''
Calculate the perceptron loss of a word/label based on the model
'''
def perc_loss(model, data):
    words = data[0]
    classified = classify(model, words)
    
    return (classified[1] - score(model, data[0], data[1]), classified[0])

'''
Calculate the hinge loss 
'''
hinge_delta = 10

def hinge_loss(model, data):
    words = data[0]
    label = data[1]
    
    maxscore = float('-inf')
    maxlabel = -1
    
    for labelc in range(model['labelcount']):
        labelstr = str(labelc) 
        lbscore = score(model, words, labelstr)
        if labelstr != label:
            lbscore += hinge_delta
        if lbscore > maxscore:
            maxscore = lbscore
            maxlabel = labelstr
            
    return (maxscore - score(model, words, label), maxlabel)

'''
Calculate the total loss of the entire data set
'''    
loss = hinge_loss

def losssum(model, datas):
    lsum = 0
    for data in datas:
        lsum += loss(model, data)[0]
    return lsum


'''
Load data from a given path, returning a set of tuples (word, label)
'''
def load_data(path):
    datas = []
    f = codecs.open(path, encoding='utf-8')
    for line in f:
        parts = line.strip().lower().split('\t')
            
        words = parts[0].split()
        label = parts[1]
        
        datas.append((words, label))
            
    return datas
'''
Initialize the model
'''
def init(datas, labelcount):
    model = {'labelcount':labelcount, 'params':{}}
    for data in datas:
        words = data[0]
        for word in words:
            for label in range(labelcount):
                if model['params'].get(word) == None:
                    model['params'][word] = {}
                model['params'][word][str(label)] = 0
    return model

'''
Update the model based on the data given
'''
def update(model, data):
    words = data[0]
    label = data[1]
    
    step = 0.005
    
    # subgradient for loss
    loss_result = loss(model, data)
    subgradient = 0 if label == loss_result[1] else 1
    
    if subgradient != 0:
        for word in words:
            theta_j = model['params'][word][label]
            # theta_j^{(t+1)} = theta_j^{t} - subg * step
            newtheta_j = theta_j + subgradient * step
            model['params'][word][label] = newtheta_j
    return

'''
Classify a given word
'''
def classify(model, words):
    maxvalue = -float('inf')
    maxlabel = -1
    
    # Available labels
    labelcount = model['labelcount']
    labels = list(map(str, list(range(labelcount))))
    
    # Find the max
    for label in labels:
        sumval = 0
        for word in words:
            if model['params'].get(word) != None:
                # Ignore unknown word
                sumval += model['params'][word][label]
        if sumval > maxvalue:
            maxvalue = sumval
            maxlabel = label
        elif sumval == maxvalue and label < maxlabel:
            maxlabel = label
    return (maxlabel, maxvalue)

'''
Calculate the accuracy of a model on a given data set
'''
def accuracy(model, datas):
    total = len(datas)
    correct = 0
    for data in datas:
        words = data[0]
        groundtruth = data[1]
        predict = classify(model, words)
        predictlabel = predict[0]
        if predictlabel == groundtruth:
            correct += 1
    return correct / total

'''
Calculate the statistical information of the model
This currently includes prediction accuracy and total loss
'''
def stat(epoch, model, train, dev, devtest):
    lossval = losssum(model, train)
    accu = accuracy(model, train)
    devaccu = accuracy(model, dev)
    devtaccu = accuracy(model, devtest)
    print('{}\t{}\t{}\t{}\t{}'.format(epoch, lossval, accu, devaccu, devtaccu))
    return
'''
Analyze the model. Here we list the first 100 words for each label
'''
def analyze(model):
    labels = {}
    for word in model['params'].keys():
        for label in model['params'][word].keys():
            if labels.get(label) == None:
                labels[label] = []
            labels[label].append((model['params'][word][label], word))
    for item in labels.items():
        # Label
        print(item[0])
        
        item[1].sort(reverse=True)
        for i in range(100):
            print(item[1][i])
    return

'''
Train a model
'''
def train(dataset_name, labelcount):    
    datas = load_data('data/{0}/{0}.train'.format(dataset_name))
    devdatas = load_data('data/{0}/{0}.dev'.format(dataset_name))
    devtestdatas = load_data('data/{0}/{0}.devtest'.format(dataset_name))
    
    model = init(datas, labelcount)
    
    max_epoch = 50
    
    for i in range(max_epoch):
        # Simply iterate the entire dataset
        for data in datas:  
            update(model, data)
        # regularize(model)
        stat(i, model, datas, devdatas, devtestdatas)
    return model

'''
The main function
'''
if __name__ == '__main__':
    model = train('CR', 2)
    #model = train('trec', 6)
    analyze(model)
