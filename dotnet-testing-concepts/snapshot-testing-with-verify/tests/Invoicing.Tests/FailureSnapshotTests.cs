namespace Invoicing.Tests;

[TestClass]
[UsesVerify]
public partial class FailureSnapshotTests
{
    // TODO-03: Snapshot the exception of an invoice with no lines. Verify has `Throws(() => ...)`, which returns
    // a task you can pass to the verification: the snapshot shows the exception type, the message and
    // ParamName. Its first run also writes the stack trace, which changes whenever a line moves: add
    // .IgnoreStackTrace() and compare the two .received files.
    [TestMethod]
    public void AnEmptyOrderIsRejected()
    {
        Assert.Fail("TODO-03: write this test.");
    }
}
