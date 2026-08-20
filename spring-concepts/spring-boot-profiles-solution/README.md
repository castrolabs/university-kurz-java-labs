# Spring Boot Profiles - Solution

## Overview

This is the official solution for the Spring Boot Profiles lab. It shows a `@Profile`-gated
bean, profile-specific property overrides via `application-{profile}.properties`, and a profile
group that expands one activated name into several.

## Key Concepts

### `@Profile` on a bean method

```java
@Bean
@Profile("!prod")
public DataSeeder dataSeeder() {
    return new DevDataSeeder();
}
```

`@Profile` is evaluated against the environment's active profiles at context refresh time. A
negated expression like `!prod` reads as "create this bean unless prod is active" — which means
it is created both when `dev` is active *and* when nothing at all is active, since `prod` isn't
active in either case. That's easy to misread as "only when nothing is active," which is why
the test suite explicitly checks the no-active-profile case rather than assuming.

### Profile-specific property files

```
application.properties       # app.environment.label=Local, verbose-logging=false
application-dev.properties   # app.environment.label=Development, verbose-logging=true
application-prod.properties  # app.environment.label=Production
```

Only the properties that differ from the default need to appear in a profile file — `prod`
overrides `label` but leaves `verbose-logging` to fall back to the value in `application
.properties`. Spring Boot loads `application-{profile}.properties` automatically for every
profile listed in `spring.profiles.active`, layering it on top of the base `application
.properties`.

### `@ConfigurationProperties` + `@EnableConfigurationProperties`

```java
@ConfigurationProperties(prefix = "app.environment")
public record EnvironmentProperties(String label, boolean verboseLogging) { }

@Configuration
@EnableConfigurationProperties(EnvironmentProperties.class)
public class AppConfig { }
```

Same mechanism as any other `@ConfigurationProperties` record: the prefix determines which keys
bind, and `@EnableConfigurationProperties` registers it as a bean. What makes it
profile-sensitive is entirely on the properties-file side — the binding code itself doesn't
know or care which profile is active.

### Profile groups

```properties
spring.profiles.group.production=prod,audit
```

```java
@Bean
@Profile("audit")
public AuditLogger auditLogger() {
    return new AuditLogger();
}
```

Activating `production` expands into `production`, `prod`, and `audit` all being active
simultaneously — so a bean gated on `audit` alone gets created, and `application-prod
.properties` still applies, without either profile needing to be listed explicitly at the
command line.

## Trade-offs and Best Practices

1. **`@Profile` negation is a common source of surprise**: `@Profile("!prod")` is "active
   unless prod," not "active only when nothing is." Verify against the actual set of active
   profiles in a given environment, not the annotation in isolation.
2. **A separate file per profile scales into a lot of files** for many environments — for a
   handful, it keeps each profile's overrides easy to scan versus multi-document YAML.
3. **Setting `spring.profiles.active` inside `application.properties`'s own default section
   defeats profiles entirely** — it becomes a fixed default baked into every environment
   instead of something the environment controls. Activate profiles from outside the properties
   file: an environment variable or a command-line argument.

## Summary

- `@Profile` on a `@Bean` method (or `@Configuration` class) restricts when that bean is
  created; negation (`!name`) means "unless this profile is active," including when no profile
  at all is active.
- `application-{profile}.properties` overrides only the keys that differ from `application
  .properties`; anything not overridden falls back to the default.
- `spring.profiles.group.<name>` expands one activated profile into several, letting a single
  flag turn on a whole environment's worth of configuration.
