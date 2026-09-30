package com.kurz.events;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContentService {

    private final JdbcClient jdbc;
    private final ApplicationEventPublisher events;

    public ContentService(JdbcClient jdbc, ApplicationEventPublisher events) {
        this.jdbc = jdbc;
        this.events = events;
    }

    @Transactional
    public long publish(String title) {
        var keys = new GeneratedKeyHolder();
        jdbc.sql("INSERT INTO content (title) VALUES (:title)")
                .param("title", title)
                .update(keys, "id");
        long id = keys.getKey().longValue();
        events.publishEvent(new ContentPublished(id, title));
        return id;
    }

    /** All or nothing: one duplicate title rolls back the whole batch. */
    @Transactional
    public void publishAll(List<String> titles) {
        titles.forEach(this::publish);
    }

    public long count() {
        return jdbc.sql("SELECT COUNT(*) FROM content").query(Long.class).single();
    }

    public void deleteAll() {
        jdbc.sql("DELETE FROM content").update();
    }
}
