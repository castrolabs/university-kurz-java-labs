import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InvoiceTest {

    @Test
    @DisplayName("total() applies the bulk discount above the threshold")
    void totalAppliesBulkDiscountAboveThreshold() {
        Invoice invoice = new Invoice(List.of(new LineItem("Widget", 3, new BigDecimal("40.00"))));

        assertEquals(new BigDecimal("108.00"), invoice.total());
    }

    @Test
    @DisplayName("total() applies no discount just below the threshold")
    void totalHasNoDiscountJustBelowThreshold() {
        Invoice invoice = new Invoice(List.of(new LineItem("Gadget", 1, new BigDecimal("99.99"))));

        assertEquals(new BigDecimal("99.99"), invoice.total());
    }

    @Test
    @DisplayName("total() sums multiple line items and applies the discount at the exact boundary")
    void totalSumsMultipleLineItemsAtExactBoundary() {
        Invoice invoice = new Invoice(List.of(
                new LineItem("A", 1, new BigDecimal("50.00")),
                new LineItem("B", 1, new BigDecimal("30.00")),
                new LineItem("C", 1, new BigDecimal("20.00"))
        ));

        assertEquals(new BigDecimal("90.00"), invoice.total());
    }

    @Test
    @DisplayName("total() of an empty invoice is zero")
    void totalOfEmptyInvoiceIsZero() {
        Invoice invoice = new Invoice(List.of());

        assertEquals(new BigDecimal("0.00"), invoice.total());
    }

    @Test
    @DisplayName("bonus: itemCount() reflects the number of line items (a trivial test)")
    void itemCountReflectsLineItemCount() {
        Invoice invoice = new Invoice(List.of(
                new LineItem("A", 1, new BigDecimal("1.00")),
                new LineItem("B", 1, new BigDecimal("2.00")),
                new LineItem("C", 1, new BigDecimal("3.00"))
        ));

        assertEquals(3, invoice.itemCount());
    }
}
