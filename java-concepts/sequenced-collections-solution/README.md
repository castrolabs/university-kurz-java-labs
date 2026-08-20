# Sequenced Collections - Solution

## Overview

This is the official solution for the Sequenced Collections lab. It demonstrates the
uniform `SequencedCollection`/`SequencedMap` API surface, the live-view nature of
`reversed()`, and an LRU-eviction helper built from `SequencedMap`'s named methods.

## Key Concepts

### getFirst() has a dedicated, checkable exception

```java
public static <T> T firstOrThrow(List<T> list) {
    return list.getFirst();
}
```

`list.get(0)` on an empty list throws `IndexOutOfBoundsException` — a generic "bad
index" signal. `getFirst()` throws `NoSuchElementException`, which says specifically
"there is nothing here," the same exception every other empty-collection query in the
JDK uses.

### One method, three concrete types

```java
public static <T> void addToFront(SequencedCollection<T> collection, T value) {
    collection.addFirst(value);
}
```

Because `ArrayList`, `ArrayDeque`, and `LinkedHashSet` all implement
`SequencedCollection`, this single method works unmodified against any of them — no
`instanceof` branching, no separate overloads for `List` vs `Deque`.

### reversed() is a live view, not a copy

```java
public static <T> List<T> reversedView(List<T> list) {
    return list.reversed();
}
```

The returned list is backed by the original, exactly like `Collections.unmodifiableList`
or a `subList` is backed by *its* source. A structural change on either side — adding
to the original, or calling `addFirst` on the view — is visible through the other,
because there is only one underlying sequence of elements; `reversed()` just changes
which end is "first."

### SequencedMap gives first-entry access without an Iterator

```java
public static <K, V> V firstValueOrNull(SequencedMap<K, V> map) {
    return map.isEmpty() ? null : map.firstEntry().getValue();
}
```

Before JEP 431, getting the first entry of a `LinkedHashMap` meant creating an
`Iterator` and calling `next()` once. `firstEntry()` says what that dance meant,
directly — and works the same way on any `SequencedMap`, including a `TreeMap`.

### putLast + pollFirstEntry as a direct eviction API

```java
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
```

`putLast` both updates the value and moves the entry to the most-recently-used
position in one call; `pollFirstEntry` removes and returns the least-recently-used
entry. Together they express an LRU policy directly, instead of relying on
`LinkedHashMap`'s `removeEldestEntry()` override hook.

### The fix when a frozen order is what you actually want

```java
public static <T> List<T> frozenReversedCopy(List<T> list) {
    return new ArrayList<>(list.reversed());
}
```

Wrapping the live view in a new `ArrayList` copies its current elements once and
disconnects it from the original — the right tool whenever a snapshot, not a live
report, is what the caller needs.

## Summary

- `getFirst()`/`getLast()` throw `NoSuchElementException` on empty, distinct from
  `List.get(int)`'s `IndexOutOfBoundsException` for the same condition.
- Writing code against `SequencedCollection`/`SequencedMap` lets one method serve
  `List`, `Deque`, `LinkedHashSet`, and sorted/linked map types alike.
- `reversed()` returns a live view backed by the original collection — mutating either
  side is visible through the other. Wrap it in a new collection when a frozen copy is
  what's actually needed.
- `SequencedMap`'s `putFirst`/`putLast`/`pollFirstEntry`/`pollLastEntry` give
  eviction-style logic a direct, named API.
