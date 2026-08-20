import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SequencedCollection;
import java.util.SequencedMap;

public class SequencedOps {

    public static <T> T firstOrThrow(List<T> list) {
        return list.getFirst();
    }

    public static <T> void addToFront(SequencedCollection<T> collection, T value) {
        collection.addFirst(value);
    }

    public static <T> List<T> reversedView(List<T> list) {
        return list.reversed();
    }

    public static <K, V> V firstValueOrNull(SequencedMap<K, V> map) {
        return map.isEmpty() ? null : map.firstEntry().getValue();
    }

    public static <K, V> void recordMostRecent(LinkedHashMap<K, V> map, K key, V value, int capacity) {
        if (map.containsKey(key)) {
            map.putLast(key, value);
        } else {
            map.put(key, value);
        }

        if (map.size() > capacity) {
            map.pollFirstEntry();
        }
    }

    public static <T> List<T> frozenReversedCopy(List<T> list) {
        return new ArrayList<>(list.reversed());
    }
}
