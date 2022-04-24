const fs = require('fs');
const path = require('path');

class DataStorageManager {
  // Models the contents of the file
  _data = '';

  dispatch(message) {
    if (message[0] === 'init') return this._init(message[1]);
    else if (message[0] === 'words') return this._words();
    else throw new Error('Message not understood ', message[0]);
  }

  _init(path_to_file) {
    const data = fs.readFileSync(path_to_file, 'utf8');
    this._data = data.replace(/[\W_]+/gi, ' ').toLowerCase();
  }

  _words() {
    // Returns the list words in storage
    return this._data.split(/\s+/);
  }
}

class StopWordManager {
  // Models the stop word filter
  _stop_words = [];

  dispatch(message) {
    if (message[0] === 'init') return this._init();
    else if (message[0] === 'is_stop_word')
      return this._is_stop_word(message[1]);
    else throw new Error('Message not understood ', message[0]);
  }

  _init() {
    this._stop_words = fs
      .readFileSync(path.resolve(__dirname, '../stop_words.txt'), 'utf-8')
      .split(',');
    this._stop_words.push(
      ...[...new Array(26)].map((_, i) => String.fromCharCode(97 + i))
    );
  }

  _is_stop_word(word) {
    return this._stop_words.indexOf(word) !== -1;
  }
}

class WordFrequencyManager {
  // Keeps the word frequency data
  _word_freqs = {};

  dispatch(message) {
    if (message[0] === 'increment_count')
      return this._increment_count(message[1]);
    else if (message[0] === 'sorted') return this._sorted();
    else throw new Error('Message not understood ', message[0]);
  }

  _increment_count(word) {
    if (this._word_freqs.hasOwnProperty(word)) this._word_freqs[word] += 1;
    else this._word_freqs[word] = 1;
  }

  _sorted() {
    return Object.entries(this._word_freqs).sort(([, a], [, b]) => b - a);
  }
}

class WordFrequencyController {
  dispatch(message) {
    if (message[0] === 'init') return this._init(message[1]);
    else if (message[0] === 'run') return this._run();
    else throw new Error('Message not understood ', message[0]);
  }

  _init(path_to_file) {
    this._storage_manager = new DataStorageManager();
    this._stop_word_manager = new StopWordManager();
    this._word_freq_manager = new WordFrequencyManager();
    this._storage_manager.dispatch(['init', path_to_file]);
    this._stop_word_manager.dispatch(['init']);
  }

  _run() {
    for (const w of this._storage_manager.dispatch(['words']))
      if (!this._stop_word_manager.dispatch(['is_stop_word', w]))
        this._word_freq_manager.dispatch(['increment_count', w]);

    const word_freqs = this._word_freq_manager.dispatch(['sorted']);
    for (let [w, c] of word_freqs.slice(0, 25)) console.log(w, '-', c);
  }
}

/**
 * The main function
 */
const wfcontroller = new WordFrequencyController();
wfcontroller.dispatch([
  'init',
  path.resolve(__dirname, `../${process.argv[2]}`),
]);
wfcontroller.dispatch(['run']);
