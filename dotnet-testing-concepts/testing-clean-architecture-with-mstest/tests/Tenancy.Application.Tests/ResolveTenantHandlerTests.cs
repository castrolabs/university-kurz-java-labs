namespace Tenancy.Application.Tests;

[TestClass]
public sealed class ResolveTenantHandlerTests(TestContext testContext)
{
    private readonly TestContext _testContext = testContext;

    // TODO-03: A table of hosts and the status each one must produce. Make it a public static property
    // returning IEnumerable<(string Host, TenantResolutionStatus Expected)>. Cover a known tenant, the
    // same tenant in upper case, an unknown tenant and a host with no subdomain ("localhost").
    // Then point [DynamicData(nameof(...))] at it from the test below, and give the test the two parameters.
    [TestMethod]
    public async Task ShouldResolveTheTenantFromTheFirstLabelOfTheHost()
    {
        await Task.CompletedTask;
        Assert.Fail("TODO-03: write this test.");
    }

    [TestMethod]
    public async Task ShouldReturnTheStoredTenantWhenFound()
    {
        // Same instance, not just an equal-looking one: Assert.AreSame.
        await Task.CompletedTask;
        Assert.Fail("TODO-03: write this test.");
    }
}
