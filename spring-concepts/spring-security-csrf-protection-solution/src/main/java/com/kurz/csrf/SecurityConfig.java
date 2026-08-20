package com.kurz.csrf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

/**
 * CSRF protection is left at its default (enabled) for every path except the machine-to-machine
 * webhook — GET/HEAD/TRACE/OPTIONS pass CsrfFilter unconditionally regardless, but every other
 * method on every other path needs a valid token.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/webhooks/**")
                // TODO-04 (optional) applied: also deliver the token via a JavaScript-readable
                // "XSRF-TOKEN" cookie, the SPA pattern.
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()));

        return http.build();
    }
}
