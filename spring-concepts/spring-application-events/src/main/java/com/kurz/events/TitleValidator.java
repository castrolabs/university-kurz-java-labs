package com.kurz.events;

import org.springframework.stereotype.Component;

/**
 * Runs synchronously, on the publisher's thread, INSIDE its transaction.
 * Throwing here propagates out of publishEvent() and rolls the insert back.
 */
@Component
public class TitleValidator {

    // TODO-01: This listener must be able to VETO a publication: when it
    // throws, the INSERT has to roll back. Which annotation runs it on the
    // publisher's thread, inside the still-open transaction?
    public void on(ContentPublished event) {
        if (event.title().toLowerCase().contains("spam")) {
            throw new IllegalArgumentException("Rejected title: " + event.title());
        }
    }
}
