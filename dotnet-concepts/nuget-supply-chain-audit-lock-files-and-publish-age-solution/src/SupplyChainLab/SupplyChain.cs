using System.Text;
using System.Text.Json;
using System.Xml.Linq;

namespace SupplyChainLab;

public sealed record AuditSettings(
    string Mode,                                  // "all" or "direct"
    string MinLevel,                              // "low", "moderate", "high" or "critical"
    bool FailOnAdvisories,
    IReadOnlyList<string> SuppressedAdvisories,
    bool UseLockFile,
    bool LockedMode);

public sealed record SourceSpec(string Key, string Url, int MinPublishAgeHours, string[] Patterns);

public sealed record Candidate(string Version, DateTimeOffset Published);

public sealed record ResolvedPackage(string Id, string Version, bool Direct);

public sealed record Advisory(string Id, string Version, string Severity, string Url);

public sealed record Finding(string Code, string PackageId, string Url);

public static class SupplyChain
{
    public static string ProjectFile(AuditSettings s)
    {
        var sb = new StringBuilder();
        sb.AppendLine("<Project Sdk=\"Microsoft.NET.Sdk\">");
        sb.AppendLine("  <PropertyGroup>");
        sb.AppendLine("    <TargetFramework>net10.0</TargetFramework>");
        sb.AppendLine($"    <NuGetAuditMode>{s.Mode}</NuGetAuditMode>");
        sb.AppendLine($"    <NuGetAuditLevel>{s.MinLevel}</NuGetAuditLevel>");
        if (s.FailOnAdvisories)
            sb.AppendLine("    <WarningsAsErrors>$(WarningsAsErrors);NU1901;NU1902;NU1903;NU1904</WarningsAsErrors>");
        if (s.UseLockFile)
            sb.AppendLine("    <RestorePackagesWithLockFile>true</RestorePackagesWithLockFile>");
        if (s.LockedMode)
            sb.AppendLine("    <RestoreLockedMode>true</RestoreLockedMode>");
        sb.AppendLine("  </PropertyGroup>");
        sb.AppendLine("  <ItemGroup>");
        // NuGetAuditSuppress is an item, not a property.
        foreach (var url in s.SuppressedAdvisories)
            sb.AppendLine($"    <NuGetAuditSuppress Include=\"{url}\" />");
        sb.AppendLine("  </ItemGroup>");
        sb.AppendLine("</Project>");
        return sb.ToString();
    }

    public static string NuGetConfig(IReadOnlyList<SourceSpec> sources, IReadOnlyList<string> ageExceptions)
    {
        var config = new XElement("configuration",
            new XElement("packageSources",
                new XElement("clear"),
                sources.Select(s =>
                {
                    var add = new XElement("add", new XAttribute("key", s.Key), new XAttribute("value", s.Url));
                    if (s.MinPublishAgeHours > 0) add.Add(new XAttribute("minPublishAgeHours", s.MinPublishAgeHours));
                    return add;
                })),
            // Always written, even when empty: an empty section stops inherited exceptions.
            new XElement("minPublishAgeExceptions",
                ageExceptions.Select(p => new XElement("package", new XAttribute("pattern", p)))),
            new XElement("packageSourceMapping",
                sources.Where(s => s.Patterns.Length > 0).Select(s =>
                    new XElement("packageSource", new XAttribute("key", s.Key),
                        s.Patterns.Select(p => new XElement("package", new XAttribute("pattern", p)))))));
        return config.ToString();
    }

    // The highest stable version that has been published for at least minAgeHours. 0 disables the filter.
    public static string? PickLatest(IEnumerable<Candidate> candidates, DateTimeOffset now, int minAgeHours)
    {
        return candidates
            .Where(c => !c.Version.Contains('-'))
            .Where(c => minAgeHours <= 0 || now - c.Published >= TimeSpan.FromHours(minAgeHours))
            .OrderByDescending(c => Version.Parse(c.Version))
            .Select(c => c.Version)
            .FirstOrDefault();
    }

    // Restore fails with NU1020 for a floating version when a source has a minimum age and the package is not excepted.
    public static string? CheckFloating(string version, int minAgeHours, bool excepted) =>
        version.Contains('*') && minAgeHours > 0 && !excepted ? "NU1020" : null;

    private static readonly string[] Levels = ["low", "moderate", "high", "critical"];

    public static IReadOnlyList<Finding> Audit(
        IReadOnlyList<ResolvedPackage> resolved,
        IReadOnlyList<Advisory> advisories,
        string mode,
        string minLevel,
        IReadOnlyCollection<string> suppressed)
    {
        var min = Array.IndexOf(Levels, minLevel);
        var findings = new List<Finding>();
        foreach (var package in resolved)
        {
            if (mode == "direct" && !package.Direct) continue;
            foreach (var a in advisories.Where(a => a.Id == package.Id && a.Version == package.Version))
            {
                var severity = Array.IndexOf(Levels, a.Severity);
                if (severity < min || suppressed.Contains(a.Url)) continue;
                findings.Add(new Finding($"NU190{severity + 1}", package.Id, a.Url));
            }
        }
        return findings;
    }

    // Mirrors restore in locked mode: null when the lock file still matches what was resolved.
    public static string? VerifyLock(string lockJson, IReadOnlyDictionary<string, (string Version, string Hash)> restored)
    {
        using var doc = JsonDocument.Parse(lockJson);
        var locked = doc.RootElement.GetProperty("dependencies").EnumerateObject().First().Value
            .EnumerateObject()
            .ToDictionary(p => p.Name, p => (Version: p.Value.GetProperty("resolved").GetString()!, Hash: p.Value.GetProperty("contentHash").GetString()!));

        if (locked.Count != restored.Count || locked.Any(l => !restored.TryGetValue(l.Key, out var r) || r.Version != l.Value.Version))
            return "NU1004";
        return locked.Any(l => restored[l.Key].Hash != l.Value.Hash) ? "NU1403" : null;
    }
}
