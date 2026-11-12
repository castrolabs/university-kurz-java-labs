using Shop;

namespace Shop.XTests;

// MSTest: a static field plus [DoNotParallelize] on both classes. xUnit: a collection fixture, and the
// collection is also the unit of parallelism, so classes in the same collection never overlap.
public sealed class ReportStoreFixture
{
    public ReportStore Store { get; } = new();
}

[CollectionDefinition("report-store")]
public sealed class ReportStoreCollection : ICollectionFixture<ReportStoreFixture>;

[Collection("report-store")]
public sealed class ReportTests(ReportStoreFixture fixture)
{
    [Fact]
    public void AReportCanBeBuilt()
    {
        using var _ = fixture.Store.Use();
        Thread.Sleep(100);
    }

    [Fact]
    public void AnotherReportCanBeBuilt()
    {
        using var _ = fixture.Store.Use();
        Thread.Sleep(100);
    }
}

[Collection("report-store")]
public sealed class ArchiveTests(ReportStoreFixture fixture)
{
    [Fact]
    public void TheArchiveCanBeRead()
    {
        using var _ = fixture.Store.Use();
        Thread.Sleep(100);
    }
}
