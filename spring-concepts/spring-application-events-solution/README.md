# Spring Application Events - Solution

## Overview

This is the official solution for the Spring Application Events lab. `ContentService` publishes a
`ContentPublished` event inside its transaction, and four listeners each pick the listener type
and transaction phase that match what they are allowed to observe.

## Key Concepts

### Publishing inside the transaction

```java
@Transactional
public long publish(String title) {
    // INSERT ...
    events.publishEvent(new ContentPublished(id, title));
    return id;
}
```

`publishEvent()` dispatches synchronously. Plain `@EventListener` methods run right there, on the
same thread and inside the same transaction. `@TransactionalEventListener` methods are *not* called
yet: Spring registers a transaction synchronization and calls them when the transaction reaches
the chosen phase.

### A synchronous listener can veto

```java
@EventListener
public void on(ContentPublished event) {
    if (event.title().toLowerCase().contains("spam")) throw new IllegalArgumentException(...);
}
```

The exception travels back through `publishEvent()` into `publish()`, so `@Transactional` rolls
the INSERT back. As a `@TransactionalEventListener` the same code would run after the commit, when
it is too late to undo anything.

### AFTER_COMMIT and AFTER_ROLLBACK

```java
@TransactionalEventListener                                    // AFTER_COMMIT (default)
@TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
```

In the failing batch `["Intro", "Part 2", "Intro"]`, two events are published before the third
INSERT hits the `UNIQUE` constraint. A plain `@EventListener` indexer would already have indexed
"Intro" and "Part 2" (ghost entries for rows that no longer exist). The AFTER_COMMIT indexer sees
nothing; the AFTER_ROLLBACK alerter sees exactly those two.

### No transaction, no call (unless you ask)

```java
@TransactionalEventListener(fallbackExecution = true)
```

When an event is published outside any transaction, a transactional listener is silently
skipped. `fallbackExecution = true` makes it run immediately instead.

## Summary

Events remove the compile-time dependency from publisher to listener, but not the runtime
relationship: each listener has to decide whether it is part of the transaction (and can veto it)
or a consequence of it (and must wait for the outcome). Spring Modulith's
`@ApplicationModuleListener` is exactly this last case made the default: async, AFTER_COMMIT, in a
new transaction of its own.
