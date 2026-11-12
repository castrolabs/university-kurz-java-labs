namespace Invoicing.Tests;

[TestClass]
[UsesVerify]
public partial class InvoiceSnapshotTests
{
    private readonly InvoiceService _service = new();

    [TestMethod]
    public Task TheInvoiceObjectIsTheSnapshot()
    {
        var invoice = _service.Create(Orders.Coffee());

        return Verify(invoice);
    }
}
