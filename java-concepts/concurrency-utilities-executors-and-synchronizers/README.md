# Concurrency Utilities: Executors and Synchronizers

## Goal

Use the `java.util.concurrent` synchronizers for what they're each actually
good at - a one-shot startup gate, a reusable per-round rendezvous, a
throttled resource pool, and a reentrant lock - instead of reaching for
whichever one happens to compile.

## Prerequisites

- Threads and `Runnable`
- Basic `synchronized` / shared mutable state

## Task

`WorkCoordinator` bundles four independent synchronization primitives behind
one class: a `CountDownLatch` startup gate, a `CyclicBarrier` round
rendezvous, a `Semaphore`-backed permit throttle, and a `ReentrantLock`
counter. Each method exercises exactly one of them. Getting the wrong
primitive - or forgetting to release what you acquired - won't fail to
compile; it will fail (or hang) a test.

## Instructions

Complete the following TODOs in `WorkCoordinator`:

- TODO-00: Signal that one participant has arrived at the startup gate.
- TODO-01: Block until every participant has arrived at the startup gate.
- TODO-02: Block until all participants reach this round's barrier, then
  release them together - and be ready to do it again next round.
- TODO-03: Run a task while holding one of a limited number of permits,
  guaranteeing the permit is released even if the task throws.
- TODO-04: Increment a shared counter while holding a lock.

Run the tests until they all pass.

## Running the Lab

From the project root:

```bash
mvn -pl java-concepts/concurrency-utilities-executors-and-synchronizers test
```

Or from the lab directory:

```bash
cd java-concepts/concurrency-utilities-executors-and-synchronizers
mvn test
```

## Bonus (Optional)

- TODO-05 (optional): Implement `reentrantDoubleIncrement()` so it acquires
  the lock and then calls `incrementUnderLock()` - which acquires the *same*
  lock again, on the same thread - twice before releasing. A non-reentrant
  lock would deadlock a thread against itself here; `ReentrantLock` does not.
