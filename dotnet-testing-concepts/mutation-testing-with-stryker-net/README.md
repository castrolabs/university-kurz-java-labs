# Mutation Testing with Stryker.NET

## Goal

A shipping price calculator (`ShippingCalculator`: weight tiers, zones, express, free shipping from 100, a
label) is already fully implemented, and it comes with a test suite that passes and runs every line of it. Your
job is to find out what that suite does not check, using Stryker.NET, and to strengthen it until the mutants die:
read the report, see how each surviving mutant points to a weak test, write the test that kills it, and decide
what to do with the one mutant no test can kill.

## Prerequisites

- C# and MSTest basics
- .NET 10 SDK
- `microsoft-testing-platform-and-dotnet-test` helps: the test project runs on Microsoft.Testing.Platform

## Task

Read `src/Shipping/ShippingCalculator.cs`, then `tests/Shipping.Tests/ShippingCalculatorTests.cs`. The first six
tests pass and look reasonable: they all say "costs more than" or "is not empty". Four more test methods are
empty shells marked `[Ignore]` (Stryker needs a green suite to start, so a failing `Assert.Fail` would stop it).
Remove an `[Ignore]` when you write that test.

Stryker is a local .NET tool, declared in `.config/dotnet-tools.json` (version 5.1.0). Install it from the
manifest, never globally:

```bash
dotnet tool restore
```

The test project runs on Microsoft.Testing.Platform (`MSTest.Sdk` and the `global.json` next to the solution), so
Stryker needs its MTP runner. `tests/Shipping.Tests/stryker-config.json` already sets `"test-runner": "mtp"`
(on the command line it is `-t mtp`). Stryker's own default runner is `vstest`; try
`dotnet stryker --test-runner vstest` once and read what it says.

## Instructions

Run Stryker from the test project directory (it finds `stryker-config.json` there):

```bash
cd tests/Shipping.Tests
dotnet stryker
```

Complete the following TODOs:

- TODO-00: Run Stryker on the suite as it is. Write down the mutation score and how many mutants survived. Open
  `StrykerOutput/<date>/reports/mutation-report.html` and look at the surviving mutants line by line: for each
  one, what would a test have to do to fail?
- TODO-01: `TheQuoteFollowsTheTariff`: the exact price for every tier edge, zone and express.
- TODO-02: `TheWeightLimitsAreEnforced`: the limits, the `ParamName`, the `ActualValue` and the message.
- TODO-03: `FreeShippingIsDomesticAndFromOneHundred`: the free-shipping rule and its edge.
- TODO-04: `ALabelSaysTheServiceTheZoneAndTheWeight`: the whole label text.
- TODO-05: Run Stryker again. One mutant should still survive. Is it a missing test or an equivalent mutant (a
  change that cannot alter any result)? Explain it in a comment, and decide whether to leave it or to ignore
  it. If you reach for `// Stryker disable once`, check in the report how many mutants that one comment
  switched off.

Do not edit `src/Shipping`.

## Running the Lab

```bash
dotnet test                  # the suite, from the lab directory
cd tests/Shipping.Tests
dotnet stryker               # the mutation run
dotnet stryker --break-at 90 # exits with a non-zero code (2) when the score is below 90
```

The mutation run takes a few seconds here. `StrykerOutput/` is ignored by Git.

## Bonus (Optional)

- TODO-06 (optional): Make the score a gate, as a CI job would. Run `dotnet stryker --break-at 90` with the starter
  suite and with yours and compare the exit codes. The same value can live in `stryker-config.json` as
  `thresholds.break`; Stryker refuses a `break` above `low`, so check `low` too.
