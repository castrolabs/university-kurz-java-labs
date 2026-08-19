package com.kurz.httpauth;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("SecurityConfig")
class SecurityConfigTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        var context = new AnnotationConfigWebApplicationContext();
        context.register(SecurityConfig.class, GreetingController.class);
        context.setServletContext(new MockServletContext());
        context.refresh();

        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @Test
    @DisplayName("should challenge an unauthenticated API-style request with a 401 HTTP Basic challenge")
    void shouldChallengeApiClientWithHttpBasicWhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/hello"))
            .andExpect(status().isUnauthorized())
            .andExpect(header().string("WWW-Authenticate", Matchers.startsWith("Basic")));
    }

    @Test
    @DisplayName("should redirect an unauthenticated browser-style request (Accept: text/html) to the login page")
    void shouldRedirectBrowserToLoginWhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/hello").accept(MediaType.TEXT_HTML))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("should authenticate over HTTP Basic on a single request, sending credentials every time")
    void shouldAuthenticateWithHttpBasicOnASingleRequest() throws Exception {
        mockMvc.perform(get("/hello").with(httpBasic("alice", "password")))
            .andExpect(status().isOk())
            .andExpect(content().string("Hello, alice"));
    }

    @Test
    @DisplayName("should reject an HTTP Basic request carrying the wrong password")
    void shouldNotAuthenticateWithHttpBasicWhenPasswordIsWrong() throws Exception {
        mockMvc.perform(get("/hello").with(httpBasic("alice", "wrong-password")))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("should establish a session after a successful form login, so a follow-up request needs no credentials")
    void shouldEstablishSessionAfterSuccessfulFormLogin() throws Exception {
        var loginResult = mockMvc.perform(formLogin().user("alice").password("password"))
            .andExpect(status().is3xxRedirection())
            .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertNotNull(session, "a successful form login should create an HTTP session");

        mockMvc.perform(get("/hello").session(session))
            .andExpect(status().isOk())
            .andExpect(content().string("Hello, alice"));
    }

    @Test
    @DisplayName("should redirect back to the login page with an error when form login uses the wrong password")
    void shouldRedirectToLoginWithErrorWhenFormLoginUsesWrongPassword() throws Exception {
        mockMvc.perform(formLogin().user("alice").password("wrong-password"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login?error"));
    }
}
