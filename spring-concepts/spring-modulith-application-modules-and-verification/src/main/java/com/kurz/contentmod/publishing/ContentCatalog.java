package com.kurz.contentmod.publishing;

import java.util.List;

import org.springframework.stereotype.Service;

/** The read side of the publishing module that other modules are allowed to use. */
@Service
public class ContentCatalog {

    private final ContentRepository repository;

    public ContentCatalog(ContentRepository repository) {
        this.repository = repository;
    }

    // TODO-01: Return the titles of the `limit` most recently published
    // contents, newest first. Then change notification.DigestService to use
    // this method instead of reaching into ContentRepository directly.
    public List<String> recentTitles(int limit) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
