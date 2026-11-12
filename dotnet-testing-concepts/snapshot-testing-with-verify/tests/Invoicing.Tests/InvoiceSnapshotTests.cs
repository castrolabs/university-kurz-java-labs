namespace Invoicing.Tests;

// [UsesVerify] gives the class what Verify needs from MSTest (the TestContext), and `partial` lets its source
// generator add to the class. It is already here.
[TestClass]
[UsesVerify]
public partial class InvoiceSnapshotTests
{
    private readonly InvoiceService _service = new();

    // TODO-00: Snapshot a whole invoice. Change the return type to Task and end with
    // `return Verify(invoice);`. The first run fails on purpose and writes
    // InvoiceSnapshotTests.TheInvoiceObjectIsTheSnapshot.received.txt next to this file. Read it line by line.
    // Look at Id and IssuedAt: Verify already replaced them. Look at Number: it is not scrubbed yet.
    // When you agree with the file, approve it: rename .received.txt to .verified.txt. Run again, and again.
    [TestMethod]
    public void TheInvoiceObjectIsTheSnapshot()
    {
        var invoice = _service.Create(Orders.Coffee());

        Assert.Fail("TODO-00: write this test.");
    }
}
