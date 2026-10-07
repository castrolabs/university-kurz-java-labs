using Microsoft.EntityFrameworkCore;
using Tenancy.Application;
using Tenancy.Domain;

namespace Tenancy.Infrastructure.Persistence;

internal sealed class TenantRepository(TenancyDbContext db) : ITenantRepository
{
    public Task<bool> ExistsAsync(string subdomain, CancellationToken ct) =>
        db.Tenants.AnyAsync(t => t.Subdomain == subdomain, ct);

    public Task<Tenant?> FindBySubdomainAsync(string subdomain, CancellationToken ct) =>
        db.Tenants.AsNoTracking().SingleOrDefaultAsync(t => t.Subdomain == subdomain, ct);

    public async Task AddAsync(Tenant tenant, CancellationToken ct)
    {
        db.Tenants.Add(tenant);
        await db.SaveChangesAsync(ct);
    }
}
