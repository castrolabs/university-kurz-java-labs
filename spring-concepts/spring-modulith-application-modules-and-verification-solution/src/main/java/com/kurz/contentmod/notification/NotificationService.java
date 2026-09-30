package com.kurz.contentmod.notification;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.kurz.contentmod.publishing.ContentPublished;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final SubscriberRepository subscribers;
    private final List<String> sent = new CopyOnWriteArrayList<>();

    NotificationService(SubscriberRepository subscribers) {
        this.subscribers = subscribers;
    }

    @EventListener
    void on(ContentPublished event) {
        subscribers.findAll().forEach(email ->
                sent.add(email + " <- " + event.content().title()));
    }

    public List<String> sent() {
        return List.copyOf(sent);
    }

    public void clear() {
        sent.clear();
    }
}
