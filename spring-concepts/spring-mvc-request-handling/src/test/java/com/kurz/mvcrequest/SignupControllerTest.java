package com.kurz.mvcrequest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.instanceOf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SignupController.class)
class SignupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /signup shows the form with an empty SignupForm in the model")
    void showsSignupForm() throws Exception {
        mockMvc.perform(get("/signup"))
                .andExpect(status().isOk())
                .andExpect(view().name("signup-form"))
                .andExpect(model().attributeExists("signupForm"))
                .andExpect(model().attribute("signupForm", instanceOf(SignupForm.class)));
    }

    @Test
    @DisplayName("POST /signup binds form fields implicitly and redirects to /signup/success")
    void processesSignupAndRedirects() throws Exception {
        mockMvc.perform(post("/signup")
                        .param("username", "alice")
                        .param("email", "alice@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/signup/success"))
                .andExpect(flash().attribute("username", "alice"));
    }

    @Test
    @DisplayName("GET /signup/success renders the confirmation view")
    void showsSuccessPage() throws Exception {
        mockMvc.perform(get("/signup/success"))
                .andExpect(status().isOk())
                .andExpect(view().name("signup-success"));
    }
}
