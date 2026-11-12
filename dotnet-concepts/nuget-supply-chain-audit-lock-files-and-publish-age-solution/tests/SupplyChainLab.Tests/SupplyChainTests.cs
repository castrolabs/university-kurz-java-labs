using System.Text.Json;
using System.Xml.Linq;

namespace SupplyChainLab.Tests;

public sealed class SupplyChainTests : IDisposable
{
    private readonly TempRepo _repo = new();

    public void Dispose() => _repo.Dispose();

    private static string Prop(JsonElement result, string name) =>
        result.GetProperty("Properties").GetProperty(name).GetString() ?? "";

    // ---- TODO-00: project file, evaluated by MSBuild (no restore, no network) ----

    [Fact]
    public void Project_sets_audit_mode_level_and_lock_properties()
    {
        var settings = new AuditSettings("all", "moderate", false, [], UseLockFile: true, LockedMode: true);
        var project = _repo.Write("app/app.csproj", SupplyChain.ProjectFile(settings));

        var result = _repo.Evaluate(project, "NuGetAuditSuppress",
            "NuGetAuditMode,NuGetAuditLevel,RestorePackagesWithLockFile,RestoreLockedMode");

        Assert.Equal("all", Prop(result, "NuGetAuditMode"));
        Assert.Equal("moderate", Prop(result, "NuGetAuditLevel"));
        Assert.Equal("true", Prop(result, "RestorePackagesWithLockFile"));
        Assert.Equal("true", Prop(result, "RestoreLockedMode"));
    }

    [Fact]
    public void Project_leaves_lock_properties_off_when_not_requested()
    {
        var settings = new AuditSettings("direct", "high", false, [], UseLockFile: false, LockedMode: false);
        var project = _repo.Write("app/app.csproj", SupplyChain.ProjectFile(settings));

        var result = _repo.Evaluate(project, "NuGetAuditSuppress", "RestorePackagesWithLockFile,RestoreLockedMode,WarningsAsErrors");

        Assert.Equal("", Prop(result, "RestorePackagesWithLockFile"));
        Assert.Equal("", Prop(result, "RestoreLockedMode"));
        Assert.DoesNotContain("NU1903", Prop(result, "WarningsAsErrors"));
    }

    [Fact]
    public void Audit_warnings_become_errors_and_advisories_are_suppressed_as_items()
    {
        const string url = "https://github.com/advisories/GHSA-qj66-m88j-hmgj";
        var settings = new AuditSettings("all", "low", FailOnAdvisories: true, [url], false, false);
        var project = _repo.Write("app/app.csproj", SupplyChain.ProjectFile(settings));

        var result = _repo.Evaluate(project, "NuGetAuditSuppress", "WarningsAsErrors");

        foreach (var code in new[] { "NU1901", "NU1902", "NU1903", "NU1904" })
            Assert.Contains(code, Prop(result, "WarningsAsErrors"));
        Assert.True(TempRepo.HasItem(result, "NuGetAuditSuppress", url));
    }

    // ---- TODO-01: nuget.config ----

    [Fact]
    public void NuGetConfig_writes_the_age_only_for_sources_that_have_one()
    {
        var xml = SupplyChain.NuGetConfig(
            [new("nuget.org", "https://api.nuget.org/v3/index.json", 72, ["*"]),
             new("internal", "https://contoso.test/v3/index.json", 0, ["Contoso.*"])],
            []);

        var sources = XDocument.Parse(xml).Root!.Element("packageSources")!;
        Assert.NotNull(sources.Element("clear"));
        var nuget = sources.Elements("add").Single(a => (string)a.Attribute("key")! == "nuget.org");
        var local = sources.Elements("add").Single(a => (string)a.Attribute("key")! == "internal");
        Assert.Equal("72", (string?)nuget.Attribute("minPublishAgeHours"));
        Assert.Null(local.Attribute("minPublishAgeHours"));
    }

