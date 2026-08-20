package com.kurz.cors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("SecurityConfig")
class SecurityConfigTest {

    private static final String TRUSTED_ORIGIN = "https://trusted.example.com";
    private static final String UNTRUSTED_ORIGIN = "https://untrusted.example.com";

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        var context = new AnnotationConfigWebApplicationContext();
        context.register(SecurityConfig.class, OrdersController.class, PartnersController.class);
        context.setServletContext(new MockServletContext());
        context.refresh();

        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @Test
    @DisplayName("should answer a preflight OPTIONS request for an allowed origin with matching Allow-* headers")
    void shouldAnswerPreflightForAllowedOriginWithMatchingHeaders() throws Exception {
        mockMvc.perform(options("/orders")
                .header(HttpHeaders.ORIGIN, TRUSTED_ORIGIN)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Content-Type"))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, TRUSTED_ORIGIN))
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, containsString("POST")))
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, containsString("Content-Type")));
    }

    @Test
    @DisplayName("should reject a preflight OPTIONS request for an origin that isn't allowed")
    void shouldRejectPreflightForDisallowedOrigin() throws Exception {
        mockMvc.perform(options("/orders")
                .header(HttpHeaders.ORIGIN, UNTRUSTED_ORIGIN)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
            .andExpect(status().isForbidden())
            .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    @DisplayName("should include Access-Control-Allow-Origin on an actual GET request from the allowed origin")
    void shouldIncludeAllowOriginHeaderOnActualGetRequestFromAllowedOrigin() throws Exception {
        mockMvc.perform(get("/orders").header(HttpHeaders.ORIGIN, TRUSTED_ORIGIN))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, TRUSTED_ORIGIN))
            .andExpect(content().string("[]"));
    }

    @Test
    @DisplayName("should reject an actual GET request carrying an Origin header that isn't allowed — once CORS is actively configured, CorsFilter checks every request that carries an Origin header, not just preflights")
    void shouldRejectActualGetRequestFromDisallowedOrigin() throws Exception {
        mockMvc.perform(get("/orders").header(HttpHeaders.ORIGIN, UNTRUSTED_ORIGIN))
            .andExpect(status().isForbidden())
            .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    @DisplayName("should execute a POST with no Origin header at all, exactly like a curl or server-to-server call would — CorsFilter only inspects requests that carry an Origin header")
    void shouldExecuteEndpointForANonBrowserCallerWithNoOriginHeader() throws Exception {
        mockMvc.perform(post("/orders").contentType("application/json").content("{}"))
            .andExpect(status().isOk())
            .andExpect(content().string("created"));
    }

    @Test
    @DisplayName("should ignore @CrossOrigin on PartnersController — the global CorsConfigurationSource is the only thing consulted")
    void shouldIgnoreCrossOriginAnnotationWhenGlobalCorsConfigurationSourceIsConfigured() throws Exception {
        mockMvc.perform(options("/partners")
                .header(HttpHeaders.ORIGIN, "https://legacy-partner.example.com")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
            .andExpect(status().isForbidden())
            .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }
}
