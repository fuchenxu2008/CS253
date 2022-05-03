import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;

public class words1 implements Function<String, List<String>> {
  private Set<String> getStopWords() throws IOException {
    Set<String> stopWords = new HashSet<String>();
    String content = new String(Files.readAllBytes(Paths.get("../../stop_words.txt")));
    for (String stopWord : content.split(","))
      stopWords.add(stopWord);
    // Add single-letter words
    for (char c = 'a'; c <= 'z'; c++) {
      stopWords.add("" + c);
    }
    return stopWords;
  }

  @Override
  public List<String> apply(String pathToFile) {
    List<String> words = new ArrayList<String>();
    try {
      Set<String> stopWords = getStopWords();
      String content = new String(Files.readAllBytes(Paths.get(pathToFile)));
      for (String word : content.split("[\\W_]+")) {
        word = word.toLowerCase();
        if (!stopWords.contains(word))
          words.add(word);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return words;
  }
}
