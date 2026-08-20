package com.kurz.customconfigprops;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "mail")
@Validated
public record MailProperties(

        @NotBlank(message = "mail.host must not be blank")
        String host,

        @Min(value = 1, message = "mail.port must be between 1 and 65535")
        @Max(value = 65535, message = "mail.port must be between 1 and 65535")
        int port,

        @PositiveOrZero(message = "mail.retry-count must not be negative")
        int retryCount,

        boolean enabled) {

    public static MailProperties disabled(String host, int port) {
        return new MailProperties(host, port, 0, false);
    }
}
