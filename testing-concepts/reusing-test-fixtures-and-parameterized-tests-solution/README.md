# Reusing Test Fixtures and Parameterized Tests — Solution

## Overview

`OrderTest` tests the same (already-implemented) `Order`/`Inventory` pair
three ways: a factory-method-built fixture for the stock-mutation tests, a
`@CsvSource` parameterized test for the shipping-fee tiers, and a
`@MethodSource` parameterized test for the date-based return window.

## Key Concepts

- **`createInventoryWithStock(...)` is a factory method, not a shared
  field.** Each test calls it fresh, so `placeFailsWhenNotEnoughInventory`'s
  `Inventory` starts at exactly 3 units regardless of what
  `placeSucceedsWhenEnoughInventory` did to *its own* `Inventory` a moment
  earlier. A `@BeforeEach` that assigned one `Inventory` field would have
  let those two tests interfere with each other the moment their `place()`
  calls ran in the same shared instance.
- **The factory method takes its target values as parameters.** `sku` and
  `quantity` are arguments, not hardcoded inside the method — that's what
  keeps it reusable across tests that need different stock levels, instead
  of silently reintroducing the coupling problem one level down.
- **`@CsvSource` works here because every input is a compile-time
  constant.** `shippingFeeReflectsOrderTotalTiers` covers the value just
  below and the value at each tier boundary (1999/2000, 4999/5000) — plain
  `int` literals the annotation can hold directly.
- **`@MethodSource` exists for the case `@CsvSource` can't handle.**
  `returnWindowCases()` needs `LocalDate.now()` arithmetic, which isn't a
  constant the compiler can fold into an annotation. Pointing
  `@MethodSource` at a static method that builds the `Stream<Arguments>` at
  runtime is the only way to parameterize on a computed value.
- **The bonus test compares AssertJ against plain JUnit.**
  `placeSucceedsWhenEnoughInventory` uses
  `assertThat(inventory.quantityOf("SKU-1")).isEqualTo(5)`, which reads as
  a sentence; `placeFailsWhenNotEnoughInventory` keeps
  `assertEquals(3, inventory.quantityOf("SKU-1"))` so you can see the
  `expected, actual` positional order AssertJ removes.

## Summary

None of this changes what's being tested — `Order` and `Inventory` are
identical to the starter. What changes is how the tests are built: fixtures
built per-test instead of shared, and boundary cases expressed as data rows
instead of one method per case, without losing the ability to tell which
row failed.
