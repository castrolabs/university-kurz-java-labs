# Testing JPA and Hibernate

## Goal

`Country` and `CountryRepository` are already fully implemented (a Spring
Data JPA repository interface has no method bodies to write in the first
place — Spring generates the implementation from the method signature and
the `@Query` annotation). Your job is to write tests, using `@DataJpaTest`
and `TestEntityManager`, that prove the generated queries are correct and
that expose a subtler behavior the article calls out: the persistence
context buffers writes and only sends them to the database when it
flushes.

## Prerequisites

- `testing-jdbc-and-spring-jdbc` (or equivalent JDBC/H2 testing background)
- Reading `Country` and `CountryRepository` in `src/main/java` — in
  particular, notice `Country` uses `GenerationType.IDENTITY` for its id

## Task

`CountryRepositoryTest`'s `setUp()` seeds three countries via
`TestEntityManager` and clears the persistence context afterward — that
part is done for you, along with a `readNameFromDatabaseDirectly()` helper
that queries the embedded database with a plain `JdbcTemplate`, completely
bypassing Hibernate. That helper is what lets a test observe what's
*actually* been written to the database at any given moment, independent
of what the persistence context believes is true.

## Instructions

Complete the following TODOs in `CountryRepositoryTest`:

- TODO-00: `findByCodeName()` finds the matching country.
- TODO-01: `findByCodeName()` returns an empty `Optional` — not an
  exception — when nothing matches.
- TODO-02: `searchByNameFragment()`'s JPQL `LIKE` search matches a
  substring anywhere in the name.
- TODO-03 (the key exercise): mutate a managed entity's field, prove the
  raw-JDBC helper still sees the OLD value because nothing has been
  flushed yet, then flush and prove the helper now sees the NEW value.
- TODO-04: prove that `GenerationType.IDENTITY` assigns the entity's id
  immediately on `persist()`, before any explicit flush.

Run the tests until they all pass.

## Running the Lab

From the project root:

```bash
mvn -pl testing-concepts/testing-jpa-hibernate test
```

Or from the lab directory:

```bash
cd testing-concepts/testing-jpa-hibernate
mvn test
```

## Bonus (Optional)

- TODO-05 (optional): save a new country through `countryRepository` and
  assert `count()` reflects it.
