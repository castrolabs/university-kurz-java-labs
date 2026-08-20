# Testing JPA and Hibernate — Solution

## Overview

`CountryRepositoryTest` exercises the already-generated `CountryRepository`
through `@DataJpaTest`, and uses `TestEntityManager` plus a raw
`JdbcTemplate` helper to observe the persistence context's write-behind
behavior directly.

## Key Concepts

- **A missing match is `Optional.empty()`, not an exception.**
  `shouldReturnEmptyOptionalWhenCodeNameNotFound` contrasts with the
  sibling JDBC lab's `CountryDao.findByCodeName()`, which throws
  `EmptyResultDataAccessException` for the same situation — Spring Data
  JPA's `Optional<T>` return type signals "not found" as a normal value
  instead.
- **`mutationIsInvisibleToRawQueryUntilFlush` is the core lesson.** Calling
  `france.setName("New France")` only updates the managed entity in
  memory — Hibernate's dirty checking defers the actual `UPDATE` statement
  until the persistence context flushes. The test proves this by reading
  the row through a `JdbcTemplate` that has no idea Hibernate or its
  persistence context exist: right after the setter call it still reads
  the old name, and only after an explicit `entityManager.flush()` does it
  read the new one. A test that only asserted on `france.getName()` after
  the setter would trivially pass without proving anything about the
  database at all — reading through a separate, Hibernate-unaware channel
  is what makes the flush timing observable.
- **`GenerationType.IDENTITY` breaks the "nothing happens until flush"
  rule for inserts.** `identityStrategyAssignsIdBeforeFlush` calls
  `entityManager.persist(spain)` with no flush, yet `spain.getId()` is
  already non-null — because an identity/auto-increment column is the
  *only* way to obtain the generated id, Hibernate has no choice but to
  execute the `INSERT` immediately rather than deferring and batching it
  the way it can with a `SEQUENCE` generator.
- **`@Query("... where c.name like %:fragment%")` needs `@Param`.**
  `CountryRepository.searchByNameFragment` binds its JPQL parameter by
  name; without `-parameters` on javac, Spring Data can't recover the
  method parameter's name via reflection, so `@Param("fragment")` on the
  parameter is what makes the binding work.

## Summary

The flush test is the one worth re-reading: it's easy to write a
persistence test that looks like it's checking the database but is
actually only checking Hibernate's own in-memory session cache. Querying
through a completely separate JDBC path is what turns "the entity object
says X" into "the database actually contains X."
