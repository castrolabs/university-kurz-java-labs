# Concurrent Collections and Compound Actions - Solution

## Overview

This is the official solution for the Concurrent Collections and Compound Actions lab.
`ConcurrentRegistry` replaces every "check, then act" sequence with one of `ConcurrentHashMap`'s atomic
compound methods from the `ConcurrentMap` interface: `computeIfAbsent`, `putIfAbsent`, `merge`, and the
two-argument `remove`.

## Key Concepts

### getOrCreate(id): computeIfAbsent instead of containsKey + put

```java
public Account getOrCreate(String id) {
    return accounts.computeIfAbsent(id, this::newAccount);
}
```

The naive version — `if (!accounts.containsKey(id)) accounts.put(id, newAccount(id));` — is built from
two calls that are each individually thread-safe, but the sequence between them is not: if two threads
interleave between the check and the write, both see "absent," both construct a new `Account`, and the
second `put` silently discards the first. `computeIfAbsent` makes the whole "check and create" step a
single atomic operation: the map itself guarantees the mapping function runs at most once per key, no
matter how many threads call it concurrently for the same key.

### registerNickname(id, nickname): putIfAbsent as a single atomic check-and-set

```java
public boolean registerNickname(String id, String nickname) {
    return nicknames.putIfAbsent(id, nickname) == null;
}
```

`putIfAbsent` inserts only if the key is currently absent and, either way, returns the value that was
there *before* the call — `null` if this call was the one that inserted. That return value is exactly
what's needed to answer "did I just win the race," without a separate `get()` that could itself race
against another thread's write.

### recordHit(id): merge instead of get-then-put

```java
public void recordHit(String id) {
    hitCounts.merge(id, 1, Integer::sum);
}
```

The naive version — `hitCounts.put(id, hitCounts.getOrDefault(id, 0) + 1)` — reads the current count,
computes a new one, and writes it back as two separate map operations; under concurrent access, two
threads can both read the same old count and each write back `old + 1`, silently losing one of the two
increments. `merge` performs the read-compute-write as one atomic step: if the key is absent it stores
the initial value, otherwise it atomically replaces the value with the result of the remapping
function applied to the old value and the new one.

### clearNicknameIfMatches(id, expectedNickname): the conditional remove overload

```java
public boolean clearNicknameIfMatches(String id, String expectedNickname) {
    return nicknames.remove(id, expectedNickname);
}
```

`ConcurrentMap` overloads `remove` to take the expected current value: it removes the mapping only if
`id` is currently mapped to exactly `expectedNickname`, atomically. The naive equivalent —
checking the current value with `get()` and then calling `remove(id)` — has the same check-then-act gap
as every other compound action in this lab: another thread could change or remove the mapping between
the check and the removal.

## Summary

- Each individual `ConcurrentHashMap` call is thread-safe, but chaining several of them together
  ("check, then act") is not — the map guarantees nothing about what happens *between* two calls.
- `computeIfAbsent`, `putIfAbsent`, `merge`, and the two-argument `remove`/`replace` collapse a compound
  action into one atomic operation the map itself guarantees, instead of asking the caller to add
  external locking (which isn't even possible here — `ConcurrentHashMap` was designed specifically so
  it never needs to be locked for exclusive access).
- The failure mode of getting this wrong isn't a crash — it's a silently dropped update or a duplicate
  construction that only a genuinely concurrent test, not a sequential one, will ever catch.
