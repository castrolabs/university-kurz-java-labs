using System.Net;
using System.Text;
using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.Extensions.Configuration;
using Microsoft.AspNetCore.TestHost;

namespace FlagsLab.Tests;

public class FlagsTests
{
    // Flags for one test, as JSON in memory: no file, no other test sees them.
    private static WebApplicationFactory<Program> With(string flags) =>
        new WebApplicationFactory<Program>().WithWebHostBuilder(host =>
            host.ConfigureAppConfiguration((_, config) =>
            {
                config.Sources.Clear();
                var json = $$"""{ "feature_management": { "feature_flags": [ {{flags}} ] } }""";
                config.AddJsonStream(new MemoryStream(Encoding.UTF8.GetBytes(json)));
            }));

    private static async Task<string> Get(HttpClient client, string path, string? user = null, string? groups = null)
    {
        var request = new HttpRequestMessage(HttpMethod.Get, path);
        if (user is not null) request.Headers.Add("X-User", user);
        if (groups is not null) request.Headers.Add("X-Groups", groups);
        return await (await client.SendAsync(request)).Content.ReadAsStringAsync();
    }

    [Fact]
    public async Task ShouldShipEveryFlagOffByDefault()
    {
        using var factory = new WebApplicationFactory<Program>();
        var flags = await factory.CreateClient().GetStringAsync("/flags");

        Assert.Equal("""{"Banner":false,"ExpressCheckout":false,"NewPricing":false,"Promo":false,"Reports":false}""", flags);
    }

    [Theory]
    [InlineData("true", "price v2")]
    [InlineData("false", "price v1")]
    public async Task ShouldSwitchCodePathsOnAPlainFlag(string enabled, string expected)
    {
        using var factory = With($$"""{ "id": "NewPricing", "enabled": {{enabled}} }""");

        Assert.Equal(expected, await factory.CreateClient().GetStringAsync("/pricing"));
    }

    [Theory]
    [InlineData(0, "price v1")]
    [InlineData(100, "price v2")]
    public async Task ShouldApplyThePercentageFilterAtTheEdges(int percentage, string expected)
    {
        using var factory = With($$"""
            { "id": "NewPricing", "enabled": true, "conditions": { "client_filters": [
                { "name": "Microsoft.Percentage", "parameters": { "Value": {{percentage}} } } ] } }
            """);

        Assert.Equal(expected, await factory.CreateClient().GetStringAsync("/pricing"));
    }

    [Fact]
    public async Task ShouldOpenAndCloseWithATimeWindow()
    {
        string Window(DateTimeOffset start, DateTimeOffset end) => $$"""
            { "id": "Promo", "enabled": true, "conditions": { "client_filters": [
                { "name": "Microsoft.TimeWindow", "parameters": {
                    "Start": "{{start:r}}", "End": "{{end:r}}" } } ] } }
            """;
        var now = DateTimeOffset.UtcNow;

        using var open = With(Window(now.AddHours(-1), now.AddHours(1)));
        using var future = With(Window(now.AddHours(1), now.AddHours(2)));
        using var expired = With(Window(now.AddHours(-2), now.AddHours(-1)));

        Assert.Equal("promo on", await open.CreateClient().GetStringAsync("/promo"));
        Assert.Equal("promo off", await future.CreateClient().GetStringAsync("/promo"));
        Assert.Equal("promo off", await expired.CreateClient().GetStringAsync("/promo"));
    }

    private const string BetaTargeting = """
        { "id": "NewPricing", "enabled": true, "conditions": { "client_filters": [
            { "name": "Microsoft.Targeting", "parameters": { "Audience": {
                "Users": [ "alice" ],
                "Groups": [ { "Name": "beta", "RolloutPercentage": 100 } ],
                "DefaultRolloutPercentage": 0 } } } ] } }
        """;

