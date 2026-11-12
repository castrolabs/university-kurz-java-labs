using System.Security.Claims;
using System.Text.Encodings.Web;
using Microsoft.AspNetCore.Authentication;
using Microsoft.Extensions.Options;

namespace GatewayLab;

// Minimal edge authentication: one fixed key in the X-Api-Key header. A real gateway validates a JWT or a session.
public class ApiKeyHandler(IOptionsMonitor<AuthenticationSchemeOptions> options, ILoggerFactory logger, UrlEncoder encoder)
    : AuthenticationHandler<AuthenticationSchemeOptions>(options, logger, encoder)
{
    public const string SchemeName = "ApiKey";
    public const string Key = "lab-secret";

    protected override Task<AuthenticateResult> HandleAuthenticateAsync()
    {
        if (!Request.Headers.TryGetValue("X-Api-Key", out var key))
            return Task.FromResult(AuthenticateResult.NoResult());

        if (key != Key)
            return Task.FromResult(AuthenticateResult.Fail("Wrong key"));

        var identity = new ClaimsIdentity([new Claim(ClaimTypes.Name, "lab-client")], SchemeName);
        return Task.FromResult(AuthenticateResult.Success(new AuthenticationTicket(new ClaimsPrincipal(identity), SchemeName)));
    }
}
