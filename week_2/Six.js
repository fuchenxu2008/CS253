const fs = require('fs');
const path = require('path');

/**
 * The functions
 */
function read_file(path_to_file) {
  // Takes a path to a file and returns the entire contents of the file as a string
  return fs.readFileSync(path_to_file, 'utf8');
}

function filter_chars_and_normalize(str_data) {
  // Takes a string and returns a copy with all nonalphanumeric chars replaced by white space
  return str_data.replaceAll(/[\W_]+/ig, ' ').toLowerCase();
}

function scan(str_data) {
  // Takes a string and scans for words, returning a list of words.
  return str_data.split(/\s+/);
}

function remove_stop_words(word_list) {
  // Takes a list of words and returns a copy with all stop words removed 
  const stop_words = fs.readFileSync(path.resolve(__dirname, '../stop_words.txt'), 'utf-8').split(',');
  // add single-letter words
  stop_words.push(...[...new Array(26)].map((_, i) => String.fromCharCode(97 + i)));
  return word_list.filter(w => !stop_words.includes(w));
}

function frequencies(word_list) {
  // Takes a list of words and returns a dictionary associating words with frequencies of occurrence
  const word_freqs = {}
  for (let w of word_list) {
    if (word_freqs.hasOwnProperty(w))
      word_freqs[w] += 1
    else
      word_freqs[w] = 1
  }
  return word_freqs;
}

function sort(word_freq) {
  // Takes a dictionary of words and their frequencies and returns a list of pairs where the entries are sorted by frequency
  return Object.entries(word_freq).sort(([, a], [, b]) => b - a);
}

function print_all(word_freqs) {
  // Takes a list of pairs where the entries are sorted by frequency and print them recursively.
  if (word_freqs.length > 0) {
    console.log(word_freqs[0][0], '-', word_freqs[0][1])
    print_all(word_freqs.slice(1));
  }
}

/**
 * The main function
 */
print_all(sort(frequencies(remove_stop_words(scan(filter_chars_and_normalize(read_file(path.resolve(__dirname, `../${process.argv[2]}`))))))).slice(0, 25))
