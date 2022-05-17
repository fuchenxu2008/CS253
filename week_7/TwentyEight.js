const fs = require('fs');
const path = require('path');

// isalnum helper
String.prototype.isalnum = function () {
  return !!this.match(/^[a-z0-9]+$/i);
}

function* characters(filename) {
  const lines = fs.readFileSync(path.resolve(__dirname, `../${filename}`), 'utf8').split('\n');
  for (let line of lines) {
    line += '\n'
    for (let c of line)
      yield c
  }
}

function* all_words(filename) {
  let start_char = true
  let word = ''
  for (let c of characters(filename)) {
    if (start_char) {
      word = ''
      if (c.isalnum()) {
        // We found the start of a word
        word = c.toLowerCase()
        start_char = false
      }
    } else {
      if (c.isalnum())
        word = `${word}${c.toLowerCase()}`
      else {
        // We found end of word, emit it
        start_char = true
        if (word.length >= 2)
          yield word
      }
    }
  }
}

function* non_stop_words(filename) {
  const stopwords = new Set(fs.readFileSync(path.resolve(__dirname, '../stop_words.txt'), 'utf-8').split(','))
  for (let w of all_words(filename))
    if (!stopwords.has(w))
      yield w
}


function* count_and_sort(filename) {
  const freqs = {}
  for (let w of non_stop_words(filename))
    freqs[w] = (freqs[w] || 0) + 1
  yield Object.entries(freqs).sort(([, a], [, b]) => b - a)
}


/**
 * The main function
 */
for (let word_freqs of count_and_sort(process.argv[2])) {
  console.log("-----------------------------")
  for (let [w, c] of word_freqs.slice(0, 25))
    console.log(w, '-', c)
}

/**
 * 28.2
 */
function* lines(filename) {
  const lines = fs.readFileSync(path.resolve(__dirname, `../${filename}`), 'utf8').split('\n');
  for (let line of lines) {
    line += '\n'
    yield line
  }
}

function* all_words2(filename) {
  const pattern = /[a-zA-Z]{2,}/g
  const words = fs.readFileSync(filename, UTF8).match(pattern).map(word => word.toLowerCase())
  for (let word of words) {
      yield word
  }
}