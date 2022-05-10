import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

class Message {
    String type;
    Object payload;

    public Message(String type, Object payload) {
        this.type = type;
        this.payload = payload;
    }
}

class ActiveWFObject extends Thread {
    Queue<Message> queue;
    boolean _stopMe;

    public ActiveWFObject() {
        this.queue = new LinkedBlockingDeque<Message>();
        this._stopMe = false;
        this.start();
    }

    @Override
    public void run() {
        while (!this._stopMe) {
            if (!this.queue.isEmpty()) {
                Message message = this.queue.poll();
                this._dispatch(message);
                if (message.type.equals("die")) {
                    this._stopMe = true;
                }
            }
        }
    }

    public void _dispatch(Message message) {
    }
}

class Dispatcher {
    public static void send(ActiveWFObject receiver, Message message) {
        receiver.queue.offer(message);
    }
}

/** Models the contents of the file. */
class DataStorageManager extends ActiveWFObject {
    private String _data;
    private StopWordManager _stop_word_manager;

    public DataStorageManager(StopWordManager _stop_word_manager) {
        this._stop_word_manager = _stop_word_manager;
    }

    @Override
    public void _dispatch(Message message) {
        if (message.type.equals("init"))
            this._init((String) message.payload);
        else if (message.type.equals("send_word_freqs"))
            this._process_words((ActiveWFObject) message.payload);
        else
            Dispatcher.send(this._stop_word_manager, message);
    }

    public void _init(String path_to_file) {
        try {
            StringBuilder sb = new StringBuilder();
            String content = new String(Files.readAllBytes(Paths.get(path_to_file)));
            for (String word : content.split("[\\W_]+"))
                sb.append(word.toLowerCase() + " ");
            this._data = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void _process_words(ActiveWFObject recipient) {
        String[] words = this._data.split(" ");
        for (String word : words) {
            Dispatcher.send(this._stop_word_manager, new Message("filter", word));
        }
        Dispatcher.send(this._stop_word_manager, new Message("top25", recipient));
    }
}

class StopWordManager extends ActiveWFObject {
    // Models the stop word filter
    private List<String> _stop_words = new ArrayList<String>();
    private WordFrequencyManager _word_freqs_manager;

    @Override
    public void _dispatch(Message message) {
        if (message.type.equals("init"))
            this._init((WordFrequencyManager) message.payload);
        else if (message.type.equals("filter"))
            this._filter((String) message.payload);
        else
            Dispatcher.send(this._word_freqs_manager, message);
    }

    public void _init(WordFrequencyManager _word_freqs_manager) {
        try {
            String content = new String(Files.readAllBytes(Paths.get("../stop_words.txt")));
            for (String stopWord : content.split(","))
                this._stop_words.add(stopWord);
            // Add single-letter words
            for (char c = 'a'; c <= 'z'; c++) {
                this._stop_words.add("" + c);
            }
            this._word_freqs_manager = _word_freqs_manager;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void _filter(String word) {
        if (!this._stop_words.contains(word))
            Dispatcher.send(this._word_freqs_manager, new Message("word", word));
    }
}

class WordFrequencyManager extends ActiveWFObject {
    // Keeps the word frequency data
    private Map<String, Integer> _word_freqs = new HashMap<String, Integer>();

    @Override
    public void _dispatch(Message message) {
        if (message.type.equals("word"))
            this._increment_count((String) message.payload);
        else if (message.type.equals("top25"))
            this._top25((ActiveWFObject) message.payload);
    }

    public void _increment_count(String word) {
        this._word_freqs.put(word, this._word_freqs.getOrDefault(word, 0) + 1);
    }

    public void _top25(ActiveWFObject recipient) {
        List<Map.Entry<String, Integer>> freq_list = new ArrayList<>(this._word_freqs.entrySet());
        Collections.sort(freq_list, (o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        Dispatcher.send(recipient, new Message("top25", freq_list));
    }
}

class WordFrequencyController extends ActiveWFObject {
    private DataStorageManager _storage_manager;

    @Override
    @SuppressWarnings("unchecked")
    public void _dispatch(Message message) {
        if (message.type.equals("run"))
            this._run((DataStorageManager) message.payload);
        else if (message.type.equals("top25"))
            this._display((List<Map.Entry<String, Integer>>) message.payload);
        else
            throw new IllegalArgumentException("Message not understaood");
    }

    public void _run(DataStorageManager _storage_manager) {
        this._storage_manager = _storage_manager;
        Dispatcher.send(this._storage_manager, new Message("send_word_freqs", this));
    }

    public void _display(List<Map.Entry<String, Integer>> freq_list) {
        for (Map.Entry<String, Integer> entry : freq_list.subList(0, 25)) {
            System.out.println(entry.getKey() + " - " + entry.getValue());
        }
        Dispatcher.send(this._storage_manager, new Message("die", null));
        this._stopMe = true;
    }
}

public class TwentyNine {
    public static void main(String[] args) {
        WordFrequencyManager word_freq_manager = new WordFrequencyManager();

        StopWordManager stop_word_manager = new StopWordManager();
        Dispatcher.send(stop_word_manager, new Message("init", word_freq_manager));

        DataStorageManager storage_manager = new DataStorageManager(stop_word_manager);
        Dispatcher.send(storage_manager, new Message("init", args[0]));

        WordFrequencyController wfcontroller = new WordFrequencyController();
        Dispatcher.send(wfcontroller, new Message("run", storage_manager));

        // Wait for the active objects to finish
        for (Thread t : new Thread[] { word_freq_manager, stop_word_manager, storage_manager, wfcontroller }) {
            try {
                t.join();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
