package com.kurz.configprops;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        String name,
        int maxRetries,
        List<String> allowedOrigins,
        Notification notification) {

    public AppProperties {
        if (allowedOrigins == null) {
            allowedOrigins = List.of();
        }
        if (notification == null) {
            notification = new Notification("email", true);
        }
    }

    public record Notification(String channel, boolean enabled) {
    }

    public static AppProperties disabled(String name, int maxRetries) {
        return new AppProperties(name, maxRetries, List.of(), new Notification("email", false));
    }
}
