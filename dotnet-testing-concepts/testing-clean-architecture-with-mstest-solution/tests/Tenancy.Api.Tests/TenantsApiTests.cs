using System.Net;
using System.Net.Http.Json;
using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.DependencyInjection;
using Tenancy.Api.Tenants;
using Tenancy.Domain;
using Tenancy.Infrastructure.Persistence;

namespace Tenancy.Api.Tests;

// The whole stack on a real SQLite file: HTTP in, SQL out. Few tests, only for what crosses layers.
[TestClass]
[TestCategory("Integration")]
public sealed class TenantsApiTests(TestContext testContext)
{
    private string _dbFile = null!;
    private WebApplicationFactory<Program> _factory = null!;

    [TestInitialize]
    public void StartApi()
    {
        // A new database file and a new host per test: tests cannot see each other's tenants,
        // which matters because they run in parallel. Pooling=False makes closing a connection really
        // close the file, so the cleanup can delete it.
        _dbFile = Path.Combine(Path.GetTempPath(), $"tenancy-{Guid.NewGuid():N}.db");
        _factory = new WebApplicationFactory<Program>().WithWebHostBuilder(builder =>
            builder.UseSetting("ConnectionStrings:Tenancy", $"Data Source={_dbFile};Pooling=False"));
    }

    // A [TestCleanup] method can await the factory's DisposeAsync (a test class that implements
    // IAsyncDisposable would do the same).
    [TestCleanup]
    public async Task StopApi()
    {
        await _factory.DisposeAsync();
        File.Delete(_dbFile);
    }

    private HttpClient ClientFor(string host)
    {
        var client = _factory.CreateClient();
        client.BaseAddress = new Uri($"http://{host}");
        return client;
    }

    private Task<HttpResponseMessage> CreateTenantAsync(string subdomain) =>
        ClientFor("localhost").PostAsJsonAsync("/tenants", new { subdomain }, testContext.CancellationToken);

    [TestMethod]
    public async Task ShouldCreateATenantWithTheNormalizedSubdomain()
    {
        var response = await CreateTenantAsync("ACME");

        Assert.AreEqual(HttpStatusCode.Created, response.StatusCode);
        var tenant = await response.Content.ReadFromJsonAsync<TenantDto>(testContext.CancellationToken);
        Assert.AreEqual("acme", tenant!.Subdomain);
        Assert.AreEqual(7, tenant.Id.Version);
    }

    [TestMethod]
    public async Task ShouldReturn400WhenTheSubdomainIsInvalid()
    {
        var response = await CreateTenantAsync("not valid");

        Assert.AreEqual(HttpStatusCode.BadRequest, response.StatusCode);
    }

    [TestMethod]
    public async Task ShouldReturn409WhenTheSubdomainIsAlreadyTaken()
    {
        await CreateTenantAsync("acme");

        var again = await CreateTenantAsync("Acme");

        Assert.AreEqual(HttpStatusCode.Conflict, again.StatusCode);
    }

    [TestMethod]
    public async Task ShouldResolveTheCurrentTenantFromTheSubdomain()
    {
        await CreateTenantAsync("acme");
        await CreateTenantAsync("globex");

        var response = await ClientFor("acme.localhost").GetAsync("/tenants/current", testContext.CancellationToken);

        Assert.AreEqual(HttpStatusCode.OK, response.StatusCode);
        var tenant = await response.Content.ReadFromJsonAsync<TenantDto>(testContext.CancellationToken);
        Assert.AreEqual("acme", tenant!.Subdomain);
    }

    [TestMethod]
    [DataRow("ghost.localhost")]
    [DataRow("localhost")]
    public async Task ShouldReturn404ForCurrentWhenThereIsNoSuchTenant(string host)
    {
        var response = await ClientFor(host).GetAsync("/tenants/current", testContext.CancellationToken);

        Assert.AreEqual(HttpStatusCode.NotFound, response.StatusCode);
    }

    [TestMethod]
    public async Task ShouldEnforceUniqueSubdomainsInTheDatabaseItself()
    {
        await CreateTenantAsync("acme");
        using var scope = _factory.Services.CreateScope();
        var db = scope.ServiceProvider.GetRequiredService<TenancyDbContext>();

        db.Tenants.Add(new Tenant("acme"));

        // The handler's ExistsAsync check is a courtesy; the unique index is the guarantee.
        await Assert.ThrowsExactlyAsync<DbUpdateException>(() => db.SaveChangesAsync(testContext.CancellationToken));
    }
}
