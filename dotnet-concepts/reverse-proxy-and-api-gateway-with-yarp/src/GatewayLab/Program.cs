using System.Threading.RateLimiting;
using GatewayLab;

var builder = WebApplication.CreateBuilder(args);

// Backend addresses come from configuration ("Backends:0", "Backends:1"): the tests start two Kestrel servers on free ports.
var backends = builder.Configuration.GetSection("Backends").Get<string[]>() ?? [];

builder.Services.AddReverseProxy()
    .LoadFromMemory(GatewayConfig.Routes(), GatewayConfig.Clusters(backends));

builder.Services.AddAuthentication(ApiKeyHandler.SchemeName).AddScheme<Microsoft.AspNetCore.Authentication.AuthenticationSchemeOptions, ApiKeyHandler>(ApiKeyHandler.SchemeName, null);
builder.Services.AddAuthorizationBuilder().AddPolicy("apikey", p => p.RequireAuthenticatedUser());

builder.Services.AddRateLimiter(o =>
{
    o.RejectionStatusCode = StatusCodes.Status429TooManyRequests;
    o.AddPolicy("per-client", ctx => RateLimitPartition.GetFixedWindowLimiter(
        ctx.Request.Headers["X-Client-Id"].ToString(),
        _ => new FixedWindowRateLimiterOptions { PermitLimit = 2, Window = TimeSpan.FromSeconds(30) }));
});

var app = builder.Build();

app.UseRateLimiter();
app.UseAuthentication();
app.UseAuthorization();
app.MapReverseProxy();
app.Run();

public partial class Program;
