import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public class Thirty {
  static LinkedBlockingDeque<String> word_space;
  static LinkedBlockingDeque<Map<String, Integer>> freq_space;
  static Set<String> stopwords;

  /**
   * Worker function that consumes words from the word space
   * and sends partial results to the frequency space
   */
  static class ProcessWordWorker extends Thread {
    Map<String, Integer> word_freqs = new HashMap<>();

    @Override
    public void run() {
      while (true) {
        String word = null;
        try {
          word = word_space.poll(1, TimeUnit.SECONDS);
        } catch (Exception e) {
          e.printStackTrace();
        }
        if (word == null) {
          break;
        }
        if (!stopwords.contains(word) && word.length() >= 2) {
          word_freqs.put(word, word_freqs.getOrDefault(word, 0) + 1);
        }
      }
      freq_space.offer(word_freqs);
    }
  }

  public static void main(String[] args) {
    // Two data spaces
    word_space = new LinkedBlockingDeque<>();
    freq_space = new LinkedBlockingDeque<>();

    // stop words
    stopwords = new HashSet<>();
    try {
      String content = new String(Files.readAllBytes(Paths.get("../stop_words.txt")));
      for (String stopWord : content.split(","))
        stopwords.add(stopWord);
      // Add single-letter words
      for (char c = 'a'; c <= 'z'; c++) {
        stopwords.add("" + c);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    // Let's have this thread populate the word space
    try {
      String content = new String(Files.readAllBytes(Paths.get(args[0])));
      for (String word : content.split("[\\W_]+"))
        word_space.add(word.toLowerCase());
    } catch (IOException e) {
      e.printStackTrace();
    }

    // Let's create the workers and launch them at their jobs
    List<Thread> workers = new ArrayList<Thread>();
    for (int i = 0; i < 5; i++)
      workers.add(new ProcessWordWorker());
    for (Thread t : workers)
      t.start();

    // Let's wait for the workers to finish
    for (Thread t : workers) {
      try {
        t.join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    // Let's merge the partial frequency results by consuming frequency data from
    // the frequency space
    Map<String, Integer> word_freqs = new HashMap<>();
    while (!freq_space.isEmpty()) {
      Map<String, Integer> freqs = freq_space.poll();
      for (Map.Entry<String, Integer> entry : freqs.entrySet()) {
        word_freqs.put(entry.getKey(), word_freqs.getOrDefault(entry.getKey(), 0) + entry.getValue());
      }
    }

    List<Map.Entry<String, Integer>> freq_list = new ArrayList<>(word_freqs.entrySet());
    Collections.sort(freq_list, (o1, o2) -> o2.getValue().compareTo(o1.getValue()));
    for (Map.Entry<String, Integer> entry : freq_list.subList(0, 25)) {
      System.out.println(entry.getKey() + " - " + entry.getValue());
    }
  }
}
