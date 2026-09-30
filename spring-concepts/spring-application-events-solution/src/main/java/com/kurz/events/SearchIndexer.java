package com.kurz.events;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/** Must only index content that really made it into the database. */
@Component
public class SearchIndexer {

    private final List<String> indexed = new CopyOnWriteArrayList<>();

    @TransactionalEventListener // phase = AFTER_COMMIT is the default
    public void on(ContentPublished event) {
        indexed.add(event.title());
    }

    public List<String> indexed() {
        return List.copyOf(indexed);
    }

    public void clear() {
        indexed.clear();
    }
}
