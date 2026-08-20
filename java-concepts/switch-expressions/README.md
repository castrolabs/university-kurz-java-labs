# Switch Expressions

## Goal

Learn the plumbing that makes a `switch` an *expression* rather than a *statement*:
arrow-form arms that cannot fall through, `yield` for producing a value from a
block-bodied arm, multi-value case labels, and the exhaustiveness check the compiler
performs once a switch must produce a value for every possible input — including the
explicit `case null` that a switch expression needs to avoid a `NullPointerException`.

## Prerequisites

- Basic `enum` and `String` usage
- The classic colon-form `switch` statement

## Task

`OrderStatusWorkflow` models an order's lifecycle with the `OrderStatus` enum
(`PLACED`, `PAID`, `SHIPPED`, `DELIVERED`, `CANCELLED`). You'll implement four methods,
each forcing a different switch-expression mechanic: exhaustiveness over every enum
constant with no `default`, multi-value labels, a block-bodied arm that must `yield`,
and an explicit `case null` versus a mandatory `default` over `String`.

## Instructions

Complete the following TODOs in `OrderStatusWorkflow`:

- TODO-00: Implement `nextStatus()` as an exhaustive switch expression over every `OrderStatus` constant.
- TODO-01: Implement `shippingPhase()` using multi-value case labels.
- TODO-02: Implement `describe()`, with a block-bodied `PAID` arm that uses `yield`.
- TODO-03: Implement `parseStatus()`, with an explicit `case null` arm and a mandatory `default` arm.

Run the tests until they all pass.

## Running the Lab

From the project root:

```bash
mvn -pl java-concepts/switch-expressions test
```

Or from the lab directory:

```bash
cd java-concepts/switch-expressions
mvn test
```

## Bonus (Optional)

- TODO-04 (optional): Implement `priority()` using multi-value case labels over `OrderStatus`.
