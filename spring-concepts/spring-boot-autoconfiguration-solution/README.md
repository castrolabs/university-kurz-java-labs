# Spring Boot Autoconfiguration - Solution

## Overview

This is the official solution for the Spring Boot Autoconfiguration lab. It shows a minimal,
realistic `@AutoConfiguration` class and how `@ConditionalOnMissingBean` lets an application
override it one bean at a time.

## Key Concepts

### `@AutoConfiguration`

`GreetingAutoConfiguration` is annotated `@AutoConfiguration` instead of plain `@Configuration`.
It's still an ordinary source of bean definitions, but the annotation marks it as
autoconfiguration-specific to Spring Boot's tooling (ordering, the `AutoConfiguration.imports`
discovery file in real applications, etc.).

### `@ConditionalOnMissingBean`

```java
@Bean
@ConditionalOnMissingBean
GreetingService greetingService() {
    return new DefaultGreetingService();
}
```

This is the entire mechanism behind "define your own bean to override the default." Spring
Boot evaluates conditions in a specific order and only creates the autoconfigured bean if no
other bean of the same type (`GreetingService`) already exists in the context — regardless of
whether that other bean came from a `@Configuration` class, component scanning, or anywhere
else.

### Testing with `ApplicationContextRunner`

```java
private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(GreetingAutoConfiguration.class));
```

`ApplicationContextRunner` builds a throwaway `ApplicationContext` per test, without starting
an embedded server or scanning the whole classpath. `withConfiguration(AutoConfigurations.of(...))`
registers the autoconfiguration class the same way Spring Boot would at runtime, while
`withUserConfiguration(...)` simulates the application's own `@Configuration` classes — exactly
what's needed to prove both branches: default bean present, and default bean backing off.

## Implementation Details

`DefaultGreetingService` simply returns a fixed greeting. `CustomGreetingConfig`, defined as a
static nested class inside the test, stands in for "the application's own configuration" and
supplies a competing `GreetingService` bean via a lambda.

## Trade-offs and Best Practices

1. **One condition, one clear override path**: `@ConditionalOnMissingBean` only looks at bean
   *type* by default (it can also be narrowed by name), so any custom `GreetingService` bean —
   however it's created — wins over the autoconfigured default.
2. **Order matters**: Spring Boot processes user configuration before autoconfiguration, which
   is exactly why `@ConditionalOnMissingBean` works — by the time the autoconfiguration class is
   evaluated, user-defined beans already exist in the context to check against.
3. **`ApplicationContextRunner` over a full `@SpringBootTest`**: for testing autoconfiguration in
   isolation, spinning up a real `@SpringBootApplication` is unnecessary overhead — the runner
   gives fast, isolated contexts per test.

## Summary

- `@AutoConfiguration` classes are ordinary `@Configuration` classes with Boot-specific metadata.
- `@ConditionalOnMissingBean` is what makes autoconfigured beans fully overridable, one bean at
  a time.
- `ApplicationContextRunner` is the idiomatic, fast way to test conditional bean registration.
