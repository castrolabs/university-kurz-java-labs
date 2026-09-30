package com.kurz.registry.publishing;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PublishingService {

    private final JdbcClient jdbc;
    private final ApplicationEventPublisher events;

    PublishingService(JdbcClient jdbc, ApplicationEventPublisher events) {
        this.jdbc = jdbc;
        this.events = events;
    }

    @Transactional
    public long publish(String title) {
        var keys = new GeneratedKeyHolder();
        jdbc.sql("INSERT INTO content (title) VALUES (:title)").param("title", title).update(keys, "id");
        long id = keys.getKey().longValue();

        events.publishEvent(new ContentPublished(id, title));

        // A late business rule: fails AFTER the event was published.
        if (title.toLowerCase().contains("spam")) {
            throw new IllegalArgumentException("Rejected title: " + title);
        }
        return id;
    }
}
