package com.kurz.contentmod.publishing;

/** Part of the publishing module's API: other modules may listen to it. */
public record ContentPublished(Content content) {
}
