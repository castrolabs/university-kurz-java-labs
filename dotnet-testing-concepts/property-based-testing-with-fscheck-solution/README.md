# Property-Based Testing with FsCheck in MSTest 4 (Solution)

Official solution for the `property-based-testing-with-fscheck` lab. Measured with `MSTest` 4.5.3 and `FsCheck`
3.6.0 on SDK 10.0.100 and the `net10.0` project: 13 tests pass (5 examples and 8 properties, data rows counted).

## The bug

The starting `Split` did `cents / parts` and `cents % parts`. In C# the division truncates toward zero and `%`
takes the sign of the dividend, so for a negative amount the remainder is negative, the loop `i < remainder`
never runs, and the extra cents are lost. FsCheck finds it in a few runs and shrinks it to:

```text
Falsifiable, after 6 tests (5 shrinks) ...
Original:
-7L
4
Shrunk:
-1L
2
```

`Split(-1, 2)` returned `[0, 0]`, which adds up to 0. The four example tests used only positive amounts, so they
could not see it. The fix is to use the floored remainder, `((cents % parts) + parts) % parts`, and to hand out
the extra cents to the first shares.

## What each test shows

- `SharesAddUpToTheAmount`, `ThereIsOneShareForEachPart`, `NoShareIsMoreThanOneCentAwayFromAnother`,
  `TheLargerSharesComeFirst`: the properties of the result.
- `Parts` is `Arb.From(Gen.Choose(1, 20), parts => Enumerable.Range(1, parts - 1))`. With `Gen.Choose(...)
  .ToArbitrary()` alone (for the amounts too, `Gen.Choose(-100_000, 100_000)`), the same failing property
  reported `0 shrinks` and an input like `-40509L` and `4`.
- `WithoutAtLeastOnePartItThrows`: there is no `Prop.Throws` in the C# API, so the property catches the
  exception and returns a bool.
- `TheSameSeedGeneratesTheSameInputs`: `WithReplay(seed1, seed2, size)` replays a run.
- `ARefundIsSplitToo`: the shrunk counterexample kept as a plain `[DataRow]` example, so the regression has a
  name and a one-line failure even if the generators change.

Amounts near `long.MinValue` and `long.MaxValue` are out of scope: `cents - remainder` could overflow there.
The default `long` generator at the default size stays far from them.
