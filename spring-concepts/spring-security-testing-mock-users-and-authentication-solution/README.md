# Spring Security Testing: Mock Users And Authentication - Solution

## Overview

This is the official solution for the Spring Security Testing: Mock Users And Authentication lab.
It implements `SecurityConfig` (a user store with two users of deliberately different account
status) and `CustomAdminSecurityContextFactory` (the manual `SecurityContext`-building technique
behind `@WithCustomAdmin`), exercised by a test suite that uses four different ways of establishing
a principal for a `MockMvc` request.

## Key Concepts

### Why this test class needs `@ExtendWith(SpringExtension.class)`

Every other lab in this track bootstraps its test with a plain
`AnnotationConfigWebApplicationContext` built by hand in `@BeforeEach`. That doesn't work here:
`@WithMockUser`, `@WithUserDetails`, and `@WithSecurityContext` are processed by
`WithSecurityContextTestExecutionListener`, a `TestExecutionListener` that Spring's TestContext
framework registers automatically — but only when `SpringExtension` is actually driving the test
lifecycle. Without it, the annotations are silently never applied and every request looks
unauthenticated (this solution's first draft reproduced exactly that failure — every `@WithMockUser`
test got `401` until the class was switched to the pattern below).

```java
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SecurityConfig.class, InventoryController.class})
@WebAppConfiguration
class SecurityConfigTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }
}
```

This is the exact non-Boot setup the Spring Security reference documentation shows — `@WebAppConfiguration`
gives the context a real `MockServletContext`, and `.apply(springSecurity())` still wires
`FilterChainProxy` in as a servlet filter, same as the other labs.

### `@WithMockUser` never touches the real user store

```java
@Test
@WithMockUser(roles = "ADMIN")
void withMockUserAdminRoleShouldReachAdmin() throws Exception {
    mockMvc.perform(get("/inventory/admin")).andExpect(status().isOk());
}
```

No `UserDetailsService` lookup, no `PasswordEncoder`, no `AuthenticationProvider` — the
`SecurityContext` is populated directly with a fabricated `UserDetails`. Fast, and completely blind
to anything your real user store would object to.

### `@WithUserDetails` loads a real user, but still skips authentication

```java
@Test
@WithUserDetails("mary")
void withUserDetailsMaryShouldStillReachViewDespiteBeingDisabled() throws Exception {
    mockMvc.perform(get("/inventory/view")).andExpect(status().isOk());
}
```

`mary`'s account is `.disabled(true)` in `SecurityConfig`. `@WithUserDetails` calls
`loadUserByUsername("mary")` and drops the resulting `UserDetails` straight into the
`SecurityContext` — it never runs `DaoAuthenticationProvider`'s account-status checks
(`AccountStatusUserDetailsChecker`), so a disabled account authenticates in the test exactly as
readily as an enabled one. Only real authentication catches it:

```java
@Test
void realAuthenticationAsMaryShouldFailBecauseAccountIsDisabled() throws Exception {
    mockMvc.perform(get("/inventory/view").with(httpBasic("mary", "mary123")))
        .andExpect(status().isUnauthorized());
}
```

Same username, same password, two different outcomes — because one test asks "is this principal
authorized?" and the other asks "would this principal even be allowed to authenticate?".

### `@WithSecurityContext` + a custom factory: build the `Authentication` yourself

```java
public class CustomAdminSecurityContextFactory implements WithSecurityContextFactory<WithCustomAdmin> {

    @Override
    public SecurityContext createSecurityContext(WithCustomAdmin withCustomAdmin) {
        var context = SecurityContextHolder.createEmptyContext();
        var authentication = UsernamePasswordAuthenticationToken.authenticated(
            withCustomAdmin.username(), null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        context.setAuthentication(authentication);
        return context;
    }
}
```

`@WithCustomAdmin(username = "carol")` grants `ROLE_ADMIN` to a username that doesn't exist in
`SecurityConfig`'s `UserDetailsService` at all — the factory never consults it. This is the same
technique `@WithUserDetails`'s own factory uses internally (with an injected `UserDetailsService`
instead of a `List.of(...)`); reading that class is the best available worked example.

## Implementation Details

### `userDetailsService()`

```java
var john = User.withUsername("john").password(passwordEncoder.encode("john123")).authorities("ROLE_ADMIN").build();
var mary = User.withUsername("mary").password(passwordEncoder.encode("mary123")).authorities("ROLE_USER").disabled(true).build();
```

`john` is a normal, enabled admin; `mary`'s only difference is `.disabled(true)` — everything else
about her account is otherwise valid. That single flag is what makes the divergence between
`@WithUserDetails` and real authentication visible.

### Bonus: TODO-06

```java
var legacyUser = User.withUsername("legacyuser")
    .password(passwordEncoder.encode("legacyuser123"))
    .authorities("ROLE_USER")
    .credentialsExpired(true)
    .build();
```

A third failure mode — expired credentials rather than a disabled account — rejected by real
authentication for a different reason, but still invisible to `@WithMockUser`/`@WithUserDetails`,
reinforcing that the gap is general, not specific to `disabled`.

## Trade-offs and Best Practices

1. **All three `@With*` annotations skip authentication — that is the feature and the trap.** A
   test suite built entirely out of `@WithMockUser`/`@WithUserDetails` never once exercises
   `AuthenticationProvider`, `PasswordEncoder`, or account-status checks. Keep a handful of real
   `httpBasic()`/`formLogin()` tests specifically to cover that path.
2. **`@WithMockUser` is fast and self-contained; `@WithUserDetails` is faithful and coupled.** Reach
   for `@WithUserDetails` when the authorities or account state your data source produces are
   themselves part of what you're testing; reach for `@WithMockUser` everywhere else.
3. **`@WithSecurityContext` buys type and content control at the cost of a class per scenario
   shape.** Only worth it when the code under test genuinely depends on the concrete
   `Authentication`/principal type or on values no builtin annotation can express.
4. **Test authentication once, authorization many times.** This lab's `realAuthenticationAs*` tests
   are deliberately few; every other endpoint-shaped question is answered with a mock user instead
   of re-driving a real login.

## Summary

- `@WithMockUser`/`@WithUserDetails`/`@WithSecurityContext` all populate the `SecurityContext`
  directly, bypassing `AuthenticationProvider` and any account-status checks it would perform
- `@WithUserDetails` is real only up to a point: it loads your actual `UserDetails`, but still
  never asks whether that account is allowed to log in
- Real `httpBasic()`/`formLogin()` requests are the only way to exercise the authentication path
  itself — disabled or expired accounts, wrong passwords, custom `AuthenticationProvider` logic
- A non-Boot `MockMvc` test class needs `@ExtendWith(SpringExtension.class)` +
  `@ContextConfiguration` (not a hand-built `AnnotationConfigWebApplicationContext`) for the
  `@With*` annotations to have any effect at all
