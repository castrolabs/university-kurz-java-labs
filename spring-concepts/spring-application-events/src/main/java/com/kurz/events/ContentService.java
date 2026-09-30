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

    // TODO-00: Make this method transactional, INSERT the title into the
    // content table with jdbc.sql(...).param(...).update(keys, "id") using a
    // GeneratedKeyHolder, then publish a ContentPublished(id, title) through
    // the ApplicationEventPublisher and return the id. The event must be
    // published INSIDE the transaction: that is what lets the listeners below
    // pick a transaction phase.
    public long publish(String title) {
        throw new UnsupportedOperationException("Not implemented yet.");
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
