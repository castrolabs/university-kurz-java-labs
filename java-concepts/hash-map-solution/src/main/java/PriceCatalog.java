import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PriceCatalog {

    private final Map<String, Double> prices = new HashMap<>();

    public Optional<Double> setPrice(String product, double price) {
        return Optional.ofNullable(prices.put(product, price));
    }

    public Optional<Double> getPrice(String product) {
        return Optional.ofNullable(prices.get(product));
    }

    public boolean applyDiscount(String product, double percentOff) {
        Double currentPrice = prices.get(product);
        if (currentPrice == null) {
            return false;
        }
        prices.put(product, currentPrice * (1 - percentOff / 100.0));
        return true;
    }

    public Optional<String> mostExpensive() {
        String bestProduct = null;
        double bestPrice = Double.NEGATIVE_INFINITY;
        for (Map.Entry<String, Double> entry : prices.entrySet()) {
            if (entry.getValue() > bestPrice) {
                bestPrice = entry.getValue();
                bestProduct = entry.getKey();
            }
        }
        return Optional.ofNullable(bestProduct);
    }

    public int size() {
        return prices.size();
    }

    public double totalValue() {
        double total = 0;
        for (Map.Entry<String, Double> entry : prices.entrySet()) {
            total += entry.getValue();
        }
        return total;
    }
}
