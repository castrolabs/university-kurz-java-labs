package com.kurz.cors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * A centralized CORS policy on the SecurityFilterChain. Once a CorsConfigurationSource bean is
 * wired in explicitly, it is the ONLY thing Spring Security's CorsFilter consults — @CrossOrigin
 * annotations elsewhere in the app (see PartnersController) are not read at all. Authorization is
 * left wide open here on purpose: this lab is about CORS, not about who is allowed to call what.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        var config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("https://trusted.example.com"));
        config.setAllowedMethods(List.of("GET", "POST"));
        config.setAllowedHeaders(List.of("Content-Type", "Authorization"));

        var adminConfig = new CorsConfiguration();
        adminConfig.setAllowedOrigins(List.of("https://trusted.example.com"));
        adminConfig.setAllowedMethods(List.of("GET"));

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/admin/**", adminConfig);
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
            .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
