package com.kurz.events;

/** Published by DraftService, which deliberately runs WITHOUT a transaction. */
public record DraftSaved(String title) {
}
