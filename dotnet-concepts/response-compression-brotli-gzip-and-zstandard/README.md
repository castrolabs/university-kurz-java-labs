# Response Compression: Brotli, Gzip and Zstandard

## Goal

Turn on `UseResponseCompression`, see which encoding the server picks for each `Accept-Encoding`, and make the three decisions an app really has to make: HTTPS (BREACH), extra MIME types and the order of the providers. The .NET 11 pieces are the Zstandard provider and the `Vary: Accept-Encoding` header on every response.

## Prerequisites

- Minimal APIs and middleware order (see the Middleware Pipeline concept)
- What `Accept-Encoding` and `Content-Encoding` are
- .NET 11 SDK (the lab targets `net11.0`; Zstandard and the always-on `Vary` do not exist in .NET 10)

## Task

`Program.cs` has the endpoints (`/json` with 370 KB of JSON, `/events` with the same body as `application/x-ndjson`, `/tiny`, and `/image` with 50 KB of `image/png`) but no compression. The tests use `WebApplicationFactory<Program>`. The client does not decompress, so `Content-Encoding` and the body length are what travels on the wire. To play HTTPS, the tests set the base address to `https://localhost`. No Docker, no certificate.

## Instructions

Complete the following TODOs:

- TODO-00: Register response compression with the options described in the comment (HTTPS flag from configuration, one extra MIME type, optional Brotli-first providers).
- TODO-01: Add the middleware in the right place in the pipeline.

Run the tests until they all pass. For each test, say who made the decision: the client's `Accept-Encoding`, the provider order, the MIME type list or the HTTPS flag.

## Running the Lab

From the lab directory:

```bash
dotnet test
```

## Bonus (Optional)

- TODO-02 (optional): Look at the `ShouldMakeATinyBodyBiggerNotSmaller` test. The middleware has no minimum size and `/tiny` shares its MIME type with `/json`. Write down two ways to leave small bodies alone (in the app, or in a proxy that has a threshold) and which one you would pick.
- TODO-03 (optional): Set `GzipCompressionProviderOptions.Level` to `SmallestSize` and compare the size of the gzip body with the default. What did you pay for it?
