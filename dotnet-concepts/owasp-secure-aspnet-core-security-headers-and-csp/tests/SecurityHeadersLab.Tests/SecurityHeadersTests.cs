using System.Net.Http.Json;
using System.Text.RegularExpressions;
using Microsoft.AspNetCore.Mvc.Testing;

namespace SecurityHeadersLab.Tests;

public class SecurityHeadersTests(WebApplicationFactory<Program> factory)
    : IClassFixture<WebApplicationFactory<Program>>
{
    private readonly HttpClient _client = factory.CreateClient();

    private static string Csp(HttpResponseMessage r) => r.Headers.GetValues("Content-Security-Policy").Single();

    private static string NonceIn(string csp) => Regex.Match(csp, "'nonce-([^']+)'").Groups[1].Value;

    [Theory]
    [InlineData("/")]
    [InlineData("/api/ping")]
    [InlineData("/missing")]
    public async Task ShouldSendTheStaticHeadersOnEveryResponse(string path)
    {
        var response = await _client.GetAsync(path);

        Assert.Equal("nosniff", response.Headers.GetValues("X-Content-Type-Options").Single());
        Assert.Equal("strict-origin-when-cross-origin", response.Headers.GetValues("Referrer-Policy").Single());
        Assert.Contains("camera=()", response.Headers.GetValues("Permissions-Policy").Single());
    }

    [Fact]
    public async Task ShouldForbidFramingAndPlugins()
    {
        var csp = Csp(await _client.GetAsync("/"));

        Assert.Contains("frame-ancestors 'none'", csp);
        Assert.Contains("object-src 'none'", csp);
        Assert.DoesNotContain("unsafe-inline", csp);
    }

    [Fact]
    public async Task ShouldUseANewNoncePerRequest()
    {
        var first = NonceIn(Csp(await _client.GetAsync("/")));
        var second = NonceIn(Csp(await _client.GetAsync("/")));

        Assert.NotEmpty(first);
        Assert.NotEqual(first, second);
        Assert.Equal(16, Convert.FromBase64String(first).Length);
    }

    [Fact]
    public async Task ShouldPutTheHeaderNonceOnTheInlineScript()
    {
        var response = await _client.GetAsync("/");
        var nonce = NonceIn(Csp(response));

        Assert.Contains($"<script nonce=\"{nonce}\">", await response.Content.ReadAsStringAsync());
    }

    [Fact]
    public async Task UnsafeEndpointLetsTheClientBecomeAdmin()
    {
        var response = await _client.PostAsJsonAsync("/users/unsafe", new { name = "mallory", isAdmin = true });
        var user = await response.Content.ReadFromJsonAsync<User>();

        Assert.True(user!.IsAdmin); // the overposting hole, kept to show what the DTO fixes
    }

    [Fact]
    public async Task SafeEndpointIgnoresIsAdminFromTheBody()
    {
        var response = await _client.PostAsJsonAsync("/users/safe", new { name = "mallory", isAdmin = true });
        var user = await response.Content.ReadFromJsonAsync<User>();

        Assert.Equal("mallory", user!.Name);
        Assert.False(user.IsAdmin);
    }
}
