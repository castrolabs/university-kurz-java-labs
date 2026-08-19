# Spring Boot Configuration Properties - Solution

## Overview

This is the official solution for the Spring Boot Configuration Properties lab. It shows a
record-based `@ConfigurationProperties` class, how it's registered, and how relaxed binding
maps differently-cased property names onto the same Java field.

## Key Concepts

### `@ConfigurationProperties` on a record

```java
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        String name,
        int maxRetries,
        List<String> allowedOrigins,
        Notification notification) { ... }
```

Since Spring Boot 3, a record's canonical constructor is automatically treated as the binding
constructor — no `@ConstructorBinding` annotation is needed the way it once was. Each record
component becomes a bindable property under the `app` prefix: `app.name`, `app.max-retries`,
`app.allowed-origins`, and the nested `app.notification.channel` / `app.notification.enabled`.

### Relaxed binding

Spring's relaxed binding algorithm treats `app.max-retries`, `app.maxRetries`, and
`APP_MAX_RETRIES` (an environment variable) as the same logical property, resolving them all
onto the `maxRetries` component. This is different from `@Value("${...}")`, which expects the
exact property key it's given and does not perform this normalization the same way.

### `@EnableConfigurationProperties`

```java
@Configuration
@EnableConfigurationProperties(AppProperties.class)
public class AppConfig { }
```

Declaring a `@ConfigurationProperties` type isn't enough on its own — something has to register
it as a bean. `@EnableConfigurationProperties` does exactly that. The alternative,
`@ConfigurationPropertiesScan`, scans a package for `@ConfigurationProperties`-annotated types
instead of listing them explicitly.

### Defaults via the compact constructor

```java
public AppProperties {
    if (allowedOrigins == null) {
        allowedOrigins = List.of();
    }
    if (notification == null) {
        notification = new Notification("email", true);
    }
}
```

A record's compact constructor runs for every instance, including ones created by the property
binder, so it's a convenient place to normalize `null` collections and nested objects into safe
defaults without a null check at every call site.

## Trade-offs and Best Practices

1. **No compiler safety net for property names**: a typo like `app.max-retryes` silently binds
   to nothing (the field stays at its default) instead of failing to compile — always verify
   binding with a test like this one.
2. **Records vs. mutable classes**: a record gives an immutable, constructor-bound properties
   class with generated `equals`/`hashCode`/`toString` for free; a mutable class with setters
   also works but requires more boilerplate and doesn't rule out partially-constructed state.
3. **`@Value` isn't a drop-in replacement**: `@Value("${app.max-retries}")` on a single field
   works for one property at a time but doesn't group related properties into one type-safe
   object the way `@ConfigurationProperties` does.

## Summary

- `@ConfigurationProperties(prefix = "...")` on a record binds the environment onto record
  components using relaxed binding.
- `@EnableConfigurationProperties` (or `@ConfigurationPropertiesScan`) is required to register
  the type as a bean.
- A compact constructor is a clean place to apply defaults for missing nested/collection
  properties.
