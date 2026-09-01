import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PriceCatalog {

    private final Map<String, Double> prices = new HashMap<>();

    public Optional<Double> setPrice(String product, double price) {
        // TODO-00: Store `price` for `product` (put()). Return the PREVIOUS price
        // wrapped in an Optional, or Optional.empty() if `product` was new.
        // Hint: HashMap.put() already returns the old value (or null) for you.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public Optional<Double> getPrice(String product) {
        // TODO-01: Return the price for `product`, or Optional.empty() if it's
        // not in the catalog. get() on a missing key returns null, not an
        // exception, so check for that before wrapping it.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public boolean applyDiscount(String product, double percentOff) {
        // TODO-02: Read the current price for `product`, compute the discounted
        // price (price * (1 - percentOff / 100.0)), and put() it back — same
        // read-then-put pattern a balance deposit would use.
        // Return false and change nothing if `product` isn't in the catalog.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public Optional<String> mostExpensive() {
        // TODO-03: Iterate prices.entrySet(), comparing Map.Entry.getValue() to
        // find the product (the key) with the highest price. Optional.empty()
        // if the catalog is empty. This needs both the key and the value at once,
        // which is exactly what entrySet() gives you.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public int size() {
        return prices.size();
    }

    public double totalValue() {
        // TODO-04 (optional): Sum every price currently in the catalog by
        // iterating prices.entrySet().
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
