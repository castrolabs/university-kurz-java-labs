package com.kurz.boottesting;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds the in-memory stock levels. A separate bean from {@link OrderService} on
 * purpose: the two only collaborate through the event published in
 * {@link OrderService#placeOrder(Order)}, which is what makes a full-context test
 * meaningful for this pair - a slice loading only one of them couldn't exercise the
 * collaboration at all.
 */
@Service
public class InventoryService {

    private final Map<String, Integer> stock = new ConcurrentHashMap<>(Map.of(
            "widget", 100,
            "gadget", 50
    ));

    public int getStock(String product) {
        return stock.getOrDefault(product, 0);
    }

    public void reserve(String product, int quantity) {
        stock.compute(product, (name, current) -> {
            if (current == null) {
                throw new IllegalArgumentException("unknown product: " + name);
            }
            if (quantity > current) {
                throw new IllegalStateException("insufficient stock for " + name);
            }
            return current - quantity;
        });
    }
}
