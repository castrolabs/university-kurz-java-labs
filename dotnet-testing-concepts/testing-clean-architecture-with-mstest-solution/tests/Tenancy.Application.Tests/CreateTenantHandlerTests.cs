using Tenancy.Domain;

namespace Tenancy.Application.Tests;

[TestClass]
// MSTest passes the TestContext of the running test to the constructor. Its CancellationToken is
// cancelled when the test times out or the run is aborted.
public sealed class CreateTenantHandlerTests(TestContext testContext)
{
    private readonly FakeTenantRepository _tenants = new();

    private Task<CreateTenantResult> CreateAsync(string subdomain) =>
        new CreateTenantHandler(_tenants).HandleAsync(subdomain, testContext.CancellationToken);

    [TestMethod]
    public async Task ShouldCreateAndStoreATenantWithTheNormalizedSubdomain()
    {
        var result = await CreateAsync(" ACME ");

        Assert.AreEqual(CreateTenantStatus.Created, result.Status);
        Assert.AreEqual("acme", result.Tenant!.Subdomain);
        Assert.AreSame(result.Tenant, Assert.ContainsSingle(_tenants.Stored));
    }

    [TestMethod]
    public async Task ShouldReportAnInvalidSubdomainWithoutTouchingTheRepository()
    {
        var result = await CreateAsync("not valid");

        Assert.AreEqual(CreateTenantStatus.InvalidSubdomain, result.Status);
        Assert.IsNotNull(result.Error);
        Assert.IsEmpty(_tenants.Stored);
    }

    [TestMethod]
    public async Task ShouldRejectASubdomainThatIsAlreadyTakenIgnoringCase()
    {
        _tenants.Stored.Add(new Tenant("acme"));

        var result = await CreateAsync("Acme");

        Assert.AreEqual(CreateTenantStatus.SubdomainTaken, result.Status);
        Assert.HasCount(1, _tenants.Stored);
    }
}
