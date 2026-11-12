# Response Compression: Brotli, Gzip and Zstandard (Solution)

Official solution for the `response-compression-brotli-gzip-and-zstandard` lab. It needs the .NET 11 SDK.

## What changed from the starter

- `AddResponseCompression` with `EnableForHttps` read from `Compression:EnableForHttps`, `MimeTypes` set to the defaults plus `application/x-ndjson`, and, when `Compression:PreferBrotli` is true, the provider list cleared and rebuilt with Brotli then Gzip.
- `UseResponseCompression()` before the endpoints.

## Why it is shaped this way

- With no provider configured, .NET 11 offers Zstandard, Brotli and Gzip in that order, so `Accept-Encoding: gzip, br, zstd` gets `zstd`. A quality of zero (`br;q=0`) removes that encoding from the choice.
- Every response gets `Vary: Accept-Encoding`, even when the client sent no `Accept-Encoding`, so a shared cache does not hand a compressed body to a client that cannot read it. A content type that is not compressed (the PNG) does not get it.
- Compression is off for HTTPS by default. Turning it on is a decision about BREACH, not a performance switch: it is safe for responses with no secret next to reflected input.
- There is no minimum body size. The 11 byte body of `/tiny` becomes larger after Brotli, which is the argument for letting a proxy with a threshold do the work.
- `Providers.Clear()` is how you drop Zstandard. Adding only Brotli and Gzip afterwards is what makes `zstd, br, gzip` come back as `br`.

## Running

```bash
dotnet test
```
