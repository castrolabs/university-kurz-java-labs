package com.kurz.boottesting;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Reacts to {@link OrderPlacedEvent} by reserving stock. By default, Spring invokes
 * {@code @EventListener} methods synchronously on the publishing thread, so by the
 * time {@code OrderService.placeOrder(...)} returns, this listener has already run.
 */
@Component
public class InventoryListener {

    private final InventoryService inventoryService;

    public InventoryListener(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @EventListener
    public void onOrderPlaced(OrderPlacedEvent event) {
        inventoryService.reserve(event.order().product(), event.order().quantity());
    }
}
