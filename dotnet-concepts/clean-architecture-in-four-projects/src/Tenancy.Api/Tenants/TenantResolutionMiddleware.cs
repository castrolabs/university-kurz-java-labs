using Tenancy.Application;

namespace Tenancy.Api.Tenants;

public sealed class TenantResolutionMiddleware(RequestDelegate next)
{
    public async Task InvokeAsync(HttpContext http, ResolveTenantHandler resolve, TenantContext tenantContext)
    {
        var resolution = await resolve.HandleAsync(http.Request.Host.Host, http.RequestAborted);

        if (resolution.Status == TenantResolutionStatus.NotFound)
        {
            http.Response.StatusCode = StatusCodes.Status404NotFound;
            return;
        }

        tenantContext.Current = resolution.Tenant;
        await next(http);
    }
}
