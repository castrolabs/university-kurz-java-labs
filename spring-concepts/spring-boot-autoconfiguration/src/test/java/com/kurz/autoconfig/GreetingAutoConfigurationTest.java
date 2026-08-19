package com.kurz.autoconfig;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class GreetingAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(GreetingAutoConfiguration.class));

    @Test
    @DisplayName("supplies the default GreetingService when the user defines none")
    void suppliesDefaultGreetingServiceWhenNoneDefined() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(GreetingService.class);
            assertThat(context.getBean(GreetingService.class).greet())
                    .isEqualTo("Hello from the default greeting service!");
        });
    }

    @Test
    @DisplayName("backs off when the user defines their own GreetingService bean")
    void backsOffWhenUserBeanIsPresent() {
        contextRunner.withUserConfiguration(CustomGreetingConfig.class).run(context -> {
            assertThat(context).hasSingleBean(GreetingService.class);
            assertThat(context.getBean(GreetingService.class).greet())
                    .isEqualTo("Howdy from the custom greeting service!");
        });
    }

    @Configuration
    static class CustomGreetingConfig {

        @Bean
        GreetingService greetingService() {
            return () -> "Howdy from the custom greeting service!";
        }
    }
}
