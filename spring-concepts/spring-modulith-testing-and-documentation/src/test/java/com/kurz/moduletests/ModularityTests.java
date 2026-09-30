package com.kurz.moduletests;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

class ModularityTests {

    static final ApplicationModules modules = ApplicationModules.of(ModuleTestsApplication.class);

    @Test
    @DisplayName("the module structure is valid")
    void verifiesModularStructure() {
        modules.verify();
    }

    // TODO-04: Generate the documentation with new Documenter(modules) and
    // writeDocumentation(). Run it once, look inside target/spring-modulith-docs,
    // then assert that the overview diagram (components.puml), one diagram
    // per module (module-<name>.puml), one canvas per module
    // (module-<name>.adoc) and the aggregating all-docs.adoc were written.
    @Test
    @DisplayName("TODO-04: the Documenter writes PlantUML diagrams and module canvases")
    void writesDocumentation() {
        fail("TODO-04: not implemented yet");
    }
}