    [Fact]
    public void NuGetConfig_keeps_an_empty_exceptions_section_so_inherited_ones_do_not_apply()
    {
        var xml = SupplyChain.NuGetConfig([new("nuget.org", "https://api.nuget.org/v3/index.json", 48, ["*"])], []);

        var exceptions = XDocument.Parse(xml).Root!.Element("minPublishAgeExceptions");
        Assert.NotNull(exceptions);
        Assert.Empty(exceptions!.Elements());
    }

    [Fact]
    public void NuGetConfig_lists_exceptions_and_source_mapping()
    {
        var xml = SupplyChain.NuGetConfig(
            [new("nuget.org", "https://api.nuget.org/v3/index.json", 72, ["*"]),
             new("internal", "https://contoso.test/v3/index.json", 0, ["Contoso.*"])],
            ["Contoso.*"]);

        var root = XDocument.Parse(xml).Root!;
        Assert.Equal(["Contoso.*"], root.Element("minPublishAgeExceptions")!.Elements("package").Select(p => (string)p.Attribute("pattern")!));
        var mapped = root.Element("packageSourceMapping")!.Elements("packageSource")
            .Single(s => (string)s.Attribute("key")! == "internal");
        Assert.Equal("Contoso.*", (string?)mapped.Element("package")!.Attribute("pattern"));
    }

    // ---- TODO-02: automatic version choice with a minimum age ----

    private static readonly DateTimeOffset Now = new(2026, 11, 12, 12, 0, 0, TimeSpan.Zero);

    private static readonly Candidate[] Versions =
    [
        new("13.0.1", Now.AddDays(-1900)),
        new("13.0.3", Now.AddDays(-1350)),
        new("13.0.4", Now.AddDays(-420)),
        new("13.0.5", Now.AddHours(-10)),
        new("14.0.0-beta1", Now.AddDays(-2)),
    ];

    [Fact]
    public void Without_an_age_the_latest_stable_version_wins()
    {
        Assert.Equal("13.0.5", SupplyChain.PickLatest(Versions, Now, 0));
    }

    [Fact]
    public void A_version_younger_than_the_minimum_age_is_skipped()
    {
        Assert.Equal("13.0.4", SupplyChain.PickLatest(Versions, Now, 72));
        Assert.Equal("13.0.3", SupplyChain.PickLatest(Versions, Now, 15_000)); // about 625 days
    }

    [Fact]
    public void The_age_boundary_is_inclusive()
    {
        var exact = new[] { new Candidate("1.0.0", Now.AddHours(-48)) };
        Assert.Equal("1.0.0", SupplyChain.PickLatest(exact, Now, 48));
        Assert.Null(SupplyChain.PickLatest(exact, Now, 49));
    }

    [Fact]
    public void Versions_are_compared_numerically_and_nothing_eligible_gives_null()
    {
        var versions = new[] { new Candidate("1.9.0", Now.AddDays(-30)), new Candidate("1.10.0", Now.AddDays(-30)) };
        Assert.Equal("1.10.0", SupplyChain.PickLatest(versions, Now, 24));
        Assert.Null(SupplyChain.PickLatest([new Candidate("2.0.0", Now.AddHours(-1))], Now, 24));
    }

    // ---- TODO-03: floating versions ----

    [Theory]
    [InlineData("13.0.*", 72, false, "NU1020")]
    [InlineData("*", 1, false, "NU1020")]
    [InlineData("13.0.*", 0, false, null)]
    [InlineData("13.0.*", 72, true, null)]
    [InlineData("13.0.3", 72, false, null)]
    public void Floating_versions_fail_restore_only_when_an_age_is_set_and_the_package_is_not_excepted(
        string version, int hours, bool excepted, string? expected)
    {
        Assert.Equal(expected, SupplyChain.CheckFloating(version, hours, excepted));
    }

    // ---- TODO-04: audit ----

    private static readonly ResolvedPackage[] Resolved =
    [
        new("Microsoft.EntityFrameworkCore", "8.0.0", Direct: true),
        new("Microsoft.Extensions.Caching.Memory", "8.0.0", Direct: false),
        new("Newtonsoft.Json", "12.0.1", Direct: true),
    ];

