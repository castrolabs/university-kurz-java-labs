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
        return jdbc.query("select id, name, type from Ingredient", this::mapRowToIngredient);
    }

    public Ingredient findById(String id) {
        return jdbc.queryForObject(
                "select id, name, type from Ingredient where id=?",
                this::mapRowToIngredient, id);
    }

    public Ingredient findFirstIngredientOfType(String type) {
        return jdbc.queryForObject(
                "select id, name, type from Ingredient where type=?",
                this::mapRowToIngredient, type);
    }

    public void save(Ingredient ingredient) {
        jdbc.update("insert into Ingredient (id, name, type) values (?, ?, ?)",
                ingredient.id(), ingredient.name(), ingredient.type().name());
    }

    public void deleteById(String id) {
        jdbc.update("delete from Ingredient where id=?", id);
    }

    private Ingredient mapRowToIngredient(ResultSet rs, int rowNum) throws SQLException {
        return new Ingredient(
                rs.getString("id"),
                rs.getString("name"),
                Ingredient.Type.valueOf(rs.getString("type")));
    }
}
