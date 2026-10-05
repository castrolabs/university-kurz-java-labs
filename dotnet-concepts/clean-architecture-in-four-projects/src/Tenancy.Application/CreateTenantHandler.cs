using Tenancy.Domain;

namespace Tenancy.Application;

public enum CreateTenantStatus
{
    Created,
    InvalidSubdomain,
    SubdomainTaken,
}

public sealed record CreateTenantResult(CreateTenantStatus Status, Tenant? Tenant = null, string? Error = null);

public sealed class CreateTenantHandler(ITenantRepository tenants)
{
    public Task<CreateTenantResult> HandleAsync(string subdomain, CancellationToken ct)
    {
        // TODO-02: Create the Tenant (the domain decides what a valid subdomain is). Answer
        //   InvalidSubdomain with the error message when it throws, SubdomainTaken when the
        //   repository says it exists, otherwise store it and answer Created with the tenant.
        throw new NotImplementedException("Not implemented yet.");
    }
}
