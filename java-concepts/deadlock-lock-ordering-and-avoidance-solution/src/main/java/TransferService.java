public class TransferService {

    private static final Object TIE_LOCK = new Object();

    public void transfer(Account from, Account to, int amount) {
        Account first = from.getId() < to.getId() ? from : to;
        Account second = from.getId() < to.getId() ? to : from;

        synchronized (first) {
            synchronized (second) {
                from.debit(amount);
                to.credit(amount);
            }
        }
    }

    public void transferUsingIdentityHash(Account from, Account to, int amount) {
        int fromHash = System.identityHashCode(from);
        int toHash = System.identityHashCode(to);

        if (fromHash < toHash) {
            synchronized (from) {
                synchronized (to) {
                    from.debit(amount);
                    to.credit(amount);
                }
            }
        } else if (fromHash > toHash) {
            synchronized (to) {
                synchronized (from) {
                    from.debit(amount);
                    to.credit(amount);
                }
            }
        } else {
            synchronized (TIE_LOCK) {
                synchronized (from) {
                    synchronized (to) {
                        from.debit(amount);
                        to.credit(amount);
                    }
                }
            }
        }
    }
}
