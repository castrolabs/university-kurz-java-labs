# Spring Boot Custom Configuration Properties

## Goal

Turn a plain Java record into a validated, type-safe configuration holder: bind it from the
environment with `@ConfigurationProperties`, register it with `@EnableConfigurationProperties`,
and enforce Jakarta Bean Validation constraints on it with `@Validated` — so an invalid value
fails application startup instead of surfacing later as a mysterious runtime bug.

## Prerequisites

- Basic Spring Boot bean wiring (`@Configuration`, `@Bean`)
- Familiarity with Java records and constructor binding
- Basic Jakarta Bean Validation annotations (`@NotBlank`, `@Min`, `@Max`)

## Task

`MailProperties` is a record meant to hold outbound-mail configuration: a `host`, a `port`, a
`retryCount`, and an `enabled` flag. Right now it isn't wired up at all — Spring doesn't know it
should bind `mail.*` properties onto it, doesn't validate the values it receives, and doesn't
register it as a bean.

The test suite proves two different things:

- **Relaxed binding**: a property written as `mail.retry-count` in kebab-case lands on the
  `retryCount` record component.
- **Fail-fast validation**: a blank host, an out-of-range port, or a negative retry count must
  make the application context fail to start — not silently produce an object with a bad value
  inside it.

## Instructions

Complete the following TODOs:

- TODO-00: Annotate `MailProperties` with `@ConfigurationProperties(prefix = "mail")`.
- TODO-01: Annotate `MailProperties` with `@Validated` so the Jakarta Bean Validation
  constraints on its components are actually enforced during binding.
- TODO-02: Add a `@PositiveOrZero` constraint (with a message) to the `retryCount` component.
- TODO-03: Annotate `MailConfig` with `@EnableConfigurationProperties(MailProperties.class)`.

Run the tests until they all pass.

## Running the Lab

From the project root:

```bash
mvn -pl spring-concepts/spring-boot-custom-configuration-properties test
```

Or from the lab directory:

```bash
cd spring-concepts/spring-boot-custom-configuration-properties
mvn test
```

## Bonus (Optional)

- TODO-04 (optional): Add a static factory `MailProperties.disabled(String host, int port)`
  that returns an instance with `retryCount` at `0` and `enabled` at `false` — useful for tests
  and environments that must never actually send mail.
