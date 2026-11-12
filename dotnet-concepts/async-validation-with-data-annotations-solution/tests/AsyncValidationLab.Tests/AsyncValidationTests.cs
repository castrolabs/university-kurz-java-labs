using System.Net;
using System.Net.Http.Json;
using System.Text.Json;
using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.Extensions.DependencyInjection;

namespace AsyncValidationLab.Tests;

public class AsyncValidationTests(WebApplicationFactory<Program> factory)
    : IClassFixture<WebApplicationFactory<Program>>
{
    private sealed class SpyRepository : IUserRepository
    {
        public int Calls;
        public readonly TaskCompletionSource<string> SlowOutcome = new();

        public async Task<bool> EmailExistsAsync(string email, CancellationToken cancellationToken)
        {
            Interlocked.Increment(ref Calls);
            if (email == "slow@example.com")
            {
                try
                {
                    await Task.Delay(1500, cancellationToken);
                    SlowOutcome.TrySetResult("completed");
                }
                catch (OperationCanceledException)
                {
                    SlowOutcome.TrySetResult("canceled");
                    throw;
                }
                return false;
            }
            await Task.Yield();
            return email == "taken@example.com";
        }
    }

    private (HttpClient Client, SpyRepository Spy) NewClient()
    {
        var spy = new SpyRepository();
        var client = factory
            .WithWebHostBuilder(b => b.ConfigureServices(s => s.AddSingleton<IUserRepository>(spy)))
            .CreateClient();
        return (client, spy);
    }

    private static async Task<JsonElement> Errors(HttpResponseMessage response)
    {
        Assert.Equal("application/problem+json", response.Content.Headers.ContentType?.MediaType);
        using var doc = JsonDocument.Parse(await response.Content.ReadAsStringAsync());
        return doc.RootElement.GetProperty("errors").Clone();
    }

    [Fact]
    public async Task ShouldAcceptAFreeEmail()
    {
        var (client, _) = NewClient();

        var response = await client.PostAsJsonAsync("/users", new { Name = "Ana", Email = "free@example.com" });

        Assert.Equal(HttpStatusCode.Created, response.StatusCode);
    }

    [Fact]
    public async Task ShouldRejectATakenEmailWithAProblemDetails()
    {
        var (client, _) = NewClient();

        var response = await client.PostAsJsonAsync("/users", new { Name = "Ana", Email = "taken@example.com" });

        Assert.Equal(HttpStatusCode.BadRequest, response.StatusCode);
        var errors = await Errors(response);
        Assert.Equal("Email already registered.", errors.GetProperty("Email")[0].GetString());
    }

    [Fact]
    public async Task ShouldReportSyncAndAsyncFailuresTogether()
    {
        var (client, _) = NewClient();

        var response = await client.PostAsJsonAsync("/users", new { Name = "", Email = "taken@example.com" });

        var errors = await Errors(response);
        Assert.True(errors.TryGetProperty("Name", out _));
        Assert.True(errors.TryGetProperty("Email", out _));
    }

    [Fact]
    public async Task ShouldNotCallTheRepositoryWhenASyncAttributeOnTheSamePropertyFails()
    {
        var (client, spy) = NewClient();

        var response = await client.PostAsJsonAsync("/users", new { Name = "Ana", Email = "not-an-email" });

        Assert.Equal(HttpStatusCode.BadRequest, response.StatusCode);
        Assert.Equal(0, spy.Calls);
    }

    [Fact]
    public async Task ShouldRunTheObjectLevelAsyncRuleAndNameTheField()
    {
        var (client, _) = NewClient();

        var response = await client.PostAsJsonAsync("/orders",
            new { Start = "2026-12-10", End = "2026-12-01", Quantity = 1 });

        Assert.Equal(HttpStatusCode.BadRequest, response.StatusCode);
        var errors = await Errors(response);
        Assert.Equal("End must be after Start.", errors.GetProperty("End")[0].GetString());
    }

    [Fact]
    public async Task ShouldSkipTheObjectLevelRuleWhenAPropertyRuleFails()
    {
        var (client, _) = NewClient();

        var response = await client.PostAsJsonAsync("/orders",
            new { Start = "2026-12-10", End = "2026-12-01", Quantity = 0 });

        var errors = await Errors(response);
        Assert.True(errors.TryGetProperty("Quantity", out _));
        Assert.False(errors.TryGetProperty("End", out _));
    }

    [Fact]
    public async Task ShouldCancelTheLookupWhenTheClientGivesUp()
    {
        var (client, spy) = NewClient();
        using var cts = new CancellationTokenSource(TimeSpan.FromMilliseconds(200));

        await Assert.ThrowsAnyAsync<OperationCanceledException>(() =>
            client.PostAsJsonAsync("/users", new { Name = "Ana", Email = "slow@example.com" }, cts.Token));

        var outcome = await spy.SlowOutcome.Task.WaitAsync(TimeSpan.FromSeconds(5));
        Assert.Equal("canceled", outcome);
    }
}
