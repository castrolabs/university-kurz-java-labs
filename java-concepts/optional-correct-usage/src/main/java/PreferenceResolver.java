import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;

public class PreferenceResolver {

    public record Address(String zip) {}

    public Optional<String> normalize(String rawNickname) {
        // TODO-00: Wrap `rawNickname` in an Optional, treating null as absent.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public String displayName(Optional<String> nickname) {
        // TODO-01: Return the nickname if present, otherwise the literal "Guest".
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public String resolveTheme(Optional<String> storedTheme, Supplier<String> defaultThemeLoader) {
        // TODO-02: Return `storedTheme` if present. Otherwise, return whatever
        // `defaultThemeLoader` produces. `defaultThemeLoader` must NOT be called
        // at all when `storedTheme` is present.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public String zipOf(Optional<Address> address) {
        // TODO-03: Return the address's zip code, or "UNKNOWN" if the address
        // is absent.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public List<String> presentNicknames(List<Optional<String>> nicknames) {
        // TODO-04: Collect only the present values, in their original order,
        // into a List<String>.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public String requireNickname(Optional<String> nickname) {
        // TODO-05 (optional): Return the nickname if present, otherwise throw a
        // NoSuchElementException with the message "nickname required".
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
