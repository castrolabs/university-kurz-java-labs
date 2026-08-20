# Concurrency Utilities: Executors and Synchronizers - Solution

## Overview

This is the official solution for the `WorkCoordinator` lab. Each method
delegates to exactly one `java.util.concurrent` synchronizer, and the tests
are what actually verify each one behaves the way its name promises -
correct code compiles either way, but only the right choice of primitive
(and the right cleanup discipline) passes.

## Key Concepts

### CountDownLatch: a one-shot gate

```java
public void arriveAtStartupGate() {
    startupGate.countDown();
}

public void awaitStartupGate() throws InterruptedException {
    startupGate.await();
}
```

`CountDownLatch` counts down to zero exactly once and then stays open
forever - there is no way to reset it. That is why it fits a startup gate
(every participant arrives once, then everyone proceeds) and would be the
wrong choice for anything that needs to happen again on a second round.

### CyclicBarrier: a reusable rendezvous

```java
public void awaitRoundBarrier() throws InterruptedException, BrokenBarrierException {
    roundBarrier.await();
}
```

`CyclicBarrier` resets itself automatically once all parties have arrived,
so the same instance can be awaited again for the next round. The test
proves this by running three full rounds through the same barrier and
checking that no participant's `await()` returns until every other
participant has also arrived *for that round* - a `CountDownLatch` could not
do this at all past the first round.

### Semaphore: throttling with a `finally`-guarded release

```java
public void runWithPermit(Runnable task) throws InterruptedException {
    resourcePermits.acquire();
    try {
        task.run();
    } finally {
        resourcePermits.release();
    }
}
```

`acquire()` blocks only once every permit is already taken, which is what
caps concurrent execution at the configured count. The `finally` block is
not optional: without it, a task that throws leaves its permit acquired
forever, and every later caller blocks on `acquire()` permanently. `Lock`
gives no automatic release either - the discipline is the same for both.

### ReentrantLock: mutual exclusion, and reentrancy

```java
public int incrementUnderLock() {
    lock.lock();
    try {
        return counter.incrementAndGet();
    } finally {
        lock.unlock();
    }
}

public int reentrantDoubleIncrement() {
    lock.lock();
    try {
        incrementUnderLock();
        return incrementUnderLock();
    } finally {
        lock.unlock();
    }
}
```

`reentrantDoubleIncrement()` acquires `lock`, then calls a method that
acquires the *same* `lock` again on the *same* thread. A `ReentrantLock`
tracks a per-thread hold count instead of a simple owned/free flag, so the
second `lock()` call succeeds immediately instead of blocking the thread
against its own outer acquisition - which is exactly what would happen with
a naive non-reentrant lock built directly on `synchronized`-free compare-
and-swap.

## Summary

- `CountDownLatch` opens once and stays open; `CyclicBarrier` resets and can
  be reused round after round - picking the wrong one is a runtime bug, not
  a compile error.
- `Semaphore.release()` belongs in a `finally` block for the same reason
  `Lock.unlock()` does: neither is automatic the way `synchronized` is.
- `ReentrantLock` lets the thread that already holds it re-acquire it
  without deadlocking against itself, which plain mutual exclusion does not
  offer.
