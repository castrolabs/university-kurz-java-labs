# Four Pillars of a Good Unit Test

## Goal

`Invoice` is already fully implemented — your job is to write tests that
each specifically target one of the article's four pillars: protection
against regressions, resistance to refactoring, fast feedback, and
maintainability.

## Prerequisites

- JUnit 5 fundamentals (`@Test`, `assertEquals`)
- Reading `Invoice` and `LineItem` in `src/main/java` before writing any
  test — in particular, notice that summing line items happens inside a
  *private* `computeSubtotal()` method

## Task

Every TODO below asks for a test that could plausibly "just pass" if
written carelessly — the point is writing it in the way that actually earns
the pillar it's meant to demonstrate. Read each TODO's comment before
writing the test; the instruction that matters is usually *how* to assert,
not just *what* to assert.

## Instructions

Complete the following TODOs:

- TODO-00: a discount test with a hand-computed, hardcoded expected value
  (protection against regressions).
- TODO-01: the boundary case just below the discount threshold.
- TODO-02: a multi-item test at the exact boundary, asserted only through
  `total()` — never through `computeSubtotal()` or any newly-added getter
  (resistance to refactoring).
- TODO-03: an empty-invoice test with minimal setup (maintainability / fast
  feedback).

Run the tests until they all pass.

## Running the Lab

From the project root:

```bash
mvn -pl testing-concepts/four-pillars-of-a-good-unit-test test
```

Or from the lab directory:

```bash
cd testing-concepts/four-pillars-of-a-good-unit-test
mvn test
```

## Bonus (Optional)

- TODO-04 (optional): a test for `itemCount()` — notice how little this
  test protects against, compared to TODO-00, even though it's just as fast
  and just as resistant to refactoring.
