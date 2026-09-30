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

    // TODO-00: In ONE transaction: INSERT the title into `content` (use a
    // GeneratedKeyHolder for the id), publish ContentPublished(id, title), and
    // only then apply the late business rule below. Because the registry
    // writes its EVENT_PUBLICATION row inside this same transaction, a
    // rejected title must leave neither a content row nor a publication.
    //
    //     if (title.toLowerCase().contains("spam")) {
    //         throw new IllegalArgumentException("Rejected title: " + title);
    //     }
    public long publish(String title) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
