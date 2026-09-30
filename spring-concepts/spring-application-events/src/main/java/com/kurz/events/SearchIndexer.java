package com.kurz.events;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;

/** Must only index content that really made it into the database. */
@Component
public class SearchIndexer {

    private final List<String> indexed = new CopyOnWriteArrayList<>();

    // TODO-02: Index a title only once its transaction has COMMITTED. A plain
    // @EventListener would index titles from batches that later roll back.
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
