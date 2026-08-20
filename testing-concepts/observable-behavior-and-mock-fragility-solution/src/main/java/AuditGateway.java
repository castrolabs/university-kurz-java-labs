/**
 * The application's true boundary: a proxy to an out-of-process audit
 * system. Something outside this application is watching for this call -
 * that's what makes it observable behavior, not an implementation detail.
 */
public interface AuditGateway {

    void logTransfer(String accountId, int amountCents);
}
