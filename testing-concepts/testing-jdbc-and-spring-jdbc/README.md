# Testing JDBC and Spring JDBC

## Goal

`CountryDao` is already fully implemented — your job is to test it by
running real SQL against a fresh, in-memory H2 database, the way the
article demonstrates for both plain JDBC and Spring's `JdbcTemplate`.

## Prerequisites

- JUnit 5 fundamentals (`@Test`, `@BeforeEach`/`@AfterEach`, `assertThrows`)
- Reading `CountryDao` and `Country` in `src/main/java` before writing any
  test — in particular, notice that `findByNameStartingWith` uses
  `NamedParameterJdbcTemplate` with a `:prefix` placeholder, while the
  other methods use positional `?` placeholders

## Task

`CountryDaoTest`'s `setUp()` creates an embedded H2 database, creates the
`country` table, and seeds three known rows before every test; `tearDown()`
drops the table afterward. That part is done for you. Your job is to write
the test methods that exercise `CountryDao` against that database and prove
its SQL and row mapping are correct.

## Instructions

Complete the following TODOs in `CountryDaoTest`:

- TODO-00: `findAll()` returns every seeded row.
- TODO-01: `findByCodeName()` maps every column to the right field — not
  just that a row came back.
- TODO-02: `findByCodeName()` throws `EmptyResultDataAccessException` when
  no row matches.
- TODO-03: `findByNameStartingWith()` actually binds its `:prefix` named
  parameter into the `LIKE` pattern.
- TODO-04: `findByNameStartingWith()` returns an empty list (not `null`,
  no exception) when nothing matches.
- TODO-05: `insert()` persists a row that's retrievable afterward.

Run the tests until they all pass.

## Running the Lab

From the project root:

```bash
mvn -pl testing-concepts/testing-jdbc-and-spring-jdbc test
```

Or from the lab directory:

```bash
cd testing-concepts/testing-jdbc-and-spring-jdbc
mvn test
```

## Bonus (Optional)

- TODO-06 (optional): write a test that queries
  `select count(*) from country` directly with `jdbcTemplate`, bypassing
  `CountryDao`, as an independent check that `insert()` wrote exactly one
  row.
