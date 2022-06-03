import java.util.*;
import java.io.*;

public class Six {
  private static String readFile(String path) {
    String data;
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
    return data;
  }

  private static String filterAndNormalize(String data) {
    data = data.replaceAll("\\P{Alnum}", " ").toLowerCase();
    return data;
  }

  private static List<String> removeStopWords(String data) {
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

    List<String> words = new ArrayList<>();
    for (String str : data.split(" ")) {
      if (!stopWords.contains(str) && str.length() >= 2) {
        words.add(str);
      }
    }
    return words;
  }

  private static void output(Map<String, Integer> wordFreqs) {
    PriorityQueue<String> pq = new PriorityQueue<>((o1, o2) -> wordFreqs.get(o1) - wordFreqs.get(o2));
    for (String str : wordFreqs.keySet()) {
      pq.offer(str);
      if (pq.size() > 25) {
        pq.poll();
      }
    }
    List<String> list = new ArrayList<>();
    for (int i = 0; i < 25; i++) {
      list.add(0, pq.poll());
    }
    for (String str : list) {
      System.out.println(str + "  -  " + wordFreqs.get(str));
    }
  }

  private static Map<String, Integer> aggregagte(List<String> words) {
    Map<String, Integer> wordFreqs = new HashMap<>();
    for (String str : words) {
      wordFreqs.put(str, wordFreqs.getOrDefault(str, 0) + 1);
    }
    return wordFreqs;
  }

  public static void main(String[] args) {
    output(aggregagte(removeStopWords(filterAndNormalize(readFile(args[0])))));
  }
}