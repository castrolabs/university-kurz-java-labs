package com.kurz.events;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Runs synchronously, on the publisher's thread, INSIDE its transaction.
 * Throwing here propagates out of publishEvent() and rolls the insert back.
 */
@Component
public class TitleValidator {

    @EventListener
    public void on(ContentPublished event) {
        if (event.title().toLowerCase().contains("spam")) {
            throw new IllegalArgumentException("Rejected title: " + event.title());
        }
    }
}
