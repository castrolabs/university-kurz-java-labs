using System.Text.Json;
using Microsoft.AspNetCore.ResponseCompression;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddResponseCompression(options =>
{
    // Over HTTPS, compressing a page that mixes a secret with attacker-controlled text enables BREACH.
    // The default is off; the flag exists so the decision is explicit and testable.
    options.EnableForHttps = builder.Configuration.GetValue<bool>("Compression:EnableForHttps");
    options.MimeTypes = ResponseCompressionDefaults.MimeTypes.Concat(["application/x-ndjson"]);

    if (builder.Configuration.GetValue<bool>("Compression:PreferBrotli"))
    {
        options.Providers.Clear();
        options.Providers.Add<BrotliCompressionProvider>();
        options.Providers.Add<GzipCompressionProvider>();
    }
});

var app = builder.Build();

// Before anything that writes a body. A middleware placed after the endpoint never sees the response.
app.UseResponseCompression();

var catalog = Catalog.Json;

app.MapGet("/json", () => Results.Text(catalog, "application/json"));
app.MapGet("/events", () => Results.Text(catalog, "application/x-ndjson"));
app.MapGet("/tiny", () => Results.Text("{\"ok\":true}", "application/json"));
app.MapGet("/image", () => Results.Bytes(new byte[50_000], "image/png"));

app.Run();

public static class Catalog
{
    public static readonly string Json = Build();

    private static string Build()
    {
        var rnd = new Random(42);
        var items = Enumerable.Range(1, 2000).Select(i => new
        {
            id = i,
            name = $"Product {i}",
            sku = $"SKU-{rnd.Next(100000):D5}",
            price = Math.Round(rnd.NextDouble() * 500, 2),
            tags = new[] { "new", "sale", "eu" },
            description = "A reasonably long description that repeats across many records in a catalog."
        });
        return JsonSerializer.Serialize(items);
    }
}

public partial class Program;
