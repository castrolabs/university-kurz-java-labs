package com.kurz.configprops;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class AppPropertiesBindingTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(AppConfig.class);

    @Test
    @DisplayName("relaxed binding maps kebab-case and dotted property names to record components")
    void bindsRelaxedPropertyNames() {
        contextRunner
                .withPropertyValues(
                        "app.name=Kurz University",
                        "app.max-retries=5",
                        "app.allowed-origins=https://kurz.fyi,https://kurz.dev",
                        "app.notification.channel=sms",
                        "app.notification.enabled=true")
                .run(context -> {
                    AppProperties props = context.getBean(AppProperties.class);
                    assertThat(props.name()).isEqualTo("Kurz University");
                    assertThat(props.maxRetries()).isEqualTo(5);
                    assertThat(props.allowedOrigins())
                            .containsExactly("https://kurz.fyi", "https://kurz.dev");
                    assertThat(props.notification().channel()).isEqualTo("sms");
                    assertThat(props.notification().enabled()).isTrue();
                });
    }

    @Test
    @DisplayName("missing optional properties fall back to record defaults")
    void appliesDefaultsWhenPropertiesAreMissing() {
        contextRunner
                .withPropertyValues("app.name=Kurz University", "app.max-retries=3")
                .run(context -> {
                    AppProperties props = context.getBean(AppProperties.class);
                    assertThat(props.allowedOrigins()).isEmpty();
                    assertThat(props.notification().channel()).isEqualTo("email");
                    assertThat(props.notification().enabled()).isTrue();
                });
    }
}
