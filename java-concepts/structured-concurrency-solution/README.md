# Structured Concurrency - Solution

## Overview

This is the official solution for the `ProfileFetcher` lab. Every method
follows the same shape: open a scope, `fork()` the subtasks, `join()`, then
read each `Subtask`'s result - the difference between the three methods is
only which `Joiner` and configuration the scope is opened with.

## Key Concepts

### The default policy: wait for all, cancel on failure

```java
public Profile loadProfile() throws InterruptedException {
    try (var scope = StructuredTaskScope.open()) {
        Subtask<String> user = scope.fork(userFetcher);
        Subtask<List<String>> orders = scope.fork(ordersFetcher);

        scope.join();
        return new Profile(user.get(), orders.get());
    } catch (StructuredTaskScope.FailedException e) {
        throw new ProfileUnavailableException(e.getCause());
    }
}
```

`scope.fork(...)` returns a `Subtask<T>` immediately - not a `Future<T>`.
There is nothing to `cancel()` and no `get()`-blocks-you semantics on it;
the *only* place this code blocks is `scope.join()`. If either subtask
throws, the scope interrupts the other one right there (that's what the
`loadProfileCancelsSlowSiblingOnFailure` test measures via elapsed time),
and `join()` throws `FailedException` wrapping the original cause. The
`try`-with-resources close() then guarantees nothing forked inside the
block is still running once the method returns - success, failure, or
exception, there is no leftover subtask.

### A deadline for the whole operation, not each call

```java
public Profile loadProfileWithTimeout(Duration timeout) throws InterruptedException {
    try (var scope = StructuredTaskScope.open(
            Joiner.<Object>awaitAllSuccessfulOrThrow(),
            cf -> cf.withTimeout(timeout))) {
        ...
    } catch (StructuredTaskScope.FailedException e) {
        throw new ProfileUnavailableException(e.getCause());
    }
}
```

`Joiner.<Object>awaitAllSuccessfulOrThrow()` is written with an explicit
`<Object>` type witness because `userFetcher` and `ordersFetcher` return two
different types (`String` and `List<String>`); the scope's own type
parameter has to be their common supertype for `fork()` to accept both.
`cf.withTimeout(timeout)` attaches the deadline to the *scope*, not to an
individual `Future.get(timeout, unit)` call - if it elapses before both
subtasks finish, `join()` throws `StructuredTaskScope.TimeoutException`
(an unchecked exception, despite the name looking like the checked
`java.util.concurrent.TimeoutException`) and cancels both in-flight
subtasks, which is exactly what the deadline test verifies by checking both
callables' completion flags stayed `false`.

### Racing two branches and keeping only the winner

```java
public static String fetchFastest(Callable<String> a, Callable<String> b) throws InterruptedException {
    try (var scope = StructuredTaskScope.open(Joiner.<String>anySuccessfulResultOrThrow())) {
        scope.fork(a);
        scope.fork(b);
        return scope.join();
    } catch (StructuredTaskScope.FailedException e) {
        throw new ProfileUnavailableException(e.getCause());
    }
}
```

With `anySuccessfulResultOrThrow()`, `join()` itself returns the winning
value directly (instead of `void`), and the losing subtask is cancelled the
moment the first one succeeds - this is the hedged-request pattern that
`CompletableFuture.anyOf()` cannot express, since `anyOf()` only stops
*waiting* on the loser while it keeps running to completion regardless.

## Summary

- `Subtask<T>` is a plain result holder, not a `Future` - `join()` is the
  one and only place a scope blocks.
- The default `open()` policy cancels every other subtask the instant one
  fails; a hand-rolled `ExecutorService`/`Future` fan-out needs that written
  out explicitly and it's easy to get wrong for one branch, as the
  concept's own "unstructured baseline" example shows.
- A scope-level timeout (`cf.withTimeout(...)`) bounds the *whole*
  operation and cancels every subtask still running when it elapses, rather
  than bounding each call independently.
- `close()` guarantees no subtask outlives the `try` block - there is no
  "fire and forget" escape hatch, by construction.
