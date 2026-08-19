import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;

public final class Quantity implements Serializable {

    // TODO-05 (optional): Declare a `private static final long
    // serialVersionUID` so this class's serial form is pinned explicitly
    // instead of relying on the compiler-generated default, which changes
    // whenever the class's shape changes.

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
        // TODO-00: Read the default fields from the stream.

        // TODO-01: readObject never runs the constructor, so its invariant
        // (amount must not be negative) does not hold for free. Re-check it
        // here, and throw InvalidObjectException - not
        // IllegalArgumentException - when it is violated.

        throw new UnsupportedOperationException("Not implemented yet.");
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
