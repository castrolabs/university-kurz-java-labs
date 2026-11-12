using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Mvc.Testing;

namespace CompressionLab.Tests;

public class CompressionTests(WebApplicationFactory<Program> factory)
    : IClassFixture<WebApplicationFactory<Program>>
{
    // The test client does not decompress, so Content-Encoding and the body size are what the wire carries.
    private HttpClient NewClient(bool https = false, bool httpsCompression = false, bool preferBrotli = false) => factory
        .WithWebHostBuilder(b =>
        {
            b.UseSetting("Compression:EnableForHttps", httpsCompression ? "true" : "false");
            b.UseSetting("Compression:PreferBrotli", preferBrotli ? "true" : "false");
        })
        .CreateClient(new WebApplicationFactoryClientOptions
        {
            BaseAddress = new Uri(https ? "https://localhost" : "http://localhost")
        });

    private static async Task<(string Encoding, string Vary, int Bytes)> Get(HttpClient client, string path, string? acceptEncoding)
    {
        var request = new HttpRequestMessage(HttpMethod.Get, path);
        if (acceptEncoding is not null) request.Headers.TryAddWithoutValidation("Accept-Encoding", acceptEncoding);
        var response = await client.SendAsync(request);
        var body = await response.Content.ReadAsByteArrayAsync();
        return (
            string.Join(",", response.Content.Headers.ContentEncoding),
            string.Join(",", response.Headers.Vary),
            body.Length);
    }

    [Theory]
    [InlineData("gzip", "gzip")]
    [InlineData("br", "br")]
    [InlineData("zstd", "zstd")]
    public async Task ShouldCompressWithTheEncodingTheClientAccepts(string acceptEncoding, string expected)
    {
        var result = await Get(NewClient(), "/json", acceptEncoding);

        Assert.Equal(expected, result.Encoding);
        Assert.True(result.Bytes < Catalog.Json.Length / 5, $"only {result.Bytes} of {Catalog.Json.Length}");
    }

    [Fact]
    public async Task ShouldPreferZstandardWhenTheClientAcceptsAllThree()
    {
        var result = await Get(NewClient(), "/json", "gzip, br, zstd");

        Assert.Equal("zstd", result.Encoding);
    }

    [Fact]
    public async Task ShouldHonorAQualityOfZero()
    {
        var result = await Get(NewClient(), "/json", "zstd;q=0, br;q=0, gzip");

        Assert.Equal("gzip", result.Encoding);
    }

    [Fact]
    public async Task ShouldVaryEvenWhenTheClientSendsNoAcceptEncoding()
    {
        var result = await Get(NewClient(), "/json", null);

        Assert.Equal("", result.Encoding);
        Assert.Equal(Catalog.Json.Length, result.Bytes);
        Assert.Equal("Accept-Encoding", result.Vary);
    }

    [Fact]
    public async Task ShouldLeaveAlreadyCompressedContentAlone()
    {
        var result = await Get(NewClient(), "/image", "br");

        Assert.Equal("", result.Encoding);
        Assert.Equal(50_000, result.Bytes);
    }

    [Fact]
    public async Task ShouldCompressACustomMimeTypeOnceItIsListed()
    {
        var result = await Get(NewClient(), "/events", "br");

        Assert.Equal("br", result.Encoding);
    }

    [Fact]
    public async Task ShouldNotCompressOverHttpsByDefault()
    {
        var result = await Get(NewClient(https: true), "/json", "br");

        Assert.Equal("", result.Encoding);
    }

    [Fact]
    public async Task ShouldCompressOverHttpsWhenOptedIn()
    {
        var result = await Get(NewClient(https: true, httpsCompression: true), "/json", "br");

        Assert.Equal("br", result.Encoding);
    }

    [Fact]
    public async Task ShouldDropZstandardWhenBrotliIsPreferred()
    {
        var result = await Get(NewClient(preferBrotli: true), "/json", "zstd, br, gzip");

        Assert.Equal("br", result.Encoding);
    }

    [Fact]
    public async Task ShouldMakeATinyBodyBiggerNotSmaller()
    {
        // There is no minimum size: 11 bytes of JSON grow once compressed.
        var result = await Get(NewClient(), "/tiny", "br");

        Assert.Equal("br", result.Encoding);
        Assert.True(result.Bytes > "{\"ok\":true}".Length);
    }
}
