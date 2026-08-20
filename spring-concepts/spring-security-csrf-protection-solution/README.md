# Spring Security CSRF Protection - Solution

## Overview

This is the official solution for the Spring Security CSRF Protection lab. It implements
`SecurityConfig`, leaving CSRF protection at its default (enabled) everywhere except one exempted
webhook path, and `CsrfTokenController`, a single endpoint exposing the current request's token —
exercised with `MockMvc` against a real filter chain built from an
`AnnotationConfigWebApplicationContext`.

## Key Concepts

### Safe methods bypass CsrfFilter unconditionally

```java
mockMvc.perform(get("/products"))            // 200 — CsrfFilter never even looks
mockMvc.perform(post("/products"))            // 403 — no token
mockMvc.perform(post("/products").with(csrf())) // 200 — valid token supplied
```

`CsrfFilter` waves `GET`, `HEAD`, `TRACE`, and `OPTIONS` through without checking anything at all.
Every other method needs a token it previously handed the client, or the request never reaches
`ProductController`. `SecurityMockMvcRequestPostProcessors.csrf()` is `MockMvc`'s way of supplying
one without standing up a browser flow.

### Exempting one path without disabling protection everywhere

```java
http.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
    .csrf(csrf -> csrf
        .ignoringRequestMatchers("/webhooks/**")
        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()));
```

`ignoringRequestMatchers("/webhooks/**")` subtracts one path from CSRF protection; it does not
touch the "everything else" default the way `csrf(AbstractHttpConfigurer::disable)` would.
`shouldAcceptWebhookPostWithoutCsrfToken` proves the exemption; every other `POST`/`PUT`/`DELETE`
test proves protection is still fully in force everywhere else.

### Reading the token from the `_csrf` request attribute

```java
@GetMapping("/csrf")
public String csrf(HttpServletRequest request) {
    return ((CsrfToken) request.getAttribute("_csrf")).getToken();
}
```

`CsrfFilter` puts the resolved `CsrfToken` for the request on an attribute named `_csrf` before
handing off to the rest of the chain — anything downstream, including this controller, can read it
directly. This is the same mechanism the article's debugging `CsrfTokenLogger` filter uses; a real
delivery mechanism (a hidden form field, a meta tag, or — as here — a dedicated endpoint) is what
actually gets the token to a client that needs it.

### Bonus: TODO-04

```java
.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
```

Switches the token store from the default `HttpSessionCsrfTokenRepository` to a cookie the client's
JavaScript can read (`XSRF-TOKEN` by default) — the shape a same-backend single-page app needs,
since it can't read a hidden form field that was never rendered for it. `withHttpOnlyFalse()` is a
deliberate, narrow weakening: fine here because XSS already defeats CSRF protection regardless, and
wrong to apply to any other cookie in the application.

## Implementation Details

### `ProductController` and `WebhookController`

Plain, already-implemented controllers — the interesting behavior lives entirely in
`SecurityConfig`'s CSRF configuration, not in application code. `ProductController` models four
ordinary CRUD-style endpoints; `WebhookController` models exactly the case the article calls out
for exemption: a state-changing endpoint no browser with a session cookie ever calls.

## Trade-offs and Best Practices

1. **Disabling CSRF globally and exempting one path are different decisions with different
   blast radii.** `ignoringRequestMatchers("/webhooks/**")` opens exactly one door; `csrf().disable()`
   removes the wall. Reach for the former when a specific machine-to-machine endpoint doesn't rely
   on ambient session authority, not because a form was inconvenient to update.
2. **A safe-method endpoint is unprotected by construction, regardless of CSRF configuration.**
   `CsrfFilter` never inspects `GET`/`HEAD`/`TRACE`/`OPTIONS` — a state-changing operation hiding
   behind one of those methods has no CSRF protection no matter how the rest of the app is
   configured.
3. **`CookieCsrfTokenRepository.withHttpOnlyFalse()` is a targeted trade, not a general pattern.**
   It's the correct fix for a specific problem (JavaScript needs to read the token) and should not
   be reached for by default — the session-backed repository needs no such compromise.
4. **Test authentication and CSRF as adjacent but separate concerns.** `with(csrf())` supplies a
   token; it says nothing about who the caller is. A protected, authenticated endpoint needs both
   concerns satisfied independently in a test.

## Summary

- `CsrfFilter` passes `GET`/`HEAD`/`TRACE`/`OPTIONS` through unconditionally; every other method
  needs a valid token or gets `403`
- `ignoringRequestMatchers(...)` exempts specific paths without weakening protection elsewhere —
  the right tool for a machine-to-machine endpoint, the wrong one for an inconvenient form
- The token for the current request is available on the `_csrf` request attribute to anything
  downstream of `CsrfFilter` in the chain
- `CookieCsrfTokenRepository.withHttpOnlyFalse()` is how a same-backend SPA gets the token into
  JavaScript, at the cost of making that one cookie readable by any script on the page
