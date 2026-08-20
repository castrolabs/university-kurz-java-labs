# Testing JDBC and Spring JDBC — Solution

## Overview

`CountryDaoTest` runs real SQL against a fresh, in-memory H2 database
created and torn down around every test, exercising the same (already
implemented) `CountryDao` through both positional-parameter methods
(`findAll`, `findByCodeName`, `insert`) and a named-parameter method
(`findByNameStartingWith`, built on `NamedParameterJdbcTemplate`).

## Key Concepts

- **Checking every field, not just that a row came back, catches mapping
  mistakes.** `shouldMapEveryColumnCorrectly` asserts `id`, `name`, AND
  `codeName` on the returned `Country`. A `RowMapper` that read the wrong
  column, or read columns in the wrong order, would still return *a*
  `Country` — only asserting on every field would catch it.
- **`JdbcTemplate` translates an empty result into an unchecked, Spring
  exception, not a raw `SQLException`.** `shouldThrowWhenCodeNameNotFound`
  expects `EmptyResultDataAccessException` from `queryForObject()` — the
  method never returns `null` for "not found".
- **A named parameter has to actually be bound, not just parse.**
  `shouldFindCountriesByNamePrefix` doesn't just count the results — it
  asserts France and Finland are present and Germany is absent, which is
  what actually proves `:prefix` was substituted into the `LIKE` pattern
  rather than the query matching everything (or nothing).
- **An empty result is a valid, non-exceptional outcome for a list-
  returning query.** `shouldReturnEmptyListWhenPrefixMatchesNothing`
  contrasts directly with `shouldThrowWhenCodeNameNotFound`:
  `NamedParameterJdbcTemplate.query()` (list-returning) answers "nothing
  matched" with an empty list, while `JdbcTemplate.queryForObject()`
  (single-row-returning) answers the same situation by throwing.
- **A round trip proves persistence; a non-throwing call doesn't.**
  `shouldInsertAndRetrieveNewCountry` re-reads the row `insert()` wrote
  through `findByCodeName()` instead of just asserting the `insert()` call
  didn't throw.

## Summary

Every test here runs against a real embedded database rather than a mock
of `JdbcTemplate` — that's deliberate: the thing under test is SQL and row
mapping, and the fastest way to falsely pass a test like that is to mock
away the part that could actually be wrong.
