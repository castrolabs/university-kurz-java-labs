package com.kurz.profiles;

// TODO-01: Annotate this record with @ConfigurationProperties(prefix = "app.environment")
// so Spring Boot binds "app.environment.label" and "app.environment.verbose-logging" from
// whichever application*.properties file(s) are active onto the components below.
public record EnvironmentProperties(String label, boolean verboseLogging) {
}
