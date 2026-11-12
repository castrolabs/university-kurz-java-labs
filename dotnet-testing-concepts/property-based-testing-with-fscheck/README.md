# Property-Based Testing with FsCheck in MSTest 4

## Goal

`MoneySplitter.Split(cents, parts)` splits an invoice, or a refund, into shares that add up to the amount. It
comes with a small suite of example tests, and every one of them passes. Your job is to describe the method with
properties instead of examples, let FsCheck find the input nobody thought of, read the shrunk counterexample and
the seed, fix the bug, and keep what you learned as an example.

## Prerequisites

- C# and MSTest basics
- `mstest-data-driven-tests` helps: properties and `[DataRow]` rows solve different problems
- .NET 10 SDK

## Task

Read `src/Billing/MoneySplitter.cs`, then `tests/Billing.Tests/SplitExampleTests.cs` (four examples, all with
positive amounts, all green). Then open `tests/Billing.Tests/SplitPropertyTests.cs`: every method fails with a
`TODO-NN` message until you write it. The tests use `FsCheck` 3.6.0 inside ordinary `[TestMethod]`s:

```csharp
Prop.ForAll(Amounts, Parts, (cents, parts) => /* a bool */).QuickCheckThrowOnFailure();
```

There is no stable FsCheck package for MSTest 4. The community `AndreasDorfer.FsCheck.MSTest` 3.0.0 targets
MSTest 4.0.2 and, with MSTest 4.5.3, the adapter rejects its `[Property]` methods at discovery
(`UTA007`). Calling the API directly always works.

## Instructions

Complete the following TODOs in `tests/Billing.Tests/SplitPropertyTests.cs`:

- TODO-00: `SharesAddUpToTheAmount`. Write the property and run it. It fails: write down the `Original` and the
  `Shrunk` values and the seed that FsCheck prints. Then fix `MoneySplitter.Split` (the one place you may edit
  in `src/`) and run until it passes.
- TODO-01: the three other properties of `Split`: one share for each part, shares at most one cent apart, the
  larger shares first.
- TODO-02: replace the `Parts` generator with `Arb.From(Gen.Choose(1, 20), shrinker)`. Compare a failing run
  with and without the shrinker (the second argument of `Arb.From`).
- TODO-03: fewer than one part throws, for any amount.
- TODO-04: the same seed and size generate the same inputs.
- TODO-05: the shrunk counterexample, and a second refund, as `[DataRow]` examples.

## Running the Lab

```bash
dotnet test
```

Each run uses new random inputs: run a property a few times before you trust it. The failure message carries
`Replay directly at failing step with (seed1, seed2, size)`; pass those three values to
`Config.QuickThrowOnFailure.WithReplay(seed1, seed2, size)` to run the same failing input again.

## Bonus (Optional)

- TODO-06 (optional): `Classify(cents < 0, "refund")` on a property, and run it with `Config.Quick` and
  `.Check(...)` to see the percentage of refunds in the 100 runs. Is the distribution what you expected?
