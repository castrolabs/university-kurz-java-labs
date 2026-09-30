package com.kurz.moduletests.notification;

import com.kurz.moduletests.publishing.ContentCatalog;
import org.springframework.stereotype.Service;

/** Needs a bean from ANOTHER module: this is what makes module tests interesting. */
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
