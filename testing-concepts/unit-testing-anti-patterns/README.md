# Unit Testing Anti-Patterns

## Goal

`Membership`, `TaxCalculator`, and `Receipt` are already fully implemented
— your job is to write tests for them while specifically avoiding three
anti-patterns the article names: testing private state, leaking domain
knowledge into a test's expected value, and depending on the system clock
directly.

## Prerequisites

- JUnit 5 fundamentals (`@Test`, `@Nested`, `@ParameterizedTest`)
- Reading `Membership`, `TaxCalculator`, and `Receipt` in `src/main/java`
  before writing any test — note which fields have getters and which don't,
  and why

## Task

Each `@Nested` group targets one anti-pattern:

- **`WhenTestingUpgrade`**: `Membership.level` is private with no getter.
  Don't add one just to check `upgrade()` "worked" — assert through
  `discountRate()`, the one thing a caller can actually observe.
- **`WhenComputingTax`**: don't compute your parameterized test's expected
  values by calling `subtotal.multiply(RATE)` — that duplicates
  `TaxCalculator`'s own formula (and any bug in it) into the test.
  Hardcode values you work out independently.
- **`WhenRenewing`**: `Membership.renew(Clock)` already takes a `Clock`
  parameter instead of calling `Clock.systemDefaultZone()` internally —
  use `Clock.fixed(...)` to make the test deterministic.

## Instructions

Complete the following TODOs:

- TODO-00: `discountRate()` reflects `upgrade()`, asserted without any
  getter for the private `level` field.
- TODO-01: add a `@CsvSource` with hand-computed `subtotal, expectedTax`
  rows.
- TODO-02: `renew()` records the exact instant of a fixed `Clock`.

Run the tests until they all pass.

## Running the Lab

From the project root:

```bash
mvn -pl testing-concepts/unit-testing-anti-patterns test
```

Or from the lab directory:

```bash
cd testing-concepts/unit-testing-anti-patterns
mvn test
```

## Bonus (Optional)

- TODO-03 (optional): test `Receipt.summarize()` using a real
  `TaxCalculator` (not a mock — it has no I/O, so there's nothing to gain
  by doubling it) and a hardcoded expected string.
