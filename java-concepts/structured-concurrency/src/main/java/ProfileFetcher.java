import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Joiner;
import java.util.concurrent.StructuredTaskScope.Subtask;

/**
 * Fans out two independent lookups concurrently using StructuredTaskScope,
 * so both the "wait for everything" case and the "one side fails, cancel
 * the other" case are structural instead of hand-written bookkeeping.
 */
public class ProfileFetcher {

    public record Profile(String user, List<String> orders) {
    }

    public static class ProfileUnavailableException extends RuntimeException {
        public ProfileUnavailableException(Throwable cause) {
            super(cause);
        }
    }

    private final Callable<String> userFetcher;
    private final Callable<List<String>> ordersFetcher;

    public ProfileFetcher(Callable<String> userFetcher, Callable<List<String>> ordersFetcher) {
        this.userFetcher = userFetcher;
        this.ordersFetcher = ordersFetcher;
    }

    public Profile loadProfile() throws InterruptedException {
        // TODO-00: Fork userFetcher and ordersFetcher inside a
        // StructuredTaskScope.open() (the default Joiner: wait for all,
        // cancel the rest on the first failure), then join() and combine
        // both Subtask results into a Profile. If join() throws
        // StructuredTaskScope.FailedException, catch it and rethrow as
        // new ProfileUnavailableException(e.getCause()) instead of letting
        // FailedException itself escape.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public Profile loadProfileWithTimeout(Duration timeout) throws InterruptedException {
        // TODO-01: Same fan-out as loadProfile(), but open the scope with
        // Joiner.<Object>awaitAllSuccessfulOrThrow() and a config function
        // that calls cf.withTimeout(timeout), so the whole operation - not
        // each individual call - is bounded by one deadline. join() throws
        // an unchecked StructuredTaskScope.TimeoutException if the deadline
        // passes before both subtasks finish; let that propagate to the
        // caller unmodified.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public static String fetchFastest(Callable<String> a, Callable<String> b) throws InterruptedException {
        // TODO-02 (optional): Open a scope with
        // Joiner.<String>anySuccessfulResultOrThrow(), fork both `a` and
        // `b`, and return scope.join() directly - it hands back whichever
        // subtask succeeds first, and the slower one is cancelled
        // automatically.
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
