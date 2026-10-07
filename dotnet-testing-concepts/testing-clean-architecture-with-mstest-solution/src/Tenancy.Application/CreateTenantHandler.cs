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
    public async Task<CreateTenantResult> HandleAsync(string subdomain, CancellationToken ct)
    {
        Tenant tenant;
        try
        {
            tenant = new Tenant(subdomain);
        }
        catch (ArgumentException exception)
        {
            return new CreateTenantResult(CreateTenantStatus.InvalidSubdomain, Error: exception.Message);
        }

        if (await tenants.ExistsAsync(tenant.Subdomain, ct))
            return new CreateTenantResult(
                CreateTenantStatus.SubdomainTaken, Error: $"Subdomain '{tenant.Subdomain}' is already taken.");

        await tenants.AddAsync(tenant, ct);

        return new CreateTenantResult(CreateTenantStatus.Created, tenant);
    }
}
