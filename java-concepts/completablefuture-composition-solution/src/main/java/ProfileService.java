import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

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
        return findUserId(username).thenCompose(this::loadProfile);
    }

    public CompletableFuture<Profile> lookupProfileOrDefault(String username) {
        return lookupProfile(username).exceptionally(ex -> DEFAULT_PROFILE);
    }

    public CompletableFuture<String> describeLookup(String username) {
        return lookupProfile(username).handle((profile, ex) -> {
            if (ex == null) {
                return "found: " + profile.displayName();
            }
            Throwable cause = (ex instanceof CompletionException completionException)
                    ? completionException.getCause()
                    : ex;
            return "failed: " + cause.getMessage();
        });
    }

    public CompletableFuture<String> combinedGreeting(String usernameA, String usernameB) {
        CompletableFuture<Profile> profileA = lookupProfile(usernameA);
        CompletableFuture<Profile> profileB = lookupProfile(usernameB);
        return profileA.thenCombine(profileB, (a, b) -> a.displayName() + " & " + b.displayName());
    }
}
