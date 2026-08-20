package com.kurz.profiles;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@EnableConfigurationProperties(EnvironmentProperties.class)
public class AppConfig {

    @Bean
    @Profile("!prod")
    public DataSeeder dataSeeder() {
        return new DevDataSeeder();
    }

    @Bean
    @Profile("audit")
    public AuditLogger auditLogger() {
        return new AuditLogger();
    }
}
