package com.kurz.matchers;

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
 * Authorization rules for a small admin API. requestMatchers() rules are evaluated in the order
 * they are declared and the first match wins — a broader rule declared before a narrower one
 * silently shadows it. See MatcherShadowingDemonstrationTests in the test suite for a worked
 * example of the bug this class's rule ordering has to avoid.
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
        // TODO-01: Register three in-memory users, each built with User.withUsername(...)
        // .password(passwordEncoder.encode(...)).authorities(...).build():
        //   - "admin"   / "admin123"   with authority "ROLE_ADMIN"
        //   - "manager" / "manager123" with authority "ROLE_MANAGER"
        //   - "user"    / "user123"    with authority "ROLE_USER"
        // Return an InMemoryUserDetailsManager holding all three.

        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // TODO-02: Inside authorizeHttpRequests(...), declare the rule for "/admin/health" FIRST,
        // permitAll(). It must come before the "/admin/**" rule from TODO-03 — requestMatchers()
        // rules are evaluated in declaration order and the first match wins, so a broader
        // "/admin/**" rule declared first would shadow this narrower one and "/admin/health"
        // would silently start requiring ROLE_ADMIN too.

        // TODO-03: Declare "/admin/**" requiring hasRole("ADMIN"), immediately AFTER TODO-02.

        // TODO-04: Declare "/reports" requiring hasRole("MANAGER").

        // TODO-05: Declare anyRequest() as denyAll() — an explicit, reviewable catch-all for
        // every path not matched by the rules above, rather than leaving it to an implicit
        // default.

        // TODO-06: Enable httpBasic(Customizer.withDefaults()) and disable CSRF protection with
        // csrf(AbstractHttpConfigurer::disable).

        // TODO-07: Return http.build().

        // TODO-08 (optional): Once everything above passes, change the "/reports" rule from
        // TODO-04 to hasAnyRole("MANAGER", "ADMIN") so admins can also reach it — without
        // touching its position relative to the other rules.

        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
