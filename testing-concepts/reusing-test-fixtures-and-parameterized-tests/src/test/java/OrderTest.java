import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Order and Inventory are already fully implemented in src/main/java — your
 * job is to write the tests. This class deliberately has NO @BeforeEach and
 * NO shared Inventory/Order fields: Inventory is mutable, so a field shared
 * across tests would let one test's place() calls change the starting stock
 * every other test sees. Instead, build a fresh fixture per test through the
 * private factory method below (the "Object Mother" pattern).
 */
class OrderTest {

    // TODO-00: Implement this factory method: create a new Inventory,
    // call addStock(sku, quantity) on it once, and return it. Every test
    // that needs stock calls this instead of sharing one Inventory field.
    private Inventory createInventoryWithStock(String sku, int quantity) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    // TODO-01: Using createInventoryWithStock("SKU-1", 10), build a new
    // Order(inventory). Call order.place("SKU-1", 5), assert it returns
    // true, then assert inventory.quantityOf("SKU-1") is now 5.
    @Test
    @DisplayName("place() succeeds and decrements stock when enough inventory")
    void placeSucceedsWhenEnoughInventory() {
        fail("TODO-01: not implemented yet");
    }

    // TODO-02: Build a fresh inventory with only 3 units of "SKU-1" via
    // createInventoryWithStock. Assert order.place("SKU-1", 5) returns
    // false, then assert inventory.quantityOf("SKU-1") is STILL 3 - a
    // failed order must not partially decrement stock. Note this is a
    // brand new Inventory instance, unaffected by TODO-01's test.
    @Test
    @DisplayName("place() fails and leaves stock untouched when not enough inventory")
    void placeFailsWhenNotEnoughInventory() {
        fail("TODO-02: not implemented yet");
    }

    // TODO-03: Add a @CsvSource above this method with rows of
    // "orderTotalCents, expectedFeeCents" that pin down the shipping-fee
    // tiers: 999 below 2000, 499 from 2000 up to (not including) 5000, and
    // 0 (free) from 5000 up. Cover the value immediately below AND the
    // value at each boundary (e.g. 1999 vs 2000, 4999 vs 5000).
    @ParameterizedTest
    @DisplayName("shippingFeeCents() reflects the order-total tiers")
    void shippingFeeReflectsOrderTotalTiers(int orderTotalCents, int expectedFeeCents) {
        assertEquals(expectedFeeCents, Order.shippingFeeCents(orderTotalCents));
    }

    // TODO-04: Add a @MethodSource("returnWindowCases") annotation above
    // this method. The cases use LocalDate.now() arithmetic, which the
    // compiler can't treat as a constant - that's exactly why this can't be
    // a @CsvSource/@ValueSource and needs @MethodSource instead.
    @ParameterizedTest
    @DisplayName("isWithinReturnWindow() reflects the 30-day boundary")
    void isWithinReturnWindowReflectsBoundary(LocalDate purchaseDate, LocalDate today, boolean expected) {
        assertEquals(expected, Order.isWithinReturnWindow(purchaseDate, today));
    }

    // TODO-05: Implement this method so it returns a Stream<Arguments> of
    // (purchaseDate, today, expected) triples, built from LocalDate.now():
    // one case exactly 30 days after purchase (still within the window,
    // expected true), one case 31 days after (outside, expected false), and
    // one case on the purchase date itself (expected true).
    static Stream<Arguments> returnWindowCases() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    // TODO-06 (optional): Rewrite the assertions in
    // placeSucceedsWhenEnoughInventory using AssertJ instead of JUnit's
    // assertEquals/assertTrue - e.g. assertThat(order.place(...)).isTrue()
    // and assertThat(inventory.quantityOf("SKU-1")).isEqualTo(5). Compare
    // how the fluent, [subject][action][object] phrasing reads against the
    // positional expected/actual arguments of assertEquals.
}
