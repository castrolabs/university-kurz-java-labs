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
        balance -= amount;
    }

    void credit(int amount) {
        balance += amount;
    }
}
