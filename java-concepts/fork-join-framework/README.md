# Fork/Join Framework

## Goal

Implement genuine divide-and-conquer parallelism with `RecursiveTask`/
`RecursiveAction` - and prove it's actually parallel, not just an array walk
that happens to be written recursively.

## Prerequisites

- Recursion
- Basic `java.util.concurrent` (a `Thread`/`ExecutorService` mental model
  helps, but isn't required)

## Task

`ParallelSum` sums a `long[]` range and `ParallelSquare` squares one in
place - both split the range in half until it's below a threshold, then
compute the small piece directly. The trap: calling `compute()` on both
halves directly instead of `fork()`/`join()` (or `invokeAll()`) still
returns the correct answer, but runs everything on a single thread. A test
instruments `ParallelSum` to record which thread ran each leaf computation,
so a serial-in-disguise implementation fails it even though the sum is
right.

## Instructions

Complete the following TODOs in `ParallelSum`:

- TODO-00: In the base case, record the current thread's name and sum the
  range sequentially.
- TODO-01: In the recursive case, run both halves so they can execute
  concurrently, then combine their results.

Complete the following TODOs in `ParallelSquare`:

- TODO-02: In the base case, square every element in the range in place.
- TODO-03: In the recursive case, run both halves concurrently and wait for
  both to finish.

Run the tests until they all pass.

## Running the Lab

From the project root:

```bash
mvn -pl java-concepts/fork-join-framework test
```

Or from the lab directory:

```bash
cd java-concepts/fork-join-framework
mvn test
```

## Bonus (Optional)

- TODO-04 (optional): Implement `ParallelSum.sumOnCommonPool(long[] data)`
  so it sums the whole array without the caller ever constructing a
  `ForkJoinPool` - forking a task outside any pool's computational context
  routes it through `ForkJoinPool.commonPool()` automatically.
