# Domain Events and Consistency Boundaries (Solution)

Official solution for the `domain-events-and-consistency-boundaries` lab.

## What changed from the starter

- **`Order.Place`** validates the total and only records `OrderPlaced` in the aggregate. The
  aggregate does not know about handlers, the dispatcher or the database, so it can be tested with
  no infrastructure (the first two tests).
- **`DomainEventsInterceptor`** reads the tracked `AggregateRoot` entries, copies their events,
  clears them, and only then dispatches. Clearing first is what makes a second `SaveChanges` (or a
  handler that triggers one) not dispatch the same event again. Dispatching happens in
  `SavingChangesAsync`, before the commit, so a throwing handler aborts the whole save and the
  order is not persisted.
- **`OrderPlacedHandler`** only adds an `OutboxMessage` to the context it receives. The single
  outer `SaveChanges` commits order and outbox row atomically: the integration event exists if and
  only if the order does. It does not call `SaveChanges` itself, which would re-enter the interceptor.
- **`LoyaltyConsumer`** runs in its own context and transaction: the points change belongs to another
  aggregate. The `ProcessedMessage` row is saved in the same transaction as the points, so a
  duplicate delivery (at-least-once) finds it and does nothing. The composite key
  `(MessageId, Consumer)` lets several consumers handle the same message independently.

## Bonus notes

- Dispatching in `SavedChangesAsync` keeps every test green, but a crash between the commit and the
  dispatch loses the event with nothing to retry. That is the case the outbox exists for.
- A handler that saves inside `SavingChangesAsync` re-enters the interceptor. Handlers only add to
  the context, and the interceptor clears the events before dispatching so the recursion ends.
- Moving the loyalty work into an in-transaction handler couples failures: a loyalty outage makes
  checkout fail, exactly what `AHandlerThatThrowsRollsBackTheOrder` shows.
