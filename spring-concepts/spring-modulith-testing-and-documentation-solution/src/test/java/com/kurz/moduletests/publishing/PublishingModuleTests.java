package com.kurz.moduletests.publishing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.AssertablePublishedEvents;

import static org.assertj.core.api.Assertions.assertThat;

/** Lives in the module's package: @ApplicationModuleTest bootstraps THIS module only. */
@ApplicationModuleTest
class PublishingModuleTests {

    @Autowired PublishingService publishing;
    @Autowired ApplicationContext context;

    @Test
    @DisplayName("TODO-00: publishing announces the new content with a ContentPublished event")
    void publishesEvent(AssertablePublishedEvents events) {
        publishing.publish("Modular monoliths");

        assertThat(events)
                .contains(ContentPublished.class)
                .matching(ContentPublished::title, "Modular monoliths");
    }

    @Test
    @DisplayName("TODO-01: in STANDALONE mode no bean of the notification module is started")
    void bootstrapsOnlyThisModule() {
        assertThat(context.getBeanNamesForType(PublishingService.class)).hasSize(1);
        assertThat(context.containsBean("digestService")).isFalse();
        assertThat(context.containsBean("notificationListener")).isFalse();
    }
}
