using AwesomeAssertions;
using Billing;

namespace Billing.Tests;

// TODO-02: The same six checks as MsTestAssertTests, written with AwesomeAssertions (`using AwesomeAssertions;`).
// Use chaining where it reads better than separate statements, and BeEquivalentTo for the customer.
[TestClass]
public sealed class AwesomeAssertionsTests
{
    private readonly Invoice _invoice = Samples.Invoice();

    [TestMethod]
    public void TheTotalIsTheSumOfTheLines()
    {
        Assert.Fail("TODO-02: write this test.");
    }

    [TestMethod]
    public void TheDiscountIsRoundedToCents()
    {
        // BeApproximately, not Be.
        Assert.Fail("TODO-02: write this test.");
    }

    [TestMethod]
    public void TheSummaryNamesTheInvoiceAndTheCustomer()
    {
        // One chain: StartWith, Contain and a case-insensitive end.
        Assert.Fail("TODO-02: write this test.");
    }

    [TestMethod]
    public void TheLinesAreInOrderAndAllHaveStock()
    {
        Assert.Fail("TODO-02: write this test.");
    }

    [TestMethod]
    public void PayingTwiceAndAddingNothingAreRejected()
    {
        // `Invoking(...)` plus ThrowExactly<T>() and WithMessage / WithParameterName.
        Assert.Fail("TODO-02: write this test.");
    }

    [TestMethod]
    public void TheCustomerMatchesIgnoringTheEmail()
    {
        // One BeEquivalentTo with Excluding.
        Assert.Fail("TODO-02: write this test.");
    }
}
