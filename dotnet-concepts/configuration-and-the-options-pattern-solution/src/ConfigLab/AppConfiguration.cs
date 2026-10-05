using Microsoft.Extensions.Configuration;

namespace ConfigLab;

public static class AppConfiguration
{
    public const string EnvironmentVariablePrefix = "GSLAB_";

    public static IConfiguration Build(Stream baseJson, Stream? environmentJson, string[] args)
    {
        var builder = new ConfigurationBuilder().AddJsonStream(baseJson);

        if (environmentJson is not null)
            builder.AddJsonStream(environmentJson);

        return builder
            .AddEnvironmentVariables(EnvironmentVariablePrefix)
            .AddCommandLine(args)
            .Build();
    }

    public static string GetRequiredConnectionString(this IConfiguration configuration, string name) =>
        configuration.GetConnectionString(name)
            ?? throw new InvalidOperationException($"Connection string '{name}' is not configured.");
}
