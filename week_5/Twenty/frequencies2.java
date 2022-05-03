import java.util.*;
import java.util.function.Function;

public class frequencies2 implements Function<List<String>, HashMap<String, Integer>> {
  @Override
  public HashMap<String, Integer> apply(List<String> words) {
    HashMap<String, Integer> wordFreqs = new HashMap<>();
    for (String word : words) {
      wordFreqs.put(word, wordFreqs.getOrDefault(word, 0) + 1);
    }
    return wordFreqs;
  }
}
