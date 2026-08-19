package com.kurz.jparepo;

import java.util.ArrayList;
import java.util.List;

// TODO-00: Turn Author into a JPA entity:
//   - Add @Entity.
//   - Annotate id with @Id and @GeneratedValue(strategy = GenerationType.IDENTITY).
//   - Annotate books with @OneToMany(mappedBy = "author", fetch = FetchType.LAZY,
//     cascade = CascadeType.ALL).
// The fetch type matters: LAZY is the default for @OneToMany and is exactly what
// makes the naive findAll() below trigger one extra query per author.
public class Author {

    private Long id;
    private String name;
    private List<Book> books = new ArrayList<>();

    public Author() {
    }

    public Author(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Book> getBooks() {
        return books;
    }

    public void addBook(Book book) {
        books.add(book);
        book.setAuthor(this);
    }
}
