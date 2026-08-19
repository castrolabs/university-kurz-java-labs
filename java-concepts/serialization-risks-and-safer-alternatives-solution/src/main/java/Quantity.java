import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;

public final class Quantity implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int amount;

    public Quantity(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must not be negative: " + amount);
        }
        this.amount = amount;
    }

    public int amount() {
        return amount;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        if (amount < 0) {
            throw new InvalidObjectException("amount must not be negative: " + amount);
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Quantity other && amount == other.amount;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(amount);
    }
}
