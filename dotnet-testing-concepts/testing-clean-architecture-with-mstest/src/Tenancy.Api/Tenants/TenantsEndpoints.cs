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

        group.MapPost("/", async (CreateTenantRequest request, CreateTenantHandler handler, CancellationToken ct) =>
        {
            var result = await handler.HandleAsync(request.Subdomain, ct);

            return result.Status switch
            {
                CreateTenantStatus.Created => Results.Created(
                    $"/tenants/{result.Tenant!.Id}", new TenantDto(result.Tenant.Id, result.Tenant.Subdomain)),
                CreateTenantStatus.SubdomainTaken => Results.Conflict(result.Error),
                _ => Results.BadRequest(result.Error),
            };
        });

        return app;
    }
}
