import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SerializationSafetyTest {

    @Nested
    @DisplayName("Quantity")
    class QuantityTest {

        @Test
        @DisplayName("should construct a valid quantity")
        void shouldConstructAValidQuantity() {
            assertEquals(5, new Quantity(5).amount());
        }

        @Test
        @DisplayName("should allow a zero amount")
        void shouldAllowAZeroAmount() {
            assertDoesNotThrow(() -> new Quantity(0));
        }

        @Test
        @DisplayName("should reject a negative amount at construction time")
        void shouldRejectANegativeAmountAtConstruction() {
            assertThrows(IllegalArgumentException.class, () -> new Quantity(-1));
        }

        @Test
        @DisplayName("should round-trip a valid quantity through serialization")
        void shouldRoundTripAValidQuantity() throws Exception {
            Quantity original = new Quantity(5);

            Quantity roundTripped = (Quantity) deserialize(serialize(original));

            assertEquals(original, roundTripped);
        }

        @Test
        @DisplayName("should reject a corrupted negative amount on deserialization")
        void shouldRejectACorruptedAmountOnDeserialization() throws Exception {
            byte[] bytes = serialize(corruptedQuantity(-5));

            assertThrows(InvalidObjectException.class, () -> deserialize(bytes));
        }

        private Quantity corruptedQuantity(int forcedAmount) throws Exception {
            Quantity quantity = new Quantity(1);
            Field field = Quantity.class.getDeclaredField("amount");
            field.setAccessible(true);
            field.setInt(quantity, forcedAmount);
            return quantity;
        }
    }

    @Nested
    @DisplayName("Reservation")
    class ReservationTest {

        @Test
        @DisplayName("should construct a valid reservation")
        void shouldConstructAValidReservation() {
            assertEquals(4, new Reservation(4).seats());
        }

        @Test
        @DisplayName("should reject a non-positive seat count at construction time")
        void shouldRejectNonPositiveSeatsAtConstruction() {
            assertThrows(IllegalArgumentException.class, () -> new Reservation(0));
            assertThrows(IllegalArgumentException.class, () -> new Reservation(-3));
        }

        @Test
        @DisplayName("should round-trip a valid reservation through its serialization proxy")
        void shouldRoundTripAValidReservationThroughItsProxy() throws Exception {
            Reservation original = new Reservation(4);

            Reservation roundTripped = (Reservation) deserialize(serialize(original));

            assertEquals(original, roundTripped);
        }

        @Test
        @DisplayName("should block direct deserialization, forcing callers through the proxy")
        void shouldBlockDirectDeserializationBypassingTheProxy() throws Exception {
            Method readObject = Reservation.class.getDeclaredMethod("readObject", ObjectInputStream.class);
            readObject.setAccessible(true);
            Reservation reservation = new Reservation(4);

            InvocationTargetException thrown = assertThrows(InvocationTargetException.class,
                    () -> readObject.invoke(reservation, (Object) null));

            assertInstanceOf(InvalidObjectException.class, thrown.getCause());
        }
    }

    private static byte[] serialize(Object o) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(o);
        }
        return bytes.toByteArray();
    }

    private static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return in.readObject();
        }
    }
}
