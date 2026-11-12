# CSRF Protection: Antiforgery Tokens and Fetch Metadata (Solution)

Official solution for the `csrf-protection-antiforgery-tokens-and-fetch-metadata` lab. It needs the .NET 11 SDK.

## What changed from the starter

- `AddAntiforgery()` and `UseAntiforgery()` (only when `Csrf:UseTokens` is true). With the middleware on, the token is the final verdict: a same-origin form post without a token gets a 400, and a post with a valid token passes even when the headers say cross-site.
- `/token` calls `GetAndStoreTokens`, which writes the antiforgery cookie and returns the request token (`__RequestVerificationToken` is the default field name).
- `/webhook` has `.DisableAntiforgery()`. The endpoint is now guarded by its own shared secret, so a missing secret is a 401, not a 400.
- `AddCors` with the `partner` policy, `UseCors()` and `.RequireCors("partner")` on `/partner-transfer`. A cross-site form post whose `Origin` the policy allows passes the automatic check; any other origin gets a 400.

## Why it is shaped this way

- The automatic check needs no code. Without `UseAntiforgery()`, a cross-site form post is a 400, `same-origin` and `none` pass, `same-site` (a sibling subdomain) is a 400, and a request with no `Sec-Fetch-Site` and no `Origin` passes because it is not a browser cross-site request.
- Neither defense checks `/api/transfer`: a JSON endpoint has no antiforgery metadata. Its protection is that a cross-origin browser cannot send `application/json` without a CORS preflight, which is a different mechanism with different holes.
- `Csrf:UseTokens` exists only so one app can show both phases. A real app picks one.

## Running

```bash
dotnet test
```
