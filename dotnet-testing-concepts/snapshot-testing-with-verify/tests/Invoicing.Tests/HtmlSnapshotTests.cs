namespace Invoicing.Tests;

[TestClass]
[UsesVerify]
public partial class HtmlSnapshotTests
{
    private readonly InvoiceService _service = new();

    // TODO-02: Snapshot the rendered HTML as a file of its own: Verify(html, "html") writes .received.html and
    // .verified.html, which an editor shows as HTML. The date line is text, not a DateTimeOffset, so Verify does
    // not scrub it: add .ScrubInlineDateTimeOffsets("yyyy-MM-dd HH:mm:ss") to the call. The Number is already
    // handled by your TODO-01 scrubber, because it works on every snapshot.
    [TestMethod]
    public void TheRenderedInvoiceIsAnHtmlFile()
    {
        var invoice = _service.Create(Orders.Coffee());

        Assert.Fail("TODO-02: write this test.");
    }

    // TODO-02: The same for the order with markup in the customer name and the product. Read the .received.html
    // before approving it: are the characters escaped?
    [TestMethod]
    public void MarkupInNamesIsEscaped()
    {
        var invoice = _service.Create(Orders.WithMarkup());

        Assert.Fail("TODO-02: write this test.");
    }
}
