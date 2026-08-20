package com.kurz.datarest;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
