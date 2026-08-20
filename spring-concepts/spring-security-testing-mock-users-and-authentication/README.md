# Spring Security Testing: Mock Users And Authentication

## Goal

Understand what `@WithMockUser`, `@WithUserDetails`, and a custom `@WithSecurityContext` factory
actually do — and don't do. All three fabricate a `SecurityContext` directly, skipping real
authentication entirely, which is exactly what makes them fast and exactly what makes them able to
hide a bug a real login would catch.

## Prerequisites

- Basic Spring Framework knowledge
- Familiarity with `UserDetailsService`/`GrantedAuthority` (see the
  `spring-security-authorization-authorities-and-roles` lab)
- Basic familiarity with `MockMvc`

## Task

The test suite (already written, and worth reading closely) registers two users through
`SecurityConfig`'s `UserDetailsService`: `john`, a normal enabled admin, and `mary`, whose account
is **disabled**. Two of the provided tests make the same request as `mary` and get two different
answers:

- `@WithUserDetails("mary")` — reaches the endpoint. It calls `loadUserByUsername("mary")` and
  drops the result straight into the `SecurityContext`; it never checks whether the account is
  enabled.
- Real HTTP Basic authentication as `mary` — fails with `401`. A genuine login runs through
  `AuthenticationManager`/`AuthenticationProvider`, which *does* check account status.

Neither result is a bug. They're testing different things: the first assumes authentication already
happened and asks "is this principal authorized?"; the second asks "would this principal even be
allowed to authenticate?" Confusing the two — treating a green `@WithUserDetails` test as proof a
disabled or locked account can't log in — is the mistake this lab is built to make impossible to
miss.

Implement `SecurityConfig` (the user store and the authorization rules the tests exercise) and
`CustomAdminSecurityContextFactory` (the manual `SecurityContext`-building technique behind the
custom `@WithCustomAdmin` annotation).

## Instructions

Complete the following TODOs:

- TODO-00: `SecurityConfig.passwordEncoder()` — return a `BCryptPasswordEncoder`
- TODO-01: `SecurityConfig.userDetailsService()` — register `john` (enabled, `ROLE_ADMIN`) and
  `mary` (**disabled**, `ROLE_USER`)
- TODO-02: `SecurityConfig.securityFilterChain()` — require `hasRole("ADMIN")` for
  `/inventory/admin`, `authenticated()` for everything else
- TODO-03: `SecurityConfig.securityFilterChain()` — enable `httpBasic()`, disable CSRF protection
- TODO-04: `SecurityConfig.securityFilterChain()` — return the built chain
- TODO-05: `CustomAdminSecurityContextFactory.createSecurityContext()` — build a `SecurityContext`
  by hand, granting `ROLE_ADMIN` to whatever username the `@WithCustomAdmin` annotation carries

Run the tests until they all pass. Pay attention to *why* each one passes or fails — that's the
actual content of this lab, more than the code itself.

## Running the Lab

From the project root:

```bash
mvn -pl spring-concepts/spring-security-testing-mock-users-and-authentication test
```

Or from the lab directory:

```bash
cd spring-concepts/spring-security-testing-mock-users-and-authentication
mvn test
```

## Bonus (Optional)

- TODO-06 (optional): Add a third user, `legacyuser`/`legacyuser123`, `ROLE_USER`, with
  `.credentialsExpired(true)` instead of `.disabled(true)`. Real authentication rejects it with a
  different failure than `mary`'s — but the mock-user annotations still skip the check entirely,
  the same as before.
