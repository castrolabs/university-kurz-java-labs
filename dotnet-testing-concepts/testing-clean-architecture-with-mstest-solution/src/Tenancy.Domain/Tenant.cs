using System.Text.RegularExpressions;

namespace Tenancy.Domain;

public sealed partial class Tenant
{
    public Guid Id { get; private set; }

    public string Subdomain { get; private set; }

    public Tenant(string subdomain)
    {
        Id = Guid.CreateVersion7();
        Subdomain = Normalize(subdomain);
    }

    public void Rename(string subdomain) => Subdomain = Normalize(subdomain);

    public static string Normalize(string subdomain)
    {
        var normalized = (subdomain ?? string.Empty).Trim().ToLowerInvariant();

        if (!SubdomainPattern().IsMatch(normalized))
            throw new ArgumentException(
                "A subdomain is 1 to 63 letters, digits or hyphens and cannot start or end with a hyphen.",
                nameof(subdomain));

        return normalized;
    }

    [GeneratedRegex("^[a-z0-9]([a-z0-9-]{0,61}[a-z0-9])?$")]
    private static partial Regex SubdomainPattern();
}
