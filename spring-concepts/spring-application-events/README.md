# Spring Application Events

## Goal

Decouple the code that *does* something from the code that *reacts* to it, using Spring's
`ApplicationEventPublisher`, and learn how the choice between `@EventListener` and
`@TransactionalEventListener` (and its phase) decides what a listener sees when the publishing
transaction commits, rolls back, or never existed.

## Prerequisites

- Spring beans and constructor injection
- `@Transactional` and what a rollback undoes
- Basic JDBC with `JdbcClient`

## Task

`ContentService` stores content rows in an H2 table (`title` is `UNIQUE`) and announces each one
with a `ContentPublished` event. Four listeners react to it, and each one needs a *different*
relationship with the publishing transaction:

| Listener | Must... |
|---|---|
| `TitleValidator` | veto a publication: throwing must roll the INSERT back |
| `SearchIndexer` | only see content that really committed |
| `RollbackAlerter` | only see content whose transaction rolled back |
| `DraftNotifier` | run even though `DraftService` publishes with no transaction at all |

`publishAll(List)` runs in one transaction, so `["Intro", "Part 2", "Intro"]` fails on the
duplicate after two events were already published. The tests use that batch to catch listeners
that react to work that never happened.

## Instructions

- TODO-00: Implement `ContentService.publish(String)`: transactional INSERT, then publish a
  `ContentPublished(id, title)` inside the transaction, return the generated id.
- TODO-01: Annotate `TitleValidator.on(...)` so its exception propagates out of
  `publishEvent()` and rolls the transaction back.
- TODO-02: Annotate `SearchIndexer.on(...)` so it runs only after a successful commit.
- TODO-03: Annotate `RollbackAlerter.on(...)` so it runs only after a rollback.
- TODO-04: Fix `DraftNotifier`: it is a `@TransactionalEventListener`, and it is never called.

Run the tests until they all pass. The test class is deliberately not `@Transactional`: think
about why before you change that.

## Running the Lab

From the project root:

```bash
mvn -pl spring-concepts/spring-application-events test
```

Or from the lab directory:

```bash
cd spring-concepts/spring-application-events
mvn test
```

## Bonus (Optional)

- TODO-05 (optional): Add `@EnableAsync` to the application and `@Async` to `SearchIndexer.on(...)`,
  then log `Thread.currentThread().getName()` inside it. Which test starts failing, and what
  would you need (hint: Awaitility) to test an asynchronous listener reliably?
