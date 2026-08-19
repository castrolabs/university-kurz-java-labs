# CompletableFuture Composition - Solution

## Overview

This is the official solution for the CompletableFuture Composition lab.
`ProfileService` chains two dependent async steps with `thenCompose`, then
layers `exceptionally` and `handle` on top for the two shapes of failure
handling the article describes, and finishes with `thenCombine` for a pair
of lookups that do not depend on each other.

## Key Concepts

### thenCompose(Function<T, CompletableFuture<U>>): flattening dependent steps

```java
public CompletableFuture<Profile> lookupProfile(String username) {
    return findUserId(username).thenCompose(this::loadProfile);
}
```

`findUserId` and `loadProfile` are each async, and the second call needs the
first call's result to even know what to request. `thenApply` would have
worked mechanically - the lambda `id -> loadProfile(id)` is a perfectly
valid `Function` - but its return type is `CompletableFuture<Profile>`, so
`thenApply` would produce `CompletableFuture<CompletableFuture<Profile>>`.
`thenCompose` flattens it, the same way `flatMap` flattens a nested
`Optional` or `Stream`. The method's declared return type,
`CompletableFuture<Profile>`, is what makes the wrong choice fail to
compile rather than fail at runtime.

### exceptionally(Function<Throwable, T>): recovering with a fallback

```java
public CompletableFuture<Profile> lookupProfileOrDefault(String username) {
    return lookupProfile(username).exceptionally(ex -> DEFAULT_PROFILE);
}
```

`exceptionally` only runs on the failure path and must produce a value of
the same type the pipeline already carries. It never sees a successful
result, and it never runs at all when there is nothing to recover from -
that's what makes it read like the pipeline's `catch` block.

### handle(BiFunction<T, Throwable, U>): reacting to both outcomes

```java
public CompletableFuture<String> describeLookup(String username) {
    return lookupProfile(username).handle((profile, ex) -> {
        if (ex == null) {
            return "found: " + profile.displayName();
        }
        Throwable cause = (ex instanceof CompletionException completionException)
                ? completionException.getCause()
                : ex;
        return "failed: " + cause.getMessage();
    });
}
```

Unlike `exceptionally`, `handle` runs on *both* paths - `(result, null)` on
success, `(null, throwable)` on failure - and it is free to change the
type, here from `Profile` to `String`. The `Throwable` a downstream stage
receives is typically a `CompletionException` that wraps the exception the
upstream stage actually threw (a `NoSuchElementException`, in this
service's case), so `describeLookup` unwraps it via `getCause()` before
reading the message. Skipping that unwrap would print
`"failed: java.util.concurrent.CompletionException: java.util.NoSuchElementException: ..."`
instead of the clean message.

### thenCombine(CompletableFuture<U>, BiFunction<T, U, V>): joining independent results

```java
public CompletableFuture<String> combinedGreeting(String usernameA, String usernameB) {
    CompletableFuture<Profile> profileA = lookupProfile(usernameA);
    CompletableFuture<Profile> profileB = lookupProfile(usernameB);
    return profileA.thenCombine(profileB, (a, b) -> a.displayName() + " & " + b.displayName());
}
```

Both lookups are already in flight by the time `thenCombine` is called -
neither needs the other's result, so there is no reason to make them wait
on each other the way `thenCompose` would. Using `thenCompose` here would
still compile and still produce the right answer; it would just serialize
two calls that could have overlapped, trading latency for nothing.

## Summary

- `thenCompose` flattens a step whose function itself returns a
  `CompletableFuture`; `thenApply` would nest it instead, and a precisely
  typed return signature can turn that mistake into a compile error rather
  than a runtime surprise.
- `exceptionally` recovers on failure only and preserves the type;
  `handle` sees both outcomes and can change the type.
- A downstream stage's `Throwable` is usually a `CompletionException`
  wrapping the real cause - unwrap it with `getCause()` before inspecting
  the failure.
- Reach for `thenCompose` only when one step genuinely needs another's
  result; independent work belongs in `thenCombine` (or `allOf`), so it can
  run concurrently instead of being serialized for no reason.
