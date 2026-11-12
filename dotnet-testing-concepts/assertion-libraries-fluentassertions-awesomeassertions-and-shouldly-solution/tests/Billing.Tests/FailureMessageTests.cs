using System.Globalization;
using AwesomeAssertions;
using AwesomeAssertions.Execution;
using Billing;
using Shouldly;

namespace Billing.Tests;

// What each library prints when the same check fails. Every test makes an assertion fail on purpose and
// asserts on the message, so you read the three styles side by side.
[TestClass]
public sealed class FailureMessageTests
{
    private readonly Invoice _invoice = Samples.Invoice();

    [TestMethod]
    public void MsTestQuotesBothValuesAndTheCallItself()
    {
        var failure = Assert.ThrowsExactly<AssertFailedException>(() => Assert.AreEqual(25.50m, _invoice.Total));

        Assert.Contains("expected: 25.50", failure.Message);
        Assert.Contains("actual:   25.48", failure.Message);
        Assert.Contains("Assert.AreEqual(25.50m, _invoice.Total)", failure.Message);
    }

    [TestMethod]
    public void AwesomeAssertionsNamesTheSubjectAndTheDifference()
    {
        var failure = Assert.ThrowsExactly<AssertFailedException>(() => _invoice.Total.Should().Be(25.50m));

        Assert.AreEqual("Expected _invoice.Total to be 25.50M, but found 25.48M (difference of -0.02).", failure.Message);
    }

    [TestMethod]
    public void AwesomeAssertionsCarriesTheReasonYouGive()
    {
        var failure = Assert.ThrowsExactly<AssertFailedException>(
            () => _invoice.Total.Should().Be(1m, "the {0} must match", "total"));

        Assert.Contains("because the total must match", failure.Message);
    }

    [TestMethod]
    public void AwesomeAssertionsScopeReportsEveryFailureAtOnce()
    {
        var failure = Assert.ThrowsExactly<AssertFailedException>(() =>
        {
            using var scope = new AssertionScope();
            _invoice.Total.Should().Be(1m);
            _invoice.Summary().Should().Contain("Grace");
        });

        var lines = failure.Message.Split('\n', StringSplitOptions.RemoveEmptyEntries);
        Assert.HasCount(2, lines);
        Assert.StartsWith("Expected _invoice.Total to be 1M", lines[0]);
        Assert.StartsWith("Expected _invoice.Summary()", lines[1]);
    }

    [TestMethod]
    public void ShouldlyPrintsTheCodeYouWroteAndBothValues()
    {
        // Shouldly formats numbers with the current culture, and prints the source line of the failing call.
        CultureInfo.CurrentCulture = CultureInfo.InvariantCulture;

        var failure = Assert.ThrowsExactly<ShouldAssertException>(() =>
        {
            _invoice.Total.ShouldBe(25.50m);
        });

        Assert.Contains("_invoice.Total", failure.Message);
        Assert.Contains("should be", failure.Message);
        Assert.Contains("25.50m", failure.Message);
        Assert.Contains("but was", failure.Message);
        Assert.Contains("25.48m", failure.Message);
    }
}
