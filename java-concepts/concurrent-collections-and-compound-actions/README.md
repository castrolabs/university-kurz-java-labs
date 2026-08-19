# Concurrent Collections and Compound Actions

## Goal

Implement "get or create", "set only if absent", and "increment" on a `ConcurrentHashMap` using its
real atomic compound methods — and see, with a genuine multi-threaded test, why a "check, then act"
sequence built from individually thread-safe calls is not itself thread-safe.

## Prerequisites

- Basic Java syntax
- `ConcurrentHashMap` basics
- `ExecutorService`, `CountDownLatch` (used by the tests, not something you need to write)

## Task

`ConcurrentRegistry` is a small shared registry backed by three `ConcurrentHashMap`s: accounts,
nicknames, and hit counts. Each operation looks simple sequentially, but every one of them is a
compound action — a read followed by a write that must behave as a single, atomic step even when many
threads call it on the same key at the same time.

## Instructions

Complete the following TODOs in `ConcurrentRegistry`:

- TODO-00: `getOrCreate(id)` — return the existing `Account` for `id`, or atomically create and store
  one if absent. A `containsKey`-then-`put` sequence is NOT safe here.
- TODO-01: `registerNickname(id, nickname)` — atomically set a nickname only if none is registered yet,
  returning whether this call was the one that set it.
- TODO-02: `recordHit(id)` — atomically increment a per-id hit counter, without losing updates when
  many threads hit the same id concurrently.

Run the tests until they all pass. The concurrent tests spin up dozens to hundreds of threads racing on
the same key through a `CountDownLatch` starting gate — a naive check-then-act implementation will fail
them (extra constructions, more than one "winning" nickname registration, or a hit count lower than
expected), not because of a compile error, but because the race actually happened.

## Running the Lab

From the project root:

```bash
mvn -pl java-concepts/concurrent-collections-and-compound-actions test
```

Or from the lab directory:

```bash
cd java-concepts/concurrent-collections-and-compound-actions
mvn test
```

## Bonus (Optional)

- TODO-03 (optional): Implement `clearNicknameIfMatches(id, expectedNickname)` so it atomically removes
  the nickname for `id` only if its current value equals `expectedNickname`, returning whether it was
  removed.
