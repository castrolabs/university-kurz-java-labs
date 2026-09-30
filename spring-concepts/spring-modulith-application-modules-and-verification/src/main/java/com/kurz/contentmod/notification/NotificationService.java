package com.kurz.contentmod.notification;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.kurz.contentmod.publishing.Content;
import org.springframework.stereotype.Service;

// TODO-03: Once the cycle is gone, make the module's intent explicit: add a
// package-info.java to this package annotated with
// @org.springframework.modulith.ApplicationModule(allowedDependencies = "publishing")
// so that any NEW dependency of notification on another module fails verify().
@Service
public class NotificationService {

    private final SubscriberRepository subscribers;
    private final List<String> sent = new CopyOnWriteArrayList<>();

    NotificationService(SubscriberRepository subscribers) {
        this.subscribers = subscribers;
    }

    public void notifySubscribers(Content content) {
        subscribers.findAll().forEach(email ->
                sent.add(email + " <- " + content.title()));
    }

    public List<String> sent() {
        return List.copyOf(sent);
    }

    public void clear() {
        sent.clear();
    }
}
