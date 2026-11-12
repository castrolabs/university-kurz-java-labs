namespace Dispatch.Tests;

// Three tests that fail the way real suites fail in CI: one passes alone and fails in the full run, one
// never finishes, one kills the process. They are switched off unless DISPATCH_SUSPECTS=1, so a plain
// `dotnet test` stays green. Do not edit them: the lab is to diagnose them, then write the fixed versions
// in FixedTests.cs.
[TestClass]
[TestCategory("Suspect")]
public sealed class SuspectTests
{
    private static void RequireSuspects()
    {
        if (Environment.GetEnvironmentVariable("DISPATCH_SUSPECTS") != "1")
        {
            Assert.Inconclusive("Set DISPATCH_SUSPECTS=1 to run the suspects.");
        }
    }

    [TestMethod]
    public void TicketsAreSequential()
    {
        RequireSuspects();
        var first = TicketCounter.Next();
        Assert.AreEqual(first + 1, TicketCounter.Next());
    }

    // Flaky: it assumes it is the first test to take a ticket. Run alone it
    // passes; in the full run it comes after TicketsAreSequential and fails.
    [TestMethod]
    public void TheFirstTicketOfTheDayIsOne()
    {
        RequireSuspects();
        Assert.AreEqual(1, TicketCounter.Next());
    }

    // Hangs: nothing ever posts to this inbox, and Receive() waits forever.
    [TestMethod]
    public void TheDepotAnswers()
    {
        RequireSuspects();
        var inbox = new Inbox();
        Assert.AreEqual("ok", inbox.Receive());
    }

    // Crashes: the route loops back to its first stop, and the recursive Length overflows the stack.
    [TestMethod]
    public void ARouteCanBeCounted()
    {
        RequireSuspects();
        var a = new Stop("A");
        var b = new Stop("B") { Next = a };
        a.Next = b;
        Assert.AreEqual(2, Route.Length(a));
    }
}
