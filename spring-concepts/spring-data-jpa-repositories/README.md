# Spring Data JPA Repositories

## Goal

Map two related entities with Spring Data JPA, and fix the classic N+1 query
problem a lazy `@OneToMany` association creates — by measuring, not guessing,
how many SQL queries actually run.

## Prerequisites

- Basic Spring Data JPA (`@Entity`, `@Id`, repository interfaces)
- Familiarity with `@OneToMany`/`@ManyToOne` associations
- Basic SQL joins

## Task

`Author` has many `Book`s. Right now neither class is a JPA entity, so
`AuthorRepository` can't even start up. Once you map them, `findAll()` works —
but iterating the result and reading each author's `books` triggers a
*separate* query per author, because `@OneToMany` defaults to lazy loading.
`findAllWithBooksJoinFetch()` is meant to fetch every author and their books
in a single query instead; right now it doesn't.

The test class counts actual SQL queries via Hibernate's statistics API, so
you can see the N+1 behavior — and the fix — as numbers, not as an assumption.

## Instructions

Complete the following TODOs:

- TODO-00: Turn `Author` into a JPA entity — `@Entity`, `@Id` +
  `@GeneratedValue`, and a lazy `@OneToMany(mappedBy = "author")` to `Book`.
- TODO-01: Turn `Book` into a JPA entity — `@Entity`, `@Id` +
  `@GeneratedValue`, and a `@ManyToOne` back to `Author` with `@JoinColumn`.
- TODO-02: Fix `AuthorRepository.findAllWithBooksJoinFetch()`'s `@Query` so it
  loads every author's books in the same query (`join fetch`), instead of
  leaving them to lazy-load one author at a time.

Run the tests until they all pass.

## Running the Lab

From the project root:

```bash
mvn -pl spring-concepts/spring-data-jpa-repositories test
```

Or from the lab directory:

```bash
cd spring-concepts/spring-data-jpa-repositories
mvn test
```

## Bonus (Optional)

- TODO-03 (optional): Add a second method, `findAllWithBooksEntityGraph()`,
  that achieves the same single-query result using `@EntityGraph` instead of
  a hand-written `join fetch` JPQL query.
