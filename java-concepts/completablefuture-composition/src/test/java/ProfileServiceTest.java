import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("ProfileService")
class ProfileServiceTest {

    private ProfileService service;

    @BeforeEach
    void setUp() {
        Map<String, Long> userIds = Map.of(
                "alice", 1L,
                "bob", 2L,
                "dave", 4L
        );
        Map<Long, Profile> profiles = Map.of(
                1L, new Profile(1L, "Alice"),
                4L, new Profile(4L, "Dave")
                // no entry for id 2: "bob" resolves to an id with no profile behind it
        );
        service = new ProfileService(userIds, profiles);
    }

    @Test
    @DisplayName("should flatten the id lookup and the profile lookup into a single result")
    void shouldChainDependentLookupsIntoAFlattenedResult() {
        Profile profile = service.lookupProfile("alice").join();

        assertEquals(new Profile(1L, "Alice"), profile);
    }

    @Test
    @DisplayName("should fail when the username has no known id")
    void shouldFailLookupWhenUsernameIsUnknown() {
        CompletableFuture<Profile> lookup = service.lookupProfile("carol");

        CompletionException thrown = assertThrows(CompletionException.class, lookup::join);
        assertInstanceOf(NoSuchElementException.class, thrown.getCause());
    }

    @Test
    @DisplayName("should fail when the resolved id has no profile behind it")
    void shouldFailLookupWhenProfileIsMissing() {
        CompletableFuture<Profile> lookup = service.lookupProfile("bob");

        CompletionException thrown = assertThrows(CompletionException.class, lookup::join);
        assertInstanceOf(NoSuchElementException.class, thrown.getCause());
    }

    @Test
    @DisplayName("should return the real profile when the lookup succeeds")
    void shouldReturnRealProfileWhenLookupSucceeds() {
        Profile profile = service.lookupProfileOrDefault("dave").join();

        assertEquals(new Profile(4L, "Dave"), profile);
    }

    @Test
    @DisplayName("should fall back to the default profile when the username is unknown")
    void shouldReturnDefaultProfileWhenUsernameIsUnknown() {
        Profile profile = service.lookupProfileOrDefault("carol").join();

        assertEquals(ProfileService.DEFAULT_PROFILE, profile);
    }

    @Test
    @DisplayName("should fall back to the default profile when the id has no profile")
    void shouldReturnDefaultProfileWhenProfileIsMissing() {
        Profile profile = service.lookupProfileOrDefault("bob").join();

        assertEquals(ProfileService.DEFAULT_PROFILE, profile);
    }

    @Test
    @DisplayName("should describe a successful lookup with the profile's display name")
    void shouldDescribeSuccessfulLookup() {
        String description = service.describeLookup("alice").join();

        assertEquals("found: Alice", description);
    }

    @Test
    @DisplayName("should describe a failed lookup with the unwrapped failure message")
    void shouldDescribeFailedLookupWithUnwrappedMessage() {
        String description = service.describeLookup("carol").join();

        assertEquals("failed: No such user: carol", description);
    }

    @Test
    @DisplayName("should combine two independent lookups into a single greeting")
    void shouldCombineTwoIndependentLookups() {
        String greeting = service.combinedGreeting("alice", "dave").join();

        assertEquals("Alice & Dave", greeting);
    }
}
