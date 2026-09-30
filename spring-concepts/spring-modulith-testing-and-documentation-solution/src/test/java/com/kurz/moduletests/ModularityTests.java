package com.kurz.moduletests;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

import static org.assertj.core.api.Assertions.assertThat;

class ModularityTests {

    static final ApplicationModules modules = ApplicationModules.of(ModuleTestsApplication.class);

    @Test
    @DisplayName("the module structure is valid")
    void verifiesModularStructure() {
        modules.verify();
    }

    @Test
    @DisplayName("TODO-04: the Documenter writes PlantUML diagrams and module canvases")
    void writesDocumentation() {
        new Documenter(modules).writeDocumentation();

        Path docs = Path.of("target", "spring-modulith-docs");
        assertThat(docs.resolve("components.puml")).exists();
        assertThat(docs.resolve("module-publishing.puml")).exists();
        assertThat(docs.resolve("module-notification.adoc")).exists();
        assertThat(Files.exists(docs.resolve("all-docs.adoc"))).isTrue();
    }
}
