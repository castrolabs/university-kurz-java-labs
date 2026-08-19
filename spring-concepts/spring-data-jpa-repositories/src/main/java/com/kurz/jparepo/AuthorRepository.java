package com.kurz.jparepo;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface AuthorRepository extends ListCrudRepository<Author, Long> {

    List<Author> findByNameContainingIgnoreCase(String fragment);

    // TODO-02: This query only loads the Author rows. Reading a.getBooks() on
    // any of the returned authors triggers a separate, lazy-loaded query for
    // that author's books -- one extra query PER author (the N+1 problem).
    //
    // Replace the query string below with:
    //   "select distinct a from Author a join fetch a.books"
    // so every author AND its books come back in a single query.
    @Query("select a from Author a")
    List<Author> findAllWithBooksJoinFetch();

    // TODO-03 (optional): Add a method achieving the same single-query result
    // without writing JPQL, using @EntityGraph instead:
    //
    //   @EntityGraph(attributePaths = "books")
    //   List<Author> findAllBy();
    //
    // "findAllBy()" is Spring Data's naming convention for "match everything",
    // the same way findAll() does -- it just leaves room for the @EntityGraph
    // annotation without overriding the inherited findAll().
}
