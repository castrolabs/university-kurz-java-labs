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
    public Task<TenantResolution> HandleAsync(string host, CancellationToken ct)
    {
        // TODO-03: The tenant is the first label of the host ("acme.example.com" -> "acme",
        //   case-insensitive). A host with a single label ("localhost") has no subdomain.
        //   Otherwise look the tenant up and answer Found or NotFound.
        throw new NotImplementedException("Not implemented yet.");
    }
}
