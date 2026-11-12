using Microsoft.FeatureManagement.FeatureFilters;

namespace FlagsLab;

// Who is calling: X-User and X-Groups (comma separated). A real app reads the authenticated user's claims.
public class HeaderTargetingContextAccessor(IHttpContextAccessor http) : ITargetingContextAccessor
{
    public ValueTask<TargetingContext> GetContextAsync()
    {
        var headers = http.HttpContext!.Request.Headers;
        var groups = headers["X-Groups"].ToString().Split(',', StringSplitOptions.RemoveEmptyEntries | StringSplitOptions.TrimEntries);
        return ValueTask.FromResult(new TargetingContext { UserId = headers["X-User"].ToString(), Groups = groups });
    }
}
