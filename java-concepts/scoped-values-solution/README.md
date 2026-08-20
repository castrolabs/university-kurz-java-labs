# Scoped Values - Solution

## Overview

This is the official solution for the `RequestContext` lab. Every method is
a one-line delegation to `ScopedValue` - the interesting behavior lives in
the API's own guarantees, which the tests exercise directly.

## Key Concepts

### Reading: get(), isBound(), orElse()

```java
public static String currentUser() {
    return CURRENT_USER.get();
}

public static boolean hasCurrentUser() {
    return CURRENT_USER.isBound();
}

public static String currentUserOrDefault(String fallback) {
    return CURRENT_USER.orElse(fallback);
}
```

`get()` throws `NoSuchElementException` the instant nothing is bound on the
current thread - there is no `null` return the way `ThreadLocal.get()`
would give you, so a missing binding can never be silently confused with a
binding whose value happens to be `null`. `isBound()`/`orElse()` exist for
the code paths that can legitimately run both inside and outside a binding.

### Binding: where(...).run(...) and where(...).call(...)

```java
public static <T, X extends Throwable> T runAs(String user, ScopedValue.CallableOp<T, X> action) throws X {
    return ScopedValue.where(CURRENT_USER, user).call(action);
}

public static void runAs(String user, Runnable action) {
    ScopedValue.where(CURRENT_USER, user).run(action);
}
```

`where(KEY, value)` returns a `Carrier` - nothing is bound until `run()` or
`call()` actually invokes it. The binding's lifetime is exactly that call's
*dynamic extent*: every frame it calls, on this thread, until it returns.
`call()` is typed against `ScopedValue.CallableOp<T, X>` rather than
`java.util.concurrent.Callable<T>` - structurally the same single abstract
method, but a distinct type in the JDK 25 API, and propagating `X` instead
of a blanket `Exception` lets a caller's lambda declare exactly which
checked exception (if any) it throws.

### Binding two keys in one carrier

```java
public static void runAs(String user, String locale, Runnable action) {
    ScopedValue.where(CURRENT_USER, user)
            .where(CURRENT_LOCALE, locale)
            .run(action);
}
```

`Carrier.where(...)` returns a new `Carrier` with the extra binding added,
so both `CURRENT_USER` and `CURRENT_LOCALE` take effect together when
`run()` starts and are torn down together when it returns - one scope
instead of two nested `run()` calls.

### What the tests prove beyond "it compiles"

- **Unbound reads fail loudly.** `currentUser()` outside any `runAs()`
  throws `NoSuchElementException` - there's no silent fallback to chase
  down later.
- **Rebinding shadows, it doesn't mutate.** A nested `runAs("admin", ...)`
  inside an outer `runAs("alice", ...)` only changes what `CURRENT_USER`
  resolves to for its own nested call; the outer binding is back the moment
  the nested one returns. There is no `set()` anywhere a callee could use
  to corrupt the caller's view.
- **Two threads never cross-contaminate.** Each binding lives on its own
  thread's stack; two threads bound to different users concurrently never
  observe each other's value, with no lock and no synchronization needed.
- **A plain `Thread` you start yourself is outside the extent.** Only a
  `StructuredTaskScope` subtask automatically inherits the parent's
  binding (see the sibling `structured-concurrency` lab); a raw
  `new Thread(...)` reading `CURRENT_USER` gets `NoSuchElementException`
  just like any other unbound read, because that binding was never
  propagated to it.

## Summary

- `ScopedValue` has no `set()` - the only way to change what a key
  resolves to is to open a new, nested `where(...).run()`/`call()`.
- A binding's lifetime is structural (the lambda's dynamic extent), not
  something a caller must remember to clean up.
- Inheritance across threads is opt-in and mechanism-specific: structured
  concurrency's `fork()` gives it to you for free, a hand-started `Thread`
  does not.
