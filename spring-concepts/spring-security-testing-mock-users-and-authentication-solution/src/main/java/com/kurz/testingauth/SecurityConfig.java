package com.kurz.testingauth;

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
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        var john = User.withUsername("john")
            .password(passwordEncoder.encode("john123"))
            .authorities("ROLE_ADMIN")
            .build();

        var mary = User.withUsername("mary")
            .password(passwordEncoder.encode("mary123"))
            .authorities("ROLE_USER")
            .disabled(true)
            .build();

        // TODO-06 (optional) applied: a third user rejected by real authentication for a
        // different reason (expired credentials, not a disabled account) — the mock-user
        // annotations still skip the check regardless of which reason applies.
        var legacyUser = User.withUsername("legacyuser")
            .password(passwordEncoder.encode("legacyuser123"))
            .authorities("ROLE_USER")
            .credentialsExpired(true)
            .build();

        return new InMemoryUserDetailsManager(john, mary, legacyUser);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/inventory/admin").hasRole("ADMIN")
                .anyRequest().authenticated())
            .httpBasic(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
