# Observable Behavior and Mock Fragility

## Goal

`TransferService` is already fully implemented — your job is to test it
while applying the article's boundary rule: only mock communications that
cross the application boundary and stay observable to something outside
it. `Ledger` (in-process) is never mocked in this lab; `AuditGateway`
(the true system boundary) is the only thing that is.

## Prerequisites

- `test-doubles-stubs-and-mocking` (Mockito `@Mock`, `when()`, `verify()`)
- `observable-behavior-and-mock-fragility` article — the mock-vs-stub
  distinction and the intra-system vs. inter-system split
- Reading `TransferService`, `Ledger`, and `AuditGateway` in
  `src/main/java` before writing any test

## Task

`TransferService` depends on two collaborators: `Ledger`, an in-process
class that holds balances entirely in memory, and `AuditGateway`, an
interface standing in for an out-of-process audit system. No client of
`TransferService` ever asked for a specific sequence of calls onto
`Ledger` — they asked for a transfer to succeed or fail, and for the
balance to reflect it. An external audit system, on the other hand, is
genuinely watching for `logTransfer(...)` to be called — that call is
observable behavior, not an implementation detail.

You'll write tests that debit a *real* `Ledger` and assert on its balance,
while mocking and verifying only `AuditGateway`.

## Instructions

Complete the following TODOs:

- TODO-00: implement `createLedgerWithBalance(...)` — builds a real
  `Ledger`, never a mock.
- TODO-01: a successful transfer debits the real ledger's balance and logs
  to the audit gateway.
- TODO-02: an insufficient balance leaves the ledger untouched and never
  reaches the audit gateway.
- TODO-03: transferring the exact balance — asserted only through the
  ledger's real balance, never through a mocked/verified call to
  `Ledger.debit(...)`.

Run the tests until they all pass.

## Running the Lab

From the project root:

```bash
mvn -pl testing-concepts/observable-behavior-and-mock-fragility test
```

Or from the lab directory:

```bash
cd testing-concepts/observable-behavior-and-mock-fragility
mvn test
```

## Bonus (Optional)

- TODO-04 (optional): verify `mockAuditGateway.logTransfer(...)` was
  called exactly twice after two transfers, using
  `verify(mock, times(2))`.
