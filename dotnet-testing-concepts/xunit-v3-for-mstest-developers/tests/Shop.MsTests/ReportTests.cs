using Shop;

namespace Shop.MsTests;

// One ReportStore for the whole assembly. Two classes use it, so the classes must not run at the same time.
internal static class SharedReportStore
{
    public static readonly ReportStore Instance = new();
}

[TestClass]
[DoNotParallelize]
public sealed class ReportTests
{
    [TestMethod]
    public void AReportCanBeBuilt()
    {
        using var _ = SharedReportStore.Instance.Use();
        Thread.Sleep(100);
    }

    [TestMethod]
    public void AnotherReportCanBeBuilt()
    {
        using var _ = SharedReportStore.Instance.Use();
        Thread.Sleep(100);
    }
}

[TestClass]
[DoNotParallelize]
public sealed class ArchiveTests
{
    [TestMethod]
    public void TheArchiveCanBeRead()
    {
        using var _ = SharedReportStore.Instance.Use();
        Thread.Sleep(100);
    }
}
