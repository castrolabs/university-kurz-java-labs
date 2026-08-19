package com.kurz.configprops;

import java.util.List;

// TODO-00: Add @ConfigurationProperties(prefix = "app") here so Spring Boot binds
// properties like "app.max-retries" onto the maxRetries component below via
// relaxed binding.
public record AppProperties(
        String name,
        int maxRetries,
        List<String> allowedOrigins,
        Notification notification) {

    public AppProperties {
        // TODO-02: Default allowedOrigins to List.of() and notification to
        // new Notification("email", true) whenever they weren't supplied,
        // so callers never have to null-check them.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public record Notification(String channel, boolean enabled) {
    }

    // TODO-03 (optional): Add a static factory disabled(String name, int maxRetries)
    // that returns an AppProperties with notifications turned off — useful for tests
    // that don't care about the notification channel.
}
