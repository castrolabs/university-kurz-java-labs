using System.Security.Cryptography;

namespace SecurityHeadersLab;

public sealed class SecurityHeadersMiddleware(RequestDelegate next)
{
    public const string NonceKey = "csp-nonce";

    public Task InvokeAsync(HttpContext context)
    {
        // TODO-00: generate a nonce of 16 random bytes (RandomNumberGenerator.GetBytes),
        // Base64 encoded, new on every request, and store it in context.Items[NonceKey].

        // TODO-01: register context.Response.OnStarting(...) and, inside it, set:
        //   X-Content-Type-Options: nosniff
        //   Referrer-Policy: strict-origin-when-cross-origin
        //   Permissions-Policy: camera=(), microphone=(), geolocation=()
        // Why OnStarting and not setting them before next(context)?

        // TODO-02: in the same callback set Content-Security-Policy:
        //   default-src 'self'; script-src 'self' 'nonce-<nonce>'; object-src 'none';
        //   base-uri 'self'; frame-ancestors 'none'
        // Do not use 'unsafe-inline'.

        return next(context);
    }
}
