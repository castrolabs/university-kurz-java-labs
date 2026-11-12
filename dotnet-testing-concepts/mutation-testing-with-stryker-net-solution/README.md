# Mutation Testing with Stryker.NET (Solution)

Official solution for the `mutation-testing-with-stryker-net` lab. Measured with Stryker.NET 5.1.0 (a local tool,
`.config/dotnet-tools.json`), `MSTest.Sdk` 4.5.3 and Microsoft.Testing.Platform, on SDK 10.0.100 and the
`net10.0` project.

## Score before and after

| | Starter suite | This solution |
| --- | --- | --- |
| Tests | 6 passing (4 `[Ignore]`d shells) | 25 passing (data rows counted) |
| Mutants created | 40 | 40 |
| Not tested (compile error, filtered) | 1 `CompileError`, 3 `Ignored` | 1 `CompileError`, 3 `Ignored` |
| Not covered by any test | 2 | 0 |
| Killed | 14 | 35 |
| Survived | 20 | 1 |
| **Mutation score** | **38.89 %** | **97.22 %** |
| `dotnet stryker --break-at 90` | exit code 2 | exit code 0 |
| Time of the Stryker run | 5 to 6 s | 5 to 6 s |

The starter suite already ran every line of `ShippingCalculator`. Line coverage said "done"; the mutation score
said 39 %.

## What changed from the starter

Only `tests/Shipping.Tests/ShippingCalculatorTests.cs`. `src/Shipping` is untouched.

- **`TheQuoteFollowsTheTariff`** (`[DynamicData]`, 16 rows) gives the exact price of every tier edge and of one
  step past it, each zone and express. It kills the tier mutants (`<=` to `<` or `>`, conditions forced to `true`
  or `false`), the `Math.Ceiling(weightKg - 10) * 0.80m` arithmetic, the `price *= 1.5m` for express that no
  test reached (a `NoCoverage` mutant) and the rounding row (`4.90 x 1.5 x 1.5 = 11.025`, so
  `MidpointRounding.AwayFromZero` is 11.03).
- **The weight limits** are three tests: `0` and `-1` with `ParamName`, `ActualValue` and a piece of the message
  (which kills the string mutant that replaces the message by `""`), and `30.01`. Exactly 30 kg is a row of the
  tariff test, so a `> MaxWeightKg` that becomes `>=` fails there.
- **Free shipping**: 100 is free (also with express), 99.99 pays, a European order of 150 pays.
- **The label**: the whole text for three parcels, which kills the `STD`/`EXP` conditional mutants and the string
  mutants.

## What survived in the starter and which test kills it

| Surviving mutant | Weak test | Test that kills it |
| --- | --- | --- |
| `WeightKg <= 0` to `< 0` | `ANegativeWeightIsRejected` only tries -1 | the 0 row of `AWeightOfZeroOrLessIsRejected` |
| `WeightKg > MaxWeightKg` to `>=` | nothing tries 30 | the 30 row of `TheQuoteFollowsTheTariff` |
| the message replaced by `""` | the test never reads the message | `StringAssert.Contains(error.Message, ...)` |
| `OrderValue >= 100` to `> 100` | `AnExpensiveOrderShipsForLess` uses 150 | `ADomesticOrderOfOneHundredShipsForFree` |
| `if (parcel.Express)` negated | no express parcel in the suite | the express rows of the tariff test |
| tier conditions (`<= 1`, `<= 5`) | "heavy costs more than light" | the edge rows (1, 1.01, 5, 5.01) |
| `Math.Ceiling(weightKg - 10) * 0.80m` | only 20 kg, only "more than" | 10.01, 11, 11.01, 20, 30 kg with exact prices |
| `STD` / `EXP` conditional | `ALabelIsNotEmpty` | `ALabelSaysTheServiceTheZoneAndTheWeight` |

## The mutant that still survives

`weightKg <= 10` to `weightKg < 10` in `BasePrice`. At exactly 10 kg the third tier gives 12.90 and the
fallback `12.90m + Math.Ceiling(10 - 10) * 0.80m` gives 12.90 too, so no test can tell the two versions apart:
it is an equivalent mutant, not a missing test. The 97.22 % is 35 killed of 36 tested.

Two honest options: leave it and set the break threshold at 95 (the lab's `thresholds.high`), or silence it. The
silent option has a catch, measured here: `// Stryker disable once Equality` placed above `BasePrice` (an
expression-bodied method, so "once" covers the whole body) raised `Ignored` from 3 to 9 and gave a score of
100 %, because it also switched off 5 other equality mutants that tests do kill. A number of 100 % that hides
real mutants is worse than 97 %. To disable only the equivalent mutant, the condition would have to be its own
statement first. The solution leaves `src/Shipping` as it is.

## The test runner

`stryker-config.json` has `"test-runner": "mtp"`. Stryker 5.1.0 prints
`The Microsoft Test Platform testrunner is currently in preview. Results should be verified`. With the default
runner, `dotnet stryker --test-runner vstest` on this project fails with
`Could not find testhost for test source ...Shipping.Tests.dll` and `Test discovery was aborted`, and exits with 1.
Stryker 5.1.0 itself targets `net10.0`.
