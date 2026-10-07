namespace Tenancy.Application.Tests;

// TODO-02: Test CreateTenantHandler with the FakeTenantRepository. Each test checks the status the use
// case returns, and what was or was not stored.
[TestClass]
// MSTest passes the TestContext of the running test to the constructor. Pass its CancellationToken to
// HandleAsync instead of CancellationToken.None: it is cancelled when the test times out or the run is aborted.
public sealed class CreateTenantHandlerTests(TestContext testContext)
{
    private readonly TestContext _testContext = testContext;

    [TestMethod]
    public async Task ShouldCreateAndStoreATenantWithTheNormalizedSubdomain()
    {
        await Task.CompletedTask;
        Assert.Fail("TODO-02: write this test.");
    }

    [TestMethod]
    public async Task ShouldReportAnInvalidSubdomainWithoutTouchingTheRepository()
    {
        await Task.CompletedTask;
        Assert.Fail("TODO-02: write this test.");
    }

    [TestMethod]
    public async Task ShouldRejectASubdomainThatIsAlreadyTakenIgnoringCase()
    {
        // Seed the fake with "acme", then ask for "Acme".
        await Task.CompletedTask;
        Assert.Fail("TODO-02: write this test.");
    }
}
