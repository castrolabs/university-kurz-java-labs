package com.kurz.cors;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Annotated with @CrossOrigin for an origin that is deliberately NOT in SecurityConfig's global
 * CorsConfigurationSource. Once a SecurityFilterChain wires an explicit CorsConfigurationSource
 * bean, Spring Security's CorsFilter answers every preflight from that source directly — it never
 * consults @CrossOrigin at all. See SecurityConfigTest for the request that proves it.
 */
@RestController
public class PartnersController {

    @CrossOrigin(origins = "https://legacy-partner.example.com")
    @GetMapping("/partners")
    public String partners() {
        return "[]";
    }
}
