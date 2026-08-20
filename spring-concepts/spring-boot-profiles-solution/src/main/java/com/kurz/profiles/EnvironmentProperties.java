package com.kurz.profiles;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.environment")
public record EnvironmentProperties(String label, boolean verboseLogging) {
}
