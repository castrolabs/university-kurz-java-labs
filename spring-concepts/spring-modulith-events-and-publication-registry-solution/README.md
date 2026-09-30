# Spring Modulith: Events and the Event Publication Registry - Solution

## Overview

This is the official solution for the Event Publication Registry lab. Publishing and notification
are decoupled in time and in failure: the content commits even while e-mail is down, the registry
keeps the undelivered publication as `FAILED`, and one call resubmits it.

## Key Concepts

### @ApplicationModuleListener

```java
@ApplicationModuleListener
void on(ContentPublished event) { mail.send("New content: " + event.title()); }
```

It is a composed annotation: `@Async`, `@Transactional(propagation = REQUIRES_NEW)` and
`@TransactionalEventListener` (AFTER_COMMIT). The listener runs on another thread, only after the
publisher committed, inside a transaction of its own.

### The registry writes in the business transaction

With `spring-modulith-starter-jdbc` on the classpath, publishing an event that has transactional
listeners inserts one `EVENT_PUBLICATION` row per listener *in the publisher's transaction*. So:

| Situation | content | EVENT_PUBLICATION |
|---|---|---|
| listener succeeded | 1 row | `COMPLETED` |
| listener threw | 1 row | `FAILED` |
| publisher rolled back | 0 rows | 0 rows |

This is the transactional outbox pattern implemented inside the application. In Spring Modulith
2.1 the table is created automatically (`spring.modulith.events.jdbc.schema-initialization.enabled`
matches when missing); in production you normally disable that and own the DDL in Flyway or
Liquibase.

### Resubmitting

```java
failed.resubmit(ResubmissionOptions.defaults());
```

`FailedEventPublications` (new in 2.0) resubmits publications in state `FAILED`;
`ResubmissionOptions` can limit batch size, in-flight count, minimum age, or filter by event.
`spring.modulith.events.republish-outstanding-events-on-restart=true` does a similar sweep once at
startup.

## Summary

An event makes modules independent at compile time; the registry makes them independent at run
time too. Delivery becomes at-least-once, so listeners must be idempotent: a resubmitted
publication may run a listener that already did part of its work.
