# Spring JDBC Persistence with JdbcTemplate

## Goal

Replace raw JDBC's connection/statement/result-set boilerplate with
`JdbcTemplate`, and learn what `queryForObject()` does differently from
`query()` when zero or more than one row comes back.

## Prerequisites

- Basic SQL (`select`, `insert`, `where`)
- Familiarity with `ResultSet`
- Basic exception handling in Java

## Task

`IngredientRepository` wraps a `JdbcTemplate` around an `Ingredient` table.
You'll implement fetching all ingredients, fetching one by its (unique) `id`,
fetching one by its (non-unique) `type`, saving a new ingredient, and the
`RowMapper` all three read methods share.

Pay attention to `findFirstIngredientOfType()`: it uses `queryForObject()`
against a column that isn't unique. `queryForObject()` isn't just `query()`
that happens to return one item — it actively enforces that exactly one row
comes back, and throws if that assumption doesn't hold.

## Instructions

Complete the following TODOs in `IngredientRepository`:

- TODO-00: Implement `findAll()` with `jdbc.query()`.
- TODO-01: Implement `findById()` with `jdbc.queryForObject()`.
- TODO-02: Implement `findFirstIngredientOfType()` with `jdbc.queryForObject()`
  against the (non-unique) `type` column.
- TODO-03: Implement `save()` with `jdbc.update()` and positional `?`
  parameters.
- TODO-04: Implement the shared `mapRowToIngredient` `RowMapper` method.

Run the tests until they all pass.

## Running the Lab

From the project root:

```bash
mvn -pl spring-concepts/spring-jdbc-persistence-with-jdbctemplate test
```

Or from the lab directory:

```bash
cd spring-concepts/spring-jdbc-persistence-with-jdbctemplate
mvn test
```

## Bonus (Optional)

- TODO-05 (optional): Implement `deleteById()` with `jdbc.update()`.
