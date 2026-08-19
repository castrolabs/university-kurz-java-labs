# Optional Correct Usage - Solution

## Overview

This is the official solution for the Optional Correct Usage lab. `PreferenceResolver` runs through
the combinators that replace the `isPresent()`/`get()` anti-pattern: `ofNullable`, `orElse`,
`orElseGet`, `map`, `flatMap(Optional::stream)`, and `orElseThrow`.

## Key Concepts

### Optional.ofNullable: the boundary factory

```java
public Optional<String> normalize(String rawNickname) {
    return Optional.ofNullable(rawNickname);
}
```

`ofNullable` is the factory for values that might legitimately be `null` — typically at the boundary
where a legacy, null-returning API hands you something. `Optional.of(rawNickname)` would have been
wrong here: it calls `Objects.requireNonNull` internally and throws immediately the first time a caller
passes `null`.

### orElse(x): a fine choice for a constant fallback

```java
public String displayName(Optional<String> nickname) {
    return nickname.orElse("Guest");
}
```

`orElse` takes a plain value, evaluated eagerly before `orElse` is even entered. With a literal
constant like `"Guest"` that eagerness is free, so `orElse` is the more readable choice.

### orElseGet(supplier): the fix once the fallback has a cost

```java
public String resolveTheme(Optional<String> storedTheme, Supplier<String> defaultThemeLoader) {
    return storedTheme.orElseGet(defaultThemeLoader);
}
```

This is the crux of the lab. `orElseGet` takes a `Supplier` and only invokes it when the `Optional` is
actually empty. The tempting alternative, `storedTheme.orElse(defaultThemeLoader.get())`, *compiles*
— `Supplier<String>.get()` returns a `String`, exactly what `orElse(T)` expects — but Java evaluates
that argument before `orElse` even runs, so `defaultThemeLoader.get()` fires on every call, including
the overwhelmingly common case where a theme was already stored. If `defaultThemeLoader` reads a file,
hits a database, or has any other side effect, that cost (or side effect) happens unconditionally.
`orElseGet` defers the call to inside the Optional itself, so it only happens on the empty path.

### map + orElse: chaining without unwrapping

```java
public String zipOf(Optional<Address> address) {
    return address.map(Address::zip).orElse("UNKNOWN");
}
```

`map` applies the function only when a value is present and re-wraps the result, so the chain
short-circuits cleanly to `Optional.empty()` when `address` itself is absent — no manual
`isPresent()` check needed anywhere in the chain.

### flatMap(Optional::stream): collapsing a list of lookups

```java
public List<String> presentNicknames(List<Optional<String>> nicknames) {
    return nicknames.stream()
            .flatMap(Optional::stream)
            .toList();
}
```

`Optional.stream()` turns each `Optional<String>` into a `Stream` of zero or one elements. Used as a
`flatMap` mapper, it drops every empty `Optional` and unwraps every present one in a single pass —
no `filter(Optional::isPresent).map(Optional::get)` pair, and no `get()` call whose safety depends on
a check earlier in the pipeline.

### orElseThrow(supplier): building the exception only when needed

```java
public String requireNickname(Optional<String> nickname) {
    return nickname.orElseThrow(() -> new NoSuchElementException("nickname required"));
}
```

Just like `orElseGet`, the supplier overload of `orElseThrow` is only invoked on the empty path — the
exception is constructed lazily, which is exactly why there is no value-taking
`orElseThrow(SomeException)` overload in the API.

## Summary

- `orElse(x)` evaluates `x` eagerly, every single call, even when the `Optional` is present — fine for
  a free constant, a correctness bug for anything with a cost or a side effect.
- `orElseGet(supplier)` only invokes the supplier on the empty path; reach for it the moment the
  fallback is a method call rather than a literal.
- `map`/`flatMap` let a chain of lookups short-circuit at any absent link without a single manual
  `isPresent()` check.
- `flatMap(Optional::stream)` is the idiomatic way to turn a collection of lookups into just the
  present results.
