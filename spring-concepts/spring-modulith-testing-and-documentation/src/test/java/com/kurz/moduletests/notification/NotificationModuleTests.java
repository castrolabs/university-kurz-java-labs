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
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

// TODO-02 (first part): Run this class as it is. The context does not start:
// read why. Fix it WITHOUT booting the publishing module, by replacing the
// missing bean with a Mockito mock (@MockitoBean field). Then compare with
// @ApplicationModuleTest(mode = BootstrapMode.DIRECT_DEPENDENCIES).
@ApplicationModuleTest
class NotificationModuleTests {

    @Autowired DigestService digest;

    // TODO-02: With the injected Scenario, publish a ContentPublished(42,
    // "Modular monoliths"), wait for a NotificationSent event whose title
    // matches, and verify it reports 2 recipients. The listener is async:
    // Scenario does the waiting for you (no Thread.sleep).
    @Test
    @DisplayName("TODO-02: a ContentPublished event leads to a NotificationSent event")
    void notifiesSubscribers(Scenario scenario) {
        fail("TODO-02: not implemented yet");
    }

    // TODO-03: Stub the mocked catalog's recentTitles(3) to return
    // List.of("C", "B", "A") and assert weeklyDigest() is
    // "This week: C, B, A".
    @Test
    @DisplayName("TODO-03: the digest is built from the (mocked) publishing API")
    void digestUsesCatalog() {
        fail("TODO-03: not implemented yet");
    }
}
