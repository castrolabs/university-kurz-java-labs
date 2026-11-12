using Billing;

namespace Billing.Tests;

// TODO-01: Write the six checks with the assertions that ship with MSTest 4 and nothing else.
// `_invoice` is the sample invoice from Samples.cs: INV-001 for Ada Lovelace, two lines, 25.48 EUR.
[TestClass]
public sealed class MsTestAssertTests
{
    private readonly Invoice _invoice = Samples.Invoice();

    [TestMethod]
    public void TheTotalIsTheSumOfTheLines()
    {
        // The total is 25.48.
        Assert.Fail("TODO-01: write this test.");
    }

    [TestMethod]
    public void TheDiscountIsRoundedToCents()
    {
        // TotalWithDiscount(10m) is 22.93 once rounded to cents. Use the overload with a delta.
        Assert.Fail("TODO-01: write this test.");
    }

    [TestMethod]
    public void TheSummaryNamesTheInvoiceAndTheCustomer()
    {
        // Starts with "INV-001", contains "Ada Lovelace", ends with "eur" ignoring case.
        // There is no case-insensitive EndsWith in Assert: decide how you will keep a useful message.
        Assert.Fail("TODO-01: write this test.");
    }

    [TestMethod]
    public void TheLinesAreInOrderAndAllHaveStock()
    {
        // Two lines, skus A-100 then B-200 in that order, every quantity above zero.
        Assert.Fail("TODO-01: write this test.");
    }

    [TestMethod]
    public void PayingTwiceAndAddingNothingAreRejected()
    {
        // Paying twice throws InvalidOperationException "Invoice INV-001 is already paid."
        // (exact type, exact message). AddLine with quantity 0 throws ArgumentOutOfRangeException for "quantity".
        Assert.Fail("TODO-01: write this test.");
    }

    [TestMethod]
    public void TheCustomerMatchesIgnoringTheEmail()
    {
        // Compare with new Customer("Ada Lovelace", "someone.else@example.com", "UK"), ignoring the e-mail.
        // MSTest has no equivalence assertion: compare the members one by one.
        Assert.Fail("TODO-01: write this test.");
    }
}
