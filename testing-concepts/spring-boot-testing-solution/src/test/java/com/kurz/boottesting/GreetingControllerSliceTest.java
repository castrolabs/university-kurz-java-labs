package com.kurz.boottesting;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GreetingController.class)
class GreetingControllerSliceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("the web slice serves the greeting endpoint")
    void greetingEndpointReturnsHelloMessage() throws Exception {
        mockMvc.perform(get("/greeting"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, Kurz!"));
    }

    @Test
    @DisplayName("the slice does not load unrelated business-layer beans")
    void sliceDoesNotLoadBusinessLayerBeans() {
        assertEquals(0, applicationContext.getBeanNamesForType(InventoryService.class).length);
    }
}
