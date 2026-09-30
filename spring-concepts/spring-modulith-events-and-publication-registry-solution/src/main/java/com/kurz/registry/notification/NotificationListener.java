package com.kurz.registry.notification;

import com.kurz.registry.publishing.ContentPublished;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
class NotificationListener {

    private final MailGateway mail;

    NotificationListener(MailGateway mail) {
        this.mail = mail;
    }

    /** Async, after the publishing transaction commits, in a transaction of its own. */
    @ApplicationModuleListener
    void on(ContentPublished event) {
        mail.send("New content: " + event.title());
    }
}
