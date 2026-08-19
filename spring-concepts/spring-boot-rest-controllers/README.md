# Spring Boot REST Controllers

## Goal

Build a small JSON REST resource with `@RestController` and learn why `ResponseEntity` matters:
returning `null`/a plain object always answers `200 OK`, even when the resource doesn't exist.

## Prerequisites

- Basic Spring MVC (`@Controller`, request mapping annotations)
- Familiarity with HTTP status codes (`200`, `201`, `404`)
- Basic JSON (this lab uses Jackson via Spring Boot's default JSON support)

## Task

`BookController` exposes an in-memory `Book` resource: creating a book, fetching one by id, and
listing all of them. You'll implement each handler so that responses carry the right HTTP
status code and body — not just a `200 OK` no matter what happened.

The test uses `@WebMvcTest` with `MockMvc`, a fast slice test that loads only the web layer
(no database, no full application context).

## Instructions

Complete the following TODOs in `BookController`:

- TODO-00: Implement `createBook` to assign an id and store the book, returning `201 Created`.
- TODO-01: Implement `getBook` to return `200 OK` with the book when found, or a real
  `404 Not Found` (via `ResponseEntity`) when it isn't — never a `null` body with an implicit
  `200 OK`.
- TODO-02: Implement `getAllBooks` to return every stored book.

Run the tests until they all pass.

## Running the Lab

From the project root:

```bash
mvn -pl spring-concepts/spring-boot-rest-controllers test
```

Or from the lab directory:

```bash
cd spring-concepts/spring-boot-rest-controllers
mvn test
```

## Bonus (Optional)

- TODO-03 (optional): Add a `DELETE /books/{id}` endpoint returning `204 No Content`.
