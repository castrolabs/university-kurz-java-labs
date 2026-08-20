package com.kurz.jpatesting;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    @DisplayName("findByCodeName() finds the matching country")
    void shouldFindCountryByCodeName() {
        Optional<Country> france = countryRepository.findByCodeName("FR");

        assertTrue(france.isPresent());
        assertEquals("France", france.get().getName());
    }

    @Test
    @DisplayName("findByCodeName() returns an empty Optional when no country matches")
    void shouldReturnEmptyOptionalWhenCodeNameNotFound() {
        Optional<Country> result = countryRepository.findByCodeName("XX");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("searchByNameFragment() matches a substring anywhere in the name")
    void shouldSearchCountriesByNameFragment() {
        List<Country> matches = countryRepository.searchByNameFragment("ranc");

        assertEquals(1, matches.size());
        assertEquals("France", matches.get(0).getName());
    }

    @Test
    @DisplayName("an entity mutation is invisible to a raw query until the persistence context flushes")
    void mutationIsInvisibleToRawQueryUntilFlush() {
        Country france = entityManager.find(Country.class, franceId);
        france.setName("New France");

        assertEquals("France", readNameFromDatabaseDirectly(franceId));

        entityManager.flush();

        assertEquals("New France", readNameFromDatabaseDirectly(franceId));
    }

    @Test
    @DisplayName("IDENTITY generation assigns the id immediately on persist(), before any flush")
    void identityStrategyAssignsIdBeforeFlush() {
        Country spain = new Country("Spain", "ES");

        entityManager.persist(spain);

        assertNotNull(spain.getId());
    }

    @Test
    @DisplayName("bonus: save() through the repository is reflected in count()")
    void shouldSaveNewCountryThroughRepository() {
        countryRepository.save(new Country("Italy", "IT"));

        assertEquals(4, countryRepository.count());
        assertFalse(countryRepository.findByCodeName("IT").isEmpty());
    }
}
