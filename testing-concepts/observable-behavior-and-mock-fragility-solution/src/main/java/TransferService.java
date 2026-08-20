/**
 * Debits an account through the in-process {@link Ledger} and, only on
 * success, notifies the out-of-process {@link AuditGateway}. The two
 * collaborators play very different roles for testing purposes: Ledger is
 * an internal implementation detail, AuditGateway is the system boundary.
 */
public class TransferService {

    private final Ledger ledger;
    private final AuditGateway auditGateway;

    public TransferService(Ledger ledger, AuditGateway auditGateway) {
        this.ledger = ledger;
        this.auditGateway = auditGateway;
    }

    public boolean transfer(String accountId, int amountCents) {
        if (!ledger.hasSufficientFunds(accountId, amountCents)) {
            return false;
        }
        ledger.debit(accountId, amountCents);
        auditGateway.logTransfer(accountId, amountCents);
        return true;
    }
}
