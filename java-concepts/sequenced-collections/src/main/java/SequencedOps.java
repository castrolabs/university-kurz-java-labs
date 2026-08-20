import java.util.LinkedHashMap;
import java.util.List;
import java.util.SequencedCollection;
import java.util.SequencedMap;

public class SequencedOps {

    public static <T> T firstOrThrow(List<T> list) {
        // TODO-00: Return the first element of `list`.
        // Hint: use the SequencedCollection method that throws NoSuchElementException
        // on an empty list. Do NOT use list.get(0) — it throws IndexOutOfBoundsException
        // for the exact same "there is nothing here" condition, which is not what the
        // test expects.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public static <T> void addToFront(SequencedCollection<T> collection, T value) {
        // TODO-01: Insert `value` as the new first element of `collection`.
        // This method must work UNMODIFIED whether `collection` is an ArrayList, an
        // ArrayDeque, or a LinkedHashSet — write it against the SequencedCollection
        // contract only, not against any one concrete type.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public static <T> List<T> reversedView(List<T> list) {
        // TODO-02: Return a reverse-order VIEW of `list` — not a copy. A structural
        // change made through `list` afterwards must be visible through the returned
        // view, and vice versa.
        // Hint: SequencedCollection declares exactly one method for this.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public static <K, V> V firstValueOrNull(SequencedMap<K, V> map) {
        // TODO-03: Return the value of the first (earliest-encounter-order) entry in
        // `map`, or null if `map` is empty. Do not use an Iterator or a keySet/values
        // walk — SequencedMap has a direct method for "the first entry".
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public static <K, V> void recordMostRecent(LinkedHashMap<K, V> map, K key, V value, int capacity) {
        // TODO-04: Maintain `map` as a most-recently-used-last structure with at most
        // `capacity` entries:
        //   - if `key` is already present, update its value AND move it to the END
        //     (the most-recently-used position);
        //   - otherwise insert it as a new last entry;
        //   - after that, if the map holds more than `capacity` entries, evict the
        //     entry at the FRONT (the least-recently-used one).
        // Hint: SequencedMap has named methods for "put/move an entry to last" and for
        // "remove and return the first entry" — no manual iteration needed.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public static <T> List<T> frozenReversedCopy(List<T> list) {
        // TODO-05 (optional): Return a reverse-order COPY of `list` that will NOT
        // reflect later mutations to `list` — the opposite of TODO-02's live view.
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
