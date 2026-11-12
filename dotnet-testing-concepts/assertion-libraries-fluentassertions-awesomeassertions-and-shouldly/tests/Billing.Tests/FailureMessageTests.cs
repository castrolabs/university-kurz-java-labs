using System.Globalization;
using AwesomeAssertions;
using AwesomeAssertions.Execution;
using Billing;
using Shouldly;

namespace Billing.Tests;

// TODO-03: What each library prints when the same check fails. Make an assertion fail on purpose inside
// Assert.ThrowsExactly<...>, then assert on the message of the exception it returns.
// Run a test with a deliberately wrong assert first and read the message before you pin it down.
[TestClass]
public sealed class FailureMessageTests
{
    private readonly Invoice _invoice = Samples.Invoice();

    [TestMethod]
    public void MsTestQuotesBothValuesAndTheCallItself()
    {
        // Assert.AreEqual(25.50m, _invoice.Total) throws AssertFailedException. The message has the expected
        // value, the actual value and the text of the call.
        Assert.Fail("TODO-03: write this test.");
    }

    [TestMethod]
    public void AwesomeAssertionsNamesTheSubjectAndTheDifference()
    {
        // _invoice.Total.Should().Be(25.50m) throws AssertFailedException too (AwesomeAssertions finds MSTest
        // by itself). Assert the whole message: it names `_invoice.Total` and the difference.
        Assert.Fail("TODO-03: write this test.");
    }

    [TestMethod]
    public void AwesomeAssertionsCarriesTheReasonYouGive()
    {
        // Be(1m, "the {0} must match", "total"): the reason is part of the message.
        Assert.Fail("TODO-03: write this test.");
    }

    [TestMethod]
    public void AwesomeAssertionsScopeReportsEveryFailureAtOnce()
    {
        // Two failing assertions inside a `using var scope = new AssertionScope();`: one exception, two lines.
        Assert.Fail("TODO-03: write this test.");
    }

    [TestMethod]
    public void ShouldlyPrintsTheCodeYouWroteAndBothValues()
    {
        // _invoice.Total.ShouldBe(25.50m) throws ShouldAssertException. Shouldly formats numbers with the
        // current culture and prints the source line of the failing call: pin the culture first, and use a
        // lambda with a block body so the source line is only the assertion.
        Assert.Fail("TODO-03: write this test.");
    }
}
