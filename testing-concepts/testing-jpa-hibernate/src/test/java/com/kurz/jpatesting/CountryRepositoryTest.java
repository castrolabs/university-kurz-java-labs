package com.kurz.jpatesting;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * CountryRepository is a Spring Data JPA repository interface - there's no
 * implementation to read, Spring generates it. Your job is to write tests
 * that prove the generated queries are correct, AND to prove a subtler
 * point the article makes: an entity's in-memory field change isn't sent
 * to the database as SQL until the persistence context flushes, whether
 * that flush is explicit or triggered automatically.
 *
 * readNameFromDatabaseDirectly() below bypasses Hibernate entirely, using
 * a plain JdbcTemplate over the same embedded database, so you can observe
 * exactly what's actually been written to the database at any point -
 * independent of whatever Hibernate's persistence context thinks is true.
 */
@DataJpaTest
@DisplayName("CountryRepository")
class CountryRepositoryTest {

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DataSource dataSource;

    private Long franceId;

    @BeforeEach
    void setUp() {
        Country france = entityManager.persistAndFlush(new Country("France", "FR"));
        entityManager.persistAndFlush(new Country("Finland", "FI"));
        entityManager.persistAndFlush(new Country("Germany", "DE"));
        entityManager.clear();

        franceId = france.getId();
    }

    private String readNameFromDatabaseDirectly(Long id) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate.queryForObject("select name from country where id = ?", String.class, id);
    }

    // TODO-00: Call countryRepository.findByCodeName("FR") and assert the
    // Optional is present with a Country whose name is "France".
    @Test
    @DisplayName("findByCodeName() finds the matching country")
    void shouldFindCountryByCodeName() {
        fail("TODO-00: not implemented yet");
    }

    // TODO-01: Call countryRepository.findByCodeName("XX") - a code that
    // doesn't exist - and assert the returned Optional is empty. Unlike
    // the raw-JDBC CountryDao in the sibling lab, a Spring Data JPA
    // repository returning Optional signals "not found" without throwing.
    @Test
    @DisplayName("findByCodeName() returns an empty Optional when no country matches")
    void shouldReturnEmptyOptionalWhenCodeNameNotFound() {
        fail("TODO-01: not implemented yet");
    }

    // TODO-02: Call countryRepository.searchByNameFragment("ranc") and
    // assert it returns exactly one country, and that its name is
    // "France" - proving the %:fragment% JPQL LIKE search matches a
    // substring, not just a prefix or an exact name.
    @Test
    @DisplayName("searchByNameFragment() matches a substring anywhere in the name")
    void shouldSearchCountriesByNameFragment() {
        fail("TODO-02: not implemented yet");
    }

    // TODO-03: This is the key test of the lab. Follow these steps:
    //   1. Fetch France with entityManager.find(Country.class, franceId).
    //   2. Call setName("New France") on it - a plain field mutation, no
    //      save()/persist() call, relying on Hibernate's dirty checking.
    //   3. Immediately call readNameFromDatabaseDirectly(franceId) and
    //      assert it STILL returns "France" - the old value. The setter
    //      only changed the in-memory entity; Hibernate hasn't sent an
    //      UPDATE to the database yet, so a query that bypasses Hibernate
    //      (like this raw JdbcTemplate one) can't see the change.
    //   4. Call entityManager.flush() to force Hibernate to write the
    //      pending UPDATE.
    //   5. Call readNameFromDatabaseDirectly(franceId) again and assert it
    //      NOW returns "New France".
    @Test
    @DisplayName("an entity mutation is invisible to a raw query until the persistence context flushes")
    void mutationIsInvisibleToRawQueryUntilFlush() {
        fail("TODO-03: not implemented yet");
    }

    // TODO-04: Build `new Country("Spain", "ES")` and pass it to
    // entityManager.persist(...) - NOT persistAndFlush. Immediately after,
    // without calling flush() yourself, assert the entity's getId() is
    // already non-null. This is specific to GenerationType.IDENTITY: since
    // the database's auto-increment column is the only source of the id,
    // Hibernate has no choice but to execute the INSERT right away, unlike
    // a deferred-until-flush strategy such as SEQUENCE.
    @Test
    @DisplayName("IDENTITY generation assigns the id immediately on persist(), before any flush")
    void identityStrategyAssignsIdBeforeFlush() {
        fail("TODO-04: not implemented yet");
    }

    // TODO-05 (optional): Call countryRepository.save(new Country("Italy",
    // "IT")) and assert countryRepository.count() is now 4 - the 3 seeded
    // in setUp() plus the one you just saved.
}
