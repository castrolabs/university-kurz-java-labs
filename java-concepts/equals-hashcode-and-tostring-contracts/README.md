# Equals, HashCode, and ToString Contracts

## Goal

Implement `equals()`, `hashCode()`, and `toString()` for a small value class correctly — and see, with
a real `HashSet`/`HashMap`, exactly what goes wrong when `equals()` and `hashCode()` disagree.

## Prerequisites

- Basic Java syntax
- `Object`'s `equals`/`hashCode`/`toString` methods
- Familiarity with `HashSet` and `HashMap`

## Task

`GridPosition` is a simple `(row, col)` value class. You'll give it logical equality — two separate
instances with the same coordinates should compare equal, hash identically, and print readably —
instead of the identity-based equality `Object` provides by default.

## Instructions

Complete the following TODOs in `GridPosition`:

- TODO-00: Implement `equals()` so two `GridPosition`s with the same `row` and `col` are equal.
- TODO-01: Implement `hashCode()` consistent with `equals()` — every field `equals()` reads must be
  folded into the hash too.
- TODO-02: Implement `toString()` to return exactly `"(row, col)"`, e.g. `"(2, 3)"`.

Run the tests until they all pass. Pay attention to the tests that put `GridPosition` instances in a
`HashSet`/`HashMap` and look them up by an equal-but-different instance — that's where an `equals()`
without a matching `hashCode()` actually bites.

## Running the Lab

From the project root:

```bash
mvn -pl java-concepts/equals-hashcode-and-tostring-contracts test
```

Or from the lab directory:

```bash
cd java-concepts/equals-hashcode-and-tostring-contracts
mvn test
```

## Bonus (Optional)

- TODO-03 (optional): Implement `equalsUsingGetClass`, the same equality check as TODO-00 but using
  `getClass()` instead of `instanceof`, to see the trade-off between the two approaches.
