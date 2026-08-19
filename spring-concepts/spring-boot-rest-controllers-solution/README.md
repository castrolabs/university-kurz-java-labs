# Spring Boot REST Controllers - Solution

## Overview

This is the official solution for the Spring Boot REST Controllers lab. It implements a small
in-memory `Book` resource and shows why `ResponseEntity` — not a bare return type — is what
makes a "not found" response honest.

## Key Concepts

### `@RestController` writes the response body directly

`@RestController` combines `@Controller` (component scanning) with an implicit `@ResponseBody`
on every handler method, so a returned `Book` or `List<Book>` gets serialized to JSON directly
instead of being resolved as a view name.

### `@ResponseStatus` for the success path

```java
@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
@ResponseStatus(HttpStatus.CREATED)
public Book createBook(@RequestBody Book book) { ... }
```

`@ResponseStatus(HttpStatus.CREATED)` overrides Spring MVC's default `200 OK` with the more
accurate `201 Created` — the method's return type stays a plain `Book`, since there's only one
outcome to report on the happy path.

### `ResponseEntity` for a genuine "not found"

```java
@GetMapping("/{id}")
public ResponseEntity<Book> getBook(@PathVariable("id") Long id) {
    Book book = books.get(id);
    if (book == null) {
        return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(book);
}
```

Unlike the create endpoint, this handler has two outcomes (found / not found), and each needs a
different status code. Returning a bare `Book` (or `null`) can't express that — the method would
always answer `200 OK`, with an empty JSON body when the book is missing. Wrapping the result in
`ResponseEntity<Book>` makes "not found" an honest, testable part of the response.

## Implementation Details

`books` is a `ConcurrentHashMap<Long, Book>` acting as the in-memory store, and `nextId` is an
`AtomicLong` handing out unique ids. `getAllBooks()` returns `List.copyOf(books.values())` — a
defensive, immutable snapshot rather than exposing the live map's values view.

## Trade-offs and Best Practices

1. **`null` compiles, runs, and lies**: returning `null` from a handler that isn't wrapped in
   `ResponseEntity` produces a response that looks successful (`200 OK`) but carries nothing
   usable — the bug only surfaces when a client actually inspects the body.
2. **`ResponseEntity` costs a bit more code for a more honest contract**: every branch of the
   method explicitly states its status code, which is easy to verify with a `MockMvc` test like
   this lab's `returns404WhenBookDoesNotExist`.
3. **`ProblemDetail` goes further**: Spring Framework 6+ can render a structured
   `application/problem+json` body for error responses instead of an empty one — worth reaching
   for once an API needs to explain *why* a request failed, not just report that it did.

## Summary

- `@RestController` + `@ResponseStatus` covers single-outcome handlers (like `create`).
- `ResponseEntity<T>` covers multi-outcome handlers (like `get-by-id`), where the status code
  itself depends on what happened.
- A `MockMvc` test asserting the exact status code and body shape is what catches the "always
  200 OK" mistake before it reaches a real client.
