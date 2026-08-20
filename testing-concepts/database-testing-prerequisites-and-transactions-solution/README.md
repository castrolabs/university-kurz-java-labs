# Database Testing Prerequisites and Transactions — Solution

## Overview

Two test classes exercise the same already-implemented `TransferService`
and `AccountRepository`, but with opposite transaction setups on purpose —
`TransferServiceTest` (no class-level `@Transactional`) proves that a
failed transfer really rolls back at the database level, while
`TransactionRollbackTest` (class-level `@Transactional`) demonstrates
Spring's automatic per-test rollback and how to deliberately opt out of it
with `TestTransaction`.

## Key Concepts

- **A test sharing a transaction with the code under test can "prove"
  rollback without it ever happening.** `TransferService.transfer()` is
  `@Transactional` with the default `REQUIRED` propagation, meaning it
  joins whatever transaction is already open rather than starting its
  own. If `TransferServiceTest` were itself `@Transactional`, calling
  `transfer()` would join the test's already-open transaction; the
  `IllegalStateException` from a closed destination account would mark
  that *shared* transaction rollback-only, but the physical `ROLLBACK`
  only happens when the outer (test) transaction ends — which is *after*
  the assertions run. Querying the checking account's balance at that
  point would still be inside the not-yet-rolled-back transaction, so the
  test's pass/fail would depend on incidental caching behavior, not on
  whether rollback genuinely occurred.
- **Keeping the test non-transactional makes the service's `@Transactional`
  boundary a real, standalone transaction.** `failedTransferRollsBackTheDebit`
  works because `TransferServiceTest` has no class-level
  `@Transactional`: `transfer()` opens its own transaction, and when it
  throws, that transaction is genuinely, physically rolled back before
  `transfer()` even returns. The `accountRepository.findById(checkingId)`
  call afterward runs in yet another fresh, separate transaction (Spring
  Data's repository methods are themselves transactional), so the balance
  it reads is unquestionably what's on disk — not a cached value from
  either transaction.
- **`@Transactional` on a test class is what buys automatic rollback.**
  `TransactionRollbackTest.insertedRowIsVisibleWithinTest` inserts a row
  with no matching cleanup anywhere, and
  `noRowLeaksFromAnotherTest` still finds nothing — Spring wraps each test
  method in its own transaction and rolls it back once the method
  returns, regardless of which test JUnit happens to run first.
- **`TestTransaction.flagForCommit()` + `.end()` intentionally defeats
  that automatic rollback.** `flagForCommitMakesTheRowSurviveACommit`
  forces the current test's transaction to actually commit mid-test — the
  row is provably visible afterward — which is exactly why that test
  cleans up the row manually: the framework has no rollback left to rely
  on for it.

## Summary

The two test classes are a matched pair: one shows what a *correct*
service-level rollback assertion requires (a real, standalone
transaction), the other shows what test-scoped `@Transactional` buys for
free (rollback as test cleanup) and how to explicitly step outside it when
a test genuinely needs to observe a committed state.
