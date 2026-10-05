using Microsoft.Extensions.Configuration;

namespace ConfigLab;

public static class AppConfiguration
{
    public const string EnvironmentVariablePrefix = "GSLAB_";

    public static IConfiguration Build(Stream baseJson, Stream? environmentJson, string[] args)
    {
        // TODO-00: Build the configuration from these sources, in increasing precedence:
        //   1. the base JSON stream
        //   2. the environment JSON stream, when it is not null
        //   3. environment variables that start with EnvironmentVariablePrefix
        //   4. command-line arguments
        //   A later source overrides an earlier one for the same key.
        throw new NotImplementedException("Not implemented yet.");
    }

    public static string GetRequiredConnectionString(this IConfiguration configuration, string name)
    {
        // TODO-01: Return the connection string called "name". When it is missing, throw an
        //   InvalidOperationException whose message contains the name, instead of returning null.
        throw new NotImplementedException("Not implemented yet.");
    }
}
