# Unit Testing Anti-Patterns — Solution

## Overview

Three `@Nested` groups, each a small, complete demonstration of avoiding
one specific anti-pattern from Chapter 11, plus a bonus test tying two of
them together.

## Key Concepts

- **`discountRateReflectsUpgrade` never touches `Membership.level`.**
  `Membership` has no `getLevel()` and this test doesn't add one — it
  asserts `discountRate()` before and after `upgrade()`. The production
  code never reads `level` from outside the class either; only
  `discountRate()` does, and that's already public. If a future caller
  genuinely needed the raw level, exposing it then would be adding real
  observable behavior — not a testing back door added early.
- **`calculateReturnsExpectedTax`'s `@CsvSource` values were computed by
  hand, not by calling `TaxCalculator`'s own formula.** `25.50` at 8% is
  `2.04` — worked out independently of `subtotal.multiply(new
  BigDecimal("0.08"))`. If `TaxCalculator.calculate(...)` had a bug in the
  rate or the rounding, a test that recomputed the same expression would
  have the identical bug in its "expected" value and pass anyway; this one
  has no such blind spot.
- **`renewRecordsTheFixedInstant` never calls `Instant.now()`.** Both the
  "act" (`membership.renew(fixedClock)`) and the "assert" use the *same*
  `fixedInstant` — deterministic because `Membership.renew(Clock)` takes
  the clock as a parameter instead of reading
  `Clock.systemDefaultZone()` internally. A production `@Bean` would wire
  the real system clock; only the test wires a fixed one.
- **The bonus test uses a real `TaxCalculator`, not a mock.**
  `TaxCalculator.calculate(...)` is a pure function with no I/O — there's
  nothing a mock would remove from the test except realism. Mocking is
  reserved for collaborators expensive, slow, or nondeterministic enough
  that using the real thing would hurt the test; a stateless calculation
  doesn't qualify.

## Summary

None of these four tests needed reflection, a widened-visibility field, a
recomputed formula, or an ambient clock call to pass — each anti-pattern
had a direct fix that used only what the class already exposes publicly
and deterministically.
