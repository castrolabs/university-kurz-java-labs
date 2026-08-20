package com.kurz.profiles;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// TODO-02: Annotate this class with @EnableConfigurationProperties(EnvironmentProperties.class)
// so Spring registers EnvironmentProperties as a bean bound from the environment.
@Configuration
public class AppConfig {

    // TODO-00: Annotate this bean method with @Profile("!prod") so a DataSeeder is created
    // for every profile except "prod" — including when no profile at all is active. Read the
    // article's trade-offs section on @Profile negation before assuming what "!prod" means
    // when nothing is active.
    @Bean
    public DataSeeder dataSeeder() {
        return new DevDataSeeder();
    }
}
