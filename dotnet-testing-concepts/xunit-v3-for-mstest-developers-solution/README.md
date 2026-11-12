# xUnit v3 for MSTest Developers: Porting a Suite (Solution)

Official solution for the `xunit-v3-for-mstest-developers` lab.

## What changed from the starter

- **`BasketTests`** is `[Fact]`/`[Theory]` with the constructor as the per-test setup: it takes the class
  fixture and the `ITestOutputHelper`, and builds the `Basket`. `[DataRow]` rows became `[InlineData]`, the
  tuple `[DynamicData]` became `[MemberData]` over a `TheoryData<string, decimal>`, `[TestCategory("Fast")]`
  became `[Trait("Category", "Fast")]` and `[Ignore]` became `Skip =`. `Assert.ThrowsExactly<T>` became
  `Assert.Throws<T>` (exact in xUnit), and MSTest's subclass-accepting `Assert.Throws<ArgumentException>`
  became `Assert.ThrowsAny<ArgumentException>`. The straight rename `Throws<ArgumentException>` fails with
  `Exception type was not an exact match`.
- **`CatalogFixture`** holds the `PriceCatalog` that MSTest built in a static `[ClassInitialize]`. xUnit builds it
  once per test class, before the first test, and passes it to the constructor.
  `TheCatalogIsBuiltOnceForTheWholeClass` fails if the catalog is built in the constructor instead.
- **`ReportStoreFixture`** is shared by `ReportTests` and `ArchiveTests` through a collection:
  `[CollectionDefinition("report-store")]` on a class that implements `ICollectionFixture<ReportStoreFixture>`,
  and `[Collection("report-store")]` on both test classes. MSTest got the same result with a static field and
  `[DoNotParallelize]`. Without the attribute on a class, xUnit does not run into the "busy" error: the build warns
  (`xUnit1041`, `xUnit1073`) and the test fails because the constructor parameter has no fixture. The MSTest
  suite, without `[DoNotParallelize]`, fails with `The report store is busy.`
- **`TheTestKnowsItsOwnNameAndItsCancellationToken`** writes with `ITestOutputHelper`, and reads the test name
  and the cancellation token from `TestContext.Current`. The name is the display name, which includes the
  class (`Shop.XTests.BasketTests.TheTestKnows...`), so the test checks that it contains the method name.
- A test that calls a method accepting a `CancellationToken` (for example `Task.Delay(1)`) gets the analyzer
  warning `xUnit1051`: pass `TestContext.Current.CancellationToken`.
