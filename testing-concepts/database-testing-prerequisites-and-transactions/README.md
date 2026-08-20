# Database Testing Prerequisites and Transactions

## Goal

`TransferService` is already fully implemented — your job is to write
tests that prove its transaction boundary actually works, and to
understand why the way you write the *test* determines whether that proof
is real or just a coincidence.

## Prerequisites

- `testing-jpa-hibernate` (or equivalent Spring Data JPA / `@Transactional`
  background)
- Reading `Account`, `AccountRepository`, and `TransferService` in
  `src/main/java` — `transfer()` debits the source account, then throws if
  the destination account is closed, all inside one `@Transactional`
  method

## Task

This lab has two test classes, and the difference between them is the
entire point:

- `TransferServiceTest` is **not** annotated `@Transactional`. Read the
  class-level comment carefully — it explains why that's required for the
  rollback test to actually prove anything, instead of accidentally
  passing (or failing) for the wrong reason.
- `TransactionRollbackTest` **is** annotated `@Transactional` at the class
  level, which is what makes Spring roll back every test automatically —
  demonstrated here without any manual cleanup.

## Instructions

Complete the following TODOs:

In `TransferServiceTest`:
- TODO-00: a transfer to a closed account throws, AND the source
  account's earlier debit was rolled back along with it.
- TODO-01: a successful transfer commits both the debit and the credit.

In `TransactionRollbackTest`:
- TODO-02: a row inserted in a test is visible within that same test.
- TODO-03: that row is gone in a different test — nobody deleted it
  explicitly.

Run the tests until they all pass.

## Running the Lab

From the project root:

```bash
mvn -pl testing-concepts/database-testing-prerequisites-and-transactions test
```

Or from the lab directory:

```bash
cd testing-concepts/database-testing-prerequisites-and-transactions
mvn test
```

## Bonus (Optional)

- TODO-04 (optional, in `TransactionRollbackTest`): use
  `TestTransaction.flagForCommit()` and `TestTransaction.end()` to force a
  row to survive a real commit mid-test, then clean it up yourself since
  the automatic rollback no longer applies to it.
