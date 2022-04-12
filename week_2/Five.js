const fs = require('fs');
const path = require('path');

// The shared mutable data
let data = []
let words = []
let word_freqs = []

// isalnum helper
String.prototype.isalnum = function() {
  return !!this.match(/^[a-z0-9]+$/i);
}

/**
 * The procedures
 */
function read_file(path_to_file) {
  // Takes a path to a file and assigns the entire contents of the file to the global variable data
  data = data.concat(Array.from(fs.readFileSync(path_to_file, 'utf8')));
}

function filter_chars_and_normalize() {
  // Replaces all nonalphanumeric chars in data with white space global data
  const isalnum = (c) => !!c.match(/^[a-z0-9]+$/i);
  data = data.map((c, i) => {
    return !isalnum(c) ? ' ' : c.toLowerCase();
  })
}

function scan() {
    // Scans data for words, filling the global variable words
    const data_str = data.join('')
    words = words.concat(data_str.split(/\s+/))
}

function remove_stop_words() {
  const stop_words = fs.readFileSync(path.resolve(__dirname, '../stop_words.txt'), 'utf-8').split(',');
  // add single-letter words
  stop_words.push(...[...new Array(26)].map((_, i) => String.fromCharCode(97 + i)));
  const indexes = [];
  words.forEach((_, i) => {
    if (stop_words.includes(words[i]))
      indexes.push(i);
  });
  indexes.reverse().forEach(i => words.splice(i, 1));
}

function frequencies() {
  // Creates a list of pairs associating words with frequencies
  for (let w of words) {
    const keys = word_freqs.map(wd => wd[0]);
    const index = keys.indexOf(w);
    if (index > -1)
      word_freqs[index][1] += 1;
    else
      word_freqs.push([w, 1]);
  }
}

function sort() {
  // Sorts word_freqs by frequency
  word_freqs.sort(([, a], [, b]) => b - a);
}


/**
 * The main function
 */
read_file(path.resolve(__dirname, `../${process.argv[2]}`))
filter_chars_and_normalize()
scan()
remove_stop_words()
frequencies()
sort()

for (let tf of word_freqs.slice(0, 25))
  console.log(tf[0], '-', tf[1]);
