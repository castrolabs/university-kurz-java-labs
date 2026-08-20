import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Sums a list of {@link LineItem}s and applies a bulk discount once the
 * subtotal reaches a threshold.
 */
public class Invoice {

    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");

    private final List<LineItem> items;

    public Invoice(List<LineItem> items) {
        this.items = List.copyOf(items);
    }

    public int itemCount() {
        return items.size();
    }

    /**
     * Subtotal minus a 10% discount, applied only once the subtotal is at
     * least {@code DISCOUNT_THRESHOLD}. The summing and discount logic are
     * both handled internally by {@link #computeSubtotal()} - callers only
     * ever see the final total.
     */
    public BigDecimal total() {
        BigDecimal subtotal = computeSubtotal();
        BigDecimal discount = subtotal.compareTo(DISCOUNT_THRESHOLD) >= 0
                ? subtotal.multiply(DISCOUNT_RATE)
                : BigDecimal.ZERO;
        return subtotal.subtract(discount).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal computeSubtotal() {
        return items.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
