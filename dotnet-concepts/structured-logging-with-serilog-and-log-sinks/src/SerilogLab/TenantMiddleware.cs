using Serilog.Context;

namespace SerilogLab;

public class TenantMiddleware(RequestDelegate next)
{
    public async Task InvokeAsync(HttpContext context)
    {
        var tenant = context.Request.Headers["X-Tenant"].FirstOrDefault() ?? "none";
        // TODO-02: push "Tenant" into the Serilog LogContext (LogContext.PushProperty) for the whole request
        await next(context);
    }
}