    private static readonly Advisory[] Advisories =
    [
        new("Microsoft.Extensions.Caching.Memory", "8.0.0", "high", "https://github.com/advisories/GHSA-qj66-m88j-hmgj"),
        new("Newtonsoft.Json", "12.0.1", "high", "https://github.com/advisories/GHSA-5crp-9r3c-p9vr"),
        new("Microsoft.EntityFrameworkCore", "8.0.0", "low", "https://example.test/low"),
        new("Newtonsoft.Json", "13.0.3", "critical", "https://example.test/other-version"),
    ];

    [Fact]
    public void Severity_decides_the_warning_code()
    {
        var all = SupplyChain.Audit(Resolved, Advisories, "all", "low", []);

        Assert.Contains(all, f => f is { Code: "NU1901", PackageId: "Microsoft.EntityFrameworkCore" });
        Assert.Contains(all, f => f is { Code: "NU1903", PackageId: "Newtonsoft.Json" });
        Assert.Equal(3, all.Count); // the advisory for another version of Newtonsoft.Json does not apply
    }

    [Fact]
    public void Direct_mode_ignores_transitive_packages()
    {
        var direct = SupplyChain.Audit(Resolved, Advisories, "direct", "low", []);

        Assert.DoesNotContain(direct, f => f.PackageId == "Microsoft.Extensions.Caching.Memory");
        Assert.Equal(2, direct.Count);
    }

    [Fact]
    public void The_minimum_level_hides_lower_severities()
    {
        var high = SupplyChain.Audit(Resolved, Advisories, "all", "high", []);

        Assert.DoesNotContain(high, f => f.Code == "NU1901");
        Assert.Equal(2, high.Count);
        Assert.Empty(SupplyChain.Audit(Resolved, Advisories, "all", "critical", []));
    }

    [Fact]
    public void A_suppressed_advisory_url_is_skipped()
    {
        var findings = SupplyChain.Audit(Resolved, Advisories, "all", "low",
            ["https://github.com/advisories/GHSA-qj66-m88j-hmgj"]);

        Assert.DoesNotContain(findings, f => f.PackageId == "Microsoft.Extensions.Caching.Memory");
        Assert.Equal(2, findings.Count);
    }

    // ---- TODO-05: lock file ----

    private const string Lock = """
        {
          "version": 2,
          "dependencies": {
            "net10.0": {
              "Newtonsoft.Json": { "type": "Direct", "requested": "[13.0.3, )", "resolved": "13.0.3", "contentHash": "HASH-A" },
              "Serilog": { "type": "Direct", "requested": "[4.0.0, )", "resolved": "4.0.0", "contentHash": "HASH-B" }
            }
          }
        }
        """;

    [Fact]
    public void A_matching_lock_file_passes()
    {
        var restored = new Dictionary<string, (string, string)>
        {
            ["Newtonsoft.Json"] = ("13.0.3", "HASH-A"),
            ["Serilog"] = ("4.0.0", "HASH-B"),
        };

        Assert.Null(SupplyChain.VerifyLock(Lock, restored));
    }

    [Fact]
    public void A_different_version_or_a_new_package_is_NU1004()
    {
        var bumped = new Dictionary<string, (string, string)>
        {
            ["Newtonsoft.Json"] = ("13.0.4", "HASH-A"),
            ["Serilog"] = ("4.0.0", "HASH-B"),
        };
        var added = new Dictionary<string, (string, string)>
        {
            ["Newtonsoft.Json"] = ("13.0.3", "HASH-A"),
            ["Serilog"] = ("4.0.0", "HASH-B"),
            ["Polly"] = ("8.0.0", "HASH-C"),
        };

        Assert.Equal("NU1004", SupplyChain.VerifyLock(Lock, bumped));
        Assert.Equal("NU1004", SupplyChain.VerifyLock(Lock, added));
    }

    [Fact]
    public void The_same_version_with_different_content_is_NU1403()
    {
        var tampered = new Dictionary<string, (string, string)>
        {
            ["Newtonsoft.Json"] = ("13.0.3", "HASH-A"),
            ["Serilog"] = ("4.0.0", "SOMETHING-ELSE"),
        };

        Assert.Equal("NU1403", SupplyChain.VerifyLock(Lock, tampered));
    }
}
