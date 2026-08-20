import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simple, deliberately stateful service: it accumulates an in-memory history of
 * messages. Being mutable is the point - it's what makes state leaking across tests
 * that share the same cached Spring context observable.
 */
public class MessageService {

    private final List<String> history = new ArrayList<>();

    public void addMessage(String message) {
        history.add(message);
    }

    public List<String> getHistory() {
        return Collections.unmodifiableList(history);
    }
}
