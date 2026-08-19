# Spring Security Authorization: Authorities And Roles - Solution

## Overview

This is the official solution for the Spring Security Authorization: Authorities And Roles lab. It
implements `AuthorityGrantor` and `AccessChecks`, demonstrating the `ROLE_` prefix asymmetry between
`hasAuthority()` and `hasRole()`.

## Key Concepts

### `GrantedAuthority`: one permission, one string

```java
public interface GrantedAuthority extends Serializable {
    String getAuthority();
}
```

Roles use the exact same contract as authorities — the only distinguishing marker is a `ROLE_`
prefix on the name. There is no separate "role" type in Spring Security.

### The `ROLE_` prefix asymmetry

- `hasAuthority("ADMIN")` checks for the literal authority string `"ADMIN"` — no prefixing.
- `hasRole("ADMIN")` checks for the authority string `"ROLE_ADMIN"` — it *prepends* `ROLE_`
  internally before comparing.

Granting a user `"ADMIN"` (via `.authorities("ADMIN")`) and then checking with `hasRole("ADMIN")`
silently denies access, because `hasRole()` is actually looking for `"ROLE_ADMIN"`. The mismatch is
silent for `authorities()`/`hasAuthority()` — nothing fails at startup, the check simply evaluates to
`false` at request time.

## Implementation Details

### `AuthorityGrantor`

```java
public UserDetails grantAdminRole(String username, String password) {
    return User.withUsername(username).password(password).authorities("ROLE_ADMIN").build();
}

public UserDetails grantAdminAuthorityWithoutRolePrefix(String username, String password) {
    return User.withUsername(username).password(password).authorities("ADMIN").build();
}
```

`grantAdminRole()` grants the correctly-prefixed authority a role-based check expects.
`grantAdminAuthorityWithoutRolePrefix()` deliberately grants the bare `"ADMIN"` string — this is the
mistake the lab's tests expose.

### `AuthorityGrantor.grantAdminRoleUsingRolesBuilder()` (TODO-04, optional)

```java
public UserDetails grantAdminRoleUsingRolesBuilder(String username, String password) {
    return User.withUsername(username).password(password).roles("ADMIN").build();
}
```

`.roles("ADMIN")` reaches the same `"ROLE_ADMIN"` authority as `.authorities("ROLE_ADMIN")`, but adds
the prefix automatically. Passing an already-prefixed value to `.roles()` (`.roles("ROLE_ADMIN")`)
throws at build time — unlike the silent mismatch that `.authorities()` allows.

### `AccessChecks`

```java
public boolean isGrantedByRole(UserDetails user, String role) {
    var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    return AuthorityAuthorizationManager.<Object>hasRole(role)
        .authorize(() -> authentication, new Object())
        .isGranted();
}
```

`AuthorityAuthorizationManager` is the same authorization component Spring Security's
`authorizeHttpRequests()` DSL builds under the hood for `hasRole()`/`hasAuthority()` rules — using it
directly lets this lab check authorization logic without a servlet filter chain or a running web
application. `isGrantedByAuthority()` mirrors it with `hasAuthority()` instead.

## Trade-offs and Best Practices

1. **The `ROLE_` prefix asymmetry is a real, easy mistake, not a documentation quirk.**
   `authorities("ROLE_ADMIN")` paired with `hasRole("ADMIN")` is correct; swapping either side breaks
   silently.
2. **Roles are authorities in disguise, not a separate mechanism.** Treating them as unrelated
   concepts makes the prefix rule feel arbitrary instead of explaining itself.
3. **Prefer the named methods (`hasAuthority()`/`hasRole()`) over raw SpEL (`access()`)** — they read
   clearly and stay debuggable; reach for `access()`/`WebExpressionAuthorizationManager` only when a
   rule genuinely can't be expressed with the named methods.

## Summary

- `hasAuthority(x)` checks for the literal authority string `x`
- `hasRole(x)` checks for the authority string `"ROLE_" + x`
- A user must be granted `"ROLE_ADMIN"` (not `"ADMIN"`) for `hasRole("ADMIN")` to succeed
- `User.builder().roles(...)` adds the `ROLE_` prefix automatically; `.authorities(...)` does not
- `AuthorityAuthorizationManager` lets authorization rules be unit-tested directly against an
  `Authentication`, without a web request or filter chain
