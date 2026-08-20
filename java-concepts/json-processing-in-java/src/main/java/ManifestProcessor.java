import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

/**
 * Reads package-manifest JSON documents shaped like:
 *
 * <pre>{@code
 * {
 *   "name": "robinparse",
 *   "version": "1.2.3",
 *   "description": "Another Parser for JSON",
 *   "contributors": ["Robin Smythe", "Jon Jenz", "Jan Ardann"],
 *   "homepage": "https://example.com/robinparse",
 *   "publisher": { "name": "Robin Smythe", "city": "Porto" }
 * }
 * }</pre>
 *
 * <p>{@code homepage} and {@code publisher} are deliberately left out of
 * {@link PackageManifest} — they're read through the tree model instead, to
 * practice navigating a shape you didn't bother to model as a class.
 */
public class ManifestProcessor {

    private ManifestProcessor() {
    }

    /**
     * A modeled subset of the manifest. Only the fields every manifest is
     * guaranteed to have are here — {@code homepage} and {@code publisher}
     * are optional/nested and read separately via the tree model.
     */
    public record PackageManifest(String name, String version, String description, List<String> contributors) {
    }

    /**
     * Data-binds {@code json} directly into a {@link PackageManifest} using
     * the given {@code mapper}.
     */
    public static PackageManifest readManifest(String json, JsonMapper mapper) {
        // TODO-00: Use mapper.readValue(json, PackageManifest.class).

        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * Builds a {@link JsonMapper} that fails fast on any JSON field that
     * isn't part of {@link PackageManifest}.
     *
     * <p>A plain {@code JsonMapper.builder().build()} already tolerates
     * unmodeled fields — see the "toleratesUnknownFieldByDefault" test.
     * This method is for the opposite situation: an internal API where an
     * unexpected field is itself the bug you want caught immediately,
     * instead of silently ignored.
     */
    public static JsonMapper createStrictMapper() {
        // TODO-01: Build a JsonMapper via JsonMapper.builder() that ENABLES
        // DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES.

        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * Reads the "homepage" field via the tree model, defaulting to
     * "unknown" when the field is absent — never throwing for a missing
     * optional field.
     */
    public static String readHomepage(String json, JsonMapper mapper) {
        // TODO-02: mapper.readTree(json) to get the root JsonNode, then use
        // path("homepage") — NOT get("homepage") — so a missing field
        // yields a MissingNode instead of null. Call asText("unknown") on
        // the result to supply the default in one step.

        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * Reads the nested "publisher.city" field using a JSON Pointer,
     * returning "unknown" when either "publisher" or "city" is missing.
     */
    public static String readPublisherCity(String json, JsonMapper mapper) {
        // TODO-03: mapper.readTree(json).at("/publisher/city") to navigate
        // straight to the nested field with one JSON Pointer instead of two
        // chained get()/path() calls. Check isMissingNode() and return
        // "unknown" if so; otherwise return asText().

        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * Data-binds a JSON array of manifests into a {@code List<PackageManifest>}.
     */
    public static List<PackageManifest> readManifests(String arrayJson, JsonMapper mapper) {
        // TODO-04 (optional): A plain Class literal can't describe a generic
        // List<PackageManifest> — erasure throws the element type away, and
        // mapper.readValue(arrayJson, List.class) would hand back a List of
        // raw LinkedHashMaps that blow up with a ClassCastException on first
        // use. Use mapper.readValue(arrayJson, new TypeReference<List<PackageManifest>>() {})
        // instead, which preserves the element type.

        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
