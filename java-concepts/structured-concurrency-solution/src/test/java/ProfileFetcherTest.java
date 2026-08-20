import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProfileFetcher")
class ProfileFetcherTest {

    @Test
    @DisplayName("loadProfile should combine both results when both fetchers succeed")
    void loadProfileCombinesBothResultsOnSuccess() throws InterruptedException {
        ProfileFetcher fetcher = new ProfileFetcher(() -> "alice", () -> List.of("order-1", "order-2"));

        ProfileFetcher.Profile profile = fetcher.loadProfile();

        assertEquals("alice", profile.user());
        assertEquals(List.of("order-1", "order-2"), profile.orders());
    }

    @Test
    @DisplayName("loadProfile should wrap a subtask failure in ProfileUnavailableException")
    void loadProfileWrapsSubtaskFailure() {
        ProfileFetcher fetcher = new ProfileFetcher(
                () -> "alice",
                () -> {
                    throw new IllegalStateException("orders down");
                });

        ProfileFetcher.ProfileUnavailableException thrown =
                assertThrows(ProfileFetcher.ProfileUnavailableException.class, fetcher::loadProfile);

        assertInstanceOf(IllegalStateException.class, thrown.getCause());
        assertEquals("orders down", thrown.getCause().getMessage());
    }

    @Test
    @DisplayName("loadProfile should cancel the slower sibling as soon as the other one fails")
    void loadProfileCancelsSlowSiblingOnFailure() {
        AtomicBoolean userCompletedNormally = new AtomicBoolean(false);
        Callable<String> slowUser = () -> {
            Thread.sleep(2000);
            userCompletedNormally.set(true);
            return "alice";
        };
        Callable<List<String>> failingOrders = () -> {
            throw new IllegalStateException("orders down");
        };
        ProfileFetcher fetcher = new ProfileFetcher(slowUser, failingOrders);

        long start = System.nanoTime();
        assertThrows(ProfileFetcher.ProfileUnavailableException.class, fetcher::loadProfile);
        long elapsedMillis = (System.nanoTime() - start) / 1_000_000;

        assertTrue(elapsedMillis < 1000,
                "loadProfile should have returned shortly after the failure, not after the full 2s sleep "
                        + "(took " + elapsedMillis + "ms) - the slow sibling should have been cancelled");
        assertFalse(userCompletedNormally.get(),
                "the slow subtask should have been interrupted before reaching its normal completion");
    }

    @Test
    @DisplayName("loadProfileWithTimeout should return normally when both fetchers finish inside the deadline")
    void loadProfileWithTimeoutReturnsWithinDeadline() throws Exception {
        ProfileFetcher fetcher = new ProfileFetcher(() -> "alice", () -> List.of("order-1"));

        ProfileFetcher.Profile profile = fetcher.loadProfileWithTimeout(Duration.ofSeconds(2));

        assertEquals("alice", profile.user());
        assertEquals(List.of("order-1"), profile.orders());
    }

    @Test
    @DisplayName("loadProfileWithTimeout should throw StructuredTaskScope.TimeoutException and cancel both subtasks once the deadline passes")
    void loadProfileWithTimeoutThrowsAndCancelsOnDeadlineExceeded() {
        AtomicBoolean userCompletedNormally = new AtomicBoolean(false);
        AtomicBoolean ordersCompletedNormally = new AtomicBoolean(false);
        Callable<String> slowUser = () -> {
            Thread.sleep(2000);
            userCompletedNormally.set(true);
            return "alice";
        };
        Callable<List<String>> slowOrders = () -> {
            Thread.sleep(2000);
            ordersCompletedNormally.set(true);
            return List.of("order-1");
        };
        ProfileFetcher fetcher = new ProfileFetcher(slowUser, slowOrders);

        long start = System.nanoTime();
        assertThrows(StructuredTaskScope.TimeoutException.class,
                () -> fetcher.loadProfileWithTimeout(Duration.ofMillis(200)));
        long elapsedMillis = (System.nanoTime() - start) / 1_000_000;

        assertTrue(elapsedMillis < 1000,
                "loadProfileWithTimeout should have thrown shortly after its 200ms deadline, not after the full "
                        + "2s sleep (took " + elapsedMillis + "ms)");
        assertFalse(userCompletedNormally.get(), "the user subtask should have been cancelled at the deadline");
        assertFalse(ordersCompletedNormally.get(), "the orders subtask should have been cancelled at the deadline");
    }

    @Test
    @DisplayName("fetchFastest should return the first successful result and cancel the loser")
    void fetchFastestReturnsFirstSuccessAndCancelsLoser() throws InterruptedException {
        AtomicBoolean slowCompletedNormally = new AtomicBoolean(false);
        Callable<String> fast = () -> "fast-result";
        Callable<String> slow = () -> {
            Thread.sleep(2000);
            slowCompletedNormally.set(true);
            return "slow-result";
        };

        long start = System.nanoTime();
        String result = ProfileFetcher.fetchFastest(fast, slow);
        long elapsedMillis = (System.nanoTime() - start) / 1_000_000;

        assertEquals("fast-result", result);
        assertTrue(elapsedMillis < 1000,
                "fetchFastest should return as soon as the fast subtask succeeds, not wait for the slow one");
        assertFalse(slowCompletedNormally.get(), "the losing subtask should have been cancelled");
    }
}
