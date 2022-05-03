import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.regex.*;

public class words2 implements Function<String, List<String>> {
  private Set<String> getStopWords() throws IOException {
    Set<String> stopWords = new HashSet<String>();
    Scanner f = new Scanner(new File("../../stop_words.txt"), "UTF-8");
    try {
      f.useDelimiter(",");
      while (f.hasNext())
        stopWords.add(f.next());
    } catch (Exception e) {
      e.printStackTrace();
    }
    f.close();
    return stopWords;
  }

  @Override
  public List<String> apply(String pathToFile) {
    List<String> words = new ArrayList<>();

    try {
      Set<String> stopWords = getStopWords();
      Scanner f = new Scanner(new File(pathToFile), "UTF-8");
      Pattern regex = Pattern.compile("[a-z]{2,}");
      while (f.hasNextLine()) {
        String line = f.nextLine().toLowerCase();
        Matcher matcher = regex.matcher(line);
        while (matcher.find()) {
          String word = matcher.group();
          if (!stopWords.contains(word))
            words.add(word);
        }
      }
      f.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return words;
  }
}
