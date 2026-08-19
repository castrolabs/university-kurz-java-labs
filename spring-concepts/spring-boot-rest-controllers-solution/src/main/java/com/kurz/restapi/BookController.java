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
        Book stored = new Book(nextId.getAndIncrement(), book.title(), book.author());
        books.put(stored.id(), stored);
        return stored;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBook(@PathVariable("id") Long id) {
        Book book = books.get(id);
        if (book == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(book);
    }

    @GetMapping
    public List<Book> getAllBooks() {
        return List.copyOf(books.values());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(@PathVariable("id") Long id) {
        books.remove(id);
    }
}
