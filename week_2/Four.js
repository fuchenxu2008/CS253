const fs = require('fs');
const path = require('path');
const readline = require('readline');

// isalnum helper
String.prototype.isalnum = function() {
  return !!this.match(/^[a-z0-9]+$/i);
}

// the global list of [word, frequency] pairs
const word_freqs = [];

// the list of stop words
const data = fs.readFileSync(path.resolve(__dirname, '../stop_words.txt'), 'utf-8');
const stop_words = data.split(',');
stop_words.push(...[...new Array(26)].map((_, i) => String.fromCharCode(97 + i)));

// iterate through the file one line at a time
const lr = readline.createInterface({
  input: fs.createReadStream(path.resolve(__dirname, `../${process.argv[2]}`))
});

lr.on('line', function (line) {
  // node will strip the '\n'
  line += '\n'
  let start_char = null;
  let i = 0;
  for (let c of line) {
    if (start_char === null) {
      if (c.isalnum()) {
        // We found the start of a word
        start_char = i;
      }
    } else {
      if (!c.isalnum()) {
        // We found the end of a word. Process it
        let found = false;
        const word = line.substring(start_char, i).toLowerCase();
        // Ignore stop words
        if (stop_words.indexOf(word) === -1) {
          let pair_index = 0;
          // Let's see if it already exists
          for (let pair of word_freqs) {
            if (word === pair[0]) {
              pair[1]++;
              found = true;
              break;
            }
            pair_index++;
          }
          if (!found) {
            word_freqs.push([word, 1]);
          } else if (word_freqs.length > 1) {
            // We may need to reorder
            for (let n = pair_index - 1; n >= 0; n--) {
              if (word_freqs[pair_index][1] > word_freqs[n][1]) {
                // swap
                [word_freqs[n], word_freqs[pair_index]] = [word_freqs[pair_index], word_freqs[n]];
                pair_index = n;
              }
            }
          }
        }
        // Let's reset
        start_char = null;
      }
    }
    i++;
  }
})
.on('close', function(line) {
  for (let tf of word_freqs.slice(0, 25)) {
    console.log(tf[0], '-', tf[1])
  }
});

