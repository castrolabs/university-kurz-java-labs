import java.math.BigDecimal;

/**
 * One line of an {@link Invoice}: a quantity of a product at a fixed unit
 * price.
 */
public record LineItem(String name, int quantity, BigDecimal unitPrice) {

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
