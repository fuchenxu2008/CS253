import java.util.*;
import java.io.*;

public class Seven {
  public static void main(String[] args) {
    Set<String> set = new HashSet<>();
    
    try {
      BufferedReader buffReader = new BufferedReader(new InputStreamReader(new FileInputStream("../stop_words.txt")));
      String tmp = "";
      while ((tmp = buffReader.readLine()) != null) {
        for (String word : tmp.split(",")) {
          set.add(word);
        }
      }
      buffReader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    String filepath = args[0];
    Map<String, Integer> map = new HashMap<>();
    try {
      BufferedReader buffReader = new BufferedReader(new InputStreamReader(new FileInputStream(filepath)));
      String tmp = "";
      while ((tmp = buffReader.readLine()) != null) {
        String[] arr = tmp.split("[\\p{Punct} ]");
        for (String word : arr) {
          word = word.trim().toLowerCase();
          if (!set.contains(word) && word.length() > 1) {
            map.put(word, map.getOrDefault(word, 0) + 1);
          }
        }
      }
      buffReader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    List<String> results = new ArrayList<>(map.keySet());
    Collections.sort(results, (o1, o2) -> map.get(o2) - map.get(o1));
    
    for (int i = 0; i < 25; i++) {
      System.out.println(results.get(i) + "  -  " + map.get(results.get(i)));
    }
  }
}