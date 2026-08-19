package com.kurz.jparepo;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface AuthorRepository extends ListCrudRepository<Author, Long> {

    List<Author> findByNameContainingIgnoreCase(String fragment);

    @Query("select distinct a from Author a join fetch a.books")
    List<Author> findAllWithBooksJoinFetch();

    @EntityGraph(attributePaths = "books")
    List<Author> findAllBy();
}
