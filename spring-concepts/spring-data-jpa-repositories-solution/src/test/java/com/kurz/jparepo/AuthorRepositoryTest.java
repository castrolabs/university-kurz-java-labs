package com.kurz.jparepo;

import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DataJpaTest
@DisplayName("AuthorRepository")
class AuthorRepositoryTest {

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Statistics statistics;

    @BeforeEach
    void setUp() {
        for (int i = 1; i <= 3; i++) {
            Author author = new Author("Author " + i);
            int bookCount = i == 1 ? 2 : 3;
            for (int j = 1; j <= bookCount; j++) {
                author.addBook(new Book("Book " + i + "-" + j));
            }
            entityManager.persist(author);
        }
        entityManager.flush();
        entityManager.clear();

        statistics = entityManager.getEntityManager()
                .unwrap(Session.class)
                .getSessionFactory()
                .getStatistics();
        statistics.clear();
    }

    @Test
    @DisplayName("findByNameContainingIgnoreCase finds authors by a case-insensitive fragment")
    void shouldFindAuthorsByPartialNameCaseInsensitive() {
        List<Author> authors = authorRepository.findByNameContainingIgnoreCase("author");

        assertEquals(3, authors.size());
    }

    @Test
    @DisplayName("naive findAll() triggers one extra query per author when books are accessed (N+1)")
    void naiveFindAllTriggersNPlusOneQueries() {
        List<Author> authors = authorRepository.findAll();
        statistics.clear();

        for (Author author : authors) {
            assertFalse(author.getBooks().isEmpty());
        }

        assertEquals(authors.size(), statistics.getPrepareStatementCount(),
                "expected one lazy-loading query per author");
    }

    @Test
    @DisplayName("findAllWithBooksJoinFetch() loads authors and books in exactly one query")
    void joinFetchQueryUsesExactlyOneQuery() {
        statistics.clear();

        List<Author> authors = authorRepository.findAllWithBooksJoinFetch();
        for (Author author : authors) {
            assertFalse(author.getBooks().isEmpty());
        }

        assertEquals(3, authors.size());
        assertEquals(1, statistics.getPrepareStatementCount(),
                "expected a single join-fetch query regardless of author count");
    }
}
