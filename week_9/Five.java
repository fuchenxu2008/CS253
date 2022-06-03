import java.util.*;
import java.io.*;

public class Five {
  static String data;
  static List<String> words;
  static Map<String, Integer> wordFreq;
  static PriorityQueue<String> pq;

  private static void readTextFile(String path) {
    StringBuilder sb = null;
    try {
      FileInputStream fin = new FileInputStream(path);
      InputStreamReader reader = new InputStreamReader(fin);
      BufferedReader buffReader = new BufferedReader(reader);
      sb = new StringBuilder();
      String tmp = "";
      while ((tmp = buffReader.readLine()) != null) {
        sb.append(tmp + " ");
      }
      buffReader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    data = sb.toString();
  }

  // replace all nonalphanumeric chars in data with white space and normalize to
  // lowercase
  private static void filterAndNormalize() {
    data = data.replaceAll("\\P{Alnum}", " ").toLowerCase();
  }

  // remove stop_words
  private static void removeStopWords() {
    ArrayList<String> stopWords = new ArrayList<>();
    try {
      FileInputStream fin = new FileInputStream("../stop_words.txt");
      InputStreamReader reader = new InputStreamReader(fin);
      BufferedReader buffReader = new BufferedReader(reader);
      String tmp = "";
      while ((tmp = buffReader.readLine()) != null) {
        String[] arr = tmp.split(",");
        for (String str : arr) {
          stopWords.add(str);
        }
      }
      buffReader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    words = new ArrayList<>();
    for (String str : data.split(" ")) {
      if (!stopWords.contains(str) && str.length() >= 2) {
        words.add(str);
      }
    }
  }

  private static void aggregate() {
    wordFreq = new HashMap<>();
    for (String str : words) {
      wordFreq.put(str, wordFreq.getOrDefault(str, 0) + 1);
    }
  }

  private static void sort() {
    pq = new PriorityQueue<>((o1, o2) -> wordFreq.get(o1) - wordFreq.get(o2));
    for (String str : wordFreq.keySet()) {
      pq.offer(str);
      if (pq.size() > 25) {
        pq.poll();
      }
    }
  }

  public static void main(String[] args) {
    readTextFile(args[0]);
    filterAndNormalize();
    removeStopWords();
    aggregate();
    sort();

    // output the top 25 frequent words
    List<String> list = new ArrayList<>();
    for (int i = 0; i < 25; i++) {
      list.add(0, pq.poll());
    }
    for (String str : list) {
      System.out.println(str + " - " + wordFreq.get(str));
    }
  }
}