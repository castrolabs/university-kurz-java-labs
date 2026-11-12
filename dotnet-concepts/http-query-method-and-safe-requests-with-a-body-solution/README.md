# HTTP QUERY: A Safe Request With a Body (solution)

Solution of the `http-query-method-and-safe-requests-with-a-body` lab. Requires the
.NET 11 SDK (`net11.0`).

- `Program.cs`: `MapMethods("/products", [HttpMethods.Query], ...)`, the `Accept-Query`
  header, the `spa` CORS policy and the `query` output cache policy.
- `ProductsClient.cs`: `HttpRequestMessage` with `HttpMethod.Query` and `JsonContent`.
- `QueryCachePolicy.cs`: lets `QUERY` into the output cache and puts a hash of the body in
  the cache key.

```bash
dotnet test
```
