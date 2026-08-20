# Exact Arithmetic with BigDecimal and BigInteger

## Goal

Learn why `double` is the wrong type for money, how to construct a `BigDecimal` so it carries exactly the digits you wrote (not a binary approximation), and why `divide()` forces you to name a `RoundingMode` instead of silently guessing one.

## Prerequisites

- Basic Java syntax
- Familiarity with immutable value types

## Task

Implement `MoneySplitter`, a small set of money utilities: parsing a decimal string exactly, computing one rounded share of a total, splitting a total evenly across several parties so the shares sum back to the total exactly, and converting an amount at an explicit scale and rounding policy.

## Instructions

Complete the following TODOs in `MoneySplitter`:

- TODO-00: Parse a decimal string into an exact `BigDecimal`.
- TODO-01: Compute one rounded share of a total, honoring the caller's `RoundingMode`.
- TODO-02: Split a total evenly across N parties so the shares sum back to exactly the total.
- TODO-03: Convert an amount using an explicit target scale and `RoundingMode`.

Run the tests until they all pass.

## Running the Lab

From the project root:

```bash
mvn -pl java-concepts/exact-arithmetic-with-bigdecimal-and-biginteger test
```

Or from the lab directory:

```bash
cd java-concepts/exact-arithmetic-with-bigdecimal-and-biginteger
mvn test
```

## Bonus (Optional)

- TODO-04 (optional): Sum a list of amounts exactly, without discarding any intermediate `BigDecimal`.
