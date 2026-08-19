package com.kurz.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@DisplayName("OrderController")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("should redisplay the form when name and street are blank")
    void shouldRedisplayFormWhenNameAndStreetAreBlank() throws Exception {
        mockMvc.perform(post("/orders")
                        .param("name", "")
                        .param("street", "")
                        .param("ccNumber", "4111111111111111")
                        .param("ccExpiration", "12/28")
                        .param("ccCVV", "123"))
                .andExpect(status().isOk())
                .andExpect(view().name("orderForm"))
                .andExpect(model().attributeHasFieldErrors("order", "name", "street"));
    }

    @Test
    @DisplayName("should redisplay the form when the credit card number fails the Luhn check")
    void shouldRedisplayFormWhenCreditCardNumberIsInvalid() throws Exception {
        mockMvc.perform(post("/orders")
                        .param("name", "Ada Lovelace")
                        .param("street", "1 Analytical Engine Way")
                        .param("ccNumber", "1234567890123456")
                        .param("ccExpiration", "12/28")
                        .param("ccCVV", "123"))
                .andExpect(status().isOk())
                .andExpect(view().name("orderForm"))
                .andExpect(model().attributeHasFieldErrors("order", "ccNumber"));
    }

    @Test
    @DisplayName("should redisplay the form when the expiration date is not MM/YY")
    void shouldRedisplayFormWhenExpirationIsMalformed() throws Exception {
        mockMvc.perform(post("/orders")
                        .param("name", "Ada Lovelace")
                        .param("street", "1 Analytical Engine Way")
                        .param("ccNumber", "4111111111111111")
                        .param("ccExpiration", "2028-12")
                        .param("ccCVV", "123"))
                .andExpect(status().isOk())
                .andExpect(view().name("orderForm"))
                .andExpect(model().attributeHasFieldErrors("order", "ccExpiration"));
    }

    @Test
    @DisplayName("should redisplay the form when the CVV has more than 3 digits")
    void shouldRedisplayFormWhenCvvIsNotThreeDigits() throws Exception {
        mockMvc.perform(post("/orders")
                        .param("name", "Ada Lovelace")
                        .param("street", "1 Analytical Engine Way")
                        .param("ccNumber", "4111111111111111")
                        .param("ccExpiration", "12/28")
                        .param("ccCVV", "1234"))
                .andExpect(status().isOk())
                .andExpect(view().name("orderForm"))
                .andExpect(model().attributeHasFieldErrors("order", "ccCVV"));
    }

    @Test
    @DisplayName("should redirect once every field is valid")
    void shouldRedirectWhenOrderIsFullyValid() throws Exception {
        mockMvc.perform(post("/orders")
                        .param("name", "Ada Lovelace")
                        .param("street", "1 Analytical Engine Way")
                        .param("ccNumber", "4111111111111111")
                        .param("ccExpiration", "12/28")
                        .param("ccCVV", "123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/current"));
    }
}
