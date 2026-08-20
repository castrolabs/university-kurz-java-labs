import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * TransferService is already fully implemented in src/main/java. Your job
 * is to test it while respecting the boundary rule the article gives:
 * ONLY mock communications that cross the application boundary and stay
 * observable to something outside it. Ledger is an in-process collaborator
 * (an implementation detail) - it is NEVER mocked in this file, only used
 * as a real object built by the factory method below. AuditGateway is the
 * one true boundary here, so it's the only field annotated @Mock.
 */
@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private AuditGateway mockAuditGateway;

    // TODO-00: Create a new Ledger, call openAccount(accountId,
    // startingBalanceCents) on it, and return it. This is a REAL Ledger,
    // never a mock - every test below builds one through this method.
    private Ledger createLedgerWithBalance(String accountId, int startingBalanceCents) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    // TODO-01: Using createLedgerWithBalance("acct-1", 1000), build a
    // TransferService(ledger, mockAuditGateway). Call
    // transfer("acct-1", 400), assert it returns true, then assert
    // ledger.balanceOf("acct-1") is now 600 - the REAL ledger's own state,
    // not a mocked expectation. Finally, verify(mockAuditGateway) was
    // called with logTransfer("acct-1", 400): this verify() is legitimate
    // because AuditGateway is the true system boundary, not an
    // implementation detail.
    @Test
    @DisplayName("a successful transfer debits the real ledger and logs to the audit gateway")
    void successfulTransferDebitsLedgerAndLogsToAudit() {
        fail("TODO-01: not implemented yet");
    }

    // TODO-02: Build a ledger with only 100 cents for "acct-1". Assert
    // transfer("acct-1", 400) returns false, then assert
    // ledger.balanceOf("acct-1") is STILL 100 (unchanged). Finally, use
    // Mockito.verifyNoInteractions(mockAuditGateway) to prove the audit
    // gateway - the true boundary - was never touched when the transfer
    // failed before reaching it.
    @Test
    @DisplayName("an insufficient balance leaves the ledger untouched and never reaches the audit gateway")
    void insufficientBalanceDoesNotDebitOrLog() {
        fail("TODO-02: not implemented yet");
    }

    // TODO-03: A tempting-but-fragile alternative to TODO-01 would mock
    // Ledger itself - e.g. `Ledger mockLedger = mock(Ledger.class);
    // when(mockLedger.hasSufficientFunds(...)).thenReturn(true); ...
    // verify(mockLedger).debit("acct-1", amountCents);` - and assert that
    // debit() was called with the right arguments. DON'T write it that
    // way: no client of TransferService ever asked for "call debit() on
    // the ledger", only for the transfer to succeed and the balance to
    // reflect it. Instead, build a real ledger via
    // createLedgerWithBalance("acct-1", 500), transfer the exact balance
    // (transfer("acct-1", 500)), assert it returns true, and assert
    // ledger.balanceOf("acct-1") is now 0 - the observable outcome, not
    // the internal call that produced it. This is what lets the test
    // survive Ledger.debit() being renamed or replaced by a different
    // internal call sequence entirely.
    @Test
    @DisplayName("transferring the exact balance leaves the account at zero")
    void transferringExactBalanceLeavesAccountAtZero() {
        fail("TODO-03: not implemented yet");
    }

    // TODO-04 (optional): Build a ledger with 1000 cents for "acct-1".
    // Call transfer("acct-1", 100) twice. Use
    // Mockito.verify(mockAuditGateway, Mockito.times(2)) to assert
    // logTransfer("acct-1", 100) was called exactly twice - a legitimate
    // use of verify()'s call-count checking, because it's still the
    // boundary being verified, not an in-process collaborator.
}
