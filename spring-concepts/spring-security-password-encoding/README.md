# Spring Security Password Encoding

## Goal

Understand the `PasswordEncoder` contract and why a stored password must only ever be checked with
`matches()` — never with `==` or `.equals()`.

## Prerequisites

- Basic Spring Framework knowledge
- Familiarity with authentication concepts (raw vs. stored credentials)

## Task

Implement `UserRegistry`, a small class that registers users by encoding their raw password with a
`PasswordEncoder`, and authenticates them later without ever storing — or comparing — the raw
password itself.

`BCryptPasswordEncoder` bakes a random salt into every encoded value it produces. That means calling
`encode()` twice on the exact same raw password produces two *different* encoded strings —
`encoder.encode("s3cret!").equals(encoder.encode("s3cret!"))` is `false`, even though both encodings
are valid for the same password. Comparing a submitted password against a stored hash with `.equals()`
would therefore reject a correct password almost every time. `PasswordEncoder.matches(rawPassword,
encodedPassword)` is the only correct way to check credentials — it re-encodes and compares
internally.

## Instructions

Complete the following TODOs:

- TODO-00: Encode and store a user's raw password on `register()`
- TODO-01, TODO-02, TODO-03: Implement `authenticate()` using `passwordEncoder.matches(...)`
- TODO-04: Implement `encodedPasswordOf()`

Run the tests until they all pass.

## Running the Lab

From the project root:

```bash
mvn -pl spring-concepts/spring-security-password-encoding test
```

Or from the lab directory:

```bash
cd spring-concepts/spring-security-password-encoding
mvn test
```

## Bonus

- TODO-05 (optional): Implement `usingDelegatingEncoder()` using
  `PasswordEncoderFactories.createDelegatingPasswordEncoder()`, so encoded values carry an `{bcrypt}`
  prefix and the application could support several algorithms at once
