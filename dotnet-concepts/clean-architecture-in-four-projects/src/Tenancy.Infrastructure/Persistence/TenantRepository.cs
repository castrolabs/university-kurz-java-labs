using Microsoft.EntityFrameworkCore;
using Tenancy.Application;
using Tenancy.Domain;

namespace Tenancy.Infrastructure.Persistence;

internal sealed class TenantRepository(TenancyDbContext db) : ITenantRepository
{
    public Task<bool> ExistsAsync(string subdomain, CancellationToken ct)
    {
        // TODO-05: Implement the three repository methods with EF Core. Reads that do not
        //   modify anything should not track entities, and AddAsync must persist the tenant.
        throw new NotImplementedException("Not implemented yet.");
    }

    public Task<Tenant?> FindBySubdomainAsync(string subdomain, CancellationToken ct) =>
        throw new NotImplementedException("Not implemented yet.");

    public Task AddAsync(Tenant tenant, CancellationToken ct) =>
        throw new NotImplementedException("Not implemented yet.");
}
