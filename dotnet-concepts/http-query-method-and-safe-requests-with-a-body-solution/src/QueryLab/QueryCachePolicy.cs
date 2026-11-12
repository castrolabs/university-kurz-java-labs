using System.Security.Cryptography;
using Microsoft.AspNetCore.OutputCaching;

namespace QueryLab;

/// <summary>
/// The default output cache policy only stores GET and HEAD. This one lets QUERY in and puts
/// a hash of the request body in the cache key, so two different bodies never share an entry
/// (RFC 10008 requires the content to be part of the key).
/// </summary>
public sealed class QueryCachePolicy : IOutputCachePolicy
{
    public async ValueTask CacheRequestAsync(OutputCacheContext context, CancellationToken cancellation)
    {
        var request = context.HttpContext.Request;
        if (!HttpMethods.IsQuery(request.Method)) return;

        request.EnableBuffering();
        var hash = Convert.ToHexString(await SHA256.HashDataAsync(request.Body, cancellation));
        request.Body.Position = 0;

        context.CacheVaryByRules.VaryByValues["body"] = hash;
        context.EnableOutputCaching = true;
        context.AllowCacheLookup = true;
        context.AllowCacheStorage = true;
        context.AllowLocking = true;
    }

    public ValueTask ServeFromCacheAsync(OutputCacheContext context, CancellationToken cancellation) => ValueTask.CompletedTask;

    public ValueTask ServeResponseAsync(OutputCacheContext context, CancellationToken cancellation) => ValueTask.CompletedTask;
}