    [Fact]
    public async Task ShouldTargetNamedUsersAndGroups()
    {
        using var factory = With(BetaTargeting);
        var client = factory.CreateClient();

        Assert.Equal("price v2", await Get(client, "/pricing", user: "alice"));
        Assert.Equal("price v2", await Get(client, "/pricing", user: "bob", groups: "beta"));
        Assert.Equal("price v1", await Get(client, "/pricing", user: "bob", groups: "other"));
        Assert.Equal("price v1", await Get(client, "/pricing"));
    }

    [Fact]
    public async Task ShouldKeepAUserInTheSameBucketWhenRollingOutByPercentage()
    {
        using var factory = With("""
            { "id": "NewPricing", "enabled": true, "conditions": { "client_filters": [
                { "name": "Microsoft.Targeting", "parameters": { "Audience": { "DefaultRolloutPercentage": 50 } } } ] } }
            """);
        var client = factory.CreateClient();

        var firstPass = new List<string>();
        var secondPass = new List<string>();
        for (var i = 0; i < 200; i++) firstPass.Add(await Get(client, "/pricing", user: $"user-{i}"));
        for (var i = 0; i < 200; i++) secondPass.Add(await Get(client, "/pricing", user: $"user-{i}"));

        Assert.Equal(firstPass, secondPass);
        var on = firstPass.Count(r => r == "price v2");
        Assert.InRange(on, 70, 130);
    }

    [Fact]
    public async Task ShouldReturn404ForAGatedMinimalApiEndpointWhileTheFlagIsOff()
    {
        using var off = With("""{ "id": "ExpressCheckout", "enabled": false }""");
        using var on = With("""{ "id": "ExpressCheckout", "enabled": true }""");

        Assert.Equal(HttpStatusCode.NotFound, (await off.CreateClient().GetAsync("/checkout/express")).StatusCode);
        Assert.Equal("express checkout", await on.CreateClient().GetStringAsync("/checkout/express"));
    }

    [Fact]
    public async Task ShouldReturn404ForAGatedControllerWhileTheFlagIsOff()
    {
        using var off = With("""{ "id": "Reports", "enabled": false }""");
        using var on = With("""{ "id": "Reports", "enabled": true }""");

        Assert.Equal(HttpStatusCode.NotFound, (await off.CreateClient().GetAsync("/reports")).StatusCode);
        Assert.Equal("reports", await on.CreateClient().GetStringAsync("/reports"));
    }

    [Fact]
    public async Task ShouldAllocateVariantsByUserGroupAndDefault()
    {
        using var factory = With("""
            { "id": "Banner", "enabled": true,
              "variants": [
                { "name": "Plain", "configuration_value": "Welcome" },
                { "name": "Bold", "configuration_value": "WELCOME BACK" },
                { "name": "Holiday", "configuration_value": "Happy holidays" } ],
              "allocation": {
                "user": [ { "variant": "Bold", "users": [ "alice" ] } ],
                "group": [ { "variant": "Holiday", "groups": [ "staff" ] } ],
                "default_when_enabled": "Plain",
                "default_when_disabled": "Plain" } }
            """);
        var client = factory.CreateClient();

        Assert.Equal("Bold|WELCOME BACK", await Get(client, "/banner", user: "alice"));
        Assert.Equal("Holiday|Happy holidays", await Get(client, "/banner", user: "bob", groups: "staff"));
        Assert.Equal("Plain|Welcome", await Get(client, "/banner", user: "carol"));
    }

    [Fact]
    public async Task ShouldListTheFlagsThatAreDefinedForCleanup()
    {
        using var factory = With("""
            { "id": "NewPricing", "enabled": true },
            { "id": "Promo", "enabled": false },
            { "id": "LongGoneExperiment", "enabled": true }
            """);

        var flags = await factory.CreateClient().GetStringAsync("/flags");

        Assert.Equal("""{"LongGoneExperiment":true,"NewPricing":true,"Promo":false}""", flags);
    }
}
