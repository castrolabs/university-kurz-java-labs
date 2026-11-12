# OWASP, Security Headers and CSP with a Nonce

## Goal

Add the response headers that close common browser-side holes (`nosniff`,
`Referrer-Policy`, `Permissions-Policy`, a Content-Security-Policy with a
per-request nonce and `frame-ancestors`) with a small middleware, and stop
mass assignment by binding a request DTO instead of the entity.

## Prerequisites

- Basic minimal APIs and middleware order
- .NET 10 SDK

## Task

`Program.cs` serves a page with an inline script, a ping endpoint and two user
endpoints. Nothing sets a security header yet, and `/users/safe` binds the
entity, so a client can send `"isAdmin": true`. The tests use
`WebApplicationFactory`, so no browser is needed: the policy is checked as the
header string, and the nonce is compared across requests.

## Instructions

Complete the following TODOs:

- TODO-00: Generate a 16-byte random nonce per request and store it in `HttpContext.Items`.
- TODO-01: Set `X-Content-Type-Options`, `Referrer-Policy` and `Permissions-Policy` inside `Response.OnStarting`.
- TODO-02: Set the `Content-Security-Policy` with the nonce, `object-src 'none'`, `base-uri 'self'` and `frame-ancestors 'none'`, without `unsafe-inline`.
- TODO-03: Add the middleware to the pipeline.
- TODO-04: Put the request nonce on the inline `<script>`.
- TODO-05: Make `/users/safe` bind `CreateUserRequest` and ignore `isAdmin`.

Run the tests until they all pass.

## Running the Lab

```bash
dotnet test
```

## Bonus (Optional)

- TODO-06 (optional): Start the app with `dotnet run`, open `/` in a browser and, in the console, check that an inline script without the nonce is blocked.
- TODO-07 (optional): Add `Strict-Transport-Security` only outside Development. Where does `UseHsts` already do it?
