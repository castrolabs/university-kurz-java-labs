package com.kurz.contentmod.publishing;

import java.util.List;

import com.kurz.contentmod.publishing.internal.ContentRepository;
import org.springframework.stereotype.Service;

/** The read side of the publishing module that other modules are allowed to use. */
@Service
public class ContentCatalog {

    private final ContentRepository repository;

    public ContentCatalog(ContentRepository repository) {
        this.repository = repository;
    }

    public List<String> recentTitles(int limit) {
        var all = repository.findAll();
        return all.subList(Math.max(0, all.size() - limit), all.size()).reversed().stream()
                .map(Content::title)
                .toList();
    }
}
