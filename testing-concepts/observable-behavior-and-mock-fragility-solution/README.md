# Observable Behavior and Mock Fragility — Solution

## Overview

`TransferServiceTest` tests the same (already-implemented)
`TransferService` four ways, but every test follows the same boundary
rule: `Ledger` is always a real object, `AuditGateway` is always the only
mock.

## Key Concepts

- **Ledger is never mocked, anywhere in this file.** `Ledger` is an
  in-process collaborator — a call from `TransferService` to
  `Ledger.debit(...)` is an implementation detail no client of
  `TransferService` ever asked for. Building a real `Ledger` through
  `createLedgerWithBalance(...)` and reading its actual `balanceOf(...)`
  afterward is the classical-style choice the article recommends: use the
  real, fast, deterministic object instead of doubling it.
- **`AuditGateway` is mocked and verified because it is the true system
  boundary.** `successfulTransferDebitsLedgerAndLogsToAudit` calls
  `verify(mockAuditGateway).logTransfer("acct-1", 400)` — legitimate
  because an out-of-process audit system is genuinely watching for that
  call, unlike any call `TransferService` makes to `Ledger`.
- **`insufficientBalanceDoesNotDebitOrLog` proves negative behavior on
  both sides.** It asserts the ledger's balance is unchanged (state on the
  real collaborator) *and* calls `verifyNoInteractions(mockAuditGateway)`
  (interaction on the boundary mock) — together they show the failed
  transfer never reached the point where it would debit or audit.
- **`transferringExactBalanceLeavesAccountAtZero` is the test that would
  have been fragile the other way.** The tempting alternative described in
  TODO-03 — mocking `Ledger` and calling
  `verify(mockLedger).debit("acct-1", 500)` — ties the test to the literal
  method name `debit`. This test instead asserts
  `ledger.balanceOf("acct-1") == 0`, the actual observable outcome, which
  keeps passing even if `Ledger`'s internals are restructured to reach
  zero a different way.
- **The bonus test still only mocks the boundary.** `times(2)` counts
  calls to `mockAuditGateway`, not to `Ledger` — call-count verification is
  fine here for the same reason `verify()` was fine at all: it targets the
  one collaborator whose calls are genuinely observable outside the
  application.

## Summary

Every mock-based assertion in this file targets `AuditGateway`; every
assertion about `Ledger` reads its real, post-call state. That split is
the whole lesson: mocking isn't what makes a test fragile — mocking (and
verifying) an interaction that was never part of what a client can
observe is what makes it fragile.
