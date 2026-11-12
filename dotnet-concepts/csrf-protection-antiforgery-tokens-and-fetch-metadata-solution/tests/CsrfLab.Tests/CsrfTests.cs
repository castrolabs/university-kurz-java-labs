using System.Net;
using System.Net.Http.Json;
using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Mvc.Testing;

namespace CsrfLab.Tests;

public class CsrfTests(WebApplicationFactory<Program> factory)
    : IClassFixture<WebApplicationFactory<Program>>
{
    private const string Evil = "https://evil.example";
    private const string Partner = "https://partner.example";

    // The test host is the "site". A browser would add Sec-Fetch-Site and Origin itself;
    // here the tests add them by hand, which is the same thing the server sees.
    private HttpClient NewClient(bool useTokens = false) => factory
        .WithWebHostBuilder(b => b.UseSetting("Csrf:UseTokens", useTokens ? "true" : "false"))
        .CreateClient();

    private static Task<HttpResponseMessage> PostForm(
        HttpClient client, string path, string? site, string? origin,
        Dictionary<string, string>? fields = null, string? secret = null)
    {
        var request = new HttpRequestMessage(HttpMethod.Post, path)
        {
            Content = new FormUrlEncodedContent(fields ?? new Dictionary<string, string> { ["to"] = "bob" })
        };
        if (site is not null) request.Headers.Add("Sec-Fetch-Site", site);
        if (origin is not null) request.Headers.Add("Origin", origin);
        if (secret is not null) request.Headers.Add("X-Webhook-Secret", secret);
        return client.SendAsync(request);
    }

    // ---- Phase 1: no token middleware, only the automatic check ----

    [Fact]
    public async Task ShouldRejectACrossSiteFormPost()
    {
        var response = await PostForm(NewClient(), "/transfer", "cross-site", Evil);

        Assert.Equal(HttpStatusCode.BadRequest, response.StatusCode);
    }

    [Fact]
    public async Task ShouldRejectASameSiteFormPostFromAnotherSubdomain()
    {
        // same-site is not same-origin: a sibling subdomain is still a different origin.
        var response = await PostForm(NewClient(), "/transfer", "same-site", "https://blog.example");

        Assert.Equal(HttpStatusCode.BadRequest, response.StatusCode);
    }

    [Fact]
    public async Task ShouldRejectAFormPostWhoseOnlyMetadataIsAForeignOrigin()
    {
        var response = await PostForm(NewClient(), "/transfer", null, Evil);

        Assert.Equal(HttpStatusCode.BadRequest, response.StatusCode);
    }

    [Fact]
    public async Task ShouldAcceptASameOriginFormPostWithoutAToken()
    {
        var response = await PostForm(NewClient(), "/transfer", "same-origin", null);

        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
    }

    [Fact]
    public async Task ShouldAcceptAFormPostThatCarriesNoFetchMetadata()
    {
        // curl, a server-to-server call or an old browser: no Sec-Fetch-Site, no Origin.
        var response = await PostForm(NewClient(), "/transfer", null, null);

        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
    }

    [Fact]
    public async Task ShouldLetTheWebhookReceiveACrossSiteForm()
    {
        var response = await PostForm(NewClient(), "/webhook", "cross-site", Evil,
            new Dictionary<string, string> { ["eventName"] = "paid" }, secret: "s3cret");

        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
    }

    [Fact]
    public async Task ShouldProtectTheOptedOutWebhookWithItsOwnSecret()
    {
        var response = await PostForm(NewClient(), "/webhook", "cross-site", Evil,
            new Dictionary<string, string> { ["eventName"] = "paid" });

        Assert.Equal(HttpStatusCode.Unauthorized, response.StatusCode);
    }

    [Fact]
    public async Task ShouldTrustACrossSiteFormFromACorsAllowedOrigin()
    {
        var response = await PostForm(NewClient(), "/partner-transfer", "cross-site", Partner);

        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
    }

    [Fact]
    public async Task ShouldStillRejectACrossSiteFormFromAnOriginThePolicyDoesNotList()
    {
        var response = await PostForm(NewClient(), "/partner-transfer", "cross-site", Evil);

        Assert.Equal(HttpStatusCode.BadRequest, response.StatusCode);
    }

    [Fact]
    public async Task ShouldNotCheckAJsonEndpointEvenWhenItIsCrossSite()
    {
        var request = new HttpRequestMessage(HttpMethod.Post, "/api/transfer")
        {
            Content = JsonContent.Create(new { to = "bob" })
        };
        request.Headers.Add("Sec-Fetch-Site", "cross-site");
        request.Headers.Add("Origin", Evil);

        var response = await NewClient().SendAsync(request);

        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
    }

    // ---- Phase 2: UseAntiforgery(), the token is the final verdict ----

    [Fact]
    public async Task ShouldRejectASameOriginFormPostWithoutATokenOnceTokensAreOn()
    {
        var response = await PostForm(NewClient(useTokens: true), "/transfer", "same-origin", null);

        Assert.Equal(HttpStatusCode.BadRequest, response.StatusCode);
    }

    [Fact]
    public async Task ShouldAcceptASameOriginFormPostWithTheTokenFromTheTokenEndpoint()
    {
        var client = NewClient(useTokens: true);
        var token = await client.GetFromJsonAsync<TokenResponse>("/token");
        Assert.False(string.IsNullOrEmpty(token!.Token));

        var response = await PostForm(client, "/transfer", "same-origin", null,
            new Dictionary<string, string> { ["to"] = "bob", [token!.Field] = token.Token });

        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
    }

    [Fact]
    public async Task ShouldLetAValidTokenDecideEvenWhenTheRequestLooksCrossSite()
    {
        // The token is the final verdict: only a page that could read it can send it.
        var client = NewClient(useTokens: true);
        var token = await client.GetFromJsonAsync<TokenResponse>("/token");

        var response = await PostForm(client, "/transfer", "cross-site", Evil,
            new Dictionary<string, string> { ["to"] = "bob", [token!.Field] = token.Token });

        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
    }

    [Fact]
    public async Task ShouldRejectATokenThatDoesNotMatchTheCookie()
    {
        var token = await NewClient(useTokens: true).GetFromJsonAsync<TokenResponse>("/token");
        var other = NewClient(useTokens: true); // a different client: a different cookie jar

        var response = await PostForm(other, "/transfer", "same-origin", null,
            new Dictionary<string, string> { ["to"] = "bob", [token!.Field] = token.Token });

        Assert.Equal(HttpStatusCode.BadRequest, response.StatusCode);
    }

    [Fact]
    public async Task ShouldKeepTheWebhookOpenWhenTokensAreOn()
    {
        var response = await PostForm(NewClient(useTokens: true), "/webhook", "cross-site", Evil,
            new Dictionary<string, string> { ["eventName"] = "paid" }, secret: "s3cret");

        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
    }

    [Fact]
    public async Task ShouldKeepTheJsonEndpointUncheckedWhenTokensAreOn()
    {
        var response = await NewClient(useTokens: true)
            .PostAsJsonAsync("/api/transfer", new { to = "bob" });

        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
    }
}
