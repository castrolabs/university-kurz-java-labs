package com.kurz.testingauth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @WithMockUser, @WithUserDetails, and the custom @WithCustomAdmin annotation are processed by a
 * TestExecutionListener that Spring's TestContext framework registers automatically — which is
 * why this class needs @ExtendWith(SpringExtension.class) plus @ContextConfiguration instead of
 * the plain AnnotationConfigWebApplicationContext bootstrap used by the other labs in this track.
 * Without SpringExtension driving the test lifecycle, the annotations are silently never applied.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SecurityConfig.class, InventoryController.class})
@WebAppConfiguration
@DisplayName("SecurityConfig")
class SecurityConfigTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @Test
    @DisplayName("should challenge an unauthenticated request with 401")
    void shouldChallengeUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/inventory/view"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("@WithMockUser(roles = \"USER\") should reach /inventory/view — no real user store involved")
    void withMockUserShouldReachView() throws Exception {
        mockMvc.perform(get("/inventory/view"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("@WithMockUser(roles = \"USER\") should be forbidden from /inventory/admin — wrong role")
    void withMockUserWrongRoleShouldBeForbiddenFromAdmin() throws Exception {
        mockMvc.perform(get("/inventory/admin"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("@WithMockUser(roles = \"ADMIN\") should reach /inventory/admin — matching role")
    void withMockUserAdminRoleShouldReachAdmin() throws Exception {
        mockMvc.perform(get("/inventory/admin"))
            .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails("john")
    @DisplayName("@WithUserDetails(\"john\") should reach /inventory/admin — a real, enabled admin from the UserDetailsService")
    void withUserDetailsJohnShouldReachAdmin() throws Exception {
        mockMvc.perform(get("/inventory/admin"))
            .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails("mary")
    @DisplayName("@WithUserDetails(\"mary\") should still reach /inventory/view even though mary's account is disabled — @WithUserDetails never runs the account-status checks a real login would")
    void withUserDetailsMaryShouldStillReachViewDespiteBeingDisabled() throws Exception {
        mockMvc.perform(get("/inventory/view"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("real HTTP Basic authentication as mary should fail with 401 — this is the check @WithUserDetails skipped above")
    void realAuthenticationAsMaryShouldFailBecauseAccountIsDisabled() throws Exception {
        mockMvc.perform(get("/inventory/view").with(httpBasic("mary", "mary123")))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("real HTTP Basic authentication as john with the correct password should reach /inventory/admin")
    void realAuthenticationAsJohnShouldReachAdmin() throws Exception {
        mockMvc.perform(get("/inventory/admin").with(httpBasic("john", "john123")))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("real HTTP Basic authentication as john with the wrong password should fail with 401")
    void realAuthenticationAsJohnWithWrongPasswordShouldFail() throws Exception {
        mockMvc.perform(get("/inventory/admin").with(httpBasic("john", "wrong-password")))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithCustomAdmin(username = "carol")
    @DisplayName("@WithCustomAdmin(username = \"carol\") should reach /inventory/admin, even though \"carol\" doesn't exist in the real UserDetailsService at all")
    void withCustomAdminShouldReachAdminForANonExistentUser() throws Exception {
        mockMvc.perform(get("/inventory/admin"))
            .andExpect(status().isOk());
    }
}
