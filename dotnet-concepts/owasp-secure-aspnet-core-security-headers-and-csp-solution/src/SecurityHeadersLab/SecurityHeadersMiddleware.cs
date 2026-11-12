using System.Security.Cryptography;

namespace SecurityHeadersLab;

public sealed class SecurityHeadersMiddleware(RequestDelegate next)
{
    public const string NonceKey = "csp-nonce";

    public Task InvokeAsync(HttpContext context)
    {
        // 16 random bytes per request: a nonce reused across requests is just a static allowlist entry.
        var nonce = Convert.ToBase64String(RandomNumberGenerator.GetBytes(16));
        context.Items[NonceKey] = nonce;

        // OnStarting runs right before the headers are sent, so it also covers
        // responses written by later middleware, error pages and static files.
        context.Response.OnStarting(() =>
        {
            var h = context.Response.Headers;
            h.XContentTypeOptions = "nosniff";
            h["Referrer-Policy"] = "strict-origin-when-cross-origin";
            h["Permissions-Policy"] = "camera=(), microphone=(), geolocation=()";
            h.ContentSecurityPolicy =
                $"default-src 'self'; script-src 'self' 'nonce-{nonce}'; object-src 'none'; " +
                "base-uri 'self'; frame-ancestors 'none'";
            return Task.CompletedTask;
        });

        return next(context);
    }
}
