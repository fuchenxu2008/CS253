const fs = require('fs');

const words = fs.readFileSync(`../${process.argv[2]}`, 'utf8').toLowerCase().match(/[a-z]{2,}/ig);
const stopWords = new Set(fs.readFileSync('../stop_words.txt', 'utf8').split(','));
const freqMap = words.filter(w => !stopWords.has(w)).reduce((acc, w) => { acc[w] = (acc[w] ?? 0) + 1; return acc; }, {});
Object.entries(freqMap).sort(([, a], [, b]) => b - a).slice(0, 25).forEach(([w, c]) => console.log(w, '-', c))