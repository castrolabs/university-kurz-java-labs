package com.kurz.csrf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("SecurityConfig")
class SecurityConfigTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        var context = new AnnotationConfigWebApplicationContext();
        context.register(SecurityConfig.class, ProductController.class, WebhookController.class,
            CsrfTokenController.class);
        context.setServletContext(new MockServletContext());
        context.refresh();

        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @Test
    @DisplayName("should allow a GET request with no CSRF token — safe methods bypass CsrfFilter unconditionally")
    void shouldAllowGetWithoutCsrfToken() throws Exception {
        mockMvc.perform(get("/products"))
            .andExpect(status().isOk())
            .andExpect(content().string("[]"));
    }

    @Test
    @DisplayName("should reject a POST with no CSRF token with 403")
    void shouldRejectPostWithoutCsrfToken() throws Exception {
        mockMvc.perform(post("/products"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("should accept a POST carrying a valid CSRF token")
    void shouldAcceptPostWithCsrfToken() throws Exception {
        mockMvc.perform(post("/products").with(csrf()))
            .andExpect(status().isOk())
            .andExpect(content().string("created"));
    }

    @Test
    @DisplayName("should reject a PUT with no CSRF token with 403")
    void shouldRejectPutWithoutCsrfToken() throws Exception {
        mockMvc.perform(put("/products/1"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("should accept a PUT carrying a valid CSRF token")
    void shouldAcceptPutWithCsrfToken() throws Exception {
        mockMvc.perform(put("/products/1").with(csrf()))
            .andExpect(status().isOk())
            .andExpect(content().string("updated 1"));
    }

    @Test
    @DisplayName("should reject a DELETE with no CSRF token with 403")
    void shouldRejectDeleteWithoutCsrfToken() throws Exception {
        mockMvc.perform(delete("/products/1"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("should accept a DELETE carrying a valid CSRF token")
    void shouldAcceptDeleteWithCsrfToken() throws Exception {
        mockMvc.perform(delete("/products/1").with(csrf()))
            .andExpect(status().isOk())
            .andExpect(content().string("deleted 1"));
    }

    @Test
    @DisplayName("should accept a POST to the exempted webhook path with no CSRF token at all")
    void shouldAcceptWebhookPostWithoutCsrfToken() throws Exception {
        mockMvc.perform(post("/webhooks/notify"))
            .andExpect(status().isOk())
            .andExpect(content().string("received"));
    }

    @Test
    @DisplayName("should expose the current request's CSRF token as plain text")
    void shouldExposeCurrentCsrfToken() throws Exception {
        mockMvc.perform(get("/csrf"))
            .andExpect(status().isOk())
            .andExpect(content().string(not("")));
    }
}
