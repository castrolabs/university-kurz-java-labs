# Spring Security HTTP Basic And Form Login

## Goal

Understand the two built-in ways Spring Security collects credentials from a client — HTTP Basic and
form login — and what happens when an application needs to support both at once.

## Prerequisites

- Basic Spring Framework knowledge
- Familiarity with `SecurityFilterChain`/`HttpSecurity` (see the `spring-security-user-management` lab)
- Basic familiarity with `MockMvc`

## Task

HTTP Basic sends credentials on *every* request — a header, decoded with Base64, with no session
involved — which fits a scripted or API client well but is a poor experience for a browser. Form
login trades that per-request simplicity for a server-side session: the client authenticates once via
a POST to `/login`, and every request afterward relies on the session rather than resending
credentials.

Configuring only one of the two doesn't just add a feature — it can silence behavior you assumed still
worked. Once `formLogin()` is configured, a request carrying a perfectly valid `Authorization: Basic`
header is *not* authenticated by it unless `httpBasic()` is explicitly configured too. With both
configured together, Spring Security decides which challenge to send an unauthenticated caller based
on what the request looks like: a plain API-style request gets a `401` with a `WWW-Authenticate:
Basic` header, while a browser-style request (one that accepts `text/html`) gets redirected to the
login page instead.

Implement `SecurityConfig`, a `@Configuration` class exposing a `PasswordEncoder`, a
`UserDetailsService` with a single user, and a `SecurityFilterChain` requiring authentication for
every request via both HTTP Basic and form login.

## Instructions

Complete the following TODOs in `SecurityConfig`:

- TODO-00: `passwordEncoder()` — return a `BCryptPasswordEncoder`
- TODO-01: `userDetailsService()` — register a single in-memory user
- TODO-02: `securityFilterChain()` — require authentication for every request
- TODO-03: `securityFilterChain()` — enable both `formLogin()` and `httpBasic()`
- TODO-04: `securityFilterChain()` — disable CSRF protection
- TODO-05: `securityFilterChain()` — return the built chain

Run the tests until they all pass.

## Running the Lab

From the project root:

```bash
mvn -pl spring-concepts/spring-security-http-basic-and-form-login test
```

Or from the lab directory:

```bash
cd spring-concepts/spring-security-http-basic-and-form-login
mvn test
```
