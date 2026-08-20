import java.time.LocalDate;

/**
 * Places orders against an {@link Inventory}, and exposes two pure business
 * rules — the shipping-fee tiers and the return-eligibility window — that
 * don't need an Inventory at all.
 */
public class Order {

    private final Inventory inventory;

    public Order(Inventory inventory) {
        this.inventory = inventory;
    }

    public boolean place(String sku, int quantity) {
        return inventory.remove(sku, quantity);
    }

    /**
     * Free shipping from $50.00, a flat $4.99 from $20.00 up to (not
     * including) $50.00, and $9.99 below that.
     */
    public static int shippingFeeCents(int orderTotalCents) {
        if (orderTotalCents >= 5000) {
            return 0;
        }
        if (orderTotalCents >= 2000) {
            return 499;
        }
        return 999;
    }

    /**
     * A purchase can be returned up to 30 days (inclusive) after it was made.
     */
    public static boolean isWithinReturnWindow(LocalDate purchaseDate, LocalDate today) {
        return !today.isAfter(purchaseDate.plusDays(30));
    }
}
