import java.util.*;
import java.util.function.Function;

public class frequencies1 implements Function<List<String>, HashMap<String, Integer>>{
  @Override
  public HashMap<String, Integer> apply(List<String> words) {
    HashMap<String, Integer> wordFreq = new HashMap<>();
    for (String word : words) {
      Integer count = wordFreq.get(word);
      if (count == null) {
        wordFreq.put(word, 1);
      } else {
        wordFreq.put(word, count + 1);
      }
    }
    return wordFreq;
  }
}
