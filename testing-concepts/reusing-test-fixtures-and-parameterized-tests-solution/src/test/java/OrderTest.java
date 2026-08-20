import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderTest {

    private Inventory createInventoryWithStock(String sku, int quantity) {
        Inventory inventory = new Inventory();
        inventory.addStock(sku, quantity);
        return inventory;
    }

    @Test
    @DisplayName("place() succeeds and decrements stock when enough inventory")
    void placeSucceedsWhenEnoughInventory() {
        Inventory inventory = createInventoryWithStock("SKU-1", 10);
        Order order = new Order(inventory);

        assertThat(order.place("SKU-1", 5)).isTrue();
        assertThat(inventory.quantityOf("SKU-1")).isEqualTo(5);
    }

    @Test
    @DisplayName("place() fails and leaves stock untouched when not enough inventory")
    void placeFailsWhenNotEnoughInventory() {
        Inventory inventory = createInventoryWithStock("SKU-1", 3);
        Order order = new Order(inventory);

        assertEquals(false, order.place("SKU-1", 5));
        assertEquals(3, inventory.quantityOf("SKU-1"));
    }

    @ParameterizedTest
    @DisplayName("shippingFeeCents() reflects the order-total tiers")
    @CsvSource({
            "0, 999",
            "1999, 999",
            "2000, 499",
            "4999, 499",
            "5000, 0",
            "10000, 0"
    })
    void shippingFeeReflectsOrderTotalTiers(int orderTotalCents, int expectedFeeCents) {
        assertEquals(expectedFeeCents, Order.shippingFeeCents(orderTotalCents));
    }

    @ParameterizedTest
    @DisplayName("isWithinReturnWindow() reflects the 30-day boundary")
    @MethodSource("returnWindowCases")
    void isWithinReturnWindowReflectsBoundary(LocalDate purchaseDate, LocalDate today, boolean expected) {
        assertEquals(expected, Order.isWithinReturnWindow(purchaseDate, today));
    }

    static Stream<Arguments> returnWindowCases() {
        LocalDate purchaseDate = LocalDate.now();
        return Stream.of(
                Arguments.of(purchaseDate, purchaseDate, true),
                Arguments.of(purchaseDate, purchaseDate.plusDays(30), true),
                Arguments.of(purchaseDate, purchaseDate.plusDays(31), false)
        );
    }
}
