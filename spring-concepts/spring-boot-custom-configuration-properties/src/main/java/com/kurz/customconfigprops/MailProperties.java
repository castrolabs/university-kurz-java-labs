package com.kurz.customconfigprops;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

// TODO-00: Annotate this record with @ConfigurationProperties(prefix = "mail") so Spring
// Boot binds properties like "mail.host" and "mail.retry-count" from the environment onto
// the components below. Relaxed binding is what maps the kebab-case "retry-count" key onto
// the camelCase retryCount component.
// TODO-01: Annotate this record with @Validated (org.springframework.validation.annotation)
// so the Jakarta Bean Validation constraints on the components below are actually enforced
// during binding, instead of silently being ignored.
public record MailProperties(

        @NotBlank(message = "mail.host must not be blank")
        String host,

        @Min(value = 1, message = "mail.port must be between 1 and 65535")
        @Max(value = 65535, message = "mail.port must be between 1 and 65535")
        int port,

        // TODO-02: Add a @jakarta.validation.constraints.PositiveOrZero constraint (with a
        // message) here so a negative retry count fails validation at startup instead of
        // being silently accepted.
        int retryCount,

        boolean enabled) {

    // TODO-04 (optional): Add a static factory disabled(String host, int port) that returns
    // a MailProperties with retryCount 0 and enabled false — a convenient instance for tests
    // and environments that must never actually send mail.
}
