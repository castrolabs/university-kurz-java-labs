package com.kurz.boottesting;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * OrderService, InventoryService, and InventoryListener are already fully
 * implemented in src/main/java - your job is to write full-context integration
 * tests that prove the three beans collaborate correctly through a published
 * Spring event. This needs a real, full application context: OrderService only
 * knows about ApplicationEventPublisher, never about InventoryService directly, so a
 * test that mocked InventoryService away would prove nothing about whether the
 * wiring actually works end to end.
 */
@SpringBootTest
class OrderPlacingIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private InventoryService inventoryService;

    // TODO-00: Call orderService.placeOrder(new Order("widget", 10)), then assert
    // inventoryService.getStock("widget") is now 90 (started at 100). This only works
    // because the full context wires OrderService -> ApplicationEventPublisher ->
    // InventoryListener -> InventoryService as the SAME bean instances autowired
    // above - a @WebMvcTest slice would never load InventoryListener at all.
    @Test
    @DisplayName("placing an order publishes an event the listener reacts to, updating stock")
    void placingOrderUpdatesStockThroughTheEventListener() {
        fail("TODO-00: not implemented yet");
    }

    // TODO-01: Place two orders for "gadget" (quantity 5, then quantity 5 again) and
    // assert the stock reflects both reservations cumulatively: 50 -> 45 -> 40. This
    // proves state changes made by one bean (InventoryListener, reacting to the first
    // event) are visible to another (InventoryService, queried after the second call)
    // because every bean in this test shares the SAME cached application context.
    @Test
    @DisplayName("multiple orders accumulate their effect on the same context")
    void multipleOrdersAccumulateStockChanges() {
        fail("TODO-01: not implemented yet");
    }

    // TODO-02 (optional): Assert that orderService.placeOrder(new Order("unknown", 1))
    // throws IllegalArgumentException. InventoryService.reserve(...) throws for an
    // unknown product; because @EventListener methods run synchronously by default,
    // that exception propagates all the way back through publishEvent(...) to this
    // test's call to placeOrder(...).
}
