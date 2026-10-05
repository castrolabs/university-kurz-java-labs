using Tenancy.Domain;

namespace Tenancy.Application.Tests;

public class HandlerTests
{
    private readonly FakeTenantRepository _tenants = new();

    [Fact]
    public async Task ShouldCreateAndStoreTheTenant()
    {
        var result = await new CreateTenantHandler(_tenants).HandleAsync("ACME", CancellationToken.None);

        Assert.Equal(CreateTenantStatus.Created, result.Status);
        Assert.Equal("acme", Assert.Single(_tenants.Stored).Subdomain);
    }

    [Fact]
    public async Task ShouldReportAnInvalidSubdomainWithoutStoringAnything()
    {
        var result = await new CreateTenantHandler(_tenants).HandleAsync("not valid", CancellationToken.None);

        Assert.Equal(CreateTenantStatus.InvalidSubdomain, result.Status);
        Assert.NotNull(result.Error);
        Assert.Empty(_tenants.Stored);
    }

    [Fact]
    public async Task ShouldRejectASubdomainThatIsAlreadyTakenIgnoringCase()
    {
        _tenants.Stored.Add(new Tenant("acme"));

        var result = await new CreateTenantHandler(_tenants).HandleAsync("Acme", CancellationToken.None);

        Assert.Equal(CreateTenantStatus.SubdomainTaken, result.Status);
        Assert.Single(_tenants.Stored);
    }

    [Fact]
    public async Task ShouldResolveTheTenantFromTheFirstLabelOfTheHost()
    {
        _tenants.Stored.Add(new Tenant("acme"));

        var resolution = await new ResolveTenantHandler(_tenants).HandleAsync("ACME.example.com", CancellationToken.None);

        Assert.Equal(TenantResolutionStatus.Found, resolution.Status);
        Assert.Equal("acme", resolution.Tenant!.Subdomain);
    }

    [Fact]
    public async Task ShouldReportAnUnknownSubdomainAsNotFound()
    {
        var resolution = await new ResolveTenantHandler(_tenants).HandleAsync("ghost.example.com", CancellationToken.None);

        Assert.Equal(TenantResolutionStatus.NotFound, resolution.Status);
    }

    [Fact]
    public async Task ShouldReportAHostWithoutSubdomain()
    {
        var resolution = await new ResolveTenantHandler(_tenants).HandleAsync("localhost", CancellationToken.None);

        Assert.Equal(TenantResolutionStatus.NoSubdomain, resolution.Status);
    }
}
