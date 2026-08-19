# Spring Boot Configuration Properties

## Goal

Understand relaxed binding: `@ConfigurationProperties` maps `app.max-retries` in a properties
file onto a `maxRetries` Java field automatically, including nested objects and lists — without
a single `@Value` annotation.

## Prerequisites

- Basic Spring Boot bean wiring (`@Configuration`, `@Bean`)
- Familiarity with Java records
- Comfort reading `.properties`-style key/value pairs

## Task

`AppProperties` is a record binding a small config surface: an application name, a retry
count, a list of allowed origins, and a nested `Notification` object. You'll wire it up so
Spring Boot actually binds it from the environment, and give it sensible defaults when parts
of the configuration are missing.

The test supplies properties in kebab-case (`app.max-retries`) and asserts they land on the
camelCase record component (`maxRetries`) — that mapping is relaxed binding, and it's why
`app.max-retries`, `APP_MAX_RETRIES`, and `app.maxRetries` would all work identically.

## Instructions

Complete the following TODOs:

- TODO-00: Add `@ConfigurationProperties(prefix = "app")` to the `AppProperties` record.
- TODO-01: Add `@EnableConfigurationProperties(AppProperties.class)` to `AppConfig` so Spring
  registers `AppProperties` as a bean.
- TODO-02: Implement the compact constructor in `AppProperties` to default `allowedOrigins`
  and `notification` when they aren't supplied.

Run the tests until they all pass.

## Running the Lab

From the project root:

```bash
mvn -pl spring-concepts/spring-boot-configuration-properties test
```

Or from the lab directory:

```bash
cd spring-concepts/spring-boot-configuration-properties
mvn test
```

## Bonus (Optional)

- TODO-03 (optional): Add a static factory `AppProperties.disabled(name, maxRetries)` that
  returns an instance with notifications turned off.
