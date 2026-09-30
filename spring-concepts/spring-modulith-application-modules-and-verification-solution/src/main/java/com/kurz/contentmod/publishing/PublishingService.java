package com.kurz.contentmod.publishing;

import com.kurz.contentmod.publishing.internal.ContentRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class PublishingService {

    private final ContentRepository repository;
    private final ApplicationEventPublisher events;

    public PublishingService(ContentRepository repository, ApplicationEventPublisher events) {
        this.repository = repository;
        this.events = events;
    }

    public Content publish(String title) {
        var content = repository.save(title);
        events.publishEvent(new ContentPublished(content));
        return content;
    }
}
