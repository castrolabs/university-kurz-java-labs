using Shop;

namespace Shop.XTests;

// TODO-03: Port tests/Shop.MsTests/ReportTests.cs. The MSTest suite shares one ReportStore through a static field
// and keeps the two classes from overlapping with [DoNotParallelize]. In xUnit the store lives in a collection
// fixture, and the collection is also what keeps the classes apart:
//   - a fixture class that builds the ReportStore (ReportStoreFixture, below)
//   - a [CollectionDefinition("report-store")] class that implements ICollectionFixture<ReportStoreFixture>
//   - [Collection("report-store")] on both test classes, and the fixture in their constructors
// Then remove the [Collection] attribute from one class, build, and read the warnings and the failure.
public sealed class ReportStoreFixture
{
}

public sealed class ReportTests
{
    [Fact]
    public void AReportCanBeBuilt()
    {
        Assert.Fail("TODO-03: port this test.");
    }

    [Fact]
    public void AnotherReportCanBeBuilt()
    {
        Assert.Fail("TODO-03: port this test.");
    }
}

public sealed class ArchiveTests
{
    [Fact]
    public void TheArchiveCanBeRead()
    {
        Assert.Fail("TODO-03: port this test.");
    }
}
