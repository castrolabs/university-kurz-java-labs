package com.kurz.contentmod.publishing.internal;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import com.kurz.contentmod.publishing.Content;
import org.springframework.stereotype.Repository;

/**
 * Public so PublishingService (base package) can use it, but it lives in a
 * subpackage, so Spring Modulith treats it as internal to the module.
 */
@Repository
public class ContentRepository {

    private final AtomicLong ids = new AtomicLong();
    private final List<Content> contents = new CopyOnWriteArrayList<>();

    public Content save(String title) {
        var content = new Content(ids.incrementAndGet(), title);
        contents.add(content);
        return content;
    }

    public List<Content> findAll() {
        return List.copyOf(contents);
    }

    public void deleteAll() {
        contents.clear();
    }
}
