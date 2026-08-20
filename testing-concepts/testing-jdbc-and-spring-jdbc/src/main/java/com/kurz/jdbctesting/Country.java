package com.kurz.jdbctesting;

/**
 * A country row: the id and name columns plus a short ISO-style code.
 */
public record Country(int id, String name, String codeName) {
}
