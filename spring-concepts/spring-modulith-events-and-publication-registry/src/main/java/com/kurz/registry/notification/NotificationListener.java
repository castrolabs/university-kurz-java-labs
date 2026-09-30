package com.kurz.registry.notification;

import com.kurz.registry.publishing.ContentPublished;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
class NotificationListener {

    private final MailGateway mail;

    NotificationListener(MailGateway mail) {
        this.mail = mail;
    }

    // TODO-01: With @EventListener this runs synchronously inside the
    // publishing transaction: when the mail gateway is down, publishing itself
    // fails and the content is lost. Make it the default Spring Modulith
    // integration listener instead: asynchronous, after commit, in its own
    // transaction, and tracked by the Event Publication Registry.
    @EventListener
    void on(ContentPublished event) {
        mail.send("New content: " + event.title());
    }
}
