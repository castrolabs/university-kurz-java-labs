using Serilog.Context;

namespace SerilogLab;

public class TenantMiddleware(RequestDelegate next)
{
    public async Task InvokeAsync(HttpContext context)
    {
        var tenant = context.Request.Headers["X-Tenant"].FirstOrDefault() ?? "none";
        using (LogContext.PushProperty("Tenant", tenant))
        {
            await next(context);
        }
    }
}
