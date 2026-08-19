# Equals, HashCode, and ToString Contracts - Solution

## Overview

This is the official solution for the Equals, HashCode, and ToString Contracts lab. `GridPosition`
follows the standard recipe: a reference/`instanceof` check for `equals()`, a `hashCode()` that folds
in exactly the fields `equals()` compares, and a `toString()` that documents an exact format.

## Key Concepts

### equals(): reference check, then instanceof

```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof GridPosition p)) return false;
    return row == p.row && col == p.col;
}
```

`this == o` is a cheap short-circuit for the common case of comparing an object to itself.
`instanceof GridPosition p` does double duty: it returns `false` for `null` (satisfying the
contract's non-null requirement without a separate check) and `false` for any other type, then binds
`p` for the field comparison in one step.

### hashCode(): every field equals() reads, folded consistently

```java
@Override
public int hashCode() {
    return Objects.hash(row, col);
}
```

The contract that actually matters here: *equal objects must return equal hash codes*. Nothing in the
compiler enforces this — a class can override `equals()` and leave the inherited, identity-based
`hashCode()` in place, and it compiles without a warning. The bug only shows up when someone puts two
"equal" instances in a `HashSet` and finds both landed in different buckets, so a lookup for one comes
back empty even though it's "obviously" there. `Objects.hash(row, col)` folds every field `equals()`
uses into the hash, in the same order, so two equal instances always agree.

### toString(): a stable, documented format

```java
@Override
public String toString() {
    return "(" + row + ", " + col + ")";
}
```

`Object`'s default `toString()` (`GridPosition@1a2b3c`) tells you nothing about which position this
is. `println`, string concatenation, and most debuggers call `toString()` automatically, so a
readable override pays off everywhere without being asked for explicitly. Documenting an exact
format (as done here) gives callers something stable to log or parse against — the trade-off being
that changing the format later becomes a breaking change.

### equalsUsingGetClass(): instanceof vs. getClass()

```java
public boolean equalsUsingGetClass(Object o) {
    if (o == null || o.getClass() != getClass()) return false;
    GridPosition p = (GridPosition) o;
    return row == p.row && col == p.col;
}
```

`instanceof` (used in the main `equals()`) lets a subclass that adds no new value component compare
equal to an instance of this class — useful when a subclass exists purely for behavior, not state.
`getClass()` requires an exact type match instead: it sidesteps the classic symmetry break that shows
up when a subclass *does* add a field (`p.equals(cp)` true, `cp.equals(p)` false), at the cost of also
rejecting harmless, stateless subclasses that `instanceof` would have accepted.

## Summary

- `equals()` and `hashCode()` are one contract, not two — overriding one without the other compiles
  fine and fails silently at runtime, inside whatever hash-based collection touches the object first.
- `hashCode()` must derive from exactly the fields `equals()` compares, the same way every time.
- `toString()` is invoked far more often than it looks, by anything that logs, concatenates, or
  debugs the object.
- `instanceof` and `getClass()` in `equals()` make different trade-offs between substitutability and
  symmetry — neither is universally "more correct."
