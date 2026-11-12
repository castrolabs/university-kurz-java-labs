namespace Dispatch.Tests;

// The three suspects in SuspectTests.cs, rewritten so the cause is gone and not just hidden by a retry.
// Every test fails with a TODO message until you write it.
[TestClass]
[TestCategory("Fixed")]
public sealed class FixedTests
{
    // TODO-01: TicketCounter is shared by every test in the process. Start a new sequence before each
    // test (and leave it clean after each one) with [TestInitialize] and [TestCleanup], using Reset().

    [TestMethod]
    public void TheFirstTicketOfTheDayIsOne() =>
        Assert.Fail("TODO-01: take a ticket and assert it is 1.");

    [TestMethod]
    public void TicketsAreSequential() =>
        Assert.Fail("TODO-01: take two tickets and assert the second is the first plus one.");

    // TODO-02: Inbox.Receive() waits forever. Use TryReceive with a short timeout instead.

    [TestMethod]
    public void ASilentDepotIsReportedInsteadOfWaitedFor() =>
        Assert.Fail("TODO-02: TryReceive on an empty inbox returns false (use 100 ms) and the message is null.");

    [TestMethod]
    public void ADepotMessageIsReceived() =>
        Assert.Fail("TODO-02: Post a message, then TryReceive returns true and the same message.");

    // TODO-03: Route.Length(stop) recurses. A cycle overflows the stack and kills the process. Use the
    // overload that takes a limit.

    [TestMethod]
    public void ACyclicRouteIsRejected() =>
        Assert.Fail("TODO-03: A -> B -> A with limit 1000 throws InvalidOperationException.");

    [TestMethod]
    public void AnOrdinaryRouteIsCounted() =>
        Assert.Fail("TODO-03: A -> B -> C with limit 1000 has 3 stops.");
}
