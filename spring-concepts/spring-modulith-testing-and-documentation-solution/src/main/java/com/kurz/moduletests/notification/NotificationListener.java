package com.kurz.moduletests.notification;

import java.util.List;

import com.kurz.moduletests.publishing.ContentPublished;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
class NotificationListener {

    private static final List<String> SUBSCRIBERS = List.of("ada@example.com", "linus@example.com");

    private final ApplicationEventPublisher events;

    NotificationListener(ApplicationEventPublisher events) {
        this.events = events;
    }

    @ApplicationModuleListener
    void on(ContentPublished event) {
        // ... send one e-mail per subscriber ...
        events.publishEvent(new NotificationSent(event.title(), SUBSCRIBERS.size()));
    }
}
