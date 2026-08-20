package com.kurz.csrf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

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
        // TODO-00: Permit every request: authorizeHttpRequests(authorize ->
        // authorize.anyRequest().permitAll()). This lab is about CSRF, not who is allowed to call
        // what.

        // TODO-01: Exempt only "/webhooks/**" from CSRF protection with csrf(csrf ->
        // csrf.ignoringRequestMatchers("/webhooks/**")) — do NOT call csrf(...disable), CSRF
        // protection must stay enabled (the default) for every other path.

        // TODO-02: Return http.build().

        // TODO-04 (optional): Add .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
        // to the csrf(...) customizer from TODO-01, so the token is also delivered via a
        // JavaScript-readable "XSRF-TOKEN" cookie — the pattern a same-backend single-page app
        // frontend needs, described in the article's "Scenario 2" section.

        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
