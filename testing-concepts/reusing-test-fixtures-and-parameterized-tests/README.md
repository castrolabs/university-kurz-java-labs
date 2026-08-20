# Reusing Test Fixtures and Parameterized Tests

## Goal

`Order` and `Inventory` are already fully implemented — your job is to test
them while applying two ideas from the article: reuse fixtures through
private factory methods instead of shared `@BeforeEach` fields, and collapse
near-identical boundary tests into `@ParameterizedTest`s.

## Prerequisites

- JUnit 5 fundamentals (`@Test`, `@ParameterizedTest`, `assertEquals`)
- Reading `Order` and `Inventory` in `src/main/java` before writing any test

## Task

`OrderTest` has no shared `Inventory`/`Order` fields on purpose. `Inventory`
is mutable, so a field shared across tests would let one test's `place()`
call quietly change the starting stock for every other test in the class —
exactly the coupling the article calls out as the wrong way to reuse a
fixture. Instead, you'll build a fresh `Inventory` per test through a single
private factory method (the "Object Mother" pattern).

You'll then write two parameterized tests: one whose inputs are plain
constants (`@CsvSource`), and one whose inputs are computed from
`LocalDate.now()` and therefore can't be written as a compile-time constant
at all (`@MethodSource`).

## Instructions

Complete the following TODOs:

- TODO-00: implement `createInventoryWithStock(sku, quantity)` — a factory
  method, not a shared field.
- TODO-01: a test that a successful order decrements stock.
- TODO-02: a test that a failed order leaves stock untouched.
- TODO-03: add a `@CsvSource` covering the shipping-fee tier boundaries.
- TODO-04: add a `@MethodSource("returnWindowCases")` annotation.
- TODO-05: implement `returnWindowCases()`.

Run the tests until they all pass.

## Running the Lab

From the project root:

```bash
mvn -pl testing-concepts/reusing-test-fixtures-and-parameterized-tests test
```

Or from the lab directory:

```bash
cd testing-concepts/reusing-test-fixtures-and-parameterized-tests
mvn test
```

## Bonus (Optional)

- TODO-06 (optional): rewrite the assertions in
  `placeSucceedsWhenEnoughInventory` using AssertJ's fluent
  `assertThat(...).isEqualTo(...)`/`isTrue()` instead of JUnit's
  `assertEquals`/`assertTrue`.
