package com.kurz.restapi;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/books")
public class BookController {

    private final Map<Long, Book> books = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Book createBook(@RequestBody Book book) {
        // TODO-00: Assign a new id via nextId.getAndIncrement(), store a Book with that
        // id (title and author copied from the incoming book) in the books map, and
        // return the stored book.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBook(@PathVariable("id") Long id) {
        // TODO-01: Return 200 OK with the book when it exists in the map, or 404 Not
        // Found (empty body) when it doesn't. Use ResponseEntity — never return a plain
        // null Book, which would silently answer 200 OK with an empty body instead.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @GetMapping
    public List<Book> getAllBooks() {
        // TODO-02: Return every stored book as a List.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    // TODO-03 (optional): Add a DELETE /books/{id} endpoint returning 204 No Content,
    // whether or not the id existed beforehand.
}
