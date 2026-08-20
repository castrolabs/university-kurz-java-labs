# Four Pillars of a Good Unit Test — Solution

## Overview

Five tests against the same (already-implemented) `Invoice`, each chosen to
make one of the article's four pillars concrete rather than abstract.

## Key Concepts

- **Protection against regressions needs an independently-derived
  expectation.** `totalAppliesBulkDiscountAboveThreshold` asserts against
  `new BigDecimal("108.00")` — a value worked out by hand (120.00 minus a
  10% discount), not by calling `Invoice`'s own discount formula inside the
  test. If `total()`'s discount math had a bug, a test that recomputed the
  same formula would have the identical bug baked into its "expected" value
  and would still pass; the hardcoded value has no such blind spot.
- **`totalHasNoDiscountJustBelowThreshold` and
  `totalSumsMultipleLineItemsAtExactBoundary` pin down the same boundary
  from both sides** — 99.99 (no discount) and exactly 100.00 (discount
  applies, since the threshold check is `>=`). Between them and the
  above-threshold case, all three sides of the boundary are covered.
- **Resistance to refactoring is a property of what the test touches, not
  just what it asserts.** `totalSumsMultipleLineItemsAtExactBoundary` only
  ever calls `invoice.total()`. `Invoice.computeSubtotal()` could be
  renamed, inlined, or rewritten to use a different reduction entirely, and
  this test would not need to change — because it was never coupled to
  that implementation detail in the first place.
- **Maintainability shows up as how little a test costs to keep working.**
  `totalOfEmptyInvoiceIsZero` needs zero fixture beyond `List.of()`. As
  `Invoice` grows more rules elsewhere, this test's cost to read and
  maintain stays flat — it has nothing to update.
- **The bonus test makes the trade-off visible, not just assertable.**
  `itemCountReflectsLineItemCount` is fast and essentially never gives a
  false positive — but `itemCount()` is a one-line delegation to
  `items.size()`, so there's nowhere for a real bug to hide. Compare its
  near-zero protection against `totalAppliesBulkDiscountAboveThreshold`,
  which exercises real discount logic.

## Summary

None of these tests are wrong to write — the point is that "does this pass"
was never the interesting question. Each one earns a specific pillar
through a specific choice: a hand-computed expectation, an assertion that
never leaves the public API, or a fixture with nothing to maintain. A test
suite built only from tests like the bonus one would run fast forever
without ever catching a real bug.
