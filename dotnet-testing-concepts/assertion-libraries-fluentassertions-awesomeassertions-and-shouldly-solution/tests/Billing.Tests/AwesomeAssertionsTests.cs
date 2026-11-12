using AwesomeAssertions;
using Billing;

namespace Billing.Tests;

// The same six checks, written with AwesomeAssertions.
[TestClass]
public sealed class AwesomeAssertionsTests
{
    private readonly Invoice _invoice = Samples.Invoice();

    [TestMethod]
    public void TheTotalIsTheSumOfTheLines()
    {
        _invoice.Total.Should().Be(25.48m);
    }

    [TestMethod]
    public void TheDiscountIsRoundedToCents()
    {
        _invoice.TotalWithDiscount(10m).Should().BeApproximately(22.93m, 0.005m);
    }

    [TestMethod]
    public void TheSummaryNamesTheInvoiceAndTheCustomer()
    {
        _invoice.Summary().Should()
            .StartWith("INV-001")
            .And.Contain("Ada Lovelace")
            .And.EndWithEquivalentOf("eur");
    }

    [TestMethod]
    public void TheLinesAreInOrderAndAllHaveStock()
    {
        _invoice.Lines.Should().HaveCount(2)
            .And.OnlyContain(l => l.Quantity > 0);
        _invoice.Lines.Select(l => l.Sku).Should().Equal("A-100", "B-200");
    }

    [TestMethod]
    public void PayingTwiceAndAddingNothingAreRejected()
    {
        _invoice.Pay();
        _invoice.Invoking(i => i.Pay()).Should().ThrowExactly<InvalidOperationException>()
            .WithMessage("Invoice INV-001 is already paid.");

        _invoice.Invoking(i => i.AddLine("C-300", 0, 1m)).Should().ThrowExactly<ArgumentOutOfRangeException>()
            .WithParameterName("quantity");
    }

    [TestMethod]
    public void TheCustomerMatchesIgnoringTheEmail()
    {
        var expected = new Customer("Ada Lovelace", "someone.else@example.com", "UK");

        _invoice.Customer.Should().BeEquivalentTo(expected, o => o.Excluding(c => c.Email));
    }
}
