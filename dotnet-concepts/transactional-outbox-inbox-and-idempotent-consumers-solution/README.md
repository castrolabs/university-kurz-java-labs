# Transactional Outbox, Inbox and Idempotent Consumers (Solution)

Official solution for the `transactional-outbox-inbox-and-idempotent-consumers` lab.

## What changed from the starter

- **`PlaceOrderAsync`** adds the order and the `OutboxMessage` and calls `SaveChangesAsync` once. A
  failed commit leaves neither, so there is never an event for an order that does not exist, and a
  crash after the commit leaves the row behind for the relay.
- **`DispatchBatchAsync`** selects pending, not dead-lettered, due rows ordered by `Seq`, publishes,
  and only then sets `PublishedAtMs`. It saves after each message so a crash costs at most one
  duplicate. The order is publish first and mark later on purpose: the opposite order would turn a
  crash into a lost message.
- **`RecordFailure`** counts the attempt, schedules `BaseDelay * 2^(attempts - 1)`, and dead-letters
  the row when `MaxAttempts` is reached. A dead-lettered row is skipped by the query forever, until
  someone clears `DeadLetteredAtMs` after fixing the cause.
- **`ShippingConsumer`** adds the `Shipment` and the `InboxMessage` in one `SaveChangesAsync`. The
  composite primary key `(MessageId, Consumer)` is the real guarantee: the `AnyAsync` check is only
  a cheap fast path, and a race between two deliveries is settled by the key (constraint error 19,
  mapped to `Outcome.Duplicate`).
- **`ConsumerRunner.ProcessAsync`** treats returning as the ack, redelivers with a higher count on
  an exception, and dead-letters once `DeliveryCount` reaches `maxDeliveries`.

## Bonus notes

- With the `AnyAsync` fast path removed, every test stays green: the primary key alone keeps the
  effect single, because the losing transaction rolls back the shipment together with the inbox row.
- Marking before publishing (tried: `SaveChangesAsync` and then `PublishAsync`) turns four tests red: the
  broker outage, the exhausted attempts, the crash between publish and mark, and the end-to-end. In each
  one the row is already marked as published when the publish fails, so the message is lost for good.
  That is at-most-once, the opposite trade.
- Two relays on one database can read the same pending row before either marks it, and then publish it twice. The inbox absorbs
  it. For fewer duplicates, claim rows with a lease (a `LockedUntilMs` column) or a row lock, but
  never rely on it for correctness.
