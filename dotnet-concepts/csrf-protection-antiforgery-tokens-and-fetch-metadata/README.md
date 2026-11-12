# CSRF Protection: Antiforgery Tokens and Fetch Metadata

## Goal

See the two CSRF defenses of ASP.NET Core answer the same requests: the automatic check by `Sec-Fetch-Site` and `Origin` that .NET 11 adds, and the antiforgery token that decides once `UseAntiforgery()` is on. Wire the exceptions a real app needs (a webhook, a trusted partner site) and confirm what neither defense covers.

## Prerequisites

- Basic minimal APIs and middleware order
- CORS basics (see the CORS concept)
- .NET 11 SDK (the lab targets `net11.0`; the automatic check does not exist in .NET 10)

## Task

`Program.cs` has the endpoints (`/transfer`, `/webhook`, `/partner-transfer`, `/api/transfer`, `/token`) but none of the wiring. The tests play the browser by adding `Sec-Fetch-Site` and `Origin` headers by hand through `WebApplicationFactory`, so no browser is needed. No Docker.

## Instructions

Complete the following TODOs:

- TODO-00: Register the antiforgery services.
- TODO-01: Register the `partner` CORS policy (with credentials).
- TODO-02: Add the CORS middleware.
- TODO-03: Add `UseAntiforgery()` only when `Csrf:UseTokens` is true.
- TODO-04: Make `/token` issue the cookie and return the field name and request token.
- TODO-05: Opt `/webhook` out of antiforgery.
- TODO-06: Require the `partner` CORS policy on `/partner-transfer`.

Run the tests until they all pass. For each test, say which mechanism produced the status code: the automatic check, the token, CORS, or none.

## Running the Lab

From the lab directory:

```bash
dotnet test
```
