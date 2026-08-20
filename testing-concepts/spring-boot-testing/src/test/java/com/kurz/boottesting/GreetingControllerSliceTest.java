package com.kurz.boottesting;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * GreetingController has zero collaborators, so testing it doesn't need the full
 * application graph OrderPlacingIntegrationTest depends on - a @WebMvcTest slice is
 * enough, and is far cheaper to start. These two test classes exercise the SAME
 * application from two different angles: this one asks "does one layer work in
 * isolation?", the other asks "do the beans wire together correctly end to end?".
 */
@WebMvcTest(GreetingController.class)
class GreetingControllerSliceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationContext applicationContext;

    // TODO-03: Perform GET /greeting, assert status().isOk(), and assert the response
    // body equals "Hello, Kurz!" via content().string(...).
    @Test
    @DisplayName("the web slice serves the greeting endpoint")
    void greetingEndpointReturnsHelloMessage() throws Exception {
        fail("TODO-03: not implemented yet");
    }

    // TODO-04: Assert applicationContext.getBeanNamesForType(InventoryService.class)
    // has length 0. GreetingController never depends on InventoryService, so this
    // slice never has a reason to load it - proving the choice between @SpringBootTest
    // and a slice isn't just about speed, it's about which beans actually end up
    // under test.
    @Test
    @DisplayName("the slice does not load unrelated business-layer beans")
    void sliceDoesNotLoadBusinessLayerBeans() {
        fail("TODO-04: not implemented yet");
    }
}
