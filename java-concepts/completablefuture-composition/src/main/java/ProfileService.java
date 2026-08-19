import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;

public class ProfileService {

    public static final Profile DEFAULT_PROFILE = new Profile(-1, "Unknown");

    private final Map<String, Long> userIdsByUsername;
    private final Map<Long, Profile> profilesById;

    public ProfileService(Map<String, Long> userIdsByUsername, Map<Long, Profile> profilesById) {
        this.userIdsByUsername = userIdsByUsername;
        this.profilesById = profilesById;
    }

    public CompletableFuture<Long> findUserId(String username) {
        return CompletableFuture.supplyAsync(() -> {
            Long id = userIdsByUsername.get(username);
            if (id == null) {
                throw new NoSuchElementException("No such user: " + username);
            }
            return id;
        });
    }

    public CompletableFuture<Profile> loadProfile(long userId) {
        return CompletableFuture.supplyAsync(() -> {
            Profile profile = profilesById.get(userId);
            if (profile == null) {
                throw new NoSuchElementException("No profile for id: " + userId);
            }
            return profile;
        });
    }

    public CompletableFuture<Profile> lookupProfile(String username) {
        // TODO-00: Chain findUserId(username) into loadProfile(id) so the
        // second lookup only starts once the first one's id is known. The
        // method must return CompletableFuture<Profile>, not a future of a
        // future - pick the composition method that flattens instead of
        // nesting.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public CompletableFuture<Profile> lookupProfileOrDefault(String username) {
        // TODO-01: Reuse lookupProfile(username), but recover from any
        // failure (unknown username or dangling id) by falling back to
        // DEFAULT_PROFILE instead of letting the exception propagate.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public CompletableFuture<String> describeLookup(String username) {
        // TODO-02: Reuse lookupProfile(username) and produce a description
        // that covers both outcomes: "found: <displayName>" on success, or
        // "failed: <message>" on failure. Unwrap the CompletionException the
        // pipeline hands you so <message> is the original failure's message,
        // not the wrapper's.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public CompletableFuture<String> combinedGreeting(String usernameA, String usernameB) {
        // TODO-03 (optional): Look up both usernames concurrently - neither
        // lookup depends on the other's result - and combine the two
        // profiles into a single "<name> & <name>" string once both finish.
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
