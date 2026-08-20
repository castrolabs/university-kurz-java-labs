package com.kurz.profiles;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProfileActivationTest {

    private ConfigurableApplicationContext startWithProfiles(String... profiles) {
        return new SpringApplicationBuilder(AppConfig.class)
                .web(WebApplicationType.NONE)
                .bannerMode(Banner.Mode.OFF)
                .profiles(profiles)
                .run();
    }

    @Test
    @DisplayName("dev profile creates the DataSeeder bean")
    void devProfileCreatesDataSeederBean() {
        try (ConfigurableApplicationContext context = startWithProfiles("dev")) {
            DataSeeder seeder = context.getBean(DataSeeder.class);
            assertThat(seeder.seedsData()).isTrue();
        }
    }

    @Test
    @DisplayName("prod profile does not create a DataSeeder bean")
    void prodProfileDoesNotCreateDataSeederBean() {
        try (ConfigurableApplicationContext context = startWithProfiles("prod")) {
            assertThrows(NoSuchBeanDefinitionException.class, () -> context.getBean(DataSeeder.class));
        }
    }

    @Test
    @DisplayName("with no active profile, the DataSeeder bean still exists — \"!prod\" excludes only prod")
    void noActiveProfileStillCreatesDataSeederBean() {
        try (ConfigurableApplicationContext context = startWithProfiles()) {
            DataSeeder seeder = context.getBean(DataSeeder.class);
            assertThat(seeder.seedsData()).isTrue();
        }
    }

    @Test
    @DisplayName("dev profile overrides both environment properties")
    void devProfileOverridesEnvironmentProperties() {
        try (ConfigurableApplicationContext context = startWithProfiles("dev")) {
            EnvironmentProperties props = context.getBean(EnvironmentProperties.class);
            assertThat(props.label()).isEqualTo("Development");
            assertThat(props.verboseLogging()).isTrue();
        }
    }

    @Test
    @DisplayName("prod profile overrides only the label, leaving verboseLogging at its default")
    void prodProfileOverridesLabelOnly() {
        try (ConfigurableApplicationContext context = startWithProfiles("prod")) {
            EnvironmentProperties props = context.getBean(EnvironmentProperties.class);
            assertThat(props.label()).isEqualTo("Production");
            assertThat(props.verboseLogging()).isFalse();
        }
    }

    @Test
    @DisplayName("with no active profile, application.properties defaults are used as-is")
    void defaultsApplyWhenNoProfileIsActive() {
        try (ConfigurableApplicationContext context = startWithProfiles()) {
            EnvironmentProperties props = context.getBean(EnvironmentProperties.class);
            assertThat(props.label()).isEqualTo("Local");
            assertThat(props.verboseLogging()).isFalse();
        }
    }
}
