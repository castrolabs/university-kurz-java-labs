package com.kurz.matchers;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        var admin = User.withUsername("admin")
            .password(passwordEncoder.encode("admin123"))
            .authorities("ROLE_ADMIN")
            .build();

        var manager = User.withUsername("manager")
            .password(passwordEncoder.encode("manager123"))
            .authorities("ROLE_MANAGER")
            .build();

        var user = User.withUsername("user")
            .password(passwordEncoder.encode("user123"))
            .authorities("ROLE_USER")
            .build();

        return new InMemoryUserDetailsManager(admin, manager, user);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize
                // Narrower rule first: if "/admin/**" were declared before this, it would match
                // "/admin/health" too and this permitAll() would never be reached.
                .requestMatchers("/admin/health").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // TODO-08 (optional) applied: admins can also reach /reports.
                .requestMatchers("/reports").hasAnyRole("MANAGER", "ADMIN")
                .anyRequest().denyAll())
            .httpBasic(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
