# HTTP QUERY: A Safe Request With a Body

## Goal

Replace a `POST /products/search` that only reads with the HTTP `QUERY` method:
map it in a minimal API, advertise it with `Accept-Query`, allow it through CORS,
call it with `HttpClient`, and (optionally) cache it by body.

## Prerequisites

- Basic minimal APIs and `WebApplicationFactory`
- HTTP verbs, safe and idempotent methods
- **.NET 11 SDK** (the lab targets `net11.0`: it uses `HttpMethod.Query` and the 11.0.0 test packages)

## Task

The catalog API lists products with `GET /products` and searches with
`POST /products/search`. Add the `QUERY` version of the search on `/products`. The
tests describe the contract; read their names first.

## Instructions

Complete the following TODOs:

- TODO-00: map `QUERY /products` with `MapMethods` and `HttpMethods.Query`, reading a
  `ProductFilter` from the body. A non-JSON body must give `415` and a missing body `400`.
- TODO-01: answer with `Accept-Query: application/json` on `GET /products` and on the
  QUERY endpoint.
- TODO-02: register a CORS policy `spa` (origin `https://app.test`, methods `GET` and
  `QUERY`, header `Content-Type`) and use it, so the preflight for `QUERY` succeeds.
- TODO-03: implement `ProductsClient.SearchAsync` with `HttpMethod.Query` and `JsonContent`.

Run the tests until they all pass.

## Running the Lab

From the lab directory:

```bash
dotnet test
```

## Bonus (Optional)

- TODO-04: `QueryCachePolicy` is already written. Register it with `AddOutputCache`
  under the name `query`, add `UseOutputCache()` and `.CacheOutput("query")` on the
  QUERY endpoint. The last test checks that the same body runs the filter once and
  another body runs it again. Try `.CacheOutput()` without the policy and see why the
  default does not cache `QUERY`.
