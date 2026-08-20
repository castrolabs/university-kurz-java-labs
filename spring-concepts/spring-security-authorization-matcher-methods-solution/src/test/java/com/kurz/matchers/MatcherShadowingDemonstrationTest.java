package com.kurz.matchers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockServletContext;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Entirely independent of SecurityConfig — it builds its own, deliberately misordered
 * SecurityFilterChain to demonstrate why the narrower "/admin/health" rule must be declared
 * before the broader "/admin/**" rule in SecurityConfig's own TODO-02/TODO-03. This test always
 * runs and always passes, regardless of how far along SecurityConfig's TODOs are.
 */
@DisplayName("Matcher shadowing demonstration (independent of SecurityConfig)")
class MatcherShadowingDemonstrationTest {

    private MockMvc brokenMockMvc;

    @BeforeEach
    void setUp() {
        var context = new AnnotationConfigWebApplicationContext();
        context.register(BrokenOrderConfig.class, AdminController.class);
        context.setServletContext(new MockServletContext());
        context.refresh();

        brokenMockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @Test
    @DisplayName("a broad '/admin/**' rule declared before the narrower '/admin/health' rule shadows it, blocking a request that should be public")
    void shouldShowThatBroadMatcherDeclaredFirstShadowsNarrowerRule() throws Exception {
        // With the correct ordering (SecurityConfig, once TODO-02/TODO-03 are implemented), the
        // equivalent request to /admin/health returns 200 with no credentials at all. Here,
        // because "/admin/**" was declared first, it claims the request before the
        // "/admin/health" rule below it is ever consulted.
        brokenMockMvc.perform(get("/admin/health"))
            .andExpect(status().isUnauthorized());
    }

    @Configuration
    @EnableWebSecurity
    static class BrokenOrderConfig {

        @Bean
        PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }

        @Bean
        UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
            var admin = User.withUsername("admin")
                .password(passwordEncoder.encode("admin123"))
                .authorities("ROLE_ADMIN")
                .build();

            return new InMemoryUserDetailsManager(admin);
        }

        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http.authorizeHttpRequests(authorize -> authorize
                    // Bug: the broad rule is declared FIRST, so it matches every request under
                    // /admin/**, including /admin/health — the narrower rule below can never be
                    // reached.
                    .requestMatchers("/admin/**").hasRole("ADMIN")
                    .requestMatchers("/admin/health").permitAll()
                    .anyRequest().denyAll())
                .httpBasic(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable);

            return http.build();
        }
    }
}
