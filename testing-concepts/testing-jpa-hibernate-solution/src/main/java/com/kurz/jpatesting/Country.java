package com.kurz.jpatesting;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A domain class mapped to a table via annotations. The id uses
 * {@code GenerationType.IDENTITY}: the database's auto-increment column
 * generates the id, which means Hibernate must execute the INSERT
 * immediately when the entity is persisted, rather than deferring it to
 * flush time - see the JPA/Hibernate article's note on identifier
 * generation strategies.
 */
@Entity
@Table(name = "country")
public class Country {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "code_name", nullable = false)
    private String codeName;

    protected Country() {
        // required by JPA
    }

    public Country(String name, String codeName) {
        this.name = name;
        this.codeName = codeName;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCodeName() {
        return codeName;
    }
}
