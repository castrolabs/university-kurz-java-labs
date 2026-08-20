# Spring Boot Profiles

## Goal

Use Spring profiles to make the same codebase behave differently per environment: gate an
entire bean with `@Profile`, and override configuration values with a dedicated
`application-{profile}.properties` file — then prove, with a test, exactly which profile wins
and which bean genuinely does not exist when its profile isn't active.

## Prerequisites

- Basic Spring Boot bean wiring (`@Configuration`, `@Bean`)
- Familiarity with `@ConfigurationProperties` and Java records
- Comfort reading `.properties`-style key/value pairs

## Task

This lab ships three property files — `application.properties` (defaults), plus
`application-dev.properties` and `application-prod.properties` (profile-specific overrides) —
and a `DataSeeder` bean that should exist for every profile except `prod`.

The test suite boots the application with different active profiles using
`SpringApplicationBuilder` and checks:

- Which profile activates the `DataSeeder` bean, and which one — `prod` — must **not** have it
  (asserted via `NoSuchBeanDefinitionException`, not just "some bean exists").
- That `@Profile("!prod")` means "active unless prod is active" — so with **no** profile active
  at all, the bean is still created. This is a common misreading worth verifying with a test
  rather than assuming.
- That `app.environment.label` and `app.environment.verboseLogging` differ per profile, coming
  from the matching `application-{profile}.properties` file.

## Instructions

Complete the following TODOs:

- TODO-00: Annotate the `dataSeeder()` bean method in `AppConfig` with `@Profile("!prod")`.
- TODO-01: Annotate `EnvironmentProperties` with `@ConfigurationProperties(prefix =
  "app.environment")`.
- TODO-02: Annotate `AppConfig` with `@EnableConfigurationProperties(EnvironmentProperties
  .class)`.
- TODO-03: Fill in `application-dev.properties` with `app.environment.label=Development` and
  `app.environment.verbose-logging=true`.
- TODO-04: Fill in `application-prod.properties` with `app.environment.label=Production`.

Run the tests until they all pass.

## Running the Lab

From the project root:

```bash
mvn -pl spring-concepts/spring-boot-profiles test
```

Or from the lab directory:

```bash
cd spring-concepts/spring-boot-profiles
mvn test
```

## Bonus (Optional)

- TODO-05 (optional): Define `spring.profiles.group.production=prod,audit` in
  `application.properties`, and add an `AuditLogger` bean gated by `@Profile("audit")` in
  `AppConfig` — so activating a single `production` profile turns on both `prod` and `audit`
  at once.
