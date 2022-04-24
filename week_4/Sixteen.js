const fs = require('fs');
const path = require('path');

/**
 * The event management substrate
 */
class EventManager {
  constructor() {
    this._subscriptions = {};
  }

  subscribe(event_type, handler) {
    if (this._subscriptions.hasOwnProperty(event_type))
      this._subscriptions[event_type].push(handler);
    else this._subscriptions[event_type] = [handler];
  }

  publish(event) {
    const event_type = event[0];
    if (this._subscriptions.hasOwnProperty(event_type))
      for (const h of this._subscriptions[event_type]) h(event);
  }
}
/**
 * The application entities
 */
class DataStorage {
  // Models the contents of the file
  constructor(event_manager) {
    this._event_manager = event_manager;
    this._event_manager.subscribe('load', this.load.bind(this));
    this._event_manager.subscribe('start', this.produce_words.bind(this));
  }

  load(event) {
    const path_to_file = event[1];
    const str_data = fs.readFileSync(path_to_file, 'utf8');
    this._data = str_data.replace(/[\W_]+/gi, ' ').toLowerCase();
  }

  produce_words(event) {
    for (const w of this._data.split(/\s+/))
      this._event_manager.publish(['word', w]);
    this._event_manager.publish(['eof', null]);
  }
}

class StopWordFilter {
  // Models the stop word filter
  constructor(event_manager) {
    this._stop_words = [];
    this._event_manager = event_manager;
    this._event_manager.subscribe('load', this.load.bind(this));
    this._event_manager.subscribe('word', this.is_stop_word.bind(this));
  }

  load(event) {
    this._stop_words = fs
      .readFileSync(path.resolve(__dirname, '../stop_words.txt'), 'utf-8')
      .split(',');
    this._stop_words.push(
      ...[...new Array(26)].map((_, i) => String.fromCharCode(97 + i))
    );
  }

  is_stop_word(event) {
    const word = event[1];
    if (this._stop_words.indexOf(word) === -1)
      this._event_manager.publish(['valid_word', word]);
  }
}

class WordFrequencyCounter {
  // Keeps the word frequency data
  constructor(event_manager) {
    this._word_freqs = {};
    this._event_manager = event_manager;
    this._event_manager.subscribe(
      'valid_word',
      this.increment_count.bind(this)
    );
    this._event_manager.subscribe('print', this.print_freqs.bind(this));
  }

  increment_count(event) {
    const word = event[1];
    if (this._word_freqs.hasOwnProperty(word)) this._word_freqs[word] += 1;
    else this._word_freqs[word] = 1;
  }

  print_freqs(event) {
    const word_freqs = Object.entries(this._word_freqs).sort(
      ([, a], [, b]) => b - a
    );
    for (const [w, c] of word_freqs.slice(0, 25)) console.log(w, '-', c);
  }
}

class WordFrequencyApplication {
  constructor(event_manager) {
    this._event_manager = event_manager;
    this._event_manager.subscribe('run', this.run.bind(this));
    this._event_manager.subscribe('eof', this.stop.bind(this));
  }

  run(event) {
    const path_to_file = event[1];
    this._event_manager.publish(['load', path_to_file]);
    this._event_manager.publish(['start', null]);
  }
  stop(event) {
    this._event_manager.publish(['print', null]);
  }
}

/**
 * The main function
 */
const em = new EventManager();
new DataStorage(em);
new StopWordFilter(em);
new WordFrequencyCounter(em);
new WordFrequencyApplication(em);
em.publish(['run', path.resolve(__dirname, `../${process.argv[2]}`)]);
