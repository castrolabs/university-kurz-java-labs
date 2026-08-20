package com.kurz.jdbctesting;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * CountryDao is already fully implemented in src/main/java. Your job is to
 * write tests that run real SQL against a fresh, in-memory H2 database
 * created before every test and dropped after it - no server, no shared
 * state between tests.
 */
@DisplayName("CountryDao")
class CountryDaoTest {

    private JdbcTemplate jdbcTemplate;
    private CountryDao countryDao;

    @BeforeEach
    void setUp() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:country-dao-test;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");

        DataSource ds = dataSource;
        jdbcTemplate = new JdbcTemplate(ds);

        jdbcTemplate.execute("""
                create table country (
                  id int not null primary key,
                  name varchar(60) not null,
                  code_name varchar(10) not null
                )
                """);

        jdbcTemplate.update("insert into country (id, name, code_name) values (?, ?, ?)", 1, "France", "FR");
        jdbcTemplate.update("insert into country (id, name, code_name) values (?, ?, ?)", 2, "Germany", "DE");
        jdbcTemplate.update("insert into country (id, name, code_name) values (?, ?, ?)", 3, "Finland", "FI");

        countryDao = new CountryDao(jdbcTemplate);
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("drop table country");
    }

    // TODO-00: Call countryDao.findAll() and assert it returns exactly 3
    // countries - the rows seeded in setUp().
    @Test
    @DisplayName("findAll() returns every seeded row")
    void shouldFindAllCountries() {
        fail("TODO-00: not implemented yet");
    }

    // TODO-01: Call countryDao.findByCodeName("FR") and assert on every
    // field of the returned Country - id, name, AND codeName, not just
    // that a row came back. A RowMapper that reads the wrong column, or
    // reads columns in the wrong order, still returns *a* Country; only
    // checking every field catches a mapping mistake.
    @Test
    @DisplayName("findByCodeName() maps every column to the right field")
    void shouldMapEveryColumnCorrectly() {
        fail("TODO-01: not implemented yet");
    }

    // TODO-02: Call countryDao.findByCodeName("XX") - a code that doesn't
    // exist - and assert it throws
    // org.springframework.dao.EmptyResultDataAccessException. This is
    // JdbcTemplate translating an empty ResultSet into Spring's unchecked
    // DataAccessException hierarchy, not a raw SQLException.
    @Test
    @DisplayName("findByCodeName() throws when no row matches")
    void shouldThrowWhenCodeNameNotFound() {
        fail("TODO-02: not implemented yet");
    }

    // TODO-03: Call countryDao.findByNameStartingWith("F") and assert it
    // returns exactly 2 countries (France and Finland) - NOT Germany. This
    // proves the :prefix named parameter is actually bound into the LIKE
    // pattern, rather than the query silently matching everything or
    // nothing.
    @Test
    @DisplayName("findByNameStartingWith() binds the named parameter into the LIKE pattern")
    void shouldFindCountriesByNamePrefix() {
        fail("TODO-03: not implemented yet");
    }

    // TODO-04: Call countryDao.findByNameStartingWith("Z") - a prefix that
    // matches nothing - and assert the result is an empty list (not null,
    // and the call must not throw).
    @Test
    @DisplayName("findByNameStartingWith() returns an empty list when nothing matches")
    void shouldReturnEmptyListWhenPrefixMatchesNothing() {
        fail("TODO-04: not implemented yet");
    }

    // TODO-05: Call countryDao.insert(...) with a new Country (e.g. id 4,
    // "Spain", "ES"), then call countryDao.findByCodeName("ES") and assert
    // the returned Country matches what you inserted - proving the insert
    // actually persisted, not just that the method returned without
    // throwing.
    @Test
    @DisplayName("insert() persists a country retrievable afterward")
    void shouldInsertAndRetrieveNewCountry() {
        fail("TODO-05: not implemented yet");
    }

    // TODO-06 (optional): Write a test using
    // jdbcTemplate.queryForObject("select count(*) from country", Integer.class)
    // directly - bypassing CountryDao entirely - to independently confirm
    // insert() wrote exactly one new row.
}
