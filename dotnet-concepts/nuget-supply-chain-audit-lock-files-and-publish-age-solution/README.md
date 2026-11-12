# NuGet supply chain: audit, lock files and minimum publish age

## Goal

Express the NuGet supply chain defenses as files and rules you can test: the project properties for audit and lock files, a `nuget.config` with a minimum publish age and source mapping, the automatic version choice with that age, the audit warning codes, and the checks a locked restore makes.

## Prerequisites

- Basic C# and LINQ
- The idea of `PackageReference`, `nuget.config` and `packages.lock.json`
- .NET 10 SDK (the minimum publish age itself needs the .NET 11 SDK to take effect, the lab only writes and evaluates the configuration)

## Task

`SupplyChain` is stubbed. The tests write your project text to a temp folder and let `dotnet msbuild -getProperty -getItem` evaluate it (no restore, no network), parse your `nuget.config` as XML, and exercise the pure rules (version choice, audit, lock file) with in-memory data. Nothing here downloads a package.

## Instructions

Complete the following TODOs:

- TODO-00: `ProjectFile`: audit mode and level, warnings as errors for `NU1901` to `NU1904`, lock file properties, and `NuGetAuditSuppress` as an item.
- TODO-01: `NuGetConfig`: sources, `minPublishAgeHours` only where set, the exceptions section (also when empty) and source mapping.
- TODO-02: `PickLatest`: the highest stable version that has reached the minimum age.
- TODO-03: `CheckFloating`: `NU1020` for a floating version when an age is configured and the package is not excepted.
- TODO-04: `Audit`: direct versus all, minimum severity, suppression and the `NU1901` to `NU1904` codes.
- TODO-05: `VerifyLock`: `NU1004` when the dependency set changed, `NU1403` when only the content hash differs.

Run the tests until they all pass.

## Running the Lab

From the lab directory:

```bash
dotnet test
```

## Bonus (Optional)

- TODO-06 (optional): With the .NET 11 SDK, run `dotnet nuget update source nuget.org --min-publish-age-hours 15000` in a scratch folder with its own `nuget.config`, then `dotnet package add Newtonsoft.Json`. Which version does it pick, and which would it pick without the age?
- TODO-07 (optional): Create a project with `Microsoft.EntityFrameworkCore` 8.0.0, restore it with `NuGetAuditMode` set to `all` and then `direct`, and compare. Then pin the vulnerable transitive package with Central Package Management transitive pinning and restore again.
