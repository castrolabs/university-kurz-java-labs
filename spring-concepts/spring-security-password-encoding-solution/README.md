# Spring Security Password Encoding - Solution

## Overview

This is the official solution for the Spring Security Password Encoding lab. It implements
`UserRegistry`, showing how to store passwords as encoded hashes and validate credentials safely with
`PasswordEncoder.matches()`.

## Key Concepts

### The `PasswordEncoder` contract

```java
public interface PasswordEncoder {
    String encode(CharSequence rawPassword);
    boolean matches(CharSequence rawPassword, String encodedPassword);
}
```

`encode()` produces the stored form of a password. `matches()` checks a submitted raw password
against that stored form by re-encoding the input and comparing hashes — it never reverses the
encoding, and it is the *only* correct way to validate a password.

### Why `.equals()` on encoded values is wrong

`BCryptPasswordEncoder` embeds a random salt in every encoded value:

```java
var encoder = new BCryptPasswordEncoder();
encoder.encode("s3cret!"); // $2a$10$Abc123...
encoder.encode("s3cret!"); // $2a$10$Xyz789...  (different!)
```

Both values are valid encodings of `"s3cret!"`, but they are never equal to each other. Comparing a
freshly submitted (and freshly encoded) password against a stored hash with `.equals()` would reject
correct logins almost every time — `matches()` avoids this entirely by re-hashing the raw input with
the same salt/parameters embedded in the stored value and comparing the results.

## Implementation Details

### `UserRegistry.register()`

```java
public void register(String username, String rawPassword) {
    encodedPasswordsByUsername.put(username, passwordEncoder.encode(rawPassword));
}
```

Only the encoded value is ever stored — the raw password never lives in `encodedPasswordsByUsername`.

### `UserRegistry.authenticate()`

```java
public boolean authenticate(String username, String rawPassword) {
    String encodedPassword = encodedPasswordsByUsername.get(username);
    if (encodedPassword == null) {
        return false;
    }
    return passwordEncoder.matches(rawPassword, encodedPassword);
}
```

An unknown username short-circuits to `false` before ever touching the encoder. A known username
delegates the actual comparison to `matches()`.

### `UserRegistry.usingDelegatingEncoder()` (TODO-05, optional)

```java
public static UserRegistry usingDelegatingEncoder() {
    return new UserRegistry(PasswordEncoderFactories.createDelegatingPasswordEncoder());
}
```

`PasswordEncoderFactories.createDelegatingPasswordEncoder()` returns a `DelegatingPasswordEncoder`
pre-configured with bcrypt as the default algorithm. Every encoded value is prefixed with `{bcrypt}`,
and `matches()` routes to whichever encoder that prefix names — the mechanism that lets an
application migrate to a stronger algorithm later without a disruptive mass password reset.

## Trade-offs and Best Practices

1. **Never compare passwords with `==` or `.equals()`** — only `PasswordEncoder.matches()` correctly
   validates a raw password against a stored hash, e.g.:
   ```java
   if (encodedPassword.equals(passwordEncoder.encode(rawPassword))) { ... } // WRONG — almost always false
   if (passwordEncoder.matches(rawPassword, encodedPassword)) { ... }        // correct
   ```
2. **`NoOpPasswordEncoder` and the deprecated `StandardPasswordEncoder` exist only for backward
   compatibility** — never use them in new code; `BCryptPasswordEncoder` is a solid modern default,
   with `SCryptPasswordEncoder`/`Argon2PasswordEncoder` as memory-hard alternatives when
   hardware-resistant hashing matters more than simplicity.
3. **Tune the cost roughly to one second per verification** on your own hardware — too cheap makes
   brute-forcing practical, too expensive hurts login latency under load.

## Summary

- `PasswordEncoder.encode()` produces a one-way, salted hash — never the raw password
- `PasswordEncoder.matches(raw, encoded)` is the only correct way to validate credentials
- The same raw password encodes to a *different* string every time with `BCryptPasswordEncoder`,
  which is exactly why `.equals()`-style comparison is unsafe
- `DelegatingPasswordEncoder` lets an application support multiple algorithms at once via an `{id}`
  prefix on each stored hash
