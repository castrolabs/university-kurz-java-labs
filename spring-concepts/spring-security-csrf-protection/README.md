# Spring Security CSRF Protection

## Goal

Understand why a state-changing `POST`/`PUT`/`DELETE` fails with `403` in a default Spring
Security setup while a `GET` never does, and how to exempt one legitimately non-browser endpoint
without weakening CSRF protection everywhere else.

## Prerequisites

- Basic Spring Framework knowledge
- Familiarity with `SecurityFilterChain`/`HttpSecurity`
- Basic familiarity with `MockMvc`

## Task

`CsrfFilter` lets `GET`, `HEAD`, `TRACE`, and `OPTIONS` through unconditionally. Every other
method — a `POST`, `PUT`, or `DELETE` — needs a token the server handed out earlier, or the
request fails with `403` before it ever reaches a controller. `MockMvc`'s `with(csrf())` supplies
a valid token for exactly this reason; a request built without it is indistinguishable, from the
filter's point of view, from a forged one.

Not every state-changing endpoint fits that model, though — a webhook called by another service,
never by a browser carrying a session cookie, has no CSRF exposure to begin with and needs to be
exempted explicitly rather than left broken.

Implement `SecurityConfig`, a `@Configuration` class whose `SecurityFilterChain` leaves CSRF
protection at its default (enabled) everywhere except `/webhooks/**`. Then implement
`CsrfTokenController`, a single endpoint that exposes the current request's token.

## Instructions

Complete the following TODOs:

- TODO-00: `SecurityConfig.securityFilterChain()` — permit every request
- TODO-01: `SecurityConfig.securityFilterChain()` — exempt `/webhooks/**` from CSRF protection
  with `ignoringRequestMatchers(...)`, leaving CSRF protection enabled everywhere else
- TODO-02: `SecurityConfig.securityFilterChain()` — return the built chain
- TODO-03: `CsrfTokenController.csrf()` — return the current request's token value

Run the tests until they all pass.

## Running the Lab

From the project root:

```bash
mvn -pl spring-concepts/spring-security-csrf-protection test
```

Or from the lab directory:

```bash
cd spring-concepts/spring-security-csrf-protection
mvn test
```

## Bonus (Optional)

- TODO-04 (optional): Switch the token repository to
  `CookieCsrfTokenRepository.withHttpOnlyFalse()` in the `csrf(...)` customizer from TODO-01, so
  the token is also delivered via a JavaScript-readable cookie — the pattern a same-backend
  single-page app frontend needs.
