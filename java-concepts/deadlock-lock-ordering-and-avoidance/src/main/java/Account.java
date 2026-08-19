public class Account {

    private final int id;
    private int balance;

    public Account(int id, int balance) {
        this.id = id;
        this.balance = balance;
    }

    public int getId() {
        return id;
    }

    public int getBalance() {
        return balance;
    }

    void debit(int amount) {
        // TODO-00: Subtract `amount` from the balance. Callers are responsible
        // for holding this account's lock before calling this method.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    void credit(int amount) {
        // TODO-01: Add `amount` to the balance. Callers are responsible for
        // holding this account's lock before calling this method.
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
