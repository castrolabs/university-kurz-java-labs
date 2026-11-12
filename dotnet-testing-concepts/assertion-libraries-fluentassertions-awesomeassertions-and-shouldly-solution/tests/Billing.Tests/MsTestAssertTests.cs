using Billing;

namespace Billing.Tests;

// The six checks, written with the assertions that ship with MSTest 4 and nothing else.
[TestClass]
public sealed class MsTestAssertTests
{
    private readonly Invoice _invoice = Samples.Invoice();

    [TestMethod]
    public void TheTotalIsTheSumOfTheLines()
    {
        Assert.AreEqual(25.48m, _invoice.Total);
    }

    [TestMethod]
    public void TheDiscountIsRoundedToCents()
    {
        Assert.AreEqual(22.93m, _invoice.TotalWithDiscount(10m), 0.005m);
    }

    [TestMethod]
    public void TheSummaryNamesTheInvoiceAndTheCustomer()
    {
        var summary = _invoice.Summary();

        Assert.StartsWith("INV-001", summary);
        Assert.Contains("Ada Lovelace", summary);
        Assert.IsTrue(summary.EndsWith("eur", StringComparison.OrdinalIgnoreCase), summary);
    }

    [TestMethod]
    public void TheLinesAreInOrderAndAllHaveStock()
    {
        Assert.HasCount(2, _invoice.Lines);
        Assert.AreSequenceEqual(["A-100", "B-200"], _invoice.Lines.Select(l => l.Sku));
        Assert.IsTrue(_invoice.Lines.All(l => l.Quantity > 0), "every line needs a positive quantity");
    }

    [TestMethod]
    public void PayingTwiceAndAddingNothingAreRejected()
    {
        _invoice.Pay();
        var paid = Assert.ThrowsExactly<InvalidOperationException>(() => _invoice.Pay());
        Assert.AreEqual("Invoice INV-001 is already paid.", paid.Message);

        var empty = Assert.ThrowsExactly<ArgumentOutOfRangeException>(() => _invoice.AddLine("C-300", 0, 1m));
        Assert.AreEqual("quantity", empty.ParamName);
    }

    [TestMethod]
    public void TheCustomerMatchesIgnoringTheEmail()
    {
        var expected = new Customer("Ada Lovelace", "someone.else@example.com", "UK");

        Assert.AreEqual(expected.Name, _invoice.Customer.Name);
        Assert.AreEqual(expected.Country, _invoice.Customer.Country);
    }
}
