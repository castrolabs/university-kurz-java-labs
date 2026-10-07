using Microsoft.AspNetCore.Mvc.Testing;

namespace Tenancy.Api.Tests;

// The whole stack on a real SQLite file: HTTP in, SQL out. Few tests, only for what crosses layers.
[TestClass]
[TestCategory("Integration")]
public sealed class TenantsApiTests(TestContext testContext)
{
    private readonly TestContext _testContext = testContext;
    private WebApplicationFactory<Program> _factory = null!;

    // TODO-04: Start a new API before each test with a [TestInitialize] method, and stop it after each test
    // with a [TestCleanup] method. Point ConnectionStrings:Tenancy at a new SQLite file in
    // Path.GetTempPath() (WithWebHostBuilder + UseSetting) and delete the file in the cleanup.
    // Add Pooling=False to the connection string so closing a connection really closes the file.
    // Why per test? Tests run in parallel, and each one creates tenants. The factory has DisposeAsync:
    // a [TestCleanup] method may be async, so it can await it.

    private HttpClient ClientFor(string host)
    {
        var client = _factory.CreateClient();
        client.BaseAddress = new Uri($"http://{host}");
        return client;
    }

    // TODO-05: Write the tests below. Use ClientFor("localhost") to POST /tenants with a JSON body
    // { subdomain }, and ClientFor("acme.localhost") to reach a tenant by its subdomain.
    [TestMethod]
    public async Task ShouldCreateATenantWithTheNormalizedSubdomain()
    {
        // 201, and the DTO has the lower-case subdomain and a version 7 id.
        await Task.CompletedTask;
        Assert.Fail("TODO-05: write this test.");
    }

    [TestMethod]
    public async Task ShouldReturn400WhenTheSubdomainIsInvalid()
    {
        await Task.CompletedTask;
        Assert.Fail("TODO-05: write this test.");
    }

    [TestMethod]
    public async Task ShouldReturn409WhenTheSubdomainIsAlreadyTaken()
    {
        await Task.CompletedTask;
        Assert.Fail("TODO-05: write this test.");
    }

    [TestMethod]
    public async Task ShouldResolveTheCurrentTenantFromTheSubdomain()
    {
        // Create two tenants, then ask GET /tenants/current through the host of one of them.
        await Task.CompletedTask;
        Assert.Fail("TODO-05: write this test.");
    }

    [TestMethod]
    public async Task ShouldReturn404ForCurrentWhenThereIsNoSuchTenant()
    {
        // Two hosts, same answer: a subdomain that is not a tenant, and no subdomain at all. Use [DataRow].
        await Task.CompletedTask;
        Assert.Fail("TODO-05: write this test.");
    }

    [TestMethod]
    public async Task ShouldEnforceUniqueSubdomainsInTheDatabaseItself()
    {
        // Skip the handler: resolve TenancyDbContext from _factory.Services in a scope and add a
        // duplicate Tenant directly. The handler's check is a courtesy; what does the database say?
        await Task.CompletedTask;
        Assert.Fail("TODO-05: write this test.");
    }
}
