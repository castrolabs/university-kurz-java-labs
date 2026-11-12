namespace Invoicing.Tests;

[TestClass]
[UsesVerify]
public partial class FailureSnapshotTests
{
    [TestMethod]
    public Task AnEmptyOrderIsRejected() =>
        Throws(() => new InvoiceService().Create(new Order("Ada Lovelace", [])))
            .IgnoreStackTrace();
}
