using Billing;
using Shouldly;

namespace Billing.Tests;

// Optional bonus: the same six checks, written with Shouldly.
[TestClass]
public sealed class ShouldlyTests
{
    private readonly Invoice _invoice = Samples.Invoice();

    [TestMethod]
    public void TheTotalIsTheSumOfTheLines()
    {
        _invoice.Total.ShouldBe(25.48m);
    }

    [TestMethod]
    public void TheDiscountIsRoundedToCents()
    {
        _invoice.TotalWithDiscount(10m).ShouldBe(22.93m, 0.005m);
    }

    [TestMethod]
    public void TheSummaryNamesTheInvoiceAndTheCustomer()
    {
        var summary = _invoice.Summary();

        summary.ShouldStartWith("INV-001");
        summary.ShouldContain("Ada Lovelace");
        summary.ShouldEndWith("eur", Case.Insensitive);
    }

    [TestMethod]
    public void TheLinesAreInOrderAndAllHaveStock()
    {
        _invoice.Lines.Count.ShouldBe(2);
        _invoice.Lines.Select(l => l.Sku).ShouldBe(["A-100", "B-200"]);
        _invoice.Lines.ShouldAllBe(l => l.Quantity > 0);
    }

    [TestMethod]
    public void PayingTwiceAndAddingNothingAreRejected()
    {
        _invoice.Pay();
        Should.Throw<InvalidOperationException>(() => _invoice.Pay())
            .Message.ShouldBe("Invoice INV-001 is already paid.");

        Should.Throw<ArgumentOutOfRangeException>(() => _invoice.AddLine("C-300", 0, 1m))
            .ParamName.ShouldBe("quantity");
    }

    [TestMethod]
    public void TheCustomerMatchesIgnoringTheEmail()
    {
        var expected = new Customer("Ada Lovelace", "someone.else@example.com", "UK");

        _invoice.Customer.Name.ShouldBe(expected.Name);
        _invoice.Customer.Country.ShouldBe(expected.Country);
    }
}
