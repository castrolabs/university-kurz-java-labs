using Tenancy.Domain;

namespace Tenancy.Application.Tests;

// TODO-01: A fake is a working in-memory implementation of the port, not a script of expected calls.
// Give it a public List<Tenant> called Stored: tests seed it and read it back. Then implement the three
// methods on top of that list. Remember that Task.FromResult and Task.CompletedTask exist.
internal sealed class FakeTenantRepository : ITenantRepository
{
    public Task<bool> ExistsAsync(string subdomain, CancellationToken ct) =>
        throw new NotImplementedException("TODO-01");

    public Task<Tenant?> FindBySubdomainAsync(string subdomain, CancellationToken ct) =>
        throw new NotImplementedException("TODO-01");

    public Task AddAsync(Tenant tenant, CancellationToken ct) =>
        throw new NotImplementedException("TODO-01");
}
