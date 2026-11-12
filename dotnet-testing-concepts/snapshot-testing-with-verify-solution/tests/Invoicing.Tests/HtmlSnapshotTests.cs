namespace Invoicing.Tests;

[TestClass]
[UsesVerify]
public partial class HtmlSnapshotTests
{
    private readonly InvoiceService _service = new();

    [TestMethod]
    public Task TheRenderedInvoiceIsAnHtmlFile()
    {
        var invoice = _service.Create(Orders.Coffee());

        return Verify(InvoiceHtml.Render(invoice), "html")
            .ScrubInlineDateTimeOffsets("yyyy-MM-dd HH:mm:ss");
    }

    [TestMethod]
    public Task MarkupInNamesIsEscaped()
    {
        var invoice = _service.Create(Orders.WithMarkup());

        return Verify(InvoiceHtml.Render(invoice), "html")
            .ScrubInlineDateTimeOffsets("yyyy-MM-dd HH:mm:ss");
    }
}
