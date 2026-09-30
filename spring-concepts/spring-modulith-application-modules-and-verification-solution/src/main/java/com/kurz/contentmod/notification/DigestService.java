package com.kurz.contentmod.notification;

import com.kurz.contentmod.publishing.ContentCatalog;
import org.springframework.stereotype.Service;

@Service
public class DigestService {

    private final ContentCatalog catalog;

    DigestService(ContentCatalog catalog) {
        this.catalog = catalog;
    }

    public String weeklyDigest() {
        return "This week: " + String.join(", ", catalog.recentTitles(3));
    }
}
