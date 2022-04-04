const fs = require('fs');
const path = require('path');

(async function main() {
  try {
    const [stopWords, fileContent] = await Promise.all([
      loadStopWords(),
      readArticle(),
    ]);
    const tokens = tokenize(fileContent, stopWords);
    const tokenMap = analyze(tokens);
    const topFreqWords = listFreqWords(tokenMap, 25);
    await outputResult(topFreqWords);
  } catch (error) {
    console.log('error: ', error);
  }
})();

/**
 * General read file with utf8 encoding
 *
 * @param {*} filename string
 * @returns Promise<String>
 */
function readFile(filename) {
  return new Promise((resolve, reject) => {
    fs.readFile(filename, 'utf8', (err, data) => {
      if (err) {
        return reject(err);
      }
      resolve(data);
    });
  });
}

/**
 * General write file
 *
 * @param {*} filename String
 * @param {*} content String
 * @returns Promise<void>
 */
async function writeFile(filename, content) {
  return new Promise((resolve, reject) => {
    fs.writeFile(filename, content, (err) => {
      if (err) {
        return reject(err);
      }
      resolve();
    });
  });
}

/**
 * Read stop_words.txt into set
 *
 * @returns Set<String>
 */
async function loadStopWords() {
  const data = await readFile(path.resolve(__dirname, '../stop_words.txt'));
  return new Set(data.split(','));
}

/**
 * Read target txt file
 *
 * @returns Promise<String>
 */
async function readArticle() {
  const filename = process.argv[2];
  if (!filename) {
    throw new Error('No filename provided!');
  }
  return readFile(path.resolve(__dirname, `../${filename}`));
}

/**
 * Tokenize text into lowercase tokens
 *
 * @param {*} text String
 * @returns Array<String>
 */
function tokenize(text, stopWords) {
  const pattern = new RegExp('[a-zA-Z]{2,}', 'g');
  const token = text.match(pattern);
  return token
    .map((word) => word.toLowerCase())
    .filter((token) => !stopWords.has(token));
}

/**
 * Counting tokens
 *
 * @param {*} tokens Array<String>
 * @returns Object { String: Number }
 */
function analyze(tokens) {
  return tokens.reduce((tokenMap, token) => {
    tokenMap[token] = (tokenMap[token] ?? 0) + 1;
    return tokenMap;
  }, {});
}

/**
 * Sort tokens by count descending, get top K
 *
 * @param {*} tokenMap Object { String: Number }
 * @param {*} topK Number
 * @returns Array<Tuple<String, Number>>
 */
function listFreqWords(tokenMap, topK) {
  return Object.entries(tokenMap)
    .sort(([, cnt1], [, cnt2]) => cnt2 - cnt1)
    .slice(0, topK);
}

/**
 * Write result into file
 *
 * @param {*} topFreqWords Array<Tuple<String, Number>>
 * @returns Promise<void>
 */
async function outputResult(topFreqWords) {
  const filename = 'output.txt';
  const content = topFreqWords.reduce((res, [word, freq]) => {
    return `${res}${word}  -  ${freq}\n`;
  }, '');
  console.log(content);
  return writeFile(path.resolve(__dirname, filename), content);
}
