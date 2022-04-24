const fs = require('fs');
const path = require('path');

// Auxiliary functions that can't be lambdas
function extract_words(obj, path_to_file) {
  obj['data'] = fs.readFileSync(path_to_file, 'utf8');
  const data_str = obj['data'].replace(/[\W_]+/gi, ' ').toLowerCase();
  obj['data'] = data_str.split(/\s+/);
}

function load_stop_words(obj) {
  obj['stop_words'] = fs
    .readFileSync(path.resolve(__dirname, '../stop_words.txt'), 'utf-8')
    .split(',');
  obj['stop_words'].push(
    ...[...new Array(26)].map((_, i) => String.fromCharCode(97 + i))
  );
}

function increment_count(obj, w) {
  obj['freqs'][w] = obj['freqs'].hasOwnProperty(w) ? obj['freqs'][w] + 1 : 1;
}

const data_storage_obj = {
  data: [],
  init: (path_to_file) => extract_words(data_storage_obj, path_to_file),
  words: () => data_storage_obj['data'],
};

const stop_words_obj = {
  stop_words: [],
  init: () => load_stop_words(stop_words_obj),
  is_stop_word: (word) => stop_words_obj['stop_words'].indexOf(word) !== -1,
};

const word_freqs_obj = {
  freqs: {},
  increment_count: (w) => increment_count(word_freqs_obj, w),
  sorted: () =>
    Object.entries(word_freqs_obj['freqs']).sort(([, a], [, b]) => b - a),
};

data_storage_obj['init'](path.resolve(__dirname, `../${process.argv[2]}`));
stop_words_obj['init']();

for (const w of data_storage_obj['words']())
  if (!stop_words_obj['is_stop_word'](w)) word_freqs_obj['increment_count'](w);

const word_freqs = word_freqs_obj['sorted']();
for (const [w, c] of word_freqs.slice(0, 25)) console.log(w, '-', c);
