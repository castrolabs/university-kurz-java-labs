using Tenancy.Domain;

namespace Tenancy.Application;

public interface ITenantRepository
{
    Task<bool> ExistsAsync(string subdomain, CancellationToken ct);

    Task<Tenant?> FindBySubdomainAsync(string subdomain, CancellationToken ct);

    Task AddAsync(Tenant tenant, CancellationToken ct);
}

public interface ITenantContext
{
    Tenant? Current { get; }
}

public sealed class TenantContext : ITenantContext
{
    public Tenant? Current { get; set; }
}
