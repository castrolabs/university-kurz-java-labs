using System.Diagnostics;
using System.Text.Json;

namespace SupplyChainLab.Tests;

/// <summary>A unique temp directory plus a helper that evaluates a project with the .NET CLI.</summary>
public sealed class TempRepo : IDisposable
{
    public string Root { get; } = Path.Combine(Path.GetTempPath(), "supply-chain-lab-" + Guid.NewGuid().ToString("N"));

    public TempRepo() => Directory.CreateDirectory(Root);

    public string Write(string relativePath, string content)
    {
        var path = Path.Combine(Root, relativePath);
        Directory.CreateDirectory(Path.GetDirectoryName(path)!);
        File.WriteAllText(path, content);
        return path;
    }

    /// <summary>Runs 'dotnet msbuild -getItem -getProperty': evaluation only, no restore, no network.</summary>
    public JsonElement Evaluate(string projectPath, string items, string properties)
    {
        var psi = new ProcessStartInfo("dotnet")
        {
            RedirectStandardOutput = true,
            RedirectStandardError = true,
            UseShellExecute = false,
            WorkingDirectory = Path.GetDirectoryName(projectPath)!,
        };
        foreach (var arg in new[] { "msbuild", projectPath, $"-getItem:{items}", $"-getProperty:{properties}" })
            psi.ArgumentList.Add(arg);

        foreach (var key in psi.Environment.Keys.ToList())
        {
            if (key.StartsWith("MSBUILD", StringComparison.OrdinalIgnoreCase) || key == "DOTNET_HOST_PATH")
                psi.Environment.Remove(key);
        }
        psi.Environment["DOTNET_CLI_TELEMETRY_OPTOUT"] = "1";
        psi.Environment["DOTNET_NOLOGO"] = "1";
        psi.Environment["DOTNET_SKIP_FIRST_TIME_EXPERIENCE"] = "1";

        using var process = Process.Start(psi)!;
        var stdout = process.StandardOutput.ReadToEndAsync();
        var stderr = process.StandardError.ReadToEndAsync();
        // 120 s is only a safety net against a hung child process.
        if (!process.WaitForExit(120_000))
        {
            process.Kill(true);
            throw new TimeoutException("dotnet msbuild did not finish");
        }
        var output = stdout.Result;
        Assert.True(process.ExitCode == 0, $"dotnet msbuild failed:\n{output}\n{stderr.Result}");
        return JsonDocument.Parse(output).RootElement.Clone();
    }

    /// <summary>Metadata value of the first item with this identity, or null if the item or metadata is missing.</summary>
    public static string? Metadata(JsonElement result, string itemType, string identity, string metadata)
    {
        if (!result.GetProperty("Items").TryGetProperty(itemType, out var list)) return null;
        foreach (var item in list.EnumerateArray())
        {
            if (item.GetProperty("Identity").GetString() != identity) continue;
            return item.TryGetProperty(metadata, out var m) ? m.GetString() : null;
        }
        return null;
    }

    public static bool HasItem(JsonElement result, string itemType, string identity)
    {
        if (!result.GetProperty("Items").TryGetProperty(itemType, out var list)) return false;
        return list.EnumerateArray().Any(i => i.GetProperty("Identity").GetString() == identity);
    }

    public void Dispose()
    {
        try { Directory.Delete(Root, recursive: true); } catch (IOException) { }
    }
}
