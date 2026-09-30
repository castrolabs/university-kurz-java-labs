package com.kurz.moduletests.publishing;

import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

@Service
public class ContentCatalog {

    private final JdbcClient jdbc;

    ContentCatalog(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public List<String> recentTitles(int limit) {
        return jdbc.sql("SELECT title FROM content ORDER BY id DESC LIMIT :limit")
                .param("limit", limit)
                .query(String.class)
                .list();
    }
}
