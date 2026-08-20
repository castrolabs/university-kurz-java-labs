package com.kurz.itemapi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItemService itemService;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("GET /items returns every item the service reports")
    void getAllReturnsEveryItem() throws Exception {
        when(itemService.findAll()).thenReturn(List.of(
                new Item(1L, "Keyboard", "Peripherals"),
                new Item(2L, "Monitor", "Displays")
        ));

        mockMvc.perform(get("/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Keyboard"));
    }

    @Test
    @DisplayName("GET /items/{id} returns 200 with the item when it exists")
    void getByIdReturnsItemWhenPresent() throws Exception {
        when(itemService.findById(1L)).thenReturn(Optional.of(new Item(1L, "Keyboard", "Peripherals")));

        mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Keyboard"));
    }

    @Test
    @DisplayName("GET /items/{id} returns 404 when the item does not exist")
    void getByIdReturns404WhenAbsent() throws Exception {
        when(itemService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/items/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("the web slice never loads the real ItemServiceImpl bean")
    void sliceNeverLoadsRealServiceImplementation() {
        assertThrows(NoSuchBeanDefinitionException.class, () -> applicationContext.getBean(ItemServiceImpl.class));
    }

    @Test
    @DisplayName("bonus: getById delegates to findById with the exact path id")
    void getByIdCallsServiceWithExactId() throws Exception {
        when(itemService.findById(7L)).thenReturn(Optional.of(new Item(7L, "Webcam", "Peripherals")));

        mockMvc.perform(get("/items/7"))
                .andExpect(status().isOk());

        verify(itemService).findById(eq(7L));
    }
}
