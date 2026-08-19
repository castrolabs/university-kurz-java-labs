# Deadlock: Lock Ordering and Avoidance - Solution

## Overview

This is the official solution for the Deadlock: Lock Ordering and Avoidance lab. `TransferService`
picks a single, consistent lock-acquisition order for every call, no matter which account is passed
as the source and which as the destination, so the classic AB/BA deadlock cycle can never form.

## Key Concepts

### Account.debit / Account.credit: plain mutation, guarded by the caller

```java
void debit(int amount) {
    balance -= amount;
}

void credit(int amount) {
    balance += amount;
}
```

These methods are intentionally not `synchronized` themselves - they're only ever called from inside
`TransferService`, which is responsible for holding the account's lock around them. Making them
`synchronized` too would still be correct (the lock is reentrant) but would suggest, misleadingly,
that calling `debit`/`credit` alone is safe on its own; the real safety property here is "these are
only touched while the account's lock is held," which lives in `TransferService`, not in `Account`.

### transfer: lock ordering keyed by id, not by argument position

```java
public void transfer(Account from, Account to, int amount) {
    Account first = from.getId() < to.getId() ? from : to;
    Account second = from.getId() < to.getId() ? to : from;

    synchronized (first) {
        synchronized (second) {
            from.debit(amount);
            to.credit(amount);
        }
    }
}
```

The naive version - `synchronized (from) { synchronized (to) { ... } }` - looks correct because both
locks are always acquired before either balance is touched. The bug is that *which* lock is acquired
first depends on which account the caller happened to pass as `from`. A thread calling
`transfer(alice, bob, amt)` locks `alice` then `bob`; a thread calling `transfer(bob, alice, amt)`
locks `bob` then `alice`. If those two calls run concurrently, each can end up holding the lock the
other one is waiting for - permanent deadlock.

Ordering the two locks by `getId()` instead of by argument position removes the dependency on call-site
order entirely: `transfer(alice, bob, amt)` and `transfer(bob, alice, amt)` now always lock the
lower-id account first, so they converge on the same acquisition order and the cycle can't form. Note
that `from.debit(amount)` and `to.credit(amount)` are still called with the original `from`/`to`
references - only the *order the locks are taken in* changed, not which account is debited or
credited. Transferring an account to itself (`from == to`) also just works: `first` and `second` are
the same object, `synchronized` is reentrant, and debiting and crediting the same amount on the same
account cancels out.

### transferUsingIdentityHash: the same idea without a natural key

```java
public void transferUsingIdentityHash(Account from, Account to, int amount) {
    int fromHash = System.identityHashCode(from);
    int toHash = System.identityHashCode(to);

    if (fromHash < toHash) {
        synchronized (from) {
            synchronized (to) {
                from.debit(amount);
                to.credit(amount);
            }
        }
    } else if (fromHash > toHash) {
        synchronized (to) {
            synchronized (from) {
                from.debit(amount);
                to.credit(amount);
            }
        }
    } else {
        synchronized (TIE_LOCK) {
            synchronized (from) {
                synchronized (to) {
                    from.debit(amount);
                    to.credit(amount);
                }
            }
        }
    }
}
```

`Account` happens to have a natural, unique, immutable key (`id`), so ordering by it is simplest and
two distinct accounts can never collide on it. When no such key exists, `System.identityHashCode` is
the general-purpose substitute - but unlike a real unique key, two *different* objects can (extremely
rarely) produce the same identity hash. The `TIE_LOCK` branch handles that: it forces every thread that
hits a hash collision through one extra shared lock first, so at most one such thread at a time risks
acquiring the two colliding-hash locks in an otherwise arbitrary order.

## Summary

- A lock-ordering deadlock is invisible from reading either call site alone - `transfer(alice, bob)`
  and `transfer(bob, alice)` are each "obviously correct" individually; they're only incompatible with
  each other, at runtime, under concurrency.
- The fix is a single global ordering rule applied consistently everywhere two locks are taken
  together, derived from something stable and comparable about the locked objects (a natural key, or
  `System.identityHashCode` with a tie-breaker) rather than from argument position.
- The failure mode isn't an exception - it's threads that block forever. That's why the tests bound
  concurrent runs with a timeout instead of waiting unconditionally.
