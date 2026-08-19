import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PreferenceResolver")
class PreferenceResolverTest {

    private final PreferenceResolver resolver = new PreferenceResolver();

    @Test
    @DisplayName("should wrap a non-null nickname as present")
    void shouldWrapNonNullNicknameAsPresent() {
        Optional<String> result = resolver.normalize("ace");

        assertTrue(result.isPresent());
        assertEquals("ace", result.get());
    }

    @Test
    @DisplayName("should wrap a null nickname as empty")
    void shouldWrapNullNicknameAsEmpty() {
        Optional<String> result = resolver.normalize(null);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("should return the nickname when present")
    void shouldReturnTheNicknameWhenPresent() {
        assertEquals("ace", resolver.displayName(Optional.of("ace")));
    }

    @Test
    @DisplayName("should return Guest when the nickname is absent")
    void shouldReturnGuestWhenTheNicknameIsAbsent() {
        assertEquals("Guest", resolver.displayName(Optional.empty()));
    }

    @Test
    @DisplayName("should not invoke the default theme loader when a theme is already stored")
    void shouldNotInvokeDefaultThemeLoaderWhenThemeIsPresent() {
        AtomicInteger invocations = new AtomicInteger();
        Supplier<String> defaultThemeLoader = () -> {
            invocations.incrementAndGet();
            return "dark";
        };

        String theme = resolver.resolveTheme(Optional.of("light"), defaultThemeLoader);

        assertEquals("light", theme);
        assertEquals(0, invocations.get(),
                "the supplier must not run when the Optional is already present");
    }

    @Test
    @DisplayName("should invoke the default theme loader only when no theme is stored")
    void shouldInvokeDefaultThemeLoaderWhenThemeIsAbsent() {
        AtomicInteger invocations = new AtomicInteger();
        Supplier<String> defaultThemeLoader = () -> {
            invocations.incrementAndGet();
            return "dark";
        };

        String theme = resolver.resolveTheme(Optional.empty(), defaultThemeLoader);

        assertEquals("dark", theme);
        assertEquals(1, invocations.get());
    }

    @Test
    @DisplayName("should return the zip code of a present address")
    void shouldReturnTheZipCodeOfAPresentAddress() {
        Optional<PreferenceResolver.Address> address = Optional.of(new PreferenceResolver.Address("12345"));

        assertEquals("12345", resolver.zipOf(address));
    }

    @Test
    @DisplayName("should return UNKNOWN when the address is absent")
    void shouldReturnUnknownWhenTheAddressIsAbsent() {
        assertEquals("UNKNOWN", resolver.zipOf(Optional.empty()));
    }

    @Test
    @DisplayName("should collect only the present nicknames, in order")
    void shouldCollectOnlyThePresentNicknamesInOrder() {
        List<Optional<String>> nicknames = List.of(
                Optional.of("ace"), Optional.empty(), Optional.of("bee"), Optional.empty());

        assertEquals(List.of("ace", "bee"), resolver.presentNicknames(nicknames));
    }

    @Test
    @DisplayName("should return an empty list when every nickname is absent")
    void shouldReturnAnEmptyListWhenEveryNicknameIsAbsent() {
        List<Optional<String>> nicknames = List.of(Optional.empty(), Optional.empty());

        assertTrue(resolver.presentNicknames(nicknames).isEmpty());
    }

    @Test
    @DisplayName("should return the nickname when present (optional)")
    void shouldReturnTheNicknameWhenPresentOptional() {
        assertEquals("ace", resolver.requireNickname(Optional.of("ace")));
    }

    @Test
    @DisplayName("should throw NoSuchElementException when the nickname is absent (optional)")
    void shouldThrowWhenTheNicknameIsAbsentOptional() {
        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> resolver.requireNickname(Optional.empty()));

        assertEquals("nickname required", exception.getMessage());
    }
}
