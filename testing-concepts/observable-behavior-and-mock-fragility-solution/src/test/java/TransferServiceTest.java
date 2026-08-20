import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private AuditGateway mockAuditGateway;

    private Ledger createLedgerWithBalance(String accountId, int startingBalanceCents) {
        Ledger ledger = new Ledger();
        ledger.openAccount(accountId, startingBalanceCents);
        return ledger;
    }

    @Test
    @DisplayName("a successful transfer debits the real ledger and logs to the audit gateway")
    void successfulTransferDebitsLedgerAndLogsToAudit() {
        Ledger ledger = createLedgerWithBalance("acct-1", 1000);
        TransferService service = new TransferService(ledger, mockAuditGateway);

        assertTrue(service.transfer("acct-1", 400));
        assertEquals(600, ledger.balanceOf("acct-1"));
        verify(mockAuditGateway).logTransfer("acct-1", 400);
    }

    @Test
    @DisplayName("an insufficient balance leaves the ledger untouched and never reaches the audit gateway")
    void insufficientBalanceDoesNotDebitOrLog() {
        Ledger ledger = createLedgerWithBalance("acct-1", 100);
        TransferService service = new TransferService(ledger, mockAuditGateway);

        assertFalse(service.transfer("acct-1", 400));
        assertEquals(100, ledger.balanceOf("acct-1"));
        verifyNoInteractions(mockAuditGateway);
    }

    @Test
    @DisplayName("transferring the exact balance leaves the account at zero")
    void transferringExactBalanceLeavesAccountAtZero() {
        Ledger ledger = createLedgerWithBalance("acct-1", 500);
        TransferService service = new TransferService(ledger, mockAuditGateway);

        assertTrue(service.transfer("acct-1", 500));
        assertEquals(0, ledger.balanceOf("acct-1"));
    }

    @Test
    @DisplayName("bonus: two transfers log two separate audit entries")
    void twoTransfersLogTwoAuditEntries() {
        Ledger ledger = createLedgerWithBalance("acct-1", 1000);
        TransferService service = new TransferService(ledger, mockAuditGateway);

        service.transfer("acct-1", 100);
        service.transfer("acct-1", 100);

        verify(mockAuditGateway, times(2)).logTransfer("acct-1", 100);
    }
}
