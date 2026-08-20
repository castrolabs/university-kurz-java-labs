# Sequenced Collections

## Goal

Learn the `SequencedCollection` / `SequencedSet` / `SequencedMap` interfaces (JEP 431)
that unify first/last access across `List`, `Deque`, `LinkedHashSet`, and
`LinkedHashMap`. The central trap: `reversed()` returns a **live view**, not a copy —
mutating either side is visible through the other. You'll also use the uniform
`getFirst()`/`addFirst()` family, see `NoSuchElementException` replace the mismatched
exceptions collections used to throw for "nothing here," and use `SequencedMap`'s
named methods to implement an LRU-style eviction policy.

## Prerequisites

- `List`, `Deque`, `LinkedHashSet`, `LinkedHashMap` basics
- Generic methods (`<T> T method(...)`)

## Task

`SequencedOps` is a small utility class. You'll implement five methods, each isolating
one mechanic: the exception type `getFirst()` throws on empty, writing code generically
against `SequencedCollection`, the live-view behavior of `reversed()` in both
directions, uniform access on `SequencedMap`, and an LRU-eviction helper built from
`putLast`/`pollFirstEntry`.

## Instructions

Complete the following TODOs in `SequencedOps`:

- TODO-00: Implement `firstOrThrow()` using the method that throws `NoSuchElementException` on empty, not `IndexOutOfBoundsException`.
- TODO-01: Implement `addToFront()` against the `SequencedCollection` contract so it works for a `List`, a `Deque`, and a `LinkedHashSet` alike.
- TODO-02: Implement `reversedView()` to return a live view, not a copy.
- TODO-03: Implement `firstValueOrNull()` using `SequencedMap`'s direct "first entry" access.
- TODO-04: Implement `recordMostRecent()`, an LRU-style helper using `putLast` and `pollFirstEntry`.

Run the tests until they all pass. Pay close attention to the `reversedView` tests —
they check that a mutation on either side (the original list or the reversed view) is
visible through the other.

## Running the Lab

From the project root:

```bash
mvn -pl java-concepts/sequenced-collections test
```

Or from the lab directory:

```bash
cd java-concepts/sequenced-collections
mvn test
```

## Bonus (Optional)

- TODO-05 (optional): Implement `frozenReversedCopy()` — the fix for when a live view is the wrong tool and a frozen, disconnected copy is what's actually needed.
