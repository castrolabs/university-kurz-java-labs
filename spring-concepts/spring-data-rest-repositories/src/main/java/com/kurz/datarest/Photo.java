package com.kurz.datarest;

// TODO-00: Turn Photo into a JPA entity:
//   - Add @Entity.
//   - Annotate id with @Id and @GeneratedValue(strategy = GenerationType.IDENTITY).
// Without this, PhotoRepository has no managed type to work with and the
// application context fails to start.
public class Photo {

    private Long id;
    private String caption;
    private String photographer;

    public Photo() {
    }

    public Photo(String caption, String photographer) {
        this.caption = caption;
        this.photographer = photographer;
    }

    public Long getId() {
        return id;
    }

    public String getCaption() {
        return caption;
    }

    public String getPhotographer() {
        return photographer;
    }
}
