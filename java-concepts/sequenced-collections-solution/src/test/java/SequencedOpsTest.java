import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SequencedOps")
class SequencedOpsTest {

    @Test
    @DisplayName("firstOrThrow should return the first element of a non-empty list")
    void firstOrThrowShouldReturnFirstElement() {
        List<String> names = new ArrayList<>(List.of("Ana", "Bo", "Cid"));

        assertEquals("Ana", SequencedOps.firstOrThrow(names));
    }

    @Test
    @DisplayName("firstOrThrow should throw NoSuchElementException, not IndexOutOfBoundsException, on an empty list")
    void firstOrThrowShouldThrowNoSuchElementExceptionOnEmptyList() {
        List<String> empty = new ArrayList<>();

        assertThrows(NoSuchElementException.class, () -> SequencedOps.firstOrThrow(empty));
    }

    @Test
    @DisplayName("addToFront should work unmodified against an ArrayList")
    void addToFrontShouldWorkAgainstArrayList() {
        List<String> list = new ArrayList<>(List.of("b", "c"));

        SequencedOps.addToFront(list, "a");

        assertEquals(List.of("a", "b", "c"), list);
    }

    @Test
    @DisplayName("addToFront should work unmodified against an ArrayDeque")
    void addToFrontShouldWorkAgainstArrayDeque() {
        ArrayDeque<String> deque = new ArrayDeque<>(List.of("b", "c"));

        SequencedOps.addToFront(deque, "a");

        assertEquals(List.of("a", "b", "c"), new ArrayList<>(deque));
    }

    @Test
    @DisplayName("addToFront should work unmodified against a LinkedHashSet")
    void addToFrontShouldWorkAgainstLinkedHashSet() {
        LinkedHashSet<String> set = new LinkedHashSet<>(List.of("b", "c"));

        SequencedOps.addToFront(set, "a");

        assertEquals(List.of("a", "b", "c"), new ArrayList<>(set));
    }

    @Test
    @DisplayName("reversedView should reflect a later mutation of the original list")
    void reversedViewShouldReflectMutationOfOriginal() {
        List<Integer> nums = new ArrayList<>(List.of(1, 2, 3));

        List<Integer> reversed = SequencedOps.reversedView(nums);

        assertEquals(List.of(3, 2, 1), reversed);

        nums.add(4);

        assertEquals(List.of(4, 3, 2, 1), reversed, "the view must reflect the mutation of the backing list");
    }

    @Test
    @DisplayName("reversedView should be a live view: mutating it mutates the original list")
    void reversedViewMutationShouldAffectOriginal() {
        List<Integer> nums = new ArrayList<>(List.of(1, 2, 3));

        List<Integer> reversed = SequencedOps.reversedView(nums);
        reversed.addFirst(99);

        assertEquals(List.of(1, 2, 3, 99), nums, "adding to the front of the view must append to the original list");
        assertEquals(List.of(99, 3, 2, 1), reversed);
    }

    @Test
    @DisplayName("firstValueOrNull should return the earliest-inserted value of a LinkedHashMap")
    void firstValueOrNullShouldReturnEarliestInsertedValue() {
        LinkedHashMap<String, Integer> scores = new LinkedHashMap<>();
        scores.put("a", 1);
        scores.put("b", 2);
        scores.put("c", 3);

        assertEquals(1, SequencedOps.firstValueOrNull(scores));
    }

    @Test
    @DisplayName("firstValueOrNull should return null for an empty map")
    void firstValueOrNullShouldReturnNullForEmptyMap() {
        assertNull(SequencedOps.firstValueOrNull(new LinkedHashMap<String, Integer>()));
    }

    @Test
    @DisplayName("firstValueOrNull should work against any SequencedMap, e.g. a TreeMap")
    void firstValueOrNullShouldWorkAgainstTreeMap() {
        TreeMap<Integer, String> byId = new TreeMap<>();
        byId.put(3, "third");
        byId.put(1, "first");
        byId.put(2, "second");

        assertEquals("first", SequencedOps.firstValueOrNull(byId));
    }

    @Test
    @DisplayName("recordMostRecent should evict the least-recently-used entry once over capacity")
    void recordMostRecentShouldEvictLeastRecentlyUsedEntry() {
        LinkedHashMap<String, Integer> cache = new LinkedHashMap<>();

        SequencedOps.recordMostRecent(cache, "a", 1, 2);
        SequencedOps.recordMostRecent(cache, "b", 2, 2);
        SequencedOps.recordMostRecent(cache, "c", 3, 2);

        assertEquals(List.of("b", "c"), new ArrayList<>(cache.keySet()));
        assertFalse(cache.containsKey("a"), "the least-recently-used entry must have been evicted");
    }

    @Test
    @DisplayName("recordMostRecent should move an updated existing key to the most-recent position")
    void recordMostRecentShouldMoveUpdatedKeyToMostRecentPosition() {
        LinkedHashMap<String, Integer> cache = new LinkedHashMap<>();

        SequencedOps.recordMostRecent(cache, "a", 1, 2);
        SequencedOps.recordMostRecent(cache, "b", 2, 2);
        SequencedOps.recordMostRecent(cache, "c", 3, 2);
        SequencedOps.recordMostRecent(cache, "b", 20, 2);

        assertEquals(List.of("c", "b"), new ArrayList<>(cache.keySet()));
        assertEquals(20, cache.get("b"));
    }

    @Test
    @DisplayName("frozenReversedCopy should NOT reflect a later mutation of the original list (bonus)")
    void frozenReversedCopyShouldNotReflectMutationOfOriginal() {
        List<Integer> nums = new ArrayList<>(List.of(1, 2, 3));

        List<Integer> frozen = SequencedOps.frozenReversedCopy(nums);

        assertEquals(List.of(3, 2, 1), frozen);

        nums.add(4);

        assertEquals(List.of(3, 2, 1), frozen, "a frozen copy must not change when the original list is mutated");
    }
}
