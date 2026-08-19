# Spring Security HTTP Basic And Form Login - Solution

## Overview

This is the official solution for the Spring Security HTTP Basic And Form Login lab. It implements
`SecurityConfig`, a `SecurityFilterChain` that requires authentication for every request and accepts
it through either HTTP Basic or form login, and exercises it with `MockMvc` against a real filter
chain built from an `AnnotationConfigWebApplicationContext`.

## Key Concepts

### HTTP Basic: stateless, credentials on every request

```java
http.httpBasic(Customizer.withDefaults());
```

The client sends an `Authorization: Basic <base64(username:password)>` header on every request. There
is no session and no login page — a great fit for scripted/API clients, a poor one for a browser.

### Form login: an autoconfigured login page, then a session

```java
http.formLogin(Customizer.withDefaults());
```

An unauthenticated visitor is redirected to `/login`. A successful `POST /login` establishes an HTTP
session; every request afterward is authenticated via that session, not by resending credentials.

### Combining both isn't automatic

The lab's tests enforce this directly: with only `formLogin()` configured, an `Authorization: Basic`
header on an unauthenticated request is ignored — the request is redirected to `/login` instead of
being accepted. Both must be explicitly chained together:

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
        .formLogin(Customizer.withDefaults())
        .httpBasic(Customizer.withDefaults())
        .csrf(AbstractHttpConfigurer::disable);

    return http.build();
}
```

### Which challenge fires for an unauthenticated request?

With both mechanisms configured, Spring Security resolves the challenge per request using content
negotiation: a request that does not accept `text/html` (a typical API/`curl` call) gets a `401` with
a `WWW-Authenticate: Basic` header; a request that accepts `text/html` (a browser navigating to a
page) gets redirected to `/login` instead. The tests exercise both cases explicitly.

## Implementation Details

### `passwordEncoder()` and `userDetailsService()`

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}

@Bean
public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
    var alice = User.withUsername("alice")
        .password(passwordEncoder.encode("password"))
        .authorities("ROLE_USER")
        .build();

    return new InMemoryUserDetailsManager(alice);
}
```

A single user, `alice`/`password`, registered with an encoded password — the same
`PasswordEncoder.matches()` mechanism from the `spring-security-password-encoding` lab validates it
during both HTTP Basic and form login.

### Testing without a running server

```java
var context = new AnnotationConfigWebApplicationContext();
context.register(SecurityConfig.class, GreetingController.class);
context.setServletContext(new MockServletContext());
context.refresh();

mockMvc = MockMvcBuilders.webAppContextSetup(context)
    .apply(springSecurity())
    .build();
```

`SecurityMockMvcConfigurers.springSecurity()` finds the `springSecurityFilterChain` bean that
`@EnableWebSecurity` registers and wires it into `MockMvc` as a real servlet filter — every request
in the tests goes through the actual `SecurityFilterChain`, not a mocked-out shortcut.

`SecurityMockMvcRequestPostProcessors.httpBasic(username, password)` attaches a Basic auth header to a
request; `SecurityMockMvcRequestBuilders.formLogin()` posts to `/login` with the given credentials.

## Trade-offs and Best Practices

1. **HTTP Basic sends credentials on every request, over nothing more than Base64** — fine over TLS
   for scripted/API clients, a poor fit for a human typing into a browser: no session, no logout, no
   login page.
2. **Form login trades that statelessness for a server-side session** — a good fit for a small
   application, not one that needs horizontal scalability without a shared session store.
3. **The presence of one authentication mechanism does not imply the other still works** — enabling
   `formLogin()` after relying on `httpBasic()` (or vice versa) is a common way to silently break API
   clients or browser logins; both need to be configured explicitly when an application supports both
   kinds of caller.
4. **CSRF protection is disabled in this lab's config for test simplicity** — a real browser-facing
   form login deployment should keep CSRF protection enabled and have its login form (and any
   state-changing POST) carry the CSRF token.

## Summary

- `httpBasic()` authenticates every request independently via a header — stateless
- `formLogin()` authenticates once via `POST /login`, then relies on the session
- Enabling one does not implicitly enable the other — both must be configured explicitly to support
  both kinds of client on the same application
- With both configured, Spring Security chooses the unauthenticated-request challenge (401 Basic vs.
  redirect-to-login) based on what the request's `Accept` header suggests about the caller
