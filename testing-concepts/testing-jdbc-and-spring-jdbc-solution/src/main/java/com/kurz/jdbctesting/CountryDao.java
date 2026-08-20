package com.kurz.jdbctesting;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * A small data-access class built on {@link JdbcTemplate}: no connection,
 * statement, or resource-cleanup boilerplate, just SQL and a row mapping.
 * {@link #findByNameStartingWith(String)} uses
 * {@link NamedParameterJdbcTemplate} to bind a named {@code :prefix}
 * parameter instead of a positional {@code ?} placeholder.
 */
public class CountryDao {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public CountryDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    public List<Country> findAll() {
        return jdbcTemplate.query("select id, name, code_name from country", this::mapRow);
    }

    public Country findByCodeName(String codeName) {
        return jdbcTemplate.queryForObject(
                "select id, name, code_name from country where code_name = ?",
                this::mapRow, codeName);
    }

    public List<Country> findByNameStartingWith(String prefix) {
        return namedParameterJdbcTemplate.query(
                "select id, name, code_name from country where name like :prefix",
                Map.of("prefix", prefix + "%"),
                this::mapRow);
    }

    public int insert(Country country) {
        return jdbcTemplate.update(
                "insert into country (id, name, code_name) values (?, ?, ?)",
                country.id(), country.name(), country.codeName());
    }

    private Country mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Country(rs.getInt("id"), rs.getString("name"), rs.getString("code_name"));
    }
}
