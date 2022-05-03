import java.io.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;

/** Models the contents of the file. */
class DataStorageManager {
  private List<String> words;

  public DataStorageManager(String pathToFile) throws IOException {
    this.words = new ArrayList<String>();

    String content = new String(Files.readAllBytes(Paths.get(pathToFile)));
    for (String word : content.split("[\\W_]+"))
      this.words.add(word.toLowerCase());
  }

  public List<String> getWords() {
    return this.words;
  }
}

/** Models the stop word filter. */
class StopWordManager {
  private Set<String> stopWords;

  public StopWordManager() throws IOException {
    this.stopWords = new HashSet<String>();

    String content = new String(Files.readAllBytes(Paths.get("../stop_words.txt")));
    for (String stopWord : content.split(","))
      this.stopWords.add(stopWord);

    // Add single-letter words
    for (char c = 'a'; c <= 'z'; c++) {
      this.stopWords.add("" + c);
    }
  }

  public boolean isStopWord(String word) {
    return this.stopWords.contains(word);
  }
}

/** Keeps the word frequency data. */
class WordFrequencyManager {
  private Map<String, Integer> wordFreqs;

  public WordFrequencyManager() {
    this.wordFreqs = new HashMap<String, Integer>();
  }

  public void incrementCount(String word) {
    Integer count = this.wordFreqs.get(word);
    if (count == null) {
      this.wordFreqs.put(word, 1);
    } else {
      this.wordFreqs.put(word, count + 1);
    }
  }

  public List<Pair> sorted() {
    List<Pair> pairs = new ArrayList<Pair>();
    for (Map.Entry<String, Integer> entry : wordFreqs.entrySet()) {
      pairs.add(new Pair(entry.getKey(), entry.getValue()));
    }
    Collections.sort(pairs, (o1, o2) -> o2.value - o1.value);
    return pairs;
  }
}

class Pair {
  String key;
  Integer value;

  public Pair(String key, Integer val) {
    this.key = key;
    this.value = val;
  }
}

class WordFrequencyController {
  private DataStorageManager storageManager;
  private StopWordManager stopWordManager;
  private WordFrequencyManager wordFreqManager;

  public WordFrequencyController(String pathToFile) throws IOException {
    this.storageManager = new DataStorageManager(pathToFile);
    this.stopWordManager = new StopWordManager();
    this.wordFreqManager = new WordFrequencyManager();
  }

  @SuppressWarnings("unchecked")
  public void run() {
    try {
      Method getWordsFromStorage = DataStorageManager.class.getMethod("getWords");
      Method isWordStopWord = StopWordManager.class.getMethod("isStopWord", String.class);
      Method incrementWordFreq = WordFrequencyManager.class.getMethod("incrementCount", String.class);
      Method sortedWordFreq = WordFrequencyManager.class.getMethod("sorted");

      for (String word : (List<String>) getWordsFromStorage.invoke(this.storageManager)) {
        if (!(Boolean) isWordStopWord.invoke(this.stopWordManager, word)) {
          incrementWordFreq.invoke(this.wordFreqManager, word);
        }
      }
      for (Pair pair : ((List<Pair>) sortedWordFreq.invoke(this.wordFreqManager)).subList(0, 25)) {
        System.out.println(pair.key + " - " + pair.value);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

public class Seventeen {
  static Scanner scanner = new Scanner(System.in);

  public static void main(String[] args) throws IOException {
    new WordFrequencyController(args[0]).run();

    System.out.println("\nEnter a class name: ");
    String className = scanner.nextLine();
    System.out.println();

    try {
      Class<?> c = Class.forName(className);
      System.out.println(c.getName());

      System.out.println("\n- Fields:");
      for (Field f : c.getDeclaredFields())
        System.out.printf("%s - %s%n", f.getName(), f.getGenericType());

      System.out.println("\n- Methods:");
      for (Method m : c.getDeclaredMethods())
        System.out.printf("%s -> %s%n", m.getName(), m.getGenericReturnType());

      System.out.printf("\n- Superclasses: %s%n", c.getGenericSuperclass().getTypeName());

      System.out.println("\n- Implemented Interfaces:");
      for (Type t : c.getGenericInterfaces())
        System.out.println(t.getTypeName());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}