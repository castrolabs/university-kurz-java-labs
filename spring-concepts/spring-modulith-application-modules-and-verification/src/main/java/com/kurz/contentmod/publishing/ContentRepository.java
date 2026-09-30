package com.kurz.contentmod.publishing;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

// TODO-00: This repository is an implementation detail of the publishing
// module, yet it sits in the module's base package, which Spring Modulith
// treats as the module's public API. Move it into a subpackage
// (com.kurz.contentmod.publishing.internal). It has to stay public, because
// PublishingService in the base package still uses it.
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
