# Spring Modulith: Application Modules and Verification

## Goal

Turn a "package by feature" Spring Boot monolith into a modular monolith whose boundaries are
checked by a test: hide a module's internals in a subpackage, give other modules a real API to
call, break a dependency cycle with an event, and declare which modules a module may depend on.

## Prerequisites

- Java packages and `public` vs package-private visibility
- Spring beans and `ApplicationEventPublisher` (see the `spring-application-events` lab)
- JUnit 5 and AssertJ

## Task

`com.kurz.contentmod` has two direct subpackages, so Spring Modulith sees two application
modules: `publishing` and `notification`. The code works, but:

- `PublishingService` calls `NotificationService`, and `NotificationService` takes a
  `publishing.Content`: a **cycle**.
- `notification.DigestService` autowires `publishing.ContentRepository` directly, because it is
  public and sits in `publishing`'s base package (which Modulith treats as the module's API).

`ModularityTest` runs `ApplicationModules.of(ContentModApplication.class).verify()` plus a few
targeted checks, without starting Spring. `ContentModApplicationTest` boots the app and makes sure
the refactoring does not change behaviour. Run the tests first and read the `Violations` message.

## Instructions

- TODO-00: Move `ContentRepository` into `com.kurz.contentmod.publishing.internal`. Notice that
  `DigestService` still *compiles*: run `ModularityTest` and read the new violation.
- TODO-01: Implement `ContentCatalog.recentTitles(int)` (newest first) and make `DigestService` use
  it instead of the repository.
- TODO-02: Break the cycle. `PublishingService` publishes a `ContentPublished` event;
  `NotificationService` listens to it with `@EventListener` instead of being called.
- TODO-03: Add `notification/package-info.java` with
  `@ApplicationModule(allowedDependencies = "publishing")`.

Run the tests until they all pass.

## Running the Lab

From the project root:

```bash
mvn -pl spring-concepts/spring-modulith-application-modules-and-verification test
```

Or from the lab directory:

```bash
cd spring-concepts/spring-modulith-application-modules-and-verification
mvn test
```

## Bonus (Optional)

- TODO-04 (optional): Move `ContentPublished` into `publishing.events`, mark that package with
  `@NamedInterface("events")` in its own `package-info.java`, and change notification's
  declaration to `allowedDependencies = {"publishing", "publishing :: events"}`. Then remove
  `"publishing"` from the list and read which types `verify()` now rejects.
- TODO-05 (optional): Add a third module, `analytics`, that counts `ContentPublished` events,
  and print `ApplicationModules.of(ContentModApplication.class)` to see how Modulith describes it.
