const fs = require('fs');
const path = require('path');

/**
 * The One class
 */
class TFTheOne {
  constructor(v) {
    this._value = v;
  }

  bind(func) {
    this._value = func(this._value);
    return this;
  }

  printme() {
    console.log(this._value);
  }
}

/**
 * The functions
 */
function read_file(path_to_file) {
  return fs.readFileSync(path_to_file, 'utf8');
}

function filter_chars(str_data) {
  return str_data.replace(/[\W_]+/gi, ' ');
}

function normalize(str_data) {
  return str_data.toLowerCase();
}

function scan(str_data) {
  return str_data.split(/\s+/);
}

function remove_stop_words(word_list) {
  // Takes a list of words and returns a copy with all stop words removed
  const stop_words = fs
    .readFileSync(path.resolve(__dirname, '../stop_words.txt'), 'utf-8')
    .split(',');
  // add single-letter words
  stop_words.push(
    ...[...new Array(26)].map((_, i) => String.fromCharCode(97 + i))
  );
  return word_list.filter((w) => !stop_words.includes(w));
}

function frequencies(word_list) {
  // Takes a list of words and returns a dictionary associating words with frequencies of occurrence
  const word_freqs = {};
  for (let w of word_list) {
    if (word_freqs.hasOwnProperty(w)) word_freqs[w] += 1;
    else word_freqs[w] = 1;
  }
  return word_freqs;
}

function sort(word_freq) {
  // Takes a dictionary of words and their frequencies and returns a list of pairs where the entries are sorted by frequency
  return Object.entries(word_freq).sort(([, a], [, b]) => b - a);
}

function top25_freqs(word_freqs) {
  let top25 = '';
  for (let tf of word_freqs.slice(0, 25))
    top25 += `${tf[0]} - ${tf[1]}\n`;
  return top25;
}

/**
 * The main function
 */
new TFTheOne(path.resolve(__dirname, `../${process.argv[2]}`))
  .bind(read_file)
  .bind(filter_chars)
  .bind(normalize)
  .bind(scan)
  .bind(remove_stop_words)
  .bind(frequencies)
  .bind(sort)
  .bind(top25_freqs)
  .printme();
