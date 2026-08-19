package com.kurz.jdbctemplate;

import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class IngredientRepository {

    private final JdbcTemplate jdbc;

    public IngredientRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Ingredient> findAll() {
        // TODO-00: Query "select id, name, type from Ingredient" and map every row
        // with mapRowToIngredient (a method reference works as a RowMapper).

        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public Ingredient findById(String id) {
        // TODO-01: Use jdbc.queryForObject() with "select id, name, type from
        // Ingredient where id=?", mapRowToIngredient, and id. If no row matches,
        // Spring throws EmptyResultDataAccessException on its own -- you don't
        // need to check for that yourself.

        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public Ingredient findFirstIngredientOfType(String type) {
        // TODO-02: Use jdbc.queryForObject() with "select id, name, type from
        // Ingredient where type=?", mapRowToIngredient, and type.
        //
        // "type" is NOT a unique column -- several ingredients can share it. Once
        // this is implemented, the test deliberately calls it with a type that
        // matches more than one row, to observe what queryForObject() does when
        // that happens.

        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public void save(Ingredient ingredient) {
        // TODO-03: Use jdbc.update() with "insert into Ingredient (id, name, type)
        // values (?, ?, ?)", passing ingredient.id(), ingredient.name(), and
        // ingredient.type().name() as the positional parameters.

        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public void deleteById(String id) {
        // TODO-05 (optional): Use jdbc.update() with
        // "delete from Ingredient where id=?" and id.

        throw new UnsupportedOperationException("Not implemented yet.");
    }

    private Ingredient mapRowToIngredient(ResultSet rs, int rowNum) throws SQLException {
        // TODO-04: Build an Ingredient from the current row: rs.getString("id"),
        // rs.getString("name"), and Ingredient.Type.valueOf(rs.getString("type")).

        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
