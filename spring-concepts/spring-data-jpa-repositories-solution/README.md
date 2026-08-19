# Spring Data JPA Repositories - Solution

## Overview

This is the official solution for the Spring Data JPA Repositories lab. It
demonstrates mapping a one-to-many relationship, and fixing the N+1 query
problem a lazily-loaded association creates.

## Key Concepts

### Mapping the relationship

```java
@Entity
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Book> books = new ArrayList<>();
}

@Entity
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private Author author;
}
```

`mappedBy = "author"` tells Hibernate that `Book.author` owns the
foreign key — `Author` doesn't get its own join column. `FetchType.LAZY` is
the *default* for `@OneToMany` even without stating it explicitly; it's
called out here because it's the entire reason the N+1 problem exists.

### The N+1 problem, measured

`AuthorRepositoryTest.naiveFindAllTriggersNPlusOneQueries()` seeds 3 authors
(with 2-3 books each), calls the inherited `findAll()`, clears Hibernate's
statistics, then reads `author.getBooks()` for every author:

```java
List<Author> authors = authorRepository.findAll();
statistics.clear();

for (Author author : authors) {
    author.getBooks(); // each call is a separate, lazy-loaded SELECT
}

assertEquals(authors.size(), statistics.getQueryExecutionCount());
```

`findAll()` itself only selects rows from `Author`. Every `getBooks()` call
on an unloaded lazy collection fires its own
`select * from Book where author_id = ?` — one additional query per author,
hence "N+1" (1 query for the parents, N for their children).

### Fixing it with `JOIN FETCH`

```java
@Query("select distinct a from Author a join fetch a.books")
List<Author> findAllWithBooksJoinFetch();
```

`join fetch` tells Hibernate to populate the association eagerly, as part of
the *same* SQL query, instead of leaving it lazy. `distinct` is required on
the JPQL side: a SQL join returns one row per (author, book) pair, so without
`distinct` the same `Author` object would appear once per book in the
returned `List`, even though Hibernate returns the *same* managed instance
for each occurrence.

`joinFetchQueryUsesExactlyOneQuery()` proves the fix the same way it proved
the problem — by asserting `statistics.getQueryExecutionCount() == 1`
regardless of how many authors exist.

### Deriving queries from method names

```java
List<Author> findByNameContainingIgnoreCase(String fragment);
```

Spring Data parses this method name directly: `findBy` + `NameContaining` +
`IgnoreCase` becomes
`where lower(name) like lower(concat('%', :fragment, '%'))` — no query
string, no implementation class.

## Implementation Details

### `findAllBy()` with `@EntityGraph` (`TODO-03`, optional)

```java
@EntityGraph(attributePaths = "books")
List<Author> findAllBy();
```

`@EntityGraph` achieves the same single-query, eager-fetch result as
`findAllWithBooksJoinFetch()`, but declaratively — Spring Data builds the
fetch plan instead of a hand-written `join fetch` JPQL string.
`findAllBy()` (an empty predicate) is Spring Data's naming convention for
"match everything," the same way `findAll()` does; it exists here only so the
`@EntityGraph` annotation has a method to attach to, without overriding the
repository's inherited `findAll()`.

## Trade-offs

- `CrudRepository`/`ListCrudRepository` eliminate the DAO implementation
  entirely, but a repository interface with zero implementing classes gives
  up compile-time verification — a query mistake surfaces as a startup
  failure (as seen while `Author`/`Book` weren't mapped yet) or, worse, a
  silently-slow N+1 pattern that only shows up under load.
- Query derivation from method names is fast for simple predicates but
  doesn't scale — anything beyond a couple of conditions reads better as an
  explicit `@Query`.
- `List<T>` from `findAll()`/`findAllById()` needs `ListCrudRepository`
  (Spring Data 3.0+); the older `CrudRepository` returns `Iterable<T>`
  instead — both otherwise behave identically.
- `@OneToMany` defaulting to lazy loading is usually the right call (eager
  loading by default would make every `Author` fetch pull in every book,
  everywhere) — but it means every access path to a collection needs a
  deliberate decision: lazy-load one at a time, or fetch eagerly up front
  with `JOIN FETCH`/`@EntityGraph`.

## Summary

- `@OneToMany` defaults to `FetchType.LAZY`; iterating a lazily-loaded
  collection across many parent rows produces one query per row (N+1).
- `JOIN FETCH` (with `DISTINCT`) or `@EntityGraph` collapses that into a
  single query.
- Hibernate's statistics API (`SessionFactory.getStatistics()`) turns "how
  many queries ran" from a guess into an assertion.
