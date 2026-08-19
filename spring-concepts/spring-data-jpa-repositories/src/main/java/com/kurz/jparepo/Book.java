package com.kurz.jparepo;

// TODO-01: Turn Book into a JPA entity:
//   - Add @Entity.
//   - Annotate id with @Id and @GeneratedValue(strategy = GenerationType.IDENTITY).
//   - Annotate author with @ManyToOne and @JoinColumn(name = "author_id").
public class Book {

    private Long id;
    private String title;
    private Author author;

    public Book() {
    }

    public Book(String title) {
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }
}
