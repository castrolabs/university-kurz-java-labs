# Spring Security Authorization Matcher Methods - Solution

## Overview

This is the official solution for the Spring Security Authorization Matcher Methods lab. It
implements `SecurityConfig`, a `SecurityFilterChain` whose authorization rules are ordered
deliberately to avoid a matcher-shadowing bug, and exercises it with `MockMvc` against a real
filter chain built from an `AnnotationConfigWebApplicationContext`.

## Key Concepts

### First match wins

```java
http.authorizeHttpRequests(authorize -> authorize
        .requestMatchers("/admin/health").permitAll()
        .requestMatchers("/admin/**").hasRole("ADMIN")
        .requestMatchers("/reports").hasAnyRole("MANAGER", "ADMIN")
        .anyRequest().denyAll())
```

`requestMatchers()` rules are checked top to bottom, and the *first* one that matches a request
decides the outcome — later rules are never consulted for that request. `/admin/health` has to be
declared before `/admin/**`, because `/admin/**` also matches `/admin/health` and, declared first,
would silently claim it.

### The bug this ordering avoids

The test suite's `MatcherShadowingDemonstrationTests` builds its own, deliberately misordered
chain to make the failure mode concrete:

```java
.requestMatchers("/admin/**").hasRole("ADMIN")   // declared first — too broad, too soon
.requestMatchers("/admin/health").permitAll()    // unreachable: /admin/** already matched
.anyRequest().denyAll()
```

With this ordering, an unauthenticated request to `/admin/health` returns `401` instead of `200` —
the `permitAll()` rule is syntactically present but semantically dead code.

### `anyRequest().denyAll()`: an explicit catch-all

Every path not otherwise matched is explicitly denied rather than left to an implicit default.
`denyAll()` must always be the *last* rule — Spring Security rejects a chain where a `requestMatchers()`
rule follows `anyRequest()`, since nothing after it could ever be reached.

### Two different 401/403 outcomes

- An **unauthenticated** request denied by any rule (including `denyAll()`) gets `401` with a
  `WWW-Authenticate: Basic` challenge — `ExceptionTranslationFilter` treats an anonymous principal
  as "not yet authenticated" and defers to the entry point.
- An **authenticated** request denied by a rule it doesn't satisfy gets `403` — the principal is
  known, it's simply not allowed.

Both outcomes are exercised directly: `shouldChallengeUnmatchedPathForUnauthenticatedUser` (401)
vs. `shouldDenyUnmatchedPathForAuthenticatedUserViaDenyAllCatchAll` (403), same path, same
`denyAll()` rule.

### `permitAll()` skips authorization, not authentication

`shouldRejectHealthEndpointCallWithWrongCredentials` sends *wrong* credentials to a `permitAll()`
endpoint and still gets `401`. Authentication runs before authorization is ever consulted — a
`permitAll()` endpoint never gets the chance to say "allowed" for a request whose credentials were
already rejected.

## Implementation Details

### `userDetailsService()`

Three users, one authority each, mirroring the three privilege levels the routes below check:

```java
var admin = User.withUsername("admin").password(passwordEncoder.encode("admin123")).authorities("ROLE_ADMIN").build();
var manager = User.withUsername("manager").password(passwordEncoder.encode("manager123")).authorities("ROLE_MANAGER").build();
var user = User.withUsername("user").password(passwordEncoder.encode("user123")).authorities("ROLE_USER").build();
```

### `securityFilterChain()`

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(authorize -> authorize
            .requestMatchers("/admin/health").permitAll()
            .requestMatchers("/admin/**").hasRole("ADMIN")
            .requestMatchers("/reports").hasAnyRole("MANAGER", "ADMIN")
            .anyRequest().denyAll())
        .httpBasic(Customizer.withDefaults())
        .csrf(AbstractHttpConfigurer::disable);

    return http.build();
}
```

### Bonus: TODO-08

The optional task widens `/reports` from `hasRole("MANAGER")` to `hasAnyRole("MANAGER", "ADMIN")`
— a single-argument change that does not touch the rule's position, because ordering only matters
relative to *other* matchers, not to the authorization decision a matcher makes once it wins.

## Trade-offs and Best Practices

1. **Rule order is part of the contract, not a style preference.** Reordering two
   `requestMatchers()` rules can silently change which one actually governs a path — always put
   narrower matchers before broader ones that would otherwise subsume them.
2. **`anyRequest()` must be last, and it should be explicit.** Spring Security enforces the first
   half at build time; the second half — writing `.anyRequest().denyAll()` instead of relying on
   an implicit default — is a discipline worth keeping even when it feels redundant.
3. **A `401` and a `403` mean different things and both are informative.** Don't collapse them:
   `401` says "you haven't proven who you are"; `403` says "I know who you are, and the answer is
   still no."
4. **CSRF protection is disabled in this lab's config for test simplicity** — see the
   `spring-security-csrf-protection` lab for when and how to keep it enabled.

## Summary

- `requestMatchers()` rules are first-match-wins, evaluated in declaration order
- A broad matcher declared before a narrow one silently shadows the narrow one — no error, no
  warning, just an unreachable rule
- `anyRequest()` must be the last rule in the chain; writing it out explicitly makes "everything
  else" a reviewable decision instead of an accident
- `permitAll()` only skips authorization — authentication still runs, and wrong credentials still
  fail with `401`
