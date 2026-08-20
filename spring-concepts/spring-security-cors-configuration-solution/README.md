# Spring Security CORS Configuration - Solution

## Overview

This is the official solution for the Spring Security CORS Configuration lab. It implements
`SecurityConfig`, a centralized `CorsConfigurationSource` wired into the `SecurityFilterChain`,
and exercises it with `MockMvc` against a real filter chain built from an
`AnnotationConfigWebApplicationContext`.

## Key Concepts

### One source of truth for CORS

```java
@Bean
public UrlBasedCorsConfigurationSource corsConfigurationSource() {
    var config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("https://trusted.example.com"));
    config.setAllowedMethods(List.of("GET", "POST"));
    config.setAllowedHeaders(List.of("Content-Type", "Authorization"));

    var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}

@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
        .csrf(AbstractHttpConfigurer::disable);

    return http.build();
}
```

Once `configurationSource(...)` names an explicit bean, Spring Security's `CorsFilter` asks *only*
that bean for every request carrying an `Origin` header — preflight or actual. `@CrossOrigin` on
`PartnersController` is never consulted; `shouldIgnoreCrossOriginAnnotationWhenGlobalCorsConfigurationSourceIsConfigured`
proves it by preflighting an origin the annotation explicitly allows and watching it get rejected
anyway, because it isn't in the global source.

### CorsFilter checks more than just preflights

The one correction worth internalizing against the source article's basic example: that example
has *no* CORS configuration wired in at all, so Spring never inspects the `Origin` header and the
endpoint always runs — only the browser refuses to hand the response to the calling script. The
moment `http.cors(...)` names an explicit `CorsConfigurationSource`, `CorsFilter` starts checking
*every* request that carries an `Origin` header against it — not just preflights. An actual `GET`
from a disallowed origin is rejected with `403` before `OrdersController` ever runs
(`shouldRejectActualGetRequestFromDisallowedOrigin`). A request with no `Origin` header at all —
`curl`, a server-to-server call — skips this check entirely and always reaches the controller
(`shouldExecuteEndpointForANonBrowserCallerWithNoOriginHeader`): `CorsFilter` only activates for
requests a browser would send cross-origin.

### Preflight vs. actual request

```java
mockMvc.perform(options("/orders")
    .header(ORIGIN, "https://trusted.example.com")
    .header(ACCESS_CONTROL_REQUEST_METHOD, "POST")
    .header(ACCESS_CONTROL_REQUEST_HEADERS, "Content-Type"))
```

An `OPTIONS` request carrying `Access-Control-Request-Method` (and, for non-simple headers,
`Access-Control-Request-Headers`) is how a browser asks "would this be allowed?" before sending the
real request. `DefaultCorsProcessor` answers directly out of the `CorsConfiguration` — no
controller method runs for a preflight at all, matched or not.

### Bonus: TODO-04

```java
var adminConfig = new CorsConfiguration();
adminConfig.setAllowedOrigins(List.of("https://trusted.example.com"));
adminConfig.setAllowedMethods(List.of("GET"));

source.registerCorsConfiguration("/admin/**", adminConfig);
source.registerCorsConfiguration("/**", config);
```

`UrlBasedCorsConfigurationSource` picks the *most specific* matching pattern for a given path, not
declaration order (unlike `requestMatchers()` in `HttpSecurity`), so `/admin/**` and `/**` can be
registered in either order — the more specific pattern is looked up directly. Scoping the stricter,
read-only policy to `/admin/**` keeps the blanket `/**` registration from silently covering an area
that should be more restrictive.

## Implementation Details

### Two different origin outcomes for the same rule

`shouldIncludeAllowOriginHeaderOnActualGetRequestFromAllowedOrigin` and
`shouldRejectActualGetRequestFromDisallowedOrigin` hit the exact same endpoint, differing only in
the `Origin` header — one gets `200` with `Access-Control-Allow-Origin` echoed back, the other gets
`403` with no CORS headers at all. Both are `DefaultCorsProcessor` doing exactly what it's
configured to do; neither is a bug.

## Trade-offs and Best Practices

1. **CORS is not endpoint authorization.** This lab's `authorizeHttpRequests(...).anyRequest().permitAll()`
   is deliberate — CORS answers "which browser scripts may read this response", never "who may call
   this endpoint". Authentication and authorization are separate concerns layered on top.
2. **A single `CorsConfigurationSource` bean is the one place to audit** — but a blanket `/**`
   registration quietly covers every endpoint added later. Registering narrower patterns (like this
   lab's optional `/admin/**`) for areas that need a different policy keeps that audit meaningful.
3. **`@CrossOrigin` and a `CorsConfigurationSource` bean don't combine — the bean wins outright.**
   Mixing the two without realizing it is a common way to ship an endpoint the frontend still can't
   call, despite the annotation insisting otherwise.
4. **`allowedOrigins("*")` is fine here only because credentials are never involved.** The moment
   `allowCredentials(true)` enters the picture, a wildcard origin is rejected outright by
   `CorsConfiguration` — `allowedOriginPatterns` is the replacement.

## Summary

- A `CorsConfigurationSource` bean wired into `http.cors(...)` is the single source of truth for
  CORS once it exists — `@CrossOrigin` elsewhere in the app is not consulted
- `CorsFilter` checks every request carrying an `Origin` header, not only preflights — an actual
  request from a disallowed origin is rejected with `403` before the controller runs
- A request with no `Origin` header at all bypasses CORS checking entirely — non-browser clients
  are unaffected by any of this
- `UrlBasedCorsConfigurationSource` resolves by most-specific pattern match, not registration order
