# Optional Correct Usage

## Goal

Use `Optional<T>` the way it's meant to be used — chaining `map`/`flatMap`, terminating with
`orElse`/`orElseGet`/`orElseThrow`, and collapsing a list of lookups with `flatMap(Optional::stream)` —
and, specifically, learn why `orElse(x)` and `orElseGet(supplier)` are not interchangeable.

## Prerequisites

- Basic Java syntax
- Lambdas and method references
- Streams basics

## Task

`PreferenceResolver` resolves a handful of user-preference lookups that may or may not have a value:
a nickname, a UI theme, a mailing address's zip code, and a list of optionally-set nicknames. You'll
implement each resolution using `Optional`'s combinators instead of manual `isPresent()`/`get()` checks.

## Instructions

Complete the following TODOs in `PreferenceResolver`:

- TODO-00: Wrap a possibly-null nickname in an `Optional`.
- TODO-01: Return a nickname if present, otherwise the literal `"Guest"`.
- TODO-02: Return a stored theme if present, otherwise fall back to a `Supplier`-provided default —
  **without ever invoking the supplier when a theme is already stored.**
- TODO-03: Return an address's zip code, or `"UNKNOWN"` if the address itself is absent.
- TODO-04: Collect only the present values out of a `List<Optional<String>>`, in order.

Run the tests until they all pass.

## Running the Lab

From the project root:

```bash
mvn -pl java-concepts/optional-correct-usage test
```

Or from the lab directory:

```bash
cd java-concepts/optional-correct-usage
mvn test
```

## Bonus (Optional)

- TODO-05 (optional): Implement `requireNickname` so it returns the nickname when present, or throws
  a `NoSuchElementException("nickname required")` when it's absent.
