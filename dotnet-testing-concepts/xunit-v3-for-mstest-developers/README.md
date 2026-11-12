# xUnit v3 for MSTest Developers: Porting a Suite

## Goal

`src/Shop` is a small shop: a `PriceCatalog`, a `Basket` and a single-writer `ReportStore`. The code is finished,
and `tests/Shop.MsTests` is a finished MSTest 4 suite for it. Your job is to port that suite to xUnit v3 in
`tests/Shop.XTests`, keeping the test names, and to find out what the straight rename gets wrong: where the
setup goes, how a class shares an expensive object, how two classes share a single-writer resource without
overlapping, and which `Assert.Throws` accepts a subclass.

## Prerequisites

- MSTest Fundamentals: Lifecycle and TestContext
- Choosing a Test Framework: MSTest, xUnit, NUnit and TUnit
- .NET 10 SDK (10.0.100 or later). The lab was also run on the .NET 11 SDK (11.0.100).

## Task

Run `dotnet test` once before you change anything. The MSTest suite has 13 passing tests and one skipped on purpose. In
`Shop.XTests` every test is a stub that fails with a `TODO-NN` message, except the skipped one. `global.json`
selects the Microsoft.Testing.Platform mode of `dotnet test`, which is how both projects run in one command.
`Shop.XTests` references `xunit.v3` 4.0.3 and sets `UseMicrosoftTestingPlatformRunner`; `Shop.MsTests` uses
`MSTest.Sdk` 4.5.3.

Read `tests/Shop.MsTests` first, then port file by file.

## Instructions

Complete the following TODOs in `tests/Shop.XTests`:

- TODO-01: `BasketTests`: the attribute, assertion and exception mapping for every test: `[Fact]`, `[Theory]`
  with `[InlineData]` and `[MemberData]`/`TheoryData`, `Assert.Throws` versus `Assert.ThrowsAny`, `Skip`, `Trait`,
  and the constructor as the per-test setup.
- TODO-02: `CatalogFixture` and `IClassFixture<T>`, so the catalog is built once per class and
  `TheCatalogIsBuiltOnceForTheWholeClass` passes.
- TODO-03: `ReportStoreFixture`, a `[CollectionDefinition]` and `[Collection]` on both report classes, so they
  share the store and never run at the same time.
- TODO-04: `TheTestKnowsItsOwnNameAndItsCancellationToken`: `ITestOutputHelper` and `TestContext.Current`.

Run the tests until they all pass.

## Running the Lab

From the lab directory:

```bash
dotnet test
```

One project at a time, and only the fast ones:

```bash
dotnet test --project tests/Shop.XTests
dotnet test --project tests/Shop.XTests --filter-trait "Category=Fast"
dotnet test --project tests/Shop.MsTests --filter "TestCategory=Fast"
```

To see what a test wrote to `ITestOutputHelper` or `TestContext.WriteLine` even when it passes:

```bash
dotnet test --project tests/Shop.XTests --output Detailed
```

## Bonus (Optional)

- TODO-05 (optional): Remove `[Collection("report-store")]` from one report class and build. Read the warnings
  and the failure. Then remove the `[DoNotParallelize]` attributes in the MSTest suite and run it a few
  times. What do the two frameworks say, and which one tells you at build time?
- TODO-06 (optional): Use `Assert.Throws<ArgumentException>` in `AnUnknownProductIsAnArgumentException` and read
  the failure message before you switch to `ThrowsAny`.
- TODO-07 (optional): Time the three report tests in the MSTest suite and in the xUnit suite. Then give each
  xUnit report class its own `IClassFixture<ReportStoreFixture>` and no collection, and time them again.
