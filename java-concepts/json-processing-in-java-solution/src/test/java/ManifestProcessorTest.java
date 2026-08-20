import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.exc.UnrecognizedPropertyException;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ManifestProcessor")
class ManifestProcessorTest {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    private static final String MANIFEST_JSON = """
            {
              "name": "robinparse",
              "version": "1.2.3",
              "description": "Another Parser for JSON",
              "contributors": ["Robin Smythe", "Jon Jenz", "Jan Ardann"]
            }
            """;

    private static final String MANIFEST_JSON_WITH_UNKNOWN_FIELD = """
            {
              "name": "robinparse",
              "version": "1.2.3",
              "description": "Another Parser for JSON",
              "contributors": ["Robin Smythe", "Jon Jenz", "Jan Ardann"],
              "engines": { "node": ">=18" }
            }
            """;

    private static final String MANIFEST_JSON_MISSING_DESCRIPTION = """
            {
              "name": "robinparse",
              "version": "1.2.3",
              "contributors": ["Robin Smythe"]
            }
            """;

    private static final String MANIFEST_JSON_WITH_HOMEPAGE_AND_PUBLISHER = """
            {
              "name": "robinparse",
              "version": "1.2.3",
              "description": "Another Parser for JSON",
              "contributors": ["Robin Smythe"],
              "homepage": "https://example.com/robinparse",
              "publisher": { "name": "Robin Smythe", "city": "Porto" }
            }
            """;

    private static final String MANIFEST_ARRAY_JSON = """
            [
              { "name": "robinparse", "version": "1.2.3", "description": "d1", "contributors": ["Robin Smythe"] },
              { "name": "otherlib", "version": "0.9.0", "description": "d2", "contributors": [] }
            ]
            """;

    @Test
    @DisplayName("should data-bind a well-formed manifest into a PackageManifest")
    void shouldDataBindManifest() {
        ManifestProcessor.PackageManifest manifest = ManifestProcessor.readManifest(MANIFEST_JSON, MAPPER);

        assertEquals("robinparse", manifest.name());
        assertEquals("1.2.3", manifest.version());
        assertEquals("Another Parser for JSON", manifest.description());
        assertEquals(List.of("Robin Smythe", "Jon Jenz", "Jan Ardann"), manifest.contributors());
    }

    @Test
    @DisplayName("a plain JsonMapper tolerates an unmodeled field by default (schema evolution)")
    void shouldToleratesUnknownFieldByDefault() {
        ManifestProcessor.PackageManifest manifest =
                assertDoesNotThrow(() -> ManifestProcessor.readManifest(MANIFEST_JSON_WITH_UNKNOWN_FIELD, MAPPER));

        assertEquals("robinparse", manifest.name());
    }

    @Test
    @DisplayName("should not break when an optional field is missing from the JSON (schema evolution)")
    void shouldTolerateMissingOptionalField() {
        ManifestProcessor.PackageManifest manifest =
                ManifestProcessor.readManifest(MANIFEST_JSON_MISSING_DESCRIPTION, MAPPER);

        assertEquals("robinparse", manifest.name());
        assertNull(manifest.description());
    }

    @Test
    @DisplayName("createStrictMapper() should reject an unmodeled field instead of tolerating it")
    void strictMapperRejectsUnknownField() {
        JsonMapper strict = ManifestProcessor.createStrictMapper();

        assertThrows(UnrecognizedPropertyException.class,
                () -> ManifestProcessor.readManifest(MANIFEST_JSON_WITH_UNKNOWN_FIELD, strict));
    }

    @Test
    @DisplayName("createStrictMapper() should still bind a manifest with no unmodeled fields")
    void strictMapperStillBindsCleanManifest() {
        JsonMapper strict = ManifestProcessor.createStrictMapper();

        ManifestProcessor.PackageManifest manifest = ManifestProcessor.readManifest(MANIFEST_JSON, strict);

        assertEquals("robinparse", manifest.name());
    }

    @Test
    @DisplayName("should read homepage via the tree model when present")
    void shouldReadHomepageWhenPresent() {
        assertEquals("https://example.com/robinparse",
                ManifestProcessor.readHomepage(MANIFEST_JSON_WITH_HOMEPAGE_AND_PUBLISHER, MAPPER));
    }

    @Test
    @DisplayName("should default to \"unknown\" instead of throwing when homepage is absent")
    void shouldDefaultHomepageWhenAbsent() {
        assertEquals("unknown", ManifestProcessor.readHomepage(MANIFEST_JSON, MAPPER));
    }

    @Test
    @DisplayName("should read a nested publisher.city field via JSON Pointer")
    void shouldReadPublisherCityWhenPresent() {
        assertEquals("Porto",
                ManifestProcessor.readPublisherCity(MANIFEST_JSON_WITH_HOMEPAGE_AND_PUBLISHER, MAPPER));
    }

    @Test
    @DisplayName("should default to \"unknown\" when the whole publisher object is missing")
    void shouldDefaultPublisherCityWhenPublisherMissing() {
        assertEquals("unknown", ManifestProcessor.readPublisherCity(MANIFEST_JSON, MAPPER));
    }

    @Test
    @DisplayName("should default to \"unknown\" when publisher exists but city doesn't")
    void shouldDefaultPublisherCityWhenCityMissing() {
        String json = """
                { "name": "robinparse", "version": "1.2.3", "contributors": [],
                  "publisher": { "name": "Robin Smythe" } }
                """;

        assertEquals("unknown", ManifestProcessor.readPublisherCity(json, MAPPER));
    }

    @Test
    @DisplayName("should data-bind a JSON array into a List<PackageManifest> (bonus)")
    void shouldDataBindManifestArray() {
        List<ManifestProcessor.PackageManifest> manifests =
                ManifestProcessor.readManifests(MANIFEST_ARRAY_JSON, MAPPER);

        assertEquals(2, manifests.size());
        assertEquals("robinparse", manifests.get(0).name());
        assertEquals("otherlib", manifests.get(1).name());
        assertTrue(manifests.get(1).contributors().isEmpty());
    }
}
