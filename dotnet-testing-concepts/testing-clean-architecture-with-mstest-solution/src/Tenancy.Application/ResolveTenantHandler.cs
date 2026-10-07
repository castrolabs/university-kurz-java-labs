using Tenancy.Domain;

namespace Tenancy.Application;

public enum TenantResolutionStatus
{
    NoSubdomain,
    NotFound,
    Found,
}

public sealed record TenantResolution(TenantResolutionStatus Status, Tenant? Tenant = null);

public sealed class ResolveTenantHandler(ITenantRepository tenants)
{
    public async Task<TenantResolution> HandleAsync(string host, CancellationToken ct)
    {
        var labels = host.Split('.');
        if (labels.Length < 2)
            return new TenantResolution(TenantResolutionStatus.NoSubdomain);

        var tenant = await tenants.FindBySubdomainAsync(labels[0].ToLowerInvariant(), ct);

        return tenant is null
            ? new TenantResolution(TenantResolutionStatus.NotFound)
            : new TenantResolution(TenantResolutionStatus.Found, tenant);
    }
}
