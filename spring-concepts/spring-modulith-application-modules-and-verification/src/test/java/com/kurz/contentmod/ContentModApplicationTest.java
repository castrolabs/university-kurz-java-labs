package com.kurz.contentmod;

import com.kurz.contentmod.notification.DigestService;
import com.kurz.contentmod.notification.NotificationService;
import com.kurz.contentmod.publishing.ContentCatalog;
import com.kurz.contentmod.publishing.PublishingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;

/** The refactoring must not change behaviour: same features, new boundaries. */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ContentModApplicationTest {

    @Autowired PublishingService publishing;
    @Autowired NotificationService notifications;
    @Autowired ContentCatalog catalog;
    @Autowired DigestService digest;

    @BeforeEach
    void reset() {
        notifications.clear();
    }

    @Test
    @DisplayName("TODO-02: publishing content still notifies every subscriber")
    void publishingNotifiesSubscribers() {
        publishing.publish("Modular monoliths");

        assertThat(notifications.sent()).containsExactly(
                "ada@example.com <- Modular monoliths",
                "linus@example.com <- Modular monoliths");
    }

    @Test
    @DisplayName("TODO-01: the catalog returns the most recent titles, newest first")
    void catalogListsRecentTitles() {
        publishing.publish("First");
        publishing.publish("Second");

        assertThat(catalog.recentTitles(5)).containsExactly("Second", "First");
        assertThat(catalog.recentTitles(1)).containsExactly("Second");
    }

    @Test
    @DisplayName("TODO-01: the weekly digest lists the 3 most recent titles, newest first")
    void digestListsRecentTitles() {
        publishing.publish("One");
        publishing.publish("Two");
        publishing.publish("Three");
        publishing.publish("Four");

        assertThat(digest.weeklyDigest()).isEqualTo("This week: Four, Three, Two");
    }
}
