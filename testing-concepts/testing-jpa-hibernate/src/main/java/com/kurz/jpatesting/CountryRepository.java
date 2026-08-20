package com.kurz.jpatesting;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * A Spring Data JPA repository: no implementation to write, Spring
 * generates it from the method signature (findByCodeName) or the
 * annotated JPQL (searchByNameFragment).
 */
public interface CountryRepository extends JpaRepository<Country, Long> {

    Optional<Country> findByCodeName(String codeName);

    @Query("select c from Country c where c.name like %:fragment%")
    List<Country> searchByNameFragment(@Param("fragment") String fragment);
}
