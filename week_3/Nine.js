const fs = require('fs');
const path = require('path');

/**
 * The functions
 */
function read_file(path_to_file, func) {
  const data = fs.readFileSync(path_to_file, 'utf8');
  func(data, normalize);
}

function filter_chars(str_data, func) {
  const pattern = new RegExp(/[\W_]+/ig);
  func(str_data.replace(pattern, ' '), scan);
}

function normalize(str_data, func) {
  func(str_data.toLowerCase(), remove_stop_words);
}

function scan(str_data, func) {
  func(str_data.split(/\s+/), frequencies);
}

function remove_stop_words(word_list, func) {
  const stop_words = fs
    .readFileSync(path.resolve(__dirname, '../stop_words.txt'), 'utf-8')
    .split(',');
  // add single-letter words
  stop_words.push(
    ...[...new Array(26)].map((_, i) => String.fromCharCode(97 + i))
  );
  func(
    word_list.filter((w) => !stop_words.includes(w)),
    sort
  );
}

function frequencies(word_list, func) {
  const wf = {};
  for (let w of word_list) {
    if (wf.hasOwnProperty(w)) wf[w] += 1;
    else wf[w] = 1;
  }
  func(wf, print_text);
}

function sort(wf, func) {
  func(
    Object.entries(wf).sort(([, a], [, b]) => b - a),
    no_op
  );
}

function print_text(word_freqs, func) {
  for (let [w, c] of word_freqs.slice(0, 25)) console.log(w, '-', c);
  func(null);
}

function no_op(func) {
  return;
}

/**
 * The main function
 */
read_file(path.resolve(__dirname, `../${process.argv[2]}`), filter_chars);
