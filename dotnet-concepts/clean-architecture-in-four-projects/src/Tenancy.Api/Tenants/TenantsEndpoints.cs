using Tenancy.Application;

namespace Tenancy.Api.Tenants;

public sealed record CreateTenantRequest(string Subdomain);

public sealed record TenantDto(Guid Id, string Subdomain);

public static class TenantsEndpoints
{
    public static IEndpointRouteBuilder MapTenantsEndpoints(this IEndpointRouteBuilder app)
    {
        var group = app.MapGroup("/tenants").WithTags("Tenants");

        group.MapGet("/current", (ITenantContext context) =>
            context.Current is { } tenant
                ? Results.Ok(new TenantDto(tenant.Id, tenant.Subdomain))
                : Results.NotFound());

        // TODO-06: POST "/" calls CreateTenantHandler and translates its result into HTTP:
        //   Created -> 201 with a Location header and a TenantDto (never the entity),
        //   SubdomainTaken -> 409, InvalidSubdomain -> 400, both with the error message.
        //   The handler knows nothing about HTTP; this is the only place that does.

        return app;
    }
}
