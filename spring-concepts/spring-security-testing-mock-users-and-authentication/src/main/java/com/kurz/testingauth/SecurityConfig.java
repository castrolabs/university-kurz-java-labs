package com.kurz.testingauth;

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
 * A tiny inventory API with two users of deliberately different account status: "john" is a
 * normal, enabled admin; "mary" is DISABLED. Neither fact matters to @WithMockUser or
 * @WithUserDetails, which is exactly the point — see SecurityConfigTest for the tests that make
 * that gap visible.
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
        // TODO-01: Register two in-memory users, each built with User.withUsername(...)
        // .password(passwordEncoder.encode(...)).authorities(...).build():
        //   - "john" / "john123" with authority "ROLE_ADMIN" — a normal, enabled account.
        //   - "mary" / "mary123" with authority "ROLE_USER", AND .disabled(true) — deliberately
        //     disabled, to demonstrate later that the mock-user testing annotations skip the
        //     account-status checks a real login performs.
        // Return an InMemoryUserDetailsManager holding both.

        // TODO-06 (optional): Add a third user, "legacyuser" / "legacyuser123", ROLE_USER, with
        // .credentialsExpired(true) instead of .disabled(true). Real HTTP Basic authentication
        // rejects it with a different failure (expired credentials, not a disabled account) than
        // "mary" — but @WithMockUser/@WithUserDetails still skip the check entirely, same as
        // before.

        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // TODO-02: Inside authorizeHttpRequests(...), require hasRole("ADMIN") for
        // "/inventory/admin", and authenticated() for anyRequest() (covering "/inventory/view"
        // and everything else).

        // TODO-03: Enable httpBasic(Customizer.withDefaults()) and disable CSRF protection with
        // csrf(AbstractHttpConfigurer::disable).

        // TODO-04: Return http.build().

        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
