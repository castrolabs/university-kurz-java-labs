package com.kurz.customconfigprops;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class MailPropertiesBindingTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(MailConfig.class);

    @Test
    @DisplayName("binds a fully valid property set onto MailProperties")
    void bindsValidProperties() {
        contextRunner
                .withPropertyValues(
                        "mail.host=smtp.kurz.fyi",
                        "mail.port=587",
                        "mail.retry-count=3",
                        "mail.enabled=true")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    MailProperties props = context.getBean(MailProperties.class);
                    assertThat(props.host()).isEqualTo("smtp.kurz.fyi");
                    assertThat(props.port()).isEqualTo(587);
                    assertThat(props.retryCount()).isEqualTo(3);
                    assertThat(props.enabled()).isTrue();
                });
    }

    @Test
    @DisplayName("relaxed binding maps the kebab-case mail.retry-count key onto retryCount")
    void relaxedBindingMapsRetryCountKey() {
        contextRunner
                .withPropertyValues(
                        "mail.host=smtp.kurz.fyi",
                        "mail.port=25",
                        "mail.retry-count=7",
                        "mail.enabled=false")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    MailProperties props = context.getBean(MailProperties.class);
                    assertThat(props.retryCount()).isEqualTo(7);
                });
    }

    @Test
    @DisplayName("context startup fails when the required host is blank")
    void failsStartupWhenHostIsBlank() {
        contextRunner
                .withPropertyValues(
                        "mail.host=",
                        "mail.port=25",
                        "mail.retry-count=0",
                        "mail.enabled=true")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasStackTraceContaining("mail.host");
                });
    }

    @Test
    @DisplayName("context startup fails when the port is out of range")
    void failsStartupWhenPortIsOutOfRange() {
        contextRunner
                .withPropertyValues(
                        "mail.host=smtp.kurz.fyi",
                        "mail.port=70000",
                        "mail.retry-count=0",
                        "mail.enabled=true")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasStackTraceContaining("mail.port");
                });
    }

    @Test
    @DisplayName("context startup fails when the retry count is negative")
    void failsStartupWhenRetryCountIsNegative() {
        contextRunner
                .withPropertyValues(
                        "mail.host=smtp.kurz.fyi",
                        "mail.port=25",
                        "mail.retry-count=-1",
                        "mail.enabled=true")
                .run(context -> assertThat(context).hasFailed());
    }
}
