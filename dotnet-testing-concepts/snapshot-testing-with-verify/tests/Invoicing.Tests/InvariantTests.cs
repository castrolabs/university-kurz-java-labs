namespace Invoicing.Tests;

// A snapshot records what the code did on one input. A rule that has to hold for every input is an assertion.
// These tests need no Verify.
[TestClass]
public sealed class InvariantTests
{
    private readonly InvoiceService _service = new();

    // TODO-04: Turn this into one test that runs for several quantities and prices: add int parameters
    // (quantity, price in cents, because an attribute cannot hold a decimal) and [DataRow] rows. Include a row
    // whose tax needs rounding. Assert that Total is Subtotal plus Tax.
    [TestMethod]
    public void TheTotalIsTheSubtotalPlusTheTax()
    {
        Assert.Fail("TODO-04: write this test.");
    }

    // TODO-04: The subtotal is the sum of the line totals.
    [TestMethod]
    public void TheSubtotalIsTheSumOfTheLines()
    {
        Assert.Fail("TODO-04: write this test.");
    }

    // TODO-04: Pin the one number a snapshot can hide: for Orders.Coffee() the subtotal is 5.50 and the tax is
    // 1.045 before rounding. Assert the rounded tax by hand, with a literal you worked out yourself, not
    // one copied from the code.
    [TestMethod]
    public void TheTaxIsRoundedAwayFromZeroToTheCent()
    {
        Assert.Fail("TODO-04: write this test.");
    }
}
