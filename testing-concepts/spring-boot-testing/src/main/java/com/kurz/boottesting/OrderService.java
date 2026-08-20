package com.kurz.boottesting;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Publishes an {@link OrderPlacedEvent} for every order - it never talks to
 * {@link InventoryService} directly. The stock update only happens because
 * {@link InventoryListener} is also wired into the same application context and
 * reacts to the event. Testing that chain end to end is exactly what a full
 * {@code @SpringBootTest} context is for.
 */
@Service
public class OrderService {

    private final ApplicationEventPublisher eventPublisher;

    public OrderService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void placeOrder(Order order) {
        eventPublisher.publishEvent(new OrderPlacedEvent(order));
    }
}
