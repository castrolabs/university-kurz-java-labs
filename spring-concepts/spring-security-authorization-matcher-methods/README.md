# Spring Security Authorization Matcher Methods

## Goal

Understand how `requestMatchers()` rules are evaluated — in declaration order, first match wins —
and the trap that falls out of it: a broad matcher declared before a narrower, more specific one
silently shadows it.

## Prerequisites

- Basic Spring Framework knowledge
- Familiarity with `hasRole()`/`hasAuthority()` (see the `spring-security-authorization-authorities-and-roles` lab)
- Basic familiarity with `MockMvc`

## Task

`requestMatchers("/admin/**").hasRole("ADMIN")` and `requestMatchers("/admin/health").permitAll()`
look independent, but they are not: authorization rules are checked in the order they are
declared, and the *first* matcher that matches a request wins. Declare the broad `/admin/**` rule
before the narrow `/admin/health` rule and `/admin/health` silently starts requiring `ROLE_ADMIN`
too — the narrower rule is never even consulted. The test suite includes a
`MatcherShadowingDemonstrationTests` class that reproduces this bug in isolation, independent of
your own configuration, so you can see the failure mode before you have to avoid it.

Implement `SecurityConfig`, a `@Configuration` class exposing a `PasswordEncoder`, a
`UserDetailsService` with three users of different privilege levels, and a `SecurityFilterChain`
whose authorization rules are ordered so that:

- `/admin/health` is reachable by anyone, with no credentials at all.
- `/admin/**` (everything else under `/admin`) requires `ROLE_ADMIN`.
- `/reports` requires `ROLE_MANAGER`.
- Every other request is explicitly denied — a reviewable catch-all, not an implicit default.

## Instructions

Complete the following TODOs in `SecurityConfig`:

- TODO-00: `passwordEncoder()` — return a `BCryptPasswordEncoder`
- TODO-01: `userDetailsService()` — register three in-memory users: `admin`/`ROLE_ADMIN`,
  `manager`/`ROLE_MANAGER`, `user`/`ROLE_USER`
- TODO-02: `securityFilterChain()` — declare the `/admin/health` rule (`permitAll()`) **first**
- TODO-03: `securityFilterChain()` — declare the `/admin/**` rule (`hasRole("ADMIN")`)
  **immediately after** TODO-02
- TODO-04: `securityFilterChain()` — declare the `/reports` rule (`hasRole("MANAGER")`)
- TODO-05: `securityFilterChain()` — declare `anyRequest().denyAll()` as the catch-all
- TODO-06: `securityFilterChain()` — enable `httpBasic()` and disable CSRF protection
- TODO-07: `securityFilterChain()` — return the built chain

Run the tests until they all pass.

## Running the Lab

From the project root:

```bash
mvn -pl spring-concepts/spring-security-authorization-matcher-methods test
```

Or from the lab directory:

```bash
cd spring-concepts/spring-security-authorization-matcher-methods
mvn test
```

## Bonus (Optional)

- TODO-08 (optional): Change the `/reports` rule from TODO-04 to
  `hasAnyRole("MANAGER", "ADMIN")` so admins can also reach it, without changing its position
  relative to the other rules.
