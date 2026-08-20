package com.kurz.cors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

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
        // TODO-00: Build a CorsConfiguration allowing exactly:
        //   - origin  "https://trusted.example.com"
        //   - methods "GET", "POST"
        //   - headers "Content-Type", "Authorization"
        // using config.setAllowedOrigins(List.of(...)), config.setAllowedMethods(List.of(...)),
        // config.setAllowedHeaders(List.of(...)).

        // TODO-01: Create a UrlBasedCorsConfigurationSource, register the CorsConfiguration from
        // TODO-00 for path pattern "/**" via source.registerCorsConfiguration("/**", config), and
        // return the source.

        // TODO-04 (optional): Register a second, stricter CorsConfiguration on the same source
        // for path pattern "/admin/**": same origin ("https://trusted.example.com"), but only
        // method "GET" — a read-only surface for that origin, registered before returning the
        // source.

        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // TODO-02: Enable CORS on the chain, pointing it at the bean from TODO-01:
        // http.cors(cors -> cors.configurationSource(corsConfigurationSource())). Also permit
        // every request (authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll()))
        // and disable CSRF protection (csrf(AbstractHttpConfigurer::disable)) — this lab is about
        // CORS, not authentication or CSRF.

        // TODO-03: Return http.build().

        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
