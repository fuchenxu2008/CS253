const fs = require('fs');
const path = require('path');

const LeetMapping = {
  a: 'a',
  b: '8',
  c: '<',
  d: '|)',
  e: '3',
  f: '|=',
  g: '[',
  h: '#',
  i: '!',
  j: '_|',
  k: '|<',
  l: '|',
  m: '|/|',
  n: '||',
  o: '0',
  p: '|o',
  q: 'O_',
  r: '|2',
  s: '5',
  t: '7',
  u: '|_|',
  v: '/',
  w: '|/|',
  x: '%',
  y: '`/',
  z: '2',
};

/**
 * Array helper functions
 */
const toUpperCase = (w) => w.toLowerCase();

const filterChar = (w) => w.length >= 2;

const removeStopWords = (stopWords, w) => !stopWords.has(w);

const toLeet = (w) => [...w].map((c) => LeetMapping[c]).join('');

const count2gram = (freq, word, i, arr) => {
  if (i === 0) return freq;
  const bigram = `${arr[i - 1]} ${word}`;
  freq[bigram] = (freq[bigram] || 0) + 1;
  return freq;
};

const sortFreq = ([, a], [, b]) => b - a;

const printEntry = ([k, v]) => console.log(`${k} - ${v}`);

/**
 * Main function
 */
const words = fs
  .readFileSync(path.resolve(__dirname, `../${process.argv[2]}`), 'utf8')
  .replace(/[^a-zA-z]/g, ' ')
  .split(/\s+/);

const stopWords = new Set(
  fs
    .readFileSync(path.resolve(__dirname, '../stop_words.txt'), 'utf8')
    .split(',')
    .map(toUpperCase)
);

const counted2grams = words
  .map(toUpperCase)
  .filter(filterChar)
  .filter(removeStopWords.bind(null, stopWords))
  .map(toLeet)
  .reduce(count2gram, {});

Object.entries(counted2grams).sort(sortFreq).slice(0, 5).forEach(printEntry);
