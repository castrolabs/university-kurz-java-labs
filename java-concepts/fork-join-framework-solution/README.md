# Fork/Join Framework - Solution

## Overview

This is the official solution for the Fork/Join lab. Both `ParallelSum` and
`ParallelSquare` follow the same shape the article describes: split the
range in half until it drops below a sequential threshold, then compute the
small piece directly - the difference between them is only whether the task
produces a value to combine.

## Key Concepts

### ParallelSum: fork one half, compute the other, then join

```java
left.fork();
long rightResult = right.compute();
long leftResult = left.join();

return leftResult + rightResult;
```

`fork()` schedules `left` for asynchronous execution and returns
immediately - the calling thread does not wait for it. Calling
`right.compute()` directly, instead of also forking it, runs `right` on the
*current* thread while `left` (potentially) runs on another. `join()` then
blocks only long enough for `left` to finish. This is the classic
one-fork-one-inline pattern from the JDK's own Fork/Join examples: it does
one less scheduling round-trip than forking both, while still letting two
pieces run concurrently.

The alternative that looks almost identical but silently defeats the whole
point:

```java
long leftResult = left.compute();   // no fork() - runs on this thread
long rightResult = right.compute(); // ...then this also runs on this thread
```

Both versions return the same sum. The test that catches this records
`Thread.currentThread().getName()` in every leaf call and asserts more than
one distinct thread shows up across a large array - the naive version never
leaves the one thread that called `invoke()`.

### ParallelSquare: invokeAll() for a task with no result

```java
invokeAll(
        new ParallelSquare(data, start, middle),
        new ParallelSquare(data, middle, end));
```

`RecursiveAction` has nothing to combine, so `invokeAll(...)` - which forks
every task but the last and runs that last one inline, then joins
everything - is more convenient here than a separate `fork()`/`join()` pair
per subtask. It blocks until both halves are done before `compute()`
returns, same as the explicit fork/join pair does for `ParallelSum`.

### sumOnCommonPool(): no ForkJoinPool to construct

```java
public static long sumOnCommonPool(long[] data) {
    ParallelSum task = new ParallelSum(data, 0, data.length, ConcurrentHashMap.newKeySet());
    return task.invoke();
}
```

`task.invoke()` here is called from a thread that is not already part of
any `ForkJoinPool` (the caller's own thread), so it routes through
`ForkJoinPool.commonPool()` automatically - no `new ForkJoinPool()` needed,
which is also how `Collection.parallelStream()` gets its parallelism.

## Summary

- `fork()` schedules a subtask for concurrent execution; `join()` blocks for
  its result. Calling `compute()` on a subtask instead runs it inline on the
  current thread - correct, but serial.
- `RecursiveAction`'s `invokeAll(...)` forks-and-joins a whole batch of
  subtasks in one call, which fits tasks that return nothing to combine.
- A test can't tell serial from parallel by checking the *answer* alone -
  both are correct. Recording which thread ran each leaf is what actually
  proves the work was parallelized.
