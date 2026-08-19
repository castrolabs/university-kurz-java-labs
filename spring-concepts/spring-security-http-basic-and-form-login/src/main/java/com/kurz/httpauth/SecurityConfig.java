package com.kurz.httpauth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Requires authentication for every request, and accepts it via either HTTP Basic or form login —
 * a single user, "alice"/"password", is registered for both.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // TODO-00: Return a BCryptPasswordEncoder.

        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        // TODO-01: Return an InMemoryUserDetailsManager with a single user: username "alice",
        // authority "ROLE_USER", and password "password" encoded with passwordEncoder. Use
        // User.withUsername("alice").password(passwordEncoder.encode("password")).authorities("ROLE_USER").build().

        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // TODO-02: Require authentication for every request: authorizeHttpRequests(authorize ->
        // authorize.anyRequest().authenticated()).
        // TODO-03: Enable BOTH .formLogin(Customizer.withDefaults()) and
        // .httpBasic(Customizer.withDefaults()) on the same chain — configuring only one silences
        // the other: with only formLogin(), a valid Authorization: Basic header on an unauthenticated
        // request is ignored and the request is redirected to the login page instead of being
        // accepted. With both configured, Spring Security picks the challenge per request: a plain
        // API-style request (no Accept: text/html) gets a 401 WWW-Authenticate: Basic challenge,
        // while a browser-style request (Accept: text/html) gets redirected to /login.
        // TODO-04: Disable CSRF protection: .csrf(AbstractHttpConfigurer::disable) — this lab tests
        // a stateless-style API client, not a browser form carrying a CSRF token.
        // TODO-05: Return http.build().

        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
