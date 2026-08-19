package com.kurz.autoconfig;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class GreetingAutoConfiguration {

    // TODO-01: Add @ConditionalOnMissingBean here so this default bean backs off
    // whenever the application already defines its own GreetingService bean.
    @Bean
    GreetingService greetingService() {
        return new DefaultGreetingService();
    }
}
