using System.Net;
using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Hosting;
using Microsoft.Extensions.Logging;
using Microsoft.AspNetCore.Hosting.Server;
using Microsoft.AspNetCore.Hosting.Server.Features;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;

namespace GatewayLab.Tests;

// A real Kestrel server on a free port that answers with its own name.
public sealed class TestBackend : IAsyncDisposable
{
    private readonly WebApplication _app;
    public string Name { get; }
    public string Address { get; }
    public volatile bool Healthy = true;

    private TestBackend(WebApplication app, string name, string address)
    {
        _app = app;
        Name = name;
        Address = address;
    }

    public static async Task<TestBackend> StartAsync(string name)
    {
        var builder = WebApplication.CreateBuilder();
        builder.WebHost.UseUrls("http://127.0.0.1:0");
        builder.Logging.ClearProviders();
        var app = builder.Build();
        TestBackend? self = null;
        app.MapGet("/whoami", () => name);
        app.MapGet("/echo", (HttpRequest r) => $"{name}|{r.Headers["X-Forwarded-By"]}");
        app.MapGet("/health", () => self!.Healthy ? Results.Ok() : Results.StatusCode(503));
        await app.StartAsync();
        var address = app.Services.GetRequiredService<IServer>().Features.Get<IServerAddressesFeature>()!.Addresses.First();
        self = new TestBackend(app, name, address);
        return self;
    }

    public Task StopAsync() => _app.StopAsync();

    public async ValueTask DisposeAsync()
    {
        await _app.StopAsync();
        await _app.DisposeAsync();
    }
}

public sealed class Gateway : IAsyncDisposable
{
    public TestBackend A { get; private init; } = null!;
    public TestBackend B { get; private init; } = null!;
    public HttpClient Client { get; private init; } = null!;
    private WebApplicationFactory<Program> _factory = null!;

    public static async Task<Gateway> StartAsync()
    {
        var a = await TestBackend.StartAsync("A");
        var b = await TestBackend.StartAsync("B");
        var factory = new WebApplicationFactory<Program>().WithWebHostBuilder(host =>
        {
            host.UseSetting("Backends:0", a.Address);
            host.UseSetting("Backends:1", b.Address);
        });
        return new Gateway { A = a, B = b, Client = factory.CreateClient(), _factory = factory };
    }

    public async ValueTask DisposeAsync()
    {
        Client.Dispose();
        await _factory.DisposeAsync();
        await A.DisposeAsync();
        await B.DisposeAsync();
    }
}

public class GatewayTests
{
    [Fact]
    public async Task ShouldStripThePrefixAndApplyTheTransforms()
    {
        await using var gw = await Gateway.StartAsync();

        var response = await gw.Client.GetAsync("/api/echo");

        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
        var body = await response.Content.ReadAsStringAsync();
        Assert.EndsWith("|gateway", body);
        Assert.Equal("yarp", response.Headers.GetValues("X-Gateway").Single());
    }

    [Fact]
    public async Task ShouldAlternateBetweenBackends()
    {
        await using var gw = await Gateway.StartAsync();

        var names = new List<string>();
        for (var i = 0; i < 6; i++) names.Add(await gw.Client.GetStringAsync("/api/whoami"));

        Assert.Equal(3, names.Count(n => n == "A"));
        Assert.Equal(3, names.Count(n => n == "B"));
    }

    [Fact]
    public async Task ShouldStopSendingToABackendThatIsDownWithoutWaitingForTheActiveCheck()
    {
        await using var gw = await Gateway.StartAsync();
        await gw.B.StopAsync();

        // Back-to-back requests are faster than the 1 s active interval: only the passive policy can react.
        var statuses = new List<HttpStatusCode>();
        for (var i = 0; i < 40; i++) statuses.Add((await gw.Client.GetAsync("/api/whoami")).StatusCode);

        Assert.Contains(HttpStatusCode.BadGateway, statuses);
        Assert.All(statuses.TakeLast(10), s => Assert.Equal(HttpStatusCode.OK, s));
    }

    [Fact]
    public async Task ShouldStopSendingToABackendWhoseHealthEndpointFails()
    {
        await using var gw = await Gateway.StartAsync();
        gw.B.Healthy = false;

        // B still answers /whoami with 200, so only the active /health probe can take it out.
        var deadline = DateTime.UtcNow.AddSeconds(15);
        var onlyA = false;
        while (DateTime.UtcNow < deadline && !onlyA)
        {
            await Task.Delay(500);
            var names = new List<string>();
            for (var i = 0; i < 6; i++) names.Add(await gw.Client.GetStringAsync("/api/whoami"));
            onlyA = names.All(n => n == "A");
        }

        Assert.True(onlyA, "B should have been removed by the active health check");
    }

    [Fact]
    public async Task ShouldRateLimitPerClientAtTheEdge()
    {
        await using var gw = await Gateway.StartAsync();

        async Task<HttpStatusCode> Call(string client)
        {
            var request = new HttpRequestMessage(HttpMethod.Get, "/limited/whoami");
            request.Headers.Add("X-Client-Id", client);
            return (await gw.Client.SendAsync(request)).StatusCode;
        }

        Assert.Equal(HttpStatusCode.OK, await Call("c1"));
        Assert.Equal(HttpStatusCode.OK, await Call("c1"));
        Assert.Equal(HttpStatusCode.TooManyRequests, await Call("c1"));
        Assert.Equal(HttpStatusCode.OK, await Call("c2"));
    }

    [Fact]
    public async Task ShouldAuthenticateAtTheEdgeBeforeProxying()
    {
        await using var gw = await Gateway.StartAsync();

        Assert.Equal(HttpStatusCode.Unauthorized, (await gw.Client.GetAsync("/admin/whoami")).StatusCode);

        var wrong = new HttpRequestMessage(HttpMethod.Get, "/admin/whoami");
        wrong.Headers.Add("X-Api-Key", "nope");
        Assert.Equal(HttpStatusCode.Unauthorized, (await gw.Client.SendAsync(wrong)).StatusCode);

        var right = new HttpRequestMessage(HttpMethod.Get, "/admin/whoami");
        right.Headers.Add("X-Api-Key", ApiKeyHandler.Key);
        Assert.Equal(HttpStatusCode.OK, (await gw.Client.SendAsync(right)).StatusCode);
    }
}
