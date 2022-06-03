from keras.models import Sequential
from keras.layers import Dense
import numpy as np
import sys, os, string, random
import operator

characters = string.printable
char_indices = dict((c, i) for i, c in enumerate(characters))
indices_char = dict((i, c) for i, c in enumerate(characters))

INPUT_VOCAB_SIZE = len(characters)
BATCH_SIZE = 200

def encode_one_hot(line):
    x = np.zeros((len(line), INPUT_VOCAB_SIZE))
    for i, c in enumerate(line):
        if c in characters:
            index = char_indices[c]
        else:
            index = char_indices[' ']
        x[i][index] = 1 
    return x

def decode_one_hot(x):
    s = []
    for onehot in x:
        one_index = np.argmax(onehot) 
        s.append(indices_char[one_index]) 
    return ''.join(s)
    
def build_model():
    # Normalize characters using a dense layer
    model = Sequential()
    dense_layer = Dense(INPUT_VOCAB_SIZE, 
                        input_shape=(INPUT_VOCAB_SIZE,),
                        activation='softmax')
    model.add(dense_layer)
    return model

def input_generator(nsamples):
    def generate_line():
        inline = []; outline = []
        for _ in range(nsamples):
            c = random.choice(characters) 
            expected = c.lower() if c in string.ascii_letters else ' ' 
            inline.append(c); outline.append(expected)
        return ''.join(inline), ''.join(outline)

    while True:
        input_data, expected = generate_line()
        data_in = encode_one_hot(input_data)
        data_out = encode_one_hot(expected)
        yield data_in, data_out

def train(model):
    model.compile(loss='categorical_crossentropy',
                  optimizer='adam',
                  metrics=['accuracy'])
    input_gen = input_generator(BATCH_SIZE)
    validation_gen = input_generator(BATCH_SIZE)
    model.fit_generator(input_gen,
                epochs = 50, workers=1,
                steps_per_epoch = 20,
                validation_data = validation_gen,
                validation_steps = 10)

model = build_model()
model.summary()
train(model)

all_words = []

input("Network has been trained. Press <Enter> to run program.")
with open(sys.argv[1]) as f:
    for line in f:
        if line.isspace(): continue
        batch = encode_one_hot(line)
        preds = model.predict(batch)
        normal = decode_one_hot(preds)
        all_words.extend(normal.split())

LeetMapping = {
    'a': 'a',
    'b': '8',
    'c': '<',
    'd': '|>',
    'e': '3',
    'f': '|=',
    'g': 'C-',
    'h': '4',
    'i': '1',
    'j': '7',
    'k': '|<',
    'l': '1',
    'm': '44',
    'n': '||',
    'o': '0',
    'p': '|>',
    'q': '9',
    'r': '12',
    's': '5',
    't': '7',
    'u': '|_|',
    'v': '/',
    'w': '//',
    'x': '>K',
    'y': '`/',
    'z': '2',
}

stopwords = set(open('stop_words.txt').read().split(','))

def transform_leet(w):
    return ''.join(map(lambda c: LeetMapping[c], list(w)))

words = [transform_leet(w) for w in all_words if w not in stopwords and len(w) >= 2]

freq = {}

for w in words:
    if w not in freq:
        freq[w] = 1
    else:
        freq[w] += 1

for w, c in sorted(freq.items(), key=operator.itemgetter(1), reverse=True)[:25]:
    print(w + " - " + str(c))