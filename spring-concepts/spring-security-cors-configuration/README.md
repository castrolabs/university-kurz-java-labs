# Spring Security CORS Configuration

## Goal

Understand that CORS is a browser-side relaxation, not a server-side restriction, and configure it
centrally with a `CorsConfigurationSource` on the `SecurityFilterChain` — including the surprising
consequence that doing so makes `@CrossOrigin` on a controller stop mattering.

## Prerequisites

- Basic Spring Framework knowledge
- Familiarity with `SecurityFilterChain`/`HttpSecurity`
- Basic familiarity with `MockMvc`

## Task

A browser refuses to let JavaScript running on one origin read a response from another, unless the
server's response carries the right `Access-Control-Allow-*` headers. For anything other than a
simple `GET`/`HEAD`/`POST` (or a `POST` with a body type other than form-encoded), the browser
sends a preflight `OPTIONS` request first, asking whether the real request would be allowed — and
if a `CorsConfigurationSource` bean is wired into the `SecurityFilterChain`, it is the *only* thing
that answers that preflight. A `@CrossOrigin` annotation on a controller method is not consulted at
all once that bean exists — a fact this lab's `PartnersController` deliberately puts to the test.

Implement `SecurityConfig`, a `@Configuration` class exposing a `UrlBasedCorsConfigurationSource`
that allows exactly one trusted origin, and a `SecurityFilterChain` that wires it into
`http.cors(...)`.

## Instructions

Complete the following TODOs in `SecurityConfig`:

- TODO-00: `corsConfigurationSource()` — build a `CorsConfiguration` allowing origin
  `https://trusted.example.com`, methods `GET`/`POST`, and headers `Content-Type`/`Authorization`
- TODO-01: `corsConfigurationSource()` — register it on a `UrlBasedCorsConfigurationSource` for
  `/**` and return the source
- TODO-02: `securityFilterChain()` — enable `http.cors(...)` pointed at the bean from TODO-01,
  permit every request, and disable CSRF protection
- TODO-03: `securityFilterChain()` — return the built chain

Run the tests until they all pass.

## Running the Lab

From the project root:

```bash
mvn -pl spring-concepts/spring-security-cors-configuration test
```

Or from the lab directory:

```bash
cd spring-concepts/spring-security-cors-configuration
mvn test
```

## Bonus (Optional)

- TODO-04 (optional): Register a second, stricter `CorsConfiguration` on the same source for
  `/admin/**` — same trusted origin, but only the `GET` method, modelling a read-only surface for
  that origin.
