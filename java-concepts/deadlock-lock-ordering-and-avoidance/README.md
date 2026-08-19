# Deadlock: Lock Ordering and Avoidance

## Goal

Implement a funds-transfer method that acquires two locks in a *consistent* order — and see, with a
real multi-threaded test, why locking "source, then destination" for every call is a lock-ordering
deadlock waiting to happen.

## Prerequisites

- Basic Java syntax
- `synchronized` blocks and intrinsic locks
- `ExecutorService`, `CountDownLatch` (used by the tests, not something you need to write)

## Task

`Account` holds a balance guarded by its own intrinsic lock. `TransferService.transfer` needs to hold
both accounts' locks at once to move money between them atomically. Locking `from` and then `to` looks
correct in isolation, but if one thread transfers `alice -> bob` while another simultaneously transfers
`bob -> alice`, they acquire the same two locks in opposite order — a textbook deadlock. You'll fix it
by always locking accounts in the same order, regardless of which one is the source and which is the
destination.

## Instructions

Complete the following TODOs:

- TODO-00: Implement `Account.debit(int amount)`.
- TODO-01: Implement `Account.credit(int amount)`.
- TODO-02: Implement `TransferService.transfer(from, to, amount)` so it always locks the account with
  the smaller `id` first, then the other one, before calling `from.debit(amount)` and
  `to.credit(amount)`.

Run the tests until they all pass. The concurrency test fires many threads doing `alice -> bob` and
`bob -> alice` transfers at the same time and asserts the whole batch finishes within a bounded
timeout — an inconsistent lock order will hang and time out instead of throwing a normal assertion
failure.

## Running the Lab

From the project root:

```bash
mvn -pl java-concepts/deadlock-lock-ordering-and-avoidance test
```

Or from the lab directory:

```bash
cd java-concepts/deadlock-lock-ordering-and-avoidance
mvn test
```

## Bonus (Optional)

- TODO-03 (optional): Implement `TransferService.transferUsingIdentityHash(from, to, amount)`, an
  alternate lock-ordering strategy based on `System.identityHashCode(...)` instead of `Account#getId()`,
  including the tie-breaking lock for the (extremely rare) case of a hash collision between two
  different accounts.
