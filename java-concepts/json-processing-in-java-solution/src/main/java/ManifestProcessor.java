import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
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

    public record PackageManifest(String name, String version, String description, List<String> contributors) {
    }

    public static PackageManifest readManifest(String json, JsonMapper mapper) {
        return mapper.readValue(json, PackageManifest.class);
    }

    public static JsonMapper createStrictMapper() {
        return JsonMapper.builder()
                .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
    }

    public static String readHomepage(String json, JsonMapper mapper) {
        JsonNode root = mapper.readTree(json);
        return root.path("homepage").asText("unknown");
    }

    public static String readPublisherCity(String json, JsonMapper mapper) {
        JsonNode city = mapper.readTree(json).at("/publisher/city");
        return city.isMissingNode() ? "unknown" : city.asText();
    }

    public static List<PackageManifest> readManifests(String arrayJson, JsonMapper mapper) {
        return mapper.readValue(arrayJson, new TypeReference<List<PackageManifest>>() {
        });
    }
}
