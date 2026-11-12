using System.Text.Json;

var builder = WebApplication.CreateBuilder(args);

// TODO-00: Register response compression. Four decisions go into the options:
//  - EnableForHttps comes from the configuration key "Compression:EnableForHttps" (default false):
//    compressing HTTPS responses that mix a secret with attacker-controlled text enables BREACH.
//  - The MIME types are the defaults plus "application/x-ndjson", which is not compressed otherwise.
//  - When the key "Compression:PreferBrotli" is true, drop the default providers (zstd, br, gzip)
//    and register only Brotli and Gzip, in that order.
//  - Otherwise leave the default providers alone: .NET 11 already includes Zstandard.

var app = builder.Build();

// TODO-01: Add the compression middleware. It has to come before the endpoints that write the body.

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
