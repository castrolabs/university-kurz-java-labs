using System.Net;
using System.Net.Http.Json;
using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.DependencyInjection;
using Tenancy.Domain;
using Tenancy.Infrastructure.Persistence;
using Tenancy.Api.Tenants;

namespace Tenancy.Api.Tests;

public sealed class TenantsApiTests : IDisposable
{
    private readonly string _dbFile = Path.Combine(Path.GetTempPath(), $"tenancy-{Guid.NewGuid():N}.db");
    private readonly WebApplicationFactory<Program> _factory;

    public TenantsApiTests()
    {
        _factory = new WebApplicationFactory<Program>().WithWebHostBuilder(builder =>
            builder.UseSetting("ConnectionStrings:Tenancy", $"Data Source={_dbFile}"));
    }

    public void Dispose()
    {
        _factory.Dispose();
        File.Delete(_dbFile);
    }

    private HttpClient ClientFor(string host)
    {
        var client = _factory.CreateClient();
        client.BaseAddress = new Uri($"http://{host}");
        return client;
    }

    private Task<HttpResponseMessage> CreateTenantAsync(string subdomain) =>
        ClientFor("localhost").PostAsJsonAsync("/tenants", new { subdomain });

    [Fact]
    public async Task ShouldCreateATenantWithTheNormalizedSubdomain()
    {
        var response = await CreateTenantAsync("ACME");

        Assert.Equal(HttpStatusCode.Created, response.StatusCode);
        var tenant = await response.Content.ReadFromJsonAsync<TenantDto>();
        Assert.Equal("acme", tenant!.Subdomain);
        Assert.Equal(7, tenant.Id.Version);
    }

    [Fact]
    public async Task ShouldReturn400WhenTheSubdomainIsInvalid()
    {
        var response = await CreateTenantAsync("not valid");

        Assert.Equal(HttpStatusCode.BadRequest, response.StatusCode);
    }

    [Fact]
    public async Task ShouldReturn409WhenTheSubdomainIsAlreadyTaken()
    {
        await CreateTenantAsync("acme");

        var again = await CreateTenantAsync("Acme");

        Assert.Equal(HttpStatusCode.Conflict, again.StatusCode);
    }

    [Fact]
    public async Task ShouldResolveTheCurrentTenantFromTheSubdomain()
    {
        await CreateTenantAsync("acme");
        await CreateTenantAsync("globex");

        var response = await ClientFor("acme.localhost").GetAsync("/tenants/current");

        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
        var tenant = await response.Content.ReadFromJsonAsync<TenantDto>();
        Assert.Equal("acme", tenant!.Subdomain);
    }

    [Fact]
    public async Task ShouldReturn404ForASubdomainThatIsNotATenant()
    {
        var response = await ClientFor("ghost.localhost").GetAsync("/tenants/current");

        Assert.Equal(HttpStatusCode.NotFound, response.StatusCode);
    }

    [Fact]
    public async Task ShouldReturn404ForCurrentWhenThereIsNoSubdomain()
    {
        var response = await ClientFor("localhost").GetAsync("/tenants/current");

        Assert.Equal(HttpStatusCode.NotFound, response.StatusCode);
    }

    [Fact]
    public async Task ShouldEnforceUniqueSubdomainsInTheDatabaseItself()
    {
        await CreateTenantAsync("acme");
        using var scope = _factory.Services.CreateScope();
        var db = scope.ServiceProvider.GetRequiredService<TenancyDbContext>();

        db.Tenants.Add(new Tenant("acme"));

        await Assert.ThrowsAsync<DbUpdateException>(() => db.SaveChangesAsync());
    }
}
