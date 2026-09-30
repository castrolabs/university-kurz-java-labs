package com.kurz.moduletests.publishing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.AssertablePublishedEvents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

/** Lives in the module's package: @ApplicationModuleTest bootstraps THIS module only. */
@ApplicationModuleTest
class PublishingModuleTests {

    @Autowired PublishingService publishing;
    @Autowired ApplicationContext context;

    // TODO-00: Call publishing.publish("Modular monoliths"), then assert
    // with the injected AssertablePublishedEvents that a ContentPublished
    // with that title was published:
    // assertThat(events).contains(...).matching(ContentPublished::title, ...)
    @Test
    @DisplayName("TODO-00: publishing announces the new content with a ContentPublished event")
    void publishesEvent(AssertablePublishedEvents events) {
        fail("TODO-00: not implemented yet");
    }

    // TODO-01: Prove that this is a module slice, not the whole application:
    // the context has exactly one PublishingService, and does NOT contain the
    // notification module's beans "digestService" and "notificationListener".
    @Test
    @DisplayName("TODO-01: in STANDALONE mode no bean of the notification module is started")
    void bootstrapsOnlyThisModule() {
        fail("TODO-01: not implemented yet");
    }
}
