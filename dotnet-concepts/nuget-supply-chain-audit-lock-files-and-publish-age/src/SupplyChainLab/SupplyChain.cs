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
    // TODO-00: build a .csproj (Microsoft.NET.Sdk, net10.0) from the settings. Properties:
    // NuGetAuditMode, NuGetAuditLevel, RestorePackagesWithLockFile (only when UseLockFile) and
    // RestoreLockedMode (only when LockedMode). When FailOnAdvisories is set, add
    // <WarningsAsErrors>$(WarningsAsErrors);NU1901;NU1902;NU1903;NU1904</WarningsAsErrors>.
    // Trap: NuGetAuditSuppress is an ITEM (<NuGetAuditSuppress Include="advisory url" />), not a property.
    public static string ProjectFile(AuditSettings s) => throw new NotImplementedException();

    // TODO-01: build a nuget.config. Start with <clear />, add one <add key value /> per source and
    // write the minPublishAgeHours attribute only when it is greater than zero. Write a
    // <minPublishAgeExceptions> section with one <package pattern="..." /> per exception, and write
    // it even when it is empty: an empty section stops exceptions inherited from other config files.
    // Add a <packageSourceMapping> with one <packageSource key> per source that has patterns.
    public static string NuGetConfig(IReadOnlyList<SourceSpec> sources, IReadOnlyList<string> ageExceptions)
        => throw new NotImplementedException();

    // TODO-02: automatic version choice with a minimum publish age. Return the highest STABLE
    // version (no '-' suffix) that has been published for at least minAgeHours at 'now', or null
    // when none qualifies. minAgeHours <= 0 disables the filter. The boundary is inclusive.
    // Compare versions numerically (1.10.0 is higher than 1.9.0), not as text.
    public static string? PickLatest(IEnumerable<Candidate> candidates, DateTimeOffset now, int minAgeHours)
        => throw new NotImplementedException();

    // TODO-03: restore fails with NU1020 for a floating version ('*' in it) when a source has a
    // minimum age greater than zero and the package is not excepted. Return "NU1020" or null.
    public static string? CheckFloating(string version, int minAgeHours, bool excepted)
        => throw new NotImplementedException();

    // TODO-04: the audit rules. For each resolved package and each advisory with the same id AND
    // version, produce a Finding unless: mode is "direct" and the package is not direct, the
    // severity is below minLevel (low < moderate < high < critical), or the advisory url is
    // suppressed. The code follows the severity: low NU1901, moderate NU1902, high NU1903, critical NU1904.
    public static IReadOnlyList<Finding> Audit(
        IReadOnlyList<ResolvedPackage> resolved,
        IReadOnlyList<Advisory> advisories,
        string mode,
        string minLevel,
        IReadOnlyCollection<string> suppressed)
        => throw new NotImplementedException();

    // TODO-05: restore in locked mode. Read the first target framework of the lock file
    // ("dependencies" -> framework -> package -> "resolved" and "contentHash"). Return "NU1004"
    // when the set of packages or any resolved version differs from 'restored', "NU1403" when
    // everything matches but a content hash differs, and null when the lock file still matches.
    public static string? VerifyLock(string lockJson, IReadOnlyDictionary<string, (string Version, string Hash)> restored)
        => throw new NotImplementedException();
}
