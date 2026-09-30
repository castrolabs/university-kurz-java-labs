# Spring Modulith: Testing and Documentation

## Goal

Test one application module at a time instead of the whole application, assert on the events a
module publishes and consumes, and generate architecture documentation (PlantUML diagrams and
module canvases) straight from the code.

This lab follows the reverse pattern: the application is finished and correct. You write the
tests.

## Prerequisites

- Application modules and `verify()` (see `spring-modulith-application-modules-and-verification`)
- `@ApplicationModuleListener` (see `spring-modulith-events-and-publication-registry`)
- JUnit 5, AssertJ, and basic Mockito

## Task

Two modules:

- `publishing`: `PublishingService.publish(title)` stores a row and publishes `ContentPublished`;
  `ContentCatalog` is its read API.
- `notification`: `NotificationListener` (an `@ApplicationModuleListener`) reacts to
  `ContentPublished` and publishes `NotificationSent`; `DigestService` uses `ContentCatalog`, a
  bean from the other module.

Each module test class sits in its module's package and is annotated `@ApplicationModuleTest`,
which starts only that module (bootstrap mode `STANDALONE`). `NotificationModuleTests` does not
even start yet.

## Instructions

In `publishing/PublishingModuleTests`:
- TODO-00: assert the published event with `AssertablePublishedEvents`.
- TODO-01: assert the context contains the publishing beans and none of notification's.

In `notification/NotificationModuleTests`:
- TODO-02: make the context start without booting `publishing`, then use `Scenario` to publish a
  `ContentPublished` and wait for the matching `NotificationSent`.
- TODO-03: stub the mocked `ContentCatalog` and test `DigestService`.

In `ModularityTests`:
- TODO-04: generate the documentation with `Documenter` and assert the files exist.

Run the tests until they all pass.

## Running the Lab

From the project root:

```bash
mvn -pl spring-concepts/spring-modulith-testing-and-documentation test
```

Or from the lab directory:

```bash
cd spring-concepts/spring-modulith-testing-and-documentation
mvn test
```

## Bonus (Optional)

- TODO-05 (optional): Remove the mock and use `@ApplicationModuleTest(mode =
  BootstrapMode.DIRECT_DEPENDENCIES)` instead. Which beans are in the context now, and what does
  that cost when `publishing` needs a database?
- TODO-06 (optional): Open `target/spring-modulith-docs/module-notification.adoc` and find the
  "Bean references" and "Events listened to" rows. Render `components.puml` with any PlantUML
  viewer.
