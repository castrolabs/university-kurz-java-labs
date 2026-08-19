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
        // TODO-02: Serialize a SerializationProxy instead of this instance,
        // so the stream never carries this class's raw field layout.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    private void readObject(ObjectInputStream in) throws InvalidObjectException {
        // TODO-03: A Reservation must only ever be produced by
        // SerializationProxy.readResolve(). Block any stream that tries to
        // deserialize a Reservation directly by always throwing
        // InvalidObjectException here.
        throw new UnsupportedOperationException("Not implemented yet.");
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

        private final int seats;

        SerializationProxy(Reservation reservation) {
            this.seats = reservation.seats;
        }

        private Object readResolve() {
            // TODO-04: Rebuild the Reservation through its real constructor,
            // so the invariant is enforced exactly the way it is for any
            // other caller - no separate validation logic to keep in sync.
            throw new UnsupportedOperationException("Not implemented yet.");
        }
    }
}
