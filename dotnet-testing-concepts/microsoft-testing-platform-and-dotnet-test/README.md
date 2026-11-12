# Microsoft.Testing.Platform, dotnet test and Code Coverage in CI

## Goal

`src/Checkout` prices an order: a subtotal, a tier discount, at most one coupon, and shipping that is free
above a threshold or for Gold customers. The code is finished. Your job is to move the test project from
VSTest to Microsoft.Testing.Platform (MTP), write the tests, tag them so `--filter` can slice the suite,
and bring the coverage report over the limit that `coverage.sh` enforces.

The limit is the easy part. The lab is about noticing that the report can be green while the tests check
nothing.

## Prerequisites

- MSTest Fundamentals: Lifecycle and TestContext
- Basic `dotnet test`
- .NET 10 SDK (10.0.100 or later), and `bash` for `coverage.sh` (Git Bash or WSL on Windows)

## Task

Read `src/Checkout/PriceCalculator.cs` first. Then open `tests/Checkout.Tests`. Every test method is already
declared with a hint and fails with `TODO-NN: write this test` until you write it. `RunnerTests` is already
written: it checks the setup of the suite (the runner and the categories) and fails with a TODO message
until you fix the setup.

Run `dotnet test` once before you change anything. It runs on VSTest, the default, and 15 tests fail.

## Instructions

Complete the following TODOs:

- TODO-00: Move to MTP. In `tests/Checkout.Tests/Checkout.Tests.csproj`, replace `Sdk="Microsoft.NET.Sdk"`
  with `Sdk="MSTest.Sdk/4.5.2"` and delete the `MSTest` `PackageReference` and the `Using` item (MSTest.Sdk
  brings both). Then create `global.json` next to `CheckoutLab.slnx`:

  ```json
  {
    "test": {
      "runner": "Microsoft.Testing.Platform"
    }
  }
  ```

  Try the `global.json` alone first and read the error `dotnet test` prints. `TheSuiteRunsOnMicrosoftTestingPlatform`
  turns green when the test project is the process that runs the tests.
- TODO-01: `ValidationTests`: an empty order, a line with quantity 0 or -1 (`[DataRow]`), and the subtotal.
- TODO-02: `DiscountTests`: the tier discount per tier, both sides of the `WELCOME10` limit, case and spaces,
  an unknown coupon, tier plus coupon, and the cap that keeps the discount at or below the subtotal.
- TODO-03: `ShippingTests`: below the threshold, exactly at it, after the discount, and Gold.
- TODO-04: Tag each class with `[TestCategory("Validation")]`, `[TestCategory("Discounts")]` and
  `[TestCategory("Shipping")]` (`EveryTestClassDeclaresACategory` checks it). Then run one slice, and run a
  typo on purpose and look at the exit code:

  ```bash
  dotnet test --filter "TestCategory=Shipping"
  dotnet test --filter "TestCategory=Shiping"; echo "exit code: $?"
  ```

  On VSTest a filter that matches nothing exits with 0. On MTP it exits with 8 (zero tests ran).
- TODO-05: Run the coverage gate until it prints `PASSED`:

  ```bash
  ./coverage.sh
  ```

  It runs `dotnet test --coverage --coverage-output-format cobertura`, then ReportGenerator (a local tool,
  restored from `.config/dotnet-tools.json`) with a minimum of 90% lines and 100% branches for `src/Checkout`.
  Open `coverage-report/index.html` to see which lines and branches are missing.

Run `dotnet test` until every test passes, and `./coverage.sh` until it prints `PASSED`.

## Running the Lab

From the lab directory:

```bash
dotnet test
./coverage.sh
```

## Bonus (Optional)

- TODO-06 (optional): The test project is an executable now. Build once, then run it without `dotnet test`:
  `dotnet run --project tests/Checkout.Tests -- --filter "TestCategory=Discounts"` and
  `./tests/Checkout.Tests/bin/Debug/net10.0/Checkout.Tests --list-tests` (add `.exe` on Windows).
- TODO-07 (optional): Mutation check. Change `src/Checkout/PriceCalculator.cs` on purpose, one change at a
  time, and run `dotnet test`: change `discounted >= FreeShippingThreshold` to `>`, delete
  `|| tier == Tier.Gold`, remove the `Math.Min` cap, remove `.ToUpperInvariant()`. Each change must turn a
  test red. Before you fix anything, run `./coverage.sh --filter "TestCategory!=Shipping"`: does the gate
  still pass without a single shipping assertion?
- TODO-08 (optional): Write a test in a class tagged `[TestCategory("Theater")]` that calls `Calculate`
  with every tier, coupon and amount, swallows `ArgumentException`, and asserts nothing. Run
  `./coverage.sh --filter "TestCategory=Theater"` and repeat the mutations from TODO-07 against it.
