package com.kurz.events;

/** Published once per piece of content, inside the publishing transaction. */
public record ContentPublished(long id, String title) {
}
