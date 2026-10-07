# Domain Events and Consistency Boundaries

## Goal

Raise domain events from an aggregate, dispatch them from an EF Core `SaveChanges` interceptor in
the same transaction, translate them into an outbox row, and consume the integration event in a
separate transaction, idempotently.

## Prerequisites

- Aggregates and EF Core basics
- `SaveChangesInterceptor`
- .NET 10 SDK

## Task

An `Order` is placed for a customer. Two things must follow: an integration event for other modules,
and loyalty points for the customer. The two live in different aggregates, so they cannot share a
transaction.

```text
Order.Place        -> raises OrderPlaced (the aggregate only records it)
Interceptor        -> before the commit, dispatches the events of tracked aggregates
OrderPlacedHandler -> same transaction: adds an OutboxMessage next to the order
LoyaltyConsumer    -> own transaction, after the commit: awards points to the Customer
```

The `DbContext`, the dispatcher and the event types are done. The business logic is not: the order
raises nothing, the interceptor dispatches nothing, the handler and the consumer throw. The tests
use SQLite in memory and open several contexts on the same connection, so you can see what was
really committed.

## Instructions

Complete the following TODOs:

- TODO-00: Implement `Order.Place`: validate the total and raise `OrderPlaced`.
- TODO-01: Implement `DomainEventsInterceptor.SavingChangesAsync`: collect, clear, dispatch.
- TODO-02: Implement `OrderPlacedHandler`: add the outbox row without saving.
- TODO-03: Implement `LoyaltyConsumer`: award points once per message.

Run the tests until they all pass. Pay attention to the three tests that check what is NOT in the
database after a failure or a duplicate delivery.

## Running the Lab

From the lab directory:

```bash
dotnet test
```

## Bonus (Optional)

- TODO-04 (optional): Move the dispatch to `SavedChangesAsync` (after the commit). Which test fails,
  and which failure would you never see in tests (a crash between the commit and the dispatch)?
- TODO-05 (optional): Make `OrderPlacedHandler` call `db.SaveChangesAsync()`. What happens to the
  events, and why does the interceptor need to clear them first?
- TODO-06 (optional): Register the `Exploding` handler only for `OrderPlaced`, then move the loyalty
  work into an in-transaction handler. What does a loyalty outage do to checkout now?
