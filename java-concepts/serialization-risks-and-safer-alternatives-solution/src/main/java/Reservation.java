import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;

public final class Reservation implements Serializable {

    private final int seats;

    public Reservation(int seats) {
        if (seats <= 0) {
            throw new IllegalArgumentException("seats must be positive: " + seats);
        }
        this.seats = seats;
    }

    public int seats() {
        return seats;
    }

    private Object writeReplace() {
        return new SerializationProxy(this);
    }

    private void readObject(ObjectInputStream in) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Reservation other && seats == other.seats;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(seats);
    }

    private static class SerializationProxy implements Serializable {

        private static final long serialVersionUID = 1L;

        private final int seats;

        SerializationProxy(Reservation reservation) {
            this.seats = reservation.seats;
        }

        private Object readResolve() {
            return new Reservation(seats);
        }
    }
}
