# Spring Boot Autoconfiguration

## Goal

Understand how a Spring Boot autoconfiguration class works: it contributes a default bean,
but only when nothing else in the application already defines one of the same type.

## Prerequisites

- Basic Spring dependency injection (`@Bean`, `@Configuration`)
- Familiarity with the Spring Boot autoconfiguration mechanism (`@ConditionalOnClass`,
  `@ConditionalOnMissingBean`)

## Task

`GreetingAutoConfiguration` is a minimal `@AutoConfiguration` class that contributes a
`GreetingService` bean backed by `DefaultGreetingService`. You'll implement the default
service and make the autoconfiguration back off whenever the application supplies its own
`GreetingService` bean — the same mechanism Spring Boot itself uses for things like
`DataSource` or `ObjectMapper`.

The test uses `ApplicationContextRunner`, the idiomatic way to test autoconfiguration classes
in isolation without booting a full application.

## Instructions

Complete the following TODOs:

- TODO-00: Implement `DefaultGreetingService.greet()`.
- TODO-01: Add the conditional annotation to `GreetingAutoConfiguration.greetingService()` so
  it only creates the default bean when no other `GreetingService` bean exists.

Run the tests until they all pass.

## Running the Lab

From the project root:

```bash
mvn -pl spring-concepts/spring-boot-autoconfiguration test
```

Or from the lab directory:

```bash
cd spring-concepts/spring-boot-autoconfiguration
mvn test
```
