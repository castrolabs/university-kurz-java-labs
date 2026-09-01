import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PriceCatalog")
class PriceCatalogTest {

    @Test
    @DisplayName("should return empty when setting the price of a brand new product")
    void shouldReturnEmptyWhenSettingPriceForNewProduct() {
        PriceCatalog catalog = new PriceCatalog();

        assertEquals(Optional.empty(), catalog.setPrice("Keyboard", 89.90));
        assertEquals(1, catalog.size());
    }

    @Test
    @DisplayName("should return the previous price when overwriting an existing product")
    void shouldReturnPreviousPriceWhenOverwritingAnExistingProduct() {
        PriceCatalog catalog = new PriceCatalog();
        catalog.setPrice("Keyboard", 89.90);

        Optional<Double> previous = catalog.setPrice("Keyboard", 99.90);

        assertEquals(Optional.of(89.90), previous);
    }

    @Test
    @DisplayName("should overwrite, not duplicate, when setting the price of the same product twice")
    void shouldOverwriteNotDuplicateWhenSettingPriceTwice() {
        PriceCatalog catalog = new PriceCatalog();
        catalog.setPrice("Keyboard", 89.90);

        catalog.setPrice("Keyboard", 99.90);

        assertEquals(1, catalog.size());
        assertEquals(Optional.of(99.90), catalog.getPrice("Keyboard"));
    }

    @Test
    @DisplayName("should return empty when getting the price of an unknown product")
    void shouldReturnEmptyWhenGettingPriceForUnknownProduct() {
        PriceCatalog catalog = new PriceCatalog();

        assertEquals(Optional.empty(), catalog.getPrice("Monitor"));
    }

    @Test
    @DisplayName("should return the price when the product exists")
    void shouldReturnPriceWhenProductExists() {
        PriceCatalog catalog = new PriceCatalog();
        catalog.setPrice("Monitor", 799.00);

        assertEquals(Optional.of(799.00), catalog.getPrice("Monitor"));
    }

    @Test
    @DisplayName("should apply a discount and return true")
    void shouldApplyDiscountAndReturnTrue() {
        PriceCatalog catalog = new PriceCatalog();
        catalog.setPrice("Mouse", 200.00);

        boolean applied = catalog.applyDiscount("Mouse", 25);

        assertTrue(applied);
        assertEquals(Optional.of(150.00), catalog.getPrice("Mouse"));
    }

    @Test
    @DisplayName("should return false when applying a discount to an unknown product")
    void shouldReturnFalseWhenApplyingDiscountToUnknownProduct() {
        PriceCatalog catalog = new PriceCatalog();

        assertFalse(catalog.applyDiscount("Webcam", 10));
    }

    @Test
    @DisplayName("should not change the catalog when applying a discount to an unknown product")
    void shouldNotChangeCatalogWhenDiscountAppliedToUnknownProduct() {
        PriceCatalog catalog = new PriceCatalog();
        catalog.setPrice("Mouse", 200.00);

        catalog.applyDiscount("Webcam", 10);

        assertEquals(1, catalog.size());
        assertEquals(Optional.empty(), catalog.getPrice("Webcam"));
    }

    @Test
    @DisplayName("should return empty when finding the most expensive product in an empty catalog")
    void shouldReturnEmptyWhenFindingMostExpensiveInEmptyCatalog() {
        PriceCatalog catalog = new PriceCatalog();

        assertEquals(Optional.empty(), catalog.mostExpensive());
    }

    @Test
    @DisplayName("should return the most expensive product")
    void shouldReturnMostExpensiveProduct() {
        PriceCatalog catalog = new PriceCatalog();
        catalog.setPrice("Keyboard", 89.90);
        catalog.setPrice("Monitor", 799.00);
        catalog.setPrice("Mouse", 49.90);

        assertEquals(Optional.of("Monitor"), catalog.mostExpensive());
    }

    @Test
    @DisplayName("should return the total value of every price in the catalog")
    void shouldReturnTotalValueOfAllPrices() {
        PriceCatalog catalog = new PriceCatalog();
        catalog.setPrice("Keyboard", 89.90);
        catalog.setPrice("Monitor", 799.00);
        catalog.setPrice("Mouse", 49.90);

        assertEquals(938.80, catalog.totalValue(), 0.001);
    }

    @Test
    @DisplayName("should return zero as the total value of an empty catalog")
    void shouldReturnZeroAsTotalValueOfEmptyCatalog() {
        PriceCatalog catalog = new PriceCatalog();

        assertEquals(0.0, catalog.totalValue(), 0.001);
    }
}
