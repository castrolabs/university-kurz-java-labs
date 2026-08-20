package com.kurz.jdbctesting;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    @DisplayName("findAll() returns every seeded row")
    void shouldFindAllCountries() {
        List<Country> countries = countryDao.findAll();

        assertEquals(3, countries.size());
    }

    @Test
    @DisplayName("findByCodeName() maps every column to the right field")
    void shouldMapEveryColumnCorrectly() {
        Country france = countryDao.findByCodeName("FR");

        assertEquals(1, france.id());
        assertEquals("France", france.name());
        assertEquals("FR", france.codeName());
    }

    @Test
    @DisplayName("findByCodeName() throws when no row matches")
    void shouldThrowWhenCodeNameNotFound() {
        assertThrows(EmptyResultDataAccessException.class, () -> countryDao.findByCodeName("XX"));
    }

    @Test
    @DisplayName("findByNameStartingWith() binds the named parameter into the LIKE pattern")
    void shouldFindCountriesByNamePrefix() {
        List<Country> countries = countryDao.findByNameStartingWith("F");

        assertEquals(2, countries.size());
        assertTrue(countries.stream().anyMatch(c -> c.codeName().equals("FR")));
        assertTrue(countries.stream().anyMatch(c -> c.codeName().equals("FI")));
        assertTrue(countries.stream().noneMatch(c -> c.codeName().equals("DE")));
    }

    @Test
    @DisplayName("findByNameStartingWith() returns an empty list when nothing matches")
    void shouldReturnEmptyListWhenPrefixMatchesNothing() {
        List<Country> countries = countryDao.findByNameStartingWith("Z");

        assertTrue(countries.isEmpty());
    }

    @Test
    @DisplayName("insert() persists a country retrievable afterward")
    void shouldInsertAndRetrieveNewCountry() {
        countryDao.insert(new Country(4, "Spain", "ES"));

        Country spain = countryDao.findByCodeName("ES");

        assertEquals(4, spain.id());
        assertEquals("Spain", spain.name());
        assertEquals("ES", spain.codeName());
    }

    @Test
    @DisplayName("bonus: insert() writes exactly one row, verified independently of CountryDao")
    void shouldWriteExactlyOneRowOnInsert() {
        Integer before = jdbcTemplate.queryForObject("select count(*) from country", Integer.class);

        countryDao.insert(new Country(4, "Spain", "ES"));

        Integer after = jdbcTemplate.queryForObject("select count(*) from country", Integer.class);

        assertEquals(before + 1, after);
    }
}
