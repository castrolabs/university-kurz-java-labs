package com.kurz.boottesting;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class OrderPlacingIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private InventoryService inventoryService;

    @Test
    @DisplayName("placing an order publishes an event the listener reacts to, updating stock")
    void placingOrderUpdatesStockThroughTheEventListener() {
        orderService.placeOrder(new Order("widget", 10));

        assertEquals(90, inventoryService.getStock("widget"));
    }

    @Test
    @DisplayName("multiple orders accumulate their effect on the same context")
    void multipleOrdersAccumulateStockChanges() {
        orderService.placeOrder(new Order("gadget", 5));
        assertEquals(45, inventoryService.getStock("gadget"));

        orderService.placeOrder(new Order("gadget", 5));
        assertEquals(40, inventoryService.getStock("gadget"));
    }

    @Test
    @DisplayName("bonus: an unknown product propagates the listener's exception")
    void unknownProductPropagatesException() {
        assertThrows(IllegalArgumentException.class, () -> orderService.placeOrder(new Order("unknown", 1)));
    }
}
