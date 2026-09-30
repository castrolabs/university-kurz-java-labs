package com.kurz.contentmod;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.ApplicationModule;
import org.springframework.modulith.core.ApplicationModules;

import static org.assertj.core.api.Assertions.assertThat;

/** Pure architecture checks: no Spring context is started here. */
class ModularityTest {

    static final ApplicationModules modules = ApplicationModules.of(ContentModApplication.class);

    @Test
    @DisplayName("Both packages are detected as application modules")
    void detectsModules() {
        assertThat(modules.stream().map(m -> m.getIdentifier().toString()))
                .containsExactlyInAnyOrder("notification", "publishing");
    }

    @Test
    @DisplayName("TODO-00: ContentRepository is internal to publishing, not part of its API")
    void repositoryIsInternal() {
        var publishing = modules.getModuleByName("publishing").orElseThrow();
        var repository = publishing.getType("ContentRepository").orElseThrow();

        assertThat(repository.getPackageName()).endsWith(".publishing.internal");
        assertThat(publishing.isExposed(repository)).isFalse();
    }

    @Test
    @DisplayName("TODO-02: publishing no longer depends on notification (the cycle is gone)")
    void publishingDoesNotDependOnNotification() {
        var publishing = modules.getModuleByName("publishing").orElseThrow();

        assertThat(publishing.getDirectDependencies(modules).containsModuleNamed("notification"))
                .isFalse();
    }

    @Test
    @DisplayName("TODO-03: notification declares its allowed dependencies explicitly")
    void notificationDeclaresAllowedDependencies() {
        var annotation = com.kurz.contentmod.notification.NotificationService.class
                .getPackage().getAnnotation(ApplicationModule.class);

        assertThat(annotation).as("@ApplicationModule on notification/package-info.java").isNotNull();
        assertThat(annotation.allowedDependencies()).containsExactly("publishing");
    }

    @Test
    @DisplayName("verify(): no cycles, no access to internals, only allowed dependencies")
    void verifiesModularStructure() {
        modules.verify();
    }
}
