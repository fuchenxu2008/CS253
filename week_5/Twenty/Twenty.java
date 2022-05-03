import java.io.*;
import java.net.*;
import java.util.*;
import java.util.function.Function;

public class Twenty {
  static Function<String, List<String>> wordManager;
  static Function<List<String>, HashMap<String, Integer>> freqManager;

  @SuppressWarnings("unchecked")
  public static void loadPlugins() throws Exception {
    try {
      Properties prop = new Properties();
      String fileName = "config.properties";
      FileInputStream fis = new FileInputStream(fileName);
      prop.load(fis);

      String wordsName = prop.getProperty("words");
      String frequenciesName = prop.getProperty("frequencies");
      String appPath = prop.getProperty("appPath");

      Class<?> cls = null;
      URL classUrl = new URL(appPath);
      URL[] classURLs = { classUrl };

      // Create a new class loader with the directory
      URLClassLoader cl = new URLClassLoader(classURLs);

      wordManager = (Function<String, List<String>>) cl.loadClass(wordsName).getDeclaredConstructor().newInstance();
      freqManager = (Function<List<String>, HashMap<String, Integer>>) cl.loadClass(frequenciesName)
          .getDeclaredConstructor().newInstance();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @SuppressWarnings("unchecked")
  public static void main(String[] args) throws Exception {
    loadPlugins();

    HashMap<String, Integer> wordFreqs = freqManager
        .apply(wordManager.apply(args[0]));

    for (Pair pair : sorted(wordFreqs).subList(0, 25)) {
      System.out.println(pair.key + " - " + pair.value);
    }

  }

  public static List<Pair> sorted(HashMap<String, Integer> wordFreqs) {
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
