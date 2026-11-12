namespace Dispatch.Tests;

// The three suspects, rewritten so the cause is gone and not just hidden by a retry.
[TestClass]
[TestCategory("Fixed")]
public sealed class FixedTests
{
    // The sequence is shared by every test, so each test starts a new one.
    [TestInitialize]
    public void StartANewSequence() => TicketCounter.Reset();

    // ...and leaves it clean for whoever runs next in the same process.
    [TestCleanup]
    public void LeaveItClean() => TicketCounter.Reset();

    [TestMethod]
    public void TheFirstTicketOfTheDayIsOne() => Assert.AreEqual(1, TicketCounter.Next());

    [TestMethod]
    public void TicketsAreSequential()
    {
        var first = TicketCounter.Next();
        Assert.AreEqual(first + 1, TicketCounter.Next());
    }

    // TryReceive has a deadline, so a silent depot is a failed assertion and not a stuck job.
    [TestMethod]
    public void ASilentDepotIsReportedInsteadOfWaitedFor()
    {
        var inbox = new Inbox();
        Assert.IsFalse(inbox.TryReceive(TimeSpan.FromMilliseconds(100), out var message));
        Assert.IsNull(message);
    }

    [TestMethod]
    public void ADepotMessageIsReceived()
    {
        var inbox = new Inbox();
        inbox.Post("ok");
        Assert.IsTrue(inbox.TryReceive(TimeSpan.FromSeconds(1), out var message));
        Assert.AreEqual("ok", message);
    }

    // The overload with a limit turns a cycle into an exception the test can catch.
    [TestMethod]
    public void ACyclicRouteIsRejected()
    {
        var a = new Stop("A");
        var b = new Stop("B") { Next = a };
        a.Next = b;
        Assert.Throws<InvalidOperationException>(() => Route.Length(a, limit: 1000));
    }

    [TestMethod]
    public void AnOrdinaryRouteIsCounted()
    {
        var route = new Stop("A") { Next = new Stop("B") { Next = new Stop("C") } };
        Assert.AreEqual(3, Route.Length(route, limit: 1000));
    }
}
