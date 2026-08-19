import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentRegistry {

    private final Map<String, Account> accounts = new ConcurrentHashMap<>();
    private final Map<String, String> nicknames = new ConcurrentHashMap<>();
    private final Map<String, Integer> hitCounts = new ConcurrentHashMap<>();
    private final AtomicInteger constructions = new AtomicInteger();

    public record Account(String id) {}

    public Account getOrCreate(String id) {
        return accounts.computeIfAbsent(id, this::newAccount);
    }

    public boolean registerNickname(String id, String nickname) {
        return nicknames.putIfAbsent(id, nickname) == null;
    }

    public void recordHit(String id) {
        hitCounts.merge(id, 1, Integer::sum);
    }

    public boolean clearNicknameIfMatches(String id, String expectedNickname) {
        return nicknames.remove(id, expectedNickname);
    }

    public String nicknameOf(String id) {
        return nicknames.get(id);
    }

    public int hitsFor(String id) {
        return hitCounts.getOrDefault(id, 0);
    }

    public int size() {
        return accounts.size();
    }

    public int constructions() {
        return constructions.get();
    }

    private Account newAccount(String id) {
        constructions.incrementAndGet();
        try {
            // Widen the race window a naive check-then-act implementation
            // would leave open between checking for an existing entry and
            // storing a new one.
            Thread.sleep(5);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return new Account(id);
    }
}
