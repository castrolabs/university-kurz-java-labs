public class TransferService {

    private static final Object TIE_LOCK = new Object();

    public void transfer(Account from, Account to, int amount) {
        // TODO-02: Acquire both accounts' locks in a CONSISTENT order based on
        // Account#getId() - always lock the account with the smaller id
        // first - then move `amount` from `from` to `to` via from.debit(amount)
        // and to.credit(amount). Locking `from` then `to` regardless of which
        // one has the smaller id is what causes the deadlock this lab is
        // about: two threads transferring in opposite directions between the
        // same two accounts would then acquire the locks in opposite order.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public void transferUsingIdentityHash(Account from, Account to, int amount) {
        // TODO-03 (optional): Same idea as transfer(), but order the locks
        // using System.identityHashCode(...) instead of getId(), falling back
        // to the shared TIE_LOCK on the rare case of a hash collision between
        // two different accounts (see the article's Deep Dive #2).
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
