import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;

public class PreferenceResolver {

    public record Address(String zip) {}

    public Optional<String> normalize(String rawNickname) {
        return Optional.ofNullable(rawNickname);
    }

    public String displayName(Optional<String> nickname) {
        return nickname.orElse("Guest");
    }

    public String resolveTheme(Optional<String> storedTheme, Supplier<String> defaultThemeLoader) {
        return storedTheme.orElseGet(defaultThemeLoader);
    }

    public String zipOf(Optional<Address> address) {
        return address.map(Address::zip).orElse("UNKNOWN");
    }

    public List<String> presentNicknames(List<Optional<String>> nicknames) {
        return nicknames.stream()
                .flatMap(Optional::stream)
                .toList();
    }

    public String requireNickname(Optional<String> nickname) {
        return nickname.orElseThrow(() -> new NoSuchElementException("nickname required"));
    }
}
