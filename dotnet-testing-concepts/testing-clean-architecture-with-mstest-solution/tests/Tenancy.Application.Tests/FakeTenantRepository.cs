using Tenancy.Domain;

namespace Tenancy.Application.Tests;

// A fake is a working in-memory implementation of the port, not a script of expected calls.
// Tests seed it through Stored and check the outcome by reading Stored back.
internal sealed class FakeTenantRepository : ITenantRepository
{
    public List<Tenant> Stored { get; } = [];

    public Task<bool> ExistsAsync(string subdomain, CancellationToken ct) =>
        Task.FromResult(Stored.Any(t => t.Subdomain == subdomain));

    public Task<Tenant?> FindBySubdomainAsync(string subdomain, CancellationToken ct) =>
        Task.FromResult(Stored.FirstOrDefault(t => t.Subdomain == subdomain));

    public Task AddAsync(Tenant tenant, CancellationToken ct)
    {
        Stored.Add(tenant);
        return Task.CompletedTask;
    }
}
