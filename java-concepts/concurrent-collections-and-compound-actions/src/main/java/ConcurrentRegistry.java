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
        // TODO-00: Return the existing Account for `id`, or atomically create and
        // store a new one (via newAccount(id)) if none exists yet. Use one of
        // ConcurrentHashMap's atomic compound methods (putIfAbsent,
        // computeIfAbsent, or merge) - a "check, then act" sequence
        // (`if (!accounts.containsKey(id)) accounts.put(...)`) is NOT safe
        // under concurrent access, even though each individual call is.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public boolean registerNickname(String id, String nickname) {
        // TODO-01: Atomically set the nickname for `id` only if one isn't
        // already registered. Return true if this call was the one that set
        // it, false if a nickname was already present.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public void recordHit(String id) {
        // TODO-02: Atomically increment the hit count for `id`, starting at 1
        // if this is the first hit. Do not lose updates when many threads
        // record hits for the same id at the same time.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public boolean clearNicknameIfMatches(String id, String expectedNickname) {
        // TODO-03 (optional): Atomically remove the nickname entry for `id`,
        // but only if its current value equals `expectedNickname`. Return
        // whether it was removed.
        throw new UnsupportedOperationException("Not implemented yet.");
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
