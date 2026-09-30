package com.kurz.contentmod.publishing;

import com.kurz.contentmod.notification.NotificationService;
import org.springframework.stereotype.Service;

@Service
public class PublishingService {

    private final ContentRepository repository;
    private final NotificationService notifications;

    public PublishingService(ContentRepository repository, NotificationService notifications) {
        this.repository = repository;
        this.notifications = notifications;
    }

    // TODO-02: publishing -> notification (this call) and notification ->
    // publishing (NotificationService takes a Content) form a cycle. Break it:
    // publish a ContentPublished event with an ApplicationEventPublisher
    // instead of calling NotificationService, and turn
    // NotificationService.notifySubscribers into an @EventListener for
    // ContentPublished.
    public Content publish(String title) {
        var content = repository.save(title);
        notifications.notifySubscribers(content);
        return content;
    }
}
