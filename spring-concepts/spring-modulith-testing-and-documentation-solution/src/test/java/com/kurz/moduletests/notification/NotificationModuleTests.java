package com.kurz.moduletests.notification;

import java.util.List;

import com.kurz.moduletests.publishing.ContentCatalog;
import com.kurz.moduletests.publishing.ContentPublished;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * STANDALONE would fail to start: DigestService needs ContentCatalog, a bean
 * of the publishing module. Mocking it keeps the test to this module alone.
 */
@ApplicationModuleTest
class NotificationModuleTests {

    @MockitoBean ContentCatalog catalog;
    @Autowired DigestService digest;

    @Test
    @DisplayName("TODO-02: a ContentPublished event leads to a NotificationSent event")
    void notifiesSubscribers(Scenario scenario) {
        scenario.publish(new ContentPublished(42, "Modular monoliths"))
                .andWaitForEventOfType(NotificationSent.class)
                .matching(sent -> sent.title().equals("Modular monoliths"))
                .toArriveAndVerify(sent -> assertThat(sent.recipients()).isEqualTo(2));
    }

    @Test
    @DisplayName("TODO-03: the digest is built from the (mocked) publishing API")
    void digestUsesCatalog() {
        when(catalog.recentTitles(3)).thenReturn(List.of("C", "B", "A"));

        assertThat(digest.weeklyDigest()).isEqualTo("This week: C, B, A");
    }
}
