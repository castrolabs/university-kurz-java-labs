package com.kurz.itemapi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ItemController and ItemServiceImpl are already fully implemented in src/main/java.
 * Your job is to test the controller at the web layer with MockMvc, using
 * {@code @WebMvcTest} to load only the MVC infrastructure and ItemController - not the
 * real ItemServiceImpl, which is a plain {@code @Service} and therefore out of scope
 * for this slice.
 */
@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItemService itemService;

    @Autowired
    private ApplicationContext applicationContext;

    // TODO-00: Script itemService.findAll() to return a List.of(...) with two Items of
    // your choosing. Perform GET /items, assert status().isOk(), and use jsonPath to
    // check the array has size 2 and that "$[0].name" matches the first item's name.
    @Test
    @DisplayName("GET /items returns every item the service reports")
    void getAllReturnsEveryItem() throws Exception {
        fail("TODO-00: not implemented yet");
    }

    // TODO-01: Script itemService.findById(1L) to return Optional.of(...) with a known
    // Item. Perform GET /items/1, assert status().isOk(), and check "$.id" and "$.name"
    // via jsonPath against that item's fields.
    @Test
    @DisplayName("GET /items/{id} returns 200 with the item when it exists")
    void getByIdReturnsItemWhenPresent() throws Exception {
        fail("TODO-01: not implemented yet");
    }

    // TODO-02: Script itemService.findById(999L) to return Optional.empty(). Perform
    // GET /items/999 and assert status().isNotFound() - ItemController maps a missing
    // item to 404 via ResponseEntity.notFound(), never a null body with 200 OK.
    @Test
    @DisplayName("GET /items/{id} returns 404 when the item does not exist")
    void getByIdReturns404WhenAbsent() throws Exception {
        fail("TODO-02: not implemented yet");
    }

    // TODO-03: Prove the slice never wired the real ItemServiceImpl bean - only the
    // @MockitoBean substitute above. Use applicationContext.getBean(ItemServiceImpl.class)
    // wrapped in assertThrows(NoSuchBeanDefinitionException.class, ...). If this test
    // passes, it means @WebMvcTest loaded the web layer only, not the whole application -
    // the exact opposite of what @SpringBootTest would do for the same controller.
    @Test
    @DisplayName("the web slice never loads the real ItemServiceImpl bean")
    void sliceNeverLoadsRealServiceImplementation() {
        fail("TODO-03: not implemented yet");
    }

    // TODO-04 (optional): Using Mockito.verify(itemService), assert that
    // getById(...) called itemService.findById(...) with exactly the id from the path
    // variable - not just that the response happened to look right.
}
