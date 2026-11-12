# Microsoft.Testing.Platform, dotnet test and Code Coverage in CI (Solution)

Official solution for the `microsoft-testing-platform-and-dotnet-test` lab.

## What changed from the starter

- **The runner.** `tests/Checkout.Tests/Checkout.Tests.csproj` uses `Sdk="MSTest.Sdk/4.5.2"`, which brings
  MSTest, the analyzers and the MTP runner, and whose `Default` extension profile adds Microsoft Code
  Coverage and the TRX report. `global.json` selects the MTP mode of `dotnet test` (.NET 10 SDK and later).
  With only the `global.json` and the old project, `dotnet test` stops with "global.json defines test runner
  to be Microsoft.Testing.Platform. All projects must use that test runner." Keeping
  `Microsoft.NET.Sdk` plus the `MSTest` package also works if you add `<EnableMSTestRunner>true</EnableMSTestRunner>`
  and `<OutputType>Exe</OutputType>`; the metapackage already references the coverage and TRX extensions.
- **`RunnerTests`** reads `Assembly.GetEntryAssembly()`. Under VSTest it is `testhost`; under MTP the test
  project is the executable, so it is `Checkout.Tests`.
- **`ValidationTests`** name the exact exception type: `Assert.ThrowsExactly<ArgumentException>` for an
  empty order and `ArgumentOutOfRangeException` for a bad quantity. The bad line is second, so the loop has
  to reach it.
- **`DiscountTests`** use `[DataRow]` with `double` (attribute arguments cannot be `decimal`) and convert
  in the test. Both sides of every limit are tested: `WELCOME10` at 50 and at 49.99.
- **`ShippingTests`** exist because the discount tests already execute both shipping branches. Without them
  the coverage gate passes and nothing checks the shipping.
- **Categories** on every class, so `--filter "TestCategory=Shipping"` runs one slice.

## Bonus notes

- `./coverage.sh` on the full suite: 20 tests, 93.9% lines, 100% branches (30 of 30), `PASSED`.
- Why the line limit is 90% and not 100%: lines 17 and 18 (`const decimal FreeShippingThreshold` and
  `ShippingFee`) are reported as uncovered. A `const decimal` is compiled into a static field set by a
  static constructor, while every use of the constant is inlined, so the static constructor never runs and
  no test can cover those lines.
- `./coverage.sh --filter "TestCategory!=Shipping&TestCategory!=Theater"`: 15 tests, 93.9% lines, 100%
  branches, `PASSED`, and the first two mutations below survive.
- `./coverage.sh --filter "TestCategory=Theater"`: 1 test with no assertion, 93.9% lines, 100% branches,
  `PASSED`, and every mutation below survives.
- `dotnet test --filter "TestCategory=Shiping"` exits with 8 on MTP. On VSTest the same typo printed a
  warning and exited with 0.
- Mutation check, run against this solution. Each change to `src/Checkout/PriceCalculator.cs` was tried on
  its own, then reverted:

| Change to the production code | Tests that fail (full suite) | Theater test alone |
| --- | --- | --- |
| `discounted >= FreeShippingThreshold` becomes `>` | `AnOrderOfExactlyTheThresholdShipsFree` | green |
| `\|\| tier == Tier.Gold` deleted | `GoldShipsFreeEvenBelowTheThreshold` | green |
| `Math.Min` cap removed | `TheDiscountNeverExceedsTheSubtotal` | green |
| `.ToUpperInvariant()` removed from the coupon | `ACouponIgnoresCaseAndSurroundingSpaces` | green |
| `WELCOME10` limit `>= 50m` becomes `> 50m` | `Welcome10NeedsASubtotalOfAtLeast50` (row 50) | green |
| Silver rate `0.05m` becomes `0.10m` | `TheTierDiscountDependsOnTheTier` (Silver row), `TheTierDiscountAndTheCouponAddUp` | green |
| `line.Quantity <= 0` becomes `< 0` | `ALineWithoutAPositiveQuantityIsRejected` (row 0) | green |

Coverage says which code ran. Only the mutations say which code is checked.
