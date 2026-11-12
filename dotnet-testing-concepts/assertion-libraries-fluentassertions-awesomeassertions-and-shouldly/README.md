# Assertion Libraries: MSTest 4 Assert, AwesomeAssertions and Shouldly

## Goal

`src/Billing` is a small invoicing model: an `Invoice` with lines, a total, a rounded discount, a summary
string, a `Pay()` that rejects a second payment, and a `Customer` record. The code is finished. Your job is to
write the same six checks twice, once with the assertions that ship with MSTest 4 and once with
AwesomeAssertions, and then to read the failure messages the libraries print for the same broken check.

## Prerequisites

- MSTest Fundamentals: Lifecycle and TestContext
- .NET 10 SDK (10.0.100 or later). The lab was also run on the .NET 11 SDK (11.0.100).

## Task

Run `dotnet test` once before you change anything. All 17 tests fail with a `TODO-NN` message. `global.json`
selects the Microsoft.Testing.Platform mode of `dotnet test`, and `Samples.cs` builds the invoice every test
looks at (INV-001, Ada Lovelace, two lines, 25.48 EUR). The test project already references
`AwesomeAssertions` 9.6.0 and `Shouldly` 4.3.0, and uses `MSTest.Sdk` 4.5.3.

## Instructions

Complete the following TODOs in `tests/Billing.Tests`:

- TODO-01: `MsTestAssertTests`: the six checks with the `Assert` class and nothing else.
- TODO-02: `AwesomeAssertionsTests`: the same six checks with `Should()`.
- TODO-03: `FailureMessageTests`: break a check on purpose with each library, catch the exception and assert
  on the message. Look at what each library prints before you decide what to pin down.

Run the tests until they all pass.

## Running the Lab

From the lab directory:

```bash
dotnet test
```

## Bonus (Optional)

- TODO-04 (optional): Add `ShouldlyTests.cs` with the same six checks written with Shouldly. Then try
  `_invoice.Summary().ShouldContain("ada lovelace")` and see whether it passes. Find the overload that makes it
  fail.
- TODO-05 (optional): Count the lines of each library for the six checks. Which check is the longest in MSTest,
  and which one has no MSTest equivalent at all?
- TODO-06 (optional): Break the production code on purpose (change the rounding in `TotalWithDiscount`, drop the
  `ThrowIfNegativeOrZero` in `AddLine`) and compare what your MSTest version and your AwesomeAssertions version
  say about the same bug.
