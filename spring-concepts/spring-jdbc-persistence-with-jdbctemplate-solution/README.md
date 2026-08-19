# Spring JDBC Persistence with JdbcTemplate - Solution

## Overview

This is the official solution for the Spring JDBC Persistence with
JdbcTemplate lab. It demonstrates `JdbcTemplate`'s `query()`/`queryForObject()`
pair, a shared `RowMapper`, and a plain `update()` insert.

## Key Concepts

### `query()` for many rows, `queryForObject()` for exactly one

```java
public List<Ingredient> findAll() {
    return jdbc.query("select id, name, type from Ingredient", this::mapRowToIngredient);
}

public Ingredient findById(String id) {
    return jdbc.queryForObject(
        "select id, name, type from Ingredient where id=?",
        this::mapRowToIngredient, id);
}
```

Both take a `RowMapper<T>` — here, a method reference to
`mapRowToIngredient`, since the same mapping logic is reused across every
read method. `query()` returns a `List<T>` (empty if nothing matched);
`queryForObject()` returns a single `T` and enforces, at runtime, that the
query produced **exactly one** row.

### `RowMapper`

```java
private Ingredient mapRowToIngredient(ResultSet rs, int rowNum) throws SQLException {
    return new Ingredient(
        rs.getString("id"),
        rs.getString("name"),
        Ingredient.Type.valueOf(rs.getString("type")));
}
```

One method builds an `Ingredient` from the current `ResultSet` row; every
`query()`/`queryForObject()` call in this class reuses it instead of
duplicating the mapping.

### Insert with positional parameters

```java
public void save(Ingredient ingredient) {
    jdbc.update("insert into Ingredient (id, name, type) values (?, ?, ?)",
        ingredient.id(), ingredient.name(), ingredient.type().name());
}
```

`update()` fills the `?` placeholders from the trailing varargs, in order —
no manual escaping, no string concatenation.

## Implementation Details

### `queryForObject()` enforces "exactly one row," it doesn't just hope for it

`findById()` queries by `id`, the table's primary key, so "exactly one row"
is guaranteed by the schema — if nothing matches, `queryForObject()` throws
`EmptyResultDataAccessException` instead of returning `null`:

```java
assertThrows(EmptyResultDataAccessException.class, () -> repository.findById("XXXX"));
```

`findFirstIngredientOfType()` queries by `type`, which is **not** unique —
several ingredients can share a type. Calling it with a type that matches
more than one row throws `IncorrectResultSizeDataAccessException`:

```java
assertThrows(IncorrectResultSizeDataAccessException.class,
    () -> repository.findFirstIngredientOfType("WRAP"));
```

Both exceptions come from the exact same `queryForObject()` call — the
method doesn't special-case "zero" versus "too many"; it just requires the
result size to be one, in both directions. Anyone migrating from raw JDBC
(where `rs.next()` returning `false` just means "return `null`") has to
relearn this: `queryForObject()` never returns `null`, and a query that
*can* return more than one row is a misuse of it, not an edge case to guard
against after the fact.

### `deleteById()` (`TODO-05`, optional)

```java
public void deleteById(String id) {
    jdbc.update("delete from Ingredient where id=?", id);
}
```

Same shape as `save()` — `update()` handles any DML statement, not just
inserts.

## Trade-offs

- `JdbcTemplate` removes the connection/statement/result-set boilerplate and
  the checked `SQLException` handling raw JDBC forces on every caller, but
  it's still string SQL plus positional parameters — a typo or a
  parameter-order mistake surfaces at runtime, not compile time.
- `queryForObject()` trades a `null`-returning convenience for a stricter
  guarantee: it never silently returns nothing, and it never silently
  returns "the first match" when there's more than one. Both failure modes
  become exceptions instead of bugs that surface somewhere else.
- Spring Framework 6.1 introduced `JdbcClient`, a fluent facade unifying
  `JdbcTemplate` and `NamedParameterJdbcTemplate`, now the officially
  recommended entry point for new code — `JdbcTemplate` remains fully
  supported and is what this lab uses directly, matching how the concept is
  taught end to end.

## Summary

- `query()` returns a list; `queryForObject()` returns exactly one object or
  throws.
- `EmptyResultDataAccessException` (zero rows) and
  `IncorrectResultSizeDataAccessException` (more than one row) are both
  `queryForObject()`'s doing, not something the caller has to check for
  manually.
- A `RowMapper` reused across multiple queries keeps row-to-object mapping
  in one place.
- `update()` handles inserts, updates, and deletes alike, with the same
  positional-parameter mechanics as `query()`.
