package com.kurz.matchers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("SecurityConfig")
class SecurityConfigTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        var context = new AnnotationConfigWebApplicationContext();
        context.register(SecurityConfig.class, AdminController.class);
        context.setServletContext(new MockServletContext());
        context.refresh();

        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @Test
    @DisplayName("should allow an admin to reach the admin dashboard")
    void shouldAllowAdminToAccessDashboard() throws Exception {
        mockMvc.perform(get("/admin/dashboard").with(httpBasic("admin", "admin123")))
            .andExpect(status().isOk())
            .andExpect(content().string("Admin Dashboard"));
    }

    @Test
    @DisplayName("should forbid an authenticated non-admin from reaching the admin dashboard")
    void shouldForbidNonAdminFromAccessingDashboard() throws Exception {
        mockMvc.perform(get("/admin/dashboard").with(httpBasic("user", "user123")))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("should challenge an unauthenticated request to the admin dashboard with 401")
    void shouldChallengeUnauthenticatedRequestToDashboard() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("should allow anyone to reach /admin/health without credentials — the narrower rule is not shadowed")
    void shouldAllowAnyoneToAccessHealthEndpointWithoutCredentials() throws Exception {
        mockMvc.perform(get("/admin/health"))
            .andExpect(status().isOk())
            .andExpect(content().string("OK"));
    }

    @Test
    @DisplayName("should still reject /admin/health when wrong credentials are supplied — permitAll() only skips authorization, not authentication")
    void shouldRejectHealthEndpointCallWithWrongCredentials() throws Exception {
        mockMvc.perform(get("/admin/health").with(httpBasic("admin", "wrong-password")))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("should allow a manager to reach /reports")
    void shouldAllowManagerToAccessReports() throws Exception {
        mockMvc.perform(get("/reports").with(httpBasic("manager", "manager123")))
            .andExpect(status().isOk())
            .andExpect(content().string("Reports"));
    }

    @Test
    @DisplayName("should forbid a non-manager from reaching /reports")
    void shouldForbidNonManagerFromAccessingReports() throws Exception {
        mockMvc.perform(get("/reports").with(httpBasic("user", "user123")))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("should deny an authenticated user hitting an unmatched path via the anyRequest().denyAll() catch-all")
    void shouldDenyUnmatchedPathForAuthenticatedUserViaDenyAllCatchAll() throws Exception {
        mockMvc.perform(get("/unknown-path").with(httpBasic("admin", "admin123")))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("should challenge an unauthenticated request to an unmatched path with 401")
    void shouldChallengeUnmatchedPathForUnauthenticatedUser() throws Exception {
        mockMvc.perform(get("/unknown-path"))
            .andExpect(status().isUnauthorized());
    }
}
