import java.io.*;
import java.nio.file.*;
import java.util.*;

class Pair {
  String word;
  Integer count;

  public Pair(String word, Integer count) {
    this.word = word;
    this.count = count;
  }
}

public class ThirtyTwo {
  /**
   * Concurrent worker thread for aggregating
   */
  static class ReduceWorker extends Thread {
    Map<String, Integer> freq = new HashMap<String, Integer>();
    List<Pair> list = new ArrayList<>();

    public void assign(Pair p) {
      list.add(p);
    }

    @Override
    public void run() {
      for (Pair p : list)
        freq.put(p.word, freq.getOrDefault(p.word, 0) + p.count);
    }
  }

  /**
   * Functions for map reduce
   */
  public static List<String> partition(String data_str, int nlines) {
    /**
     * Partitions the input data_str (a big string)
     * into chunks of nlines.
     */
    String[] lines = data_str.split("\n");
    List<String> batches = new ArrayList<>();
    for (int i = 0; i < lines.length; i += nlines) {
      String[] batch = Arrays.copyOfRange(lines, i, i + nlines);
      batches.add(String.join("\n", batch));
    }
    return batches;
  }

  public static List<Pair> split_words(String data_str) {
    /**
     * Takes a string, returns a list of pairs (word, 1),
     * one for each word in the input, so
     * [(w1, 1), (w2, 1), ..., (wn, 1)]
     */
    // scan words
    List<Pair> result = new ArrayList<>();
    List<String> words = new ArrayList<>();
    for (String word : data_str.split("[\\W_]+"))
      words.add(word.toLowerCase());
    // load stop_words
    String content = "";
    try {
      content = new String(Files.readAllBytes(Paths.get("../stop_words.txt")));
    } catch (IOException e) {
      e.printStackTrace();
    }
    Set<String> stopwords = new HashSet<>();
    for (String stopWord : content.split(","))
      stopwords.add(stopWord);
    // remove stop words
    for (String w : words)
      if (!stopwords.contains(w) && w.length() >= 2)
        result.add(new Pair(w, 1));
    return result;
  }

  public static void regroup(List<Pair> pairs_list, ReduceWorker[] reducer) {
    /**
     * Takes a list of lists of pairs of the form
     * [[(w1, 1), (w2, 1), ..., (wn, 1)], [(w1, 1), (w2, 1), ..., (wn, 1)], ...]
     * and returns a dictionary mapping each unique word to the
     * corresponding list of pairs, so
     * { w1-w4 : [(w1, 1), (w4, 1)...],
     * w5-wn : [(w2, 1), (w2, 1)...],
     * ...}
     */
    for (Pair p : pairs_list) {
      char ch = p.word.charAt(0);
      if (ch >= 'a' && ch <= 'e') {
        reducer[0].assign(p);
      } else if (ch >= 'f' && ch <= 'j') {
        reducer[1].assign(p);
      } else if (ch >= 'k' && ch <= 'o') {
        reducer[2].assign(p);
      } else if (ch >= 'p' && ch <= 't') {
        reducer[3].assign(p);
      } else if (ch >= 'u' && ch <= 'z') {
        reducer[4].assign(p);
      }
    }
  }

  public static List<Map.Entry<String, Integer>> count_words(ReduceWorker[] reducers) {
    Map<String, Integer> word_freq = new HashMap<>();
    for (ReduceWorker reducer : reducers)
      for (Map.Entry<String, Integer> entry : reducer.freq.entrySet())
        word_freq.put(entry.getKey(), word_freq.getOrDefault(entry.getKey(), 0) + entry.getValue());
    return new ArrayList<>(word_freq.entrySet());
  }

  /**
   * Auxiliary functions
   */
  public static String read_file(String path_to_file) {
    try {
      return new String(Files.readAllBytes(Paths.get(path_to_file)));
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static List<Map.Entry<String, Integer>> sort(List<Map.Entry<String, Integer>> freq_list) {
    Collections.sort(freq_list, (o1, o2) -> o2.getValue().compareTo(o1.getValue()));
    return freq_list;
  }

  /**
   * The main function
   * @throws InterruptedException
   */
  public static void main(String[] args) throws InterruptedException {
    List<Pair> splits = new ArrayList<>();
    List<String> batches = partition(read_file(args[0]), 200);
    for (String batch : batches)
      splits.addAll(split_words(batch));
  
    // Create workers for parallel processing
    ReduceWorker[] reducers = new ReduceWorker[5];
    for (int i = 0; i < 5; i++)
      reducers[i] = new ReduceWorker();
    regroup(splits, reducers);
    for (Thread t : reducers)
      t.start();
    for (Thread t : reducers)
      t.join();

    List<Map.Entry<String, Integer>> freq_list = sort(count_words(reducers));

    for (Map.Entry<String, Integer> entry : freq_list.subList(0, 25)) {
      System.out.println(entry.getKey() + " - " + entry.getValue());
    }
  }
}