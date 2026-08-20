package com.kurz.boottesting;

/**
 * A plain Spring application event - it doesn't need to extend
 * {@code ApplicationEvent}; any object can be published via
 * {@code ApplicationEventPublisher.publishEvent(Object)} since Spring 4.2.
 */
public record OrderPlacedEvent(Order order) {
}
