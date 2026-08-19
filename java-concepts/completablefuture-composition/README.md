# CompletableFuture Composition

## Goal

Chain dependent asynchronous steps without ending up with a nested
`CompletableFuture<CompletableFuture<T>>`, and attach failure handling as
part of the pipeline instead of wrapping a blocking `get()` in `try`/`catch`.

## Prerequisites

- Lambdas and method references
- Basic generics
- `Optional`'s `map`/`flatMap` helps, since `thenApply`/`thenCompose` follow
  the same shape

## Task

`ProfileService` looks up a user's profile in two async steps: first it
resolves a username to a numeric id (`findUserId`), then it loads the
profile for that id (`loadProfile`). Both are already implemented and both
return a `CompletableFuture`. Your job is to compose them correctly, and to
handle the case where either step fails.

The second lookup cannot even start until the first one's id is known -
that dependency is exactly what forces the choice of composition method.

## Instructions

Complete the following TODOs in `ProfileService`:

- TODO-00: Implement `lookupProfile(username)` by chaining `findUserId` into
  `loadProfile`. The method's return type is `CompletableFuture<Profile>` -
  if your chosen composition method wraps a future inside a future, this
  simply won't compile against that signature.
- TODO-01: Implement `lookupProfileOrDefault(username)` so a failed lookup
  (unknown username, or an id with no profile behind it) falls back to
  `DEFAULT_PROFILE` instead of propagating the exception.
- TODO-02: Implement `describeLookup(username)` so it reports both outcomes:
  `"found: <displayName>"` on success, `"failed: <message>"` on failure.
  The exception a downstream stage receives is usually a
  `CompletionException` wrapping the real failure - unwrap it before reading
  its message.

Run the tests until they all pass.

## Running the Lab

From the project root:

```bash
mvn -pl java-concepts/completablefuture-composition test
```

Or from the lab directory:

```bash
cd java-concepts/completablefuture-composition
mvn test
```

## Bonus (Optional)

- TODO-03 (optional): Implement `combinedGreeting(usernameA, usernameB)` so
  both usernames are looked up *concurrently* - neither depends on the
  other's result - and their display names are combined into one
  `"<name> & <name>"` string once both finish.
