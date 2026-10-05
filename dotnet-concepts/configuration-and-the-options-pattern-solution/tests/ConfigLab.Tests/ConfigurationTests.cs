using System.Text;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Options;

namespace ConfigLab.Tests;

public class ConfigurationTests
{
    private const string BaseJson = """
        {
          "ConnectionStrings": { "GameStore": "Data Source=GameStore.db" },
          "Store": { "Name": "Kurz Games", "PageSize": 10 }
        }
        """;

    private static MemoryStream Json(string json) => new(Encoding.UTF8.GetBytes(json));

    private static IConfiguration Build(string? environmentJson = null, params string[] args) =>
        AppConfiguration.Build(Json(BaseJson), environmentJson is null ? null : Json(environmentJson), args);

    private static async Task<IHost> StartHostAsync(IConfiguration configuration)
    {
        var builder = Host.CreateEmptyApplicationBuilder(new HostApplicationBuilderSettings());
        builder.Services.AddStoreOptions(configuration);
        var host = builder.Build();
        await host.StartAsync();
        return host;
    }

    [Fact]
    public void ShouldReadValuesFromTheBaseFile()
    {
        var configuration = Build();

        Assert.Equal("Kurz Games", configuration["Store:Name"]);
        Assert.Equal("Data Source=GameStore.db", configuration.GetConnectionString("GameStore"));
    }

    [Fact]
    public void ShouldLetTheEnvironmentFileOverrideTheBaseFile()
    {
        var configuration = Build("""{ "Store": { "PageSize": 25 } }""");

        Assert.Equal("25", configuration["Store:PageSize"]);
        Assert.Equal("Kurz Games", configuration["Store:Name"]);
    }

    [Fact]
    public void ShouldLetAnEnvironmentVariableOverrideBothFiles()
    {
        const string variable = AppConfiguration.EnvironmentVariablePrefix + "Store__PageSize";
        Environment.SetEnvironmentVariable(variable, "50");
        try
        {
            var configuration = Build("""{ "Store": { "PageSize": 25 } }""");

            Assert.Equal("50", configuration["Store:PageSize"]);
        }
        finally
        {
            Environment.SetEnvironmentVariable(variable, null);
        }
    }

    [Fact]
    public void ShouldLetTheCommandLineOverrideEverythingElse()
    {
        const string variable = AppConfiguration.EnvironmentVariablePrefix + "Store__PageSize";
        Environment.SetEnvironmentVariable(variable, "50");
        try
        {
            var configuration = Build(null, "--Store:PageSize=75");

            Assert.Equal("75", configuration["Store:PageSize"]);
        }
        finally
        {
            Environment.SetEnvironmentVariable(variable, null);
        }
    }

    [Fact]
    public void ShouldExplainWhichConnectionStringIsMissing()
    {
        var configuration = Build();

        var exception = Assert.Throws<InvalidOperationException>(
            () => configuration.GetRequiredConnectionString("Reports"));

        Assert.Contains("Reports", exception.Message);
        Assert.Equal("Data Source=GameStore.db", configuration.GetRequiredConnectionString("GameStore"));
    }

    [Fact]
    public async Task ShouldBindTheStoreSectionToTypedOptions()
    {
        using var host = await StartHostAsync(Build());

        var options = host.Services.GetRequiredService<IOptions<StoreOptions>>().Value;

        Assert.Equal("Kurz Games", options.Name);
        Assert.Equal(10, options.PageSize);
    }

    [Fact]
    public async Task ShouldUseTheDefaultPageSizeWhenTheKeyIsMissing()
    {
        var configuration = new ConfigurationBuilder()
            .AddInMemoryCollection(new Dictionary<string, string?> { ["Store:Name"] = "Kurz Games" })
            .Build();

        using var host = await StartHostAsync(configuration);

        Assert.Equal(20, host.Services.GetRequiredService<IOptions<StoreOptions>>().Value.PageSize);
    }

    [Fact]
    public async Task ShouldRefuseToStartWhenARequiredSettingIsMissing()
    {
        var configuration = new ConfigurationBuilder()
            .AddInMemoryCollection(new Dictionary<string, string?> { ["Store:PageSize"] = "10" })
            .Build();

        var exception = await Assert.ThrowsAsync<OptionsValidationException>(() => StartHostAsync(configuration));

        Assert.Contains("Name", exception.Message);
    }

    [Fact]
    public async Task ShouldRefuseToStartWhenTheValueIsOutOfRange()
    {
        var configuration = new ConfigurationBuilder()
            .AddInMemoryCollection(new Dictionary<string, string?>
            {
                ["Store:Name"] = "Kurz Games",
                ["Store:PageSize"] = "500",
            })
            .Build();

        await Assert.ThrowsAsync<OptionsValidationException>(() => StartHostAsync(configuration));
    }
}
