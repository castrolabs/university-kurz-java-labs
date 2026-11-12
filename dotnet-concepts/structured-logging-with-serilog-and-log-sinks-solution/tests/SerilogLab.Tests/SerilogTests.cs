using System.Net.Http.Json;
using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Configuration;
using Serilog.Core;
using Serilog.Events;

namespace SerilogLab.Tests;

public sealed class InMemorySink : ILogEventSink
{
    private readonly List<LogEvent> _events = [];

    public IReadOnlyList<LogEvent> Events { get { lock (_events) return [.. _events]; } }

    public void Emit(LogEvent logEvent) { lock (_events) _events.Add(logEvent); }

    public static string Text(LogEventPropertyValue value) => value.ToString().Trim('"');
}

public class SerilogTests : IDisposable
{
    private readonly InMemorySink _sink = new();
    private readonly string _logDir = Path.Combine(Path.GetTempPath(), $"serilog-lab-{Guid.NewGuid():N}");
    private readonly WebApplicationFactory<Program> _factory;
    private readonly HttpClient _client;

    public SerilogTests()
    {
        _factory = new WebApplicationFactory<Program>().WithWebHostBuilder(host =>
        {
            host.ConfigureAppConfiguration((_, c) => c.AddInMemoryCollection(new Dictionary<string, string?> { ["Serilog:WriteTo:1:Args:path"] = Path.Combine(_logDir, "lab-.ndjson") }));
            host.ConfigureServices(services => services.AddSingleton<ILogEventSink>(_sink));
        });
        _client = _factory.CreateClient();
    }

    public void Dispose()
    {
        _client.Dispose();
        _factory.Dispose();
        if (Directory.Exists(_logDir)) Directory.Delete(_logDir, true);
    }

    private LogEvent Single(Func<LogEvent, bool> where) => _sink.Events.Single(where);

    private static bool FromOrders(LogEvent e) =>
        e.Properties.TryGetValue("SourceContext", out var c) && InMemorySink.Text(c) == "SerilogLab.OrderService";

    [Fact]
    public async Task ShouldKeepOrderIdAndTotalAsStructuredProperties()
    {
        await _client.PostAsync("/orders/42?total=9.90", null);

        var e = Single(FromOrders);
        Assert.Equal("Order {OrderId} placed, total {Total}", e.MessageTemplate.Text);
        Assert.Equal("42", e.Properties["OrderId"].ToString());
        Assert.Equal("9.90", e.Properties["Total"].ToString());
    }

    [Fact]
    public async Task ShouldCarryTheTenantFromTheLogContextOnEveryLineOfTheRequest()
    {
        using var request = new HttpRequestMessage(HttpMethod.Post, "/orders/7?total=1");
        request.Headers.Add("X-Tenant", "acme");
        await _client.SendAsync(request);

        Assert.Equal("acme", InMemorySink.Text(Single(FromOrders).Properties["Tenant"]));
    }

    [Fact]
    public async Task ShouldMaskTheEmailWhenDestructuringTheCustomer()
    {
        await _client.PostAsJsonAsync("/customers", new Customer(5, "ana@example.com"));

        var customer = Assert.IsType<StructureValue>(Single(FromOrders).Properties["Customer"]);
        var props = customer.Properties.ToDictionary(p => p.Name, p => InMemorySink.Text(p.Value));
        Assert.Equal("5", props["Id"]);
        Assert.Equal("a***@example.com", props["Email"]);
        Assert.DoesNotContain(_sink.Events, e => e.RenderMessage().Contains("ana@example.com"));
    }

    [Fact]
    public async Task ShouldGenerateTheRefusedOrderLogWithEventId3001()
    {
        await _client.PostAsync("/orders/9/refuse", null);

        var e = Single(FromOrders);
        Assert.Equal(LogEventLevel.Warning, e.Level);
        Assert.Equal("3001", ((StructureValue)e.Properties["EventId"]).Properties.Single(p => p.Name == "Id").Value.ToString());
        Assert.Equal("\"out of stock\"", e.Properties["Reason"].ToString());
    }

    [Fact]
    public async Task ShouldWriteOneRequestSummaryWithTenantAndStatus()
    {
        using var request = new HttpRequestMessage(HttpMethod.Post, "/orders/3?total=2");
        request.Headers.Add("X-Tenant", "globex");
        await _client.SendAsync(request);

        var summary = Single(e => e.Properties.TryGetValue("SourceContext", out var c) && InMemorySink.Text(c) == "Serilog.AspNetCore.RequestLoggingMiddleware");
        Assert.Equal("HTTP {RequestMethod} {RequestPath} responded {StatusCode} in {Elapsed:0.0000} ms", summary.MessageTemplate.Text);
        Assert.Equal("200", summary.Properties["StatusCode"].ToString());
        Assert.Equal("globex", InMemorySink.Text(summary.Properties["Tenant"]));
    }

    [Fact]
    public async Task ShouldLogUnhandledExceptionsAsErrorWithTheException()
    {
        var response = await _client.GetAsync("/boom");

        Assert.Equal(System.Net.HttpStatusCode.InternalServerError, response.StatusCode);

        var summary = Single(e => e.Level == LogEventLevel.Error && e.Exception is not null
            && e.Properties.TryGetValue("SourceContext", out var c) && InMemorySink.Text(c) == "Serilog.AspNetCore.RequestLoggingMiddleware");
        Assert.Equal("kaboom", summary.Exception!.Message);
    }

    [Fact]
    public async Task ShouldApplyTheMinimumLevelOverrideFromAppsettings()
    {
        await _client.PostAsync("/orders/1?total=1", null);

        Assert.Contains(_sink.Events, FromOrders);
        Assert.DoesNotContain(_sink.Events, e => e.Properties.TryGetValue("SourceContext", out var c)
            && InMemorySink.Text(c).StartsWith("Microsoft.AspNetCore.Hosting") && e.Level < LogEventLevel.Warning);
    }

    [Fact]
    public async Task ShouldWriteCompactJsonToARollingFile()
    {
        await _client.PostAsync("/orders/11?total=5", null);
        _client.Dispose();
        _factory.Dispose();

        var file = Assert.Single(Directory.GetFiles(_logDir));
        Assert.Matches(@"lab-\d{8}\.ndjson$", file);
        var lines = File.ReadAllLines(file);
        Assert.Contains(lines, l => l.Contains("\"@mt\":\"Order {OrderId} placed, total {Total}\"") && l.Contains("\"OrderId\":11"));
    }
}
