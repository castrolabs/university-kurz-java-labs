using Microsoft.AspNetCore.Mvc;
using QueryLab;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddSingleton<Catalog>();

// TODO-02: QUERY is not a CORS-safelisted method, so a browser sends a preflight first.
// Register a CORS policy named "spa" for the origin https://app.test that allows the
// methods GET and QUERY and the header Content-Type, then call app.UseCors("spa") below.

// TODO-04 (optional): QueryCachePolicy is already written. Register it with
// AddOutputCache under the name "query", call app.UseOutputCache() below and apply
// .CacheOutput("query") to the QUERY endpoint.

var app = builder.Build();

// TODO-01: answer with the response header Accept-Query: application/json here and in
// the QUERY endpoint (a const string for the value helps), so a client can discover that the resource understands QUERY.
app.MapGet("/products", (Catalog catalog) => catalog.All());

// The old way: a POST that only reads. It is not safe or idempotent as far as HTTP can tell.
app.MapPost("/products/search", ([FromBody] ProductFilter filter, Catalog catalog) => catalog.Search(filter));

// TODO-00: map the QUERY method on /products with MapMethods and HttpMethods.Query.
// The handler reads a ProductFilter from the body ([FromBody]) and returns catalog.Search(filter).
// Notice what you get for free: 415 for a body that is not JSON and 400 for a missing body.

app.Run();

public partial class Program;
