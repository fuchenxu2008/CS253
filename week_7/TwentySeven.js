const fs = require('fs');
const path = require('path');
const readline = require('readline');

const rl = readline.createInterface({ input: process.stdin, output: process.stdout });
const prompt = (query) => new Promise((resolve) => rl.question(query, resolve));
rl.on('close', () => process.exit(0))

/**
 * The columns. Each column is a data element and a formula.
 * The first 2 columns are the input data, so no formulas.
 */
const all_words = [[], null];
const stop_words = [new Set(), null];
const non_stop_words = [
    [],
    () => all_words[0].map((w) => (stop_words[0].has(w) ? '' : w)),
];
const unique_words = [
    [],
    () => Array.from(new Set(non_stop_words[0].filter((w) => w !== ''))),
];
const counts = [
    [],
    () => Object.entries(non_stop_words[0].reduce(
        (freq, w) => {
            if (!freq.hasOwnProperty(w)) return freq;
            freq[w]++;
            return freq;
        },
        unique_words[0].reduce((freq, w) => {
            freq[w] = 0
            return freq;
        }, {})
    )),
];
const sorted_data = [
    [],
    () => counts[0].sort(([, a], [, b]) => b - a)
];

// The entire spreadsheet
const all_columns = [
    all_words,
    stop_words,
    non_stop_words,
    unique_words,
    counts,
    sorted_data,
];

/**
 * The active procedure over the columns of data.
 * Call this everytime the input data changes, or periodically.
 */
function update() {
    // Apply the formula in each column
    for (let c of all_columns) {
        if (c[1] !== null) c[0] = c[1]();
    }
}

// Load the fixed data into the first 2 columns
all_words[0] = fs
    .readFileSync(path.resolve(__dirname, `../${process.argv[2]}`), 'utf8')
    .toLowerCase()
    .match(/[a-z]{2,}/g);
stop_words[0] = new Set(
    fs
        .readFileSync(path.resolve(__dirname, '../stop_words.txt'), 'utf8')
        .split(',')
);

// Update the columns with formulas
update();

for (let [w, c] of sorted_data[0].slice(0, 25)) console.log(w, '-', c);

(async () => {
    try {
        while (true) {
            const filename = await prompt('\nInput filename(Ctrl+C to abort): ')
            all_words[0].push(...fs
                .readFileSync(path.resolve(__dirname, `../${filename}`), 'utf8')
                .toLowerCase()
                .match(/[a-z]{2,}/g))
            update();
            for (let [w, c] of sorted_data[0].slice(0, 25)) console.log(w, '-', c);
        }
    } catch (e) {
        console.errror("unable to prompt", e)
    }
})()