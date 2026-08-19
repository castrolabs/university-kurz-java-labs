package com.kurz.jdbctemplate;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("IngredientRepository")
class IngredientRepositoryTest {

    private JdbcTemplate jdbcTemplate;
    private IngredientRepository repository;

    @BeforeEach
    void setUp() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:ingredient-repository-test;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");

        DataSource ds = dataSource;
        jdbcTemplate = new JdbcTemplate(ds);

        jdbcTemplate.execute("""
                create table Ingredient (
                  id varchar(4) not null primary key,
                  name varchar(25) not null,
                  type varchar(10) not null
                )
                """);

        jdbcTemplate.update("insert into Ingredient (id, name, type) values (?, ?, ?)",
                "FLTO", "Flour Tortilla", "WRAP");
        jdbcTemplate.update("insert into Ingredient (id, name, type) values (?, ?, ?)",
                "COTO", "Corn Tortilla", "WRAP");
        jdbcTemplate.update("insert into Ingredient (id, name, type) values (?, ?, ?)",
                "GRBF", "Ground Beef", "PROTEIN");
        jdbcTemplate.update("insert into Ingredient (id, name, type) values (?, ?, ?)",
                "CHED", "Cheddar", "CHEESE");

        repository = new IngredientRepository(jdbcTemplate);
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("drop table Ingredient");
    }

    @Test
    @DisplayName("findAll() returns every seeded ingredient")
    void shouldFindAllIngredients() {
        List<Ingredient> ingredients = repository.findAll();

        assertEquals(4, ingredients.size());
    }

    @Test
    @DisplayName("findById() returns the matching ingredient")
    void shouldFindIngredientById() {
        Ingredient ingredient = repository.findById("GRBF");

        assertEquals("Ground Beef", ingredient.name());
        assertEquals(Ingredient.Type.PROTEIN, ingredient.type());
    }

    @Test
    @DisplayName("findById() throws EmptyResultDataAccessException when no row matches")
    void shouldThrowWhenIdNotFound() {
        assertThrows(EmptyResultDataAccessException.class, () -> repository.findById("XXXX"));
    }

    @Test
    @DisplayName("findFirstIngredientOfType() returns the single match when the type is unique")
    void shouldReturnSingleMatchForUniqueType() {
        Ingredient ingredient = repository.findFirstIngredientOfType("PROTEIN");

        assertEquals("GRBF", ingredient.id());
    }

    @Test
    @DisplayName("findFirstIngredientOfType() throws IncorrectResultSizeDataAccessException when the type matches more than one row")
    void shouldThrowWhenTypeMatchesMultipleRows() {
        assertThrows(IncorrectResultSizeDataAccessException.class,
                () -> repository.findFirstIngredientOfType("WRAP"));
    }

    @Test
    @DisplayName("save() inserts a new ingredient retrievable afterward")
    void shouldSaveNewIngredient() {
        repository.save(new Ingredient("SOUR", "Sour Cream", Ingredient.Type.SAUCE));

        Ingredient saved = repository.findById("SOUR");

        assertEquals("Sour Cream", saved.name());
        assertEquals(Ingredient.Type.SAUCE, saved.type());
    }
}
