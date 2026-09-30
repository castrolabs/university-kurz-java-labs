# Spring Modulith: Events and the Event Publication Registry

## Goal

Integrate two application modules with an event that survives failures: the publishing
transaction must never depend on the notification side being up, and a notification that failed
must be retryable later, because Spring Modulith recorded it in the Event Publication Registry.

## Prerequisites

- `@TransactionalEventListener` and transaction phases (see the `spring-application-events` lab)
- Application modules and verification (see the `spring-modulith-application-modules-and-verification` lab)
- Basic JDBC with `JdbcClient`

## Task

`publishing` stores content and publishes `ContentPublished`. `notification` reacts by sending an
e-mail through `MailGateway`, which the tests can switch off to simulate an outage.

`spring-modulith-starter-jdbc` is already on the classpath, so every publication to a
transactional listener is written to the `EVENT_PUBLICATION` table in the same transaction as the
business data, and later marked `COMPLETED` or `FAILED`. The tests read that table directly.

## Instructions

- TODO-00: Implement `PublishingService.publish(String)`: one transaction that inserts the row,
  publishes the event, and then applies the late "spam" rule.
- TODO-01: Replace `@EventListener` in `NotificationListener` so an e-mail outage no longer breaks
  publishing, and the failed delivery stays in the registry.
- TODO-02: Implement `NotificationRecovery.retryFailed()` so failed publications are resubmitted.

Run the tests until they all pass. They use Awaitility because the listener is asynchronous.

## Running the Lab

From the project root:

```bash
mvn -pl spring-concepts/spring-modulith-events-and-publication-registry test
```

Or from the lab directory:

```bash
cd spring-concepts/spring-modulith-events-and-publication-registry
mvn test
```

## Bonus (Optional)

- TODO-03 (optional): Set `spring.modulith.events.completion-mode=delete` and adapt the tests:
  what does the table look like after a successful delivery now? Try `archive` too.
- TODO-04 (optional): Switch `NotificationListener` to a plain `@TransactionalEventListener`.
  The tests still pass: the registry tracks any transactional listener. Log the thread name to
  see what `@ApplicationModuleListener` adds on top (async, and a new transaction for the
  listener's own writes).
