namespace Invoicing.Tests;

// A snapshot records what the code did on one input. A rule that has to hold for every input is an assertion.
[TestClass]
public sealed class InvariantTests
{
    private readonly InvoiceService _service = new();

    [TestMethod]
    [DataRow(1, 1)]
    [DataRow(3, 50)]
    [DataRow(7, 1999)]
    [DataRow(1000, 7)]
    public void TheTotalIsTheSubtotalPlusTheTax(int quantity, int priceInCents)
    {
        var invoice = _service.Create(new Order("Ada", [new OrderLine("Item", quantity, priceInCents / 100m)]));

        Assert.AreEqual(invoice.Subtotal + invoice.Tax, invoice.Total);
    }

    [TestMethod]
    public void TheSubtotalIsTheSumOfTheLines()
    {
        var invoice = _service.Create(Orders.Coffee());

        Assert.AreEqual(invoice.Lines.Sum(line => line.Total), invoice.Subtotal);
    }

    [TestMethod]
    public void TheTaxIsRoundedAwayFromZeroToTheCent()
    {
        var invoice = _service.Create(Orders.Coffee());

        Assert.AreEqual(5.50m, invoice.Subtotal);
        Assert.AreEqual(1.05m, invoice.Tax);
    }
}
