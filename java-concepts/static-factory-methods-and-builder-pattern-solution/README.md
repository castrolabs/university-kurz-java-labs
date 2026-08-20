# Static Factory Methods and Builder Pattern - Solution

## Overview

This is the official solution for the `ServerConfig` lab. It shows a `Builder` whose setters never validate anything on their own, a `build()` that centralizes every invariant, and three static factories that each demonstrate a different reason to prefer a named method over `new`.

## Key Concepts

### Setters just assign; build() is the only place that validates

```java
public Builder useTls() {
    this.useTls = true;
    return this;
}

public ServerConfig build() {
    if (host == null || host.isBlank()) {
        throw new IllegalStateException("host is required");
    }
    if (port < 1 || port > 65535) {
        throw new IllegalStateException("port must be between 1 and 65535, got " + port);
    }
    if (useTls && (tlsCertificatePath == null || tlsCertificatePath.isBlank())) {
        throw new IllegalStateException("tlsCertificatePath is required when useTls is enabled");
    }
    if (maxConnections != null && unlimited) {
        throw new IllegalStateException("maxConnections and unlimited are mutually exclusive");
    }
    return new ServerConfig(host, port, useTls, tlsCertificatePath, maxConnections, unlimited,
            connectionTimeoutSeconds);
}
```

Calling `.unlimited().maxConnections(10)` on a builder never throws — the object can sit in a temporarily "invalid" combination while it's still being configured, because the caller might still be about to call something that resolves the conflict (it won't here, but the builder can't know that mid-chain). `build()` is the one point where every optional value the caller wanted to set has already been supplied, which is exactly why it's the natural place to enforce invariants that span multiple fields — a setter, checking only its own field, could never catch the `maxConnections`/`unlimited` conflict on its own.

### Naming a constructor-like operation

```java
public static ServerConfig localhost(int port) {
    return new Builder("localhost", port).build();
}
```

`ServerConfig.localhost(9000)` says what it builds; `new ServerConfig.Builder("localhost", 9000).build()` makes the reader do the same work by hand every time. A static factory is free to bake in a default that a constructor has no natural place for.

### Caching instead of allocating

```java
private static ServerConfig cachedDefaultConfig;

public static synchronized ServerConfig defaultConfig() {
    if (cachedDefaultConfig == null) {
        cachedDefaultConfig = new Builder("localhost", 8080).build();
    }
    return cachedDefaultConfig;
}
```

A constructor can never do this — `new` always allocates. `defaultConfig()` behaves like `Boolean.valueOf(true)` handing back `Boolean.TRUE`: the first call builds the instance, every later call returns the exact same reference, which the test asserts with `assertSame`, not `assertEquals`.

### A static factory as a "wither" (bonus)

```java
public static ServerConfig withPort(ServerConfig base, int newPort) {
    Builder builder = new Builder(base.host(), newPort)
            .connectionTimeoutSeconds(base.connectionTimeoutSeconds());

    if (base.useTls()) {
        builder.useTls();
    }
    if (base.tlsCertificatePath() != null) {
        builder.tlsCertificatePath(base.tlsCertificatePath());
    }
    if (base.maxConnections() != null) {
        builder.maxConnections(base.maxConnections());
    }
    if (base.unlimited()) {
        builder.unlimited();
    }
    return builder.build();
}
```

`base` is never mutated — `ServerConfig` is immutable, so "changing" a field means building a new instance that copies every other field across. Rebuilding through `Builder` (rather than a second canonical constructor call) reuses the exact same validation `build()` already provides for free.

## Summary

- A `Builder`'s setters should only assign fields and return `this`; validating cross-field invariants belongs in `build()`, the single point where every value the caller wanted to set has already arrived.
- Static factory methods can do what constructors structurally cannot: carry a descriptive name, return a cached/shared instance instead of always allocating, and (though not shown here) return a subtype the caller never sees.
- `getInstance`/`newInstance`-style naming communicates whether a factory shares an instance or always creates a fresh one — `defaultConfig()` here behaves like the former.
- A record's compact constructor is the leaner alternative for a class with only a few, mostly-required fields; a builder earns its cost once optional, interdependent fields start piling up.
