using Microsoft.AspNetCore.Mvc;
using QueryLab;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddSingleton<Catalog>();

// QUERY is not a CORS-safelisted method, so a browser sends a preflight first.
builder.Services.AddCors(options => options.AddPolicy("spa", policy => policy
    .WithOrigins("https://app.test")
    .WithMethods("GET", "QUERY")
    .WithHeaders("Content-Type")));

builder.Services.AddOutputCache(options => options.AddPolicy("query", new QueryCachePolicy()));

var app = builder.Build();

app.UseCors("spa");
app.UseOutputCache();

const string AcceptQuery = "application/json";

app.MapGet("/products", (Catalog catalog, HttpResponse response) =>
{
    response.Headers["Accept-Query"] = AcceptQuery;
    return catalog.All();
});

// The old way: a POST that only reads. It is not safe or idempotent as far as HTTP can tell.
app.MapPost("/products/search", ([FromBody] ProductFilter filter, Catalog catalog) => catalog.Search(filter));

app.MapMethods("/products", [HttpMethods.Query],
    ([FromBody] ProductFilter filter, Catalog catalog, HttpResponse response) =>
    {
        response.Headers["Accept-Query"] = AcceptQuery;
        return catalog.Search(filter);
    })
    .CacheOutput("query");

app.Run();

public partial class Program;
