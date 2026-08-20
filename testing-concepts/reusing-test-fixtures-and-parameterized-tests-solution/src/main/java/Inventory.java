import java.util.HashMap;
import java.util.Map;

/**
 * Tracks stock levels per SKU. A collaborator of {@link Order} — kept as a
 * plain mutable class so tests can observe exactly how much stock is left
 * after an order is placed, instead of only trusting a boolean result.
 */
public class Inventory {

    private final Map<String, Integer> stock = new HashMap<>();

    public void addStock(String sku, int quantity) {
        stock.merge(sku, quantity, Integer::sum);
    }

    public int quantityOf(String sku) {
        return stock.getOrDefault(sku, 0);
    }

    /**
     * Removes {@code quantity} units of {@code sku} if enough are available.
     * Returns false and leaves stock untouched otherwise — never partially
     * decrements.
     */
    public boolean remove(String sku, int quantity) {
        if (quantity <= 0) {
            return false;
        }
        int available = quantityOf(sku);
        if (quantity > available) {
            return false;
        }
        stock.put(sku, available - quantity);
        return true;
    }
}
