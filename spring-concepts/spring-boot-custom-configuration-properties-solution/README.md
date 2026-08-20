# Spring Boot Custom Configuration Properties - Solution

## Overview

This is the official solution for the Spring Boot Custom Configuration Properties lab. It
shows a record-based `@ConfigurationProperties` class with Jakarta Bean Validation constraints
enforced at binding time via `@Validated`.

## Key Concepts

### `@ConfigurationProperties` + `@Validated` on a record

```java
@ConfigurationProperties(prefix = "mail")
@Validated
public record MailProperties(
        @NotBlank String host,
        @Min(1) @Max(65535) int port,
        @PositiveOrZero int retryCount,
        boolean enabled) { ... }
```

`@ConfigurationProperties` makes the record a binding target for anything under the `mail`
prefix; a record's canonical constructor is automatically the binding constructor since Spring
Boot 3, with no `@ConstructorBinding` needed. `@Validated` is what actually triggers Jakarta
Bean Validation on the bound values — without it, the constraint annotations are present but
inert, and an out-of-range value binds successfully with nobody ever checking it.

### Relaxed binding

`mail.retry-count` (kebab-case, the idiomatic form in YAML/properties files), `mail.retryCount`,
and the environment variable `MAIL_RETRYCOUNT` all resolve to the same `retryCount` record
component. This normalization is specific to `@ConfigurationProperties` binding — it's not
something `@Value("${...}")` does.

### `@EnableConfigurationProperties`

```java
@Configuration
@EnableConfigurationProperties(MailProperties.class)
public class MailConfig { }
```

Declaring `@ConfigurationProperties` on a type isn't enough on its own — nothing registers it as
a bean until something does. `@EnableConfigurationProperties` does exactly that. Skip it, and
`context.getBean(MailProperties.class)` throws `NoSuchBeanDefinitionException` even though the
record itself is correctly annotated.

### Fail-fast validation

```java
new ApplicationContextRunner()
        .withUserConfiguration(MailConfig.class)
        .withPropertyValues("mail.host=", "mail.port=25", "mail.retry-count=0", "mail.enabled=true")
        .run(context -> assertThat(context).hasFailed());
```

With `@Validated` in place, binding `mail.host=` (blank) throws a
`ConfigurationPropertiesBindException` wrapping a `BindValidationException` during context
refresh — the application never finishes starting. This is the core payoff of validating a
configuration property in one place: the failure happens at the moment the bad value enters the
system, with a message that names the offending property, instead of resurfacing later as an
SMTP connection error with no obvious cause.

## Trade-offs and Best Practices

1. **Validation only runs if `@Validated` is present.** The constraint annotations
   (`@NotBlank`, `@Min`, `@Max`, `@PositiveOrZero`) do nothing by themselves — they're metadata
   that `@Validated` tells Spring's `ConfigurationPropertiesBindingPostProcessor` to act on.
   Forgetting `@Validated` is a silent failure mode: the bean binds fine, the bad value just
   rides along.
2. **Records vs. mutable classes**: a record gives an immutable, constructor-bound properties
   class with generated `equals`/`hashCode`/`toString` for free, and validation runs once at
   construction — there's no window where a partially-validated mutable object exists.
3. **Fail fast beats fail later**: an invalid `mail.port` caught at startup produces one clear
   error naming the property; the same bad value left unvalidated would only surface once code
   tries to open a socket on port 70000, with a much less obvious connection between cause and
   effect.

## Summary

- `@ConfigurationProperties(prefix = "...")` on a record binds the environment onto record
  components using relaxed binding.
- `@Validated` turns the record's Jakarta Bean Validation constraints into an actual startup
  check — without it, the constraints are inert.
- `@EnableConfigurationProperties` (or `@ConfigurationPropertiesScan`) is required to register
  the type as a bean in the first place.
- An invalid property value fails the whole application context, not just one field.
