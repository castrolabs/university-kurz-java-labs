using Tenancy.Domain;

namespace Tenancy.Application.Tests;

[TestClass]
public sealed class ResolveTenantHandlerTests(TestContext testContext)
{
    // One row per host. A tuple is enough: no object[] and no casts.
    public static IEnumerable<(string Host, TenantResolutionStatus Expected)> Hosts =>
    [
        ("acme.example.com", TenantResolutionStatus.Found),
        ("ACME.example.com", TenantResolutionStatus.Found),
        ("ghost.example.com", TenantResolutionStatus.NotFound),
        ("localhost", TenantResolutionStatus.NoSubdomain),
    ];

    [TestMethod]
    [DynamicData(nameof(Hosts))]
    public async Task ShouldResolveTheTenantFromTheFirstLabelOfTheHost(string host, TenantResolutionStatus expected)
    {
        var tenants = new FakeTenantRepository();
        tenants.Stored.Add(new Tenant("acme"));

        var resolution = await new ResolveTenantHandler(tenants).HandleAsync(host, testContext.CancellationToken);

        Assert.AreEqual(expected, resolution.Status);
    }

    [TestMethod]
    public async Task ShouldReturnTheStoredTenantWhenFound()
    {
        var tenants = new FakeTenantRepository();
        var acme = new Tenant("acme");
        tenants.Stored.Add(acme);

        var resolution = await new ResolveTenantHandler(tenants).HandleAsync("acme.example.com", testContext.CancellationToken);

        Assert.AreSame(acme, resolution.Tenant);
    }
}
