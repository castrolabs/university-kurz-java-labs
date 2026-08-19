package com.kurz.restapi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /books creates a book, returns 201 with an assigned id")
    void createsBookAndReturns201() throws Exception {
        Book newBook = new Book(null, "Effective Java", "Joshua Bloch");

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newBook)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Effective Java"))
                .andExpect(jsonPath("$.author").value("Joshua Bloch"));
    }

    @Test
    @DisplayName("GET /books/{id} returns 200 with the book when it exists")
    void returnsBookWhenItExists() throws Exception {
        Book newBook = new Book(null, "Java Concurrency in Practice", "Brian Goetz");

        String response = mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newBook)))
                .andReturn().getResponse().getContentAsString();

        Long createdId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/books/{id}", createdId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdId))
                .andExpect(jsonPath("$.title").value("Java Concurrency in Practice"))
                .andExpect(jsonPath("$.author").value("Brian Goetz"));
    }

    @Test
    @DisplayName("GET /books/{id} returns a real 404 with no body when the book does not exist")
    void returns404WhenBookDoesNotExist() throws Exception {
        mockMvc.perform(get("/books/{id}", 999_999L))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("GET /books returns every stored book")
    void returnsAllBooks() throws Exception {
        Book newBook = new Book(null, "Clean Code", "Robert C. Martin");

        mockMvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newBook)));

        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.not(org.hamcrest.Matchers.empty())));
    }
}
