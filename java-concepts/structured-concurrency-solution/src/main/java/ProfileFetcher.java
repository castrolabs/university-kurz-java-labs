import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Joiner;
import java.util.concurrent.StructuredTaskScope.Subtask;

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
        try (var scope = StructuredTaskScope.open()) {
            Subtask<String> user = scope.fork(userFetcher);
            Subtask<List<String>> orders = scope.fork(ordersFetcher);

            scope.join();
            return new Profile(user.get(), orders.get());
        } catch (StructuredTaskScope.FailedException e) {
            throw new ProfileUnavailableException(e.getCause());
        }
    }

    public Profile loadProfileWithTimeout(Duration timeout) throws InterruptedException {
        try (var scope = StructuredTaskScope.open(
                Joiner.<Object>awaitAllSuccessfulOrThrow(),
                cf -> cf.withTimeout(timeout))) {
            Subtask<String> user = scope.fork(userFetcher);
            Subtask<List<String>> orders = scope.fork(ordersFetcher);

            scope.join();
            return new Profile(user.get(), orders.get());
        } catch (StructuredTaskScope.FailedException e) {
            throw new ProfileUnavailableException(e.getCause());
        }
    }

    public static String fetchFastest(Callable<String> a, Callable<String> b) throws InterruptedException {
        try (var scope = StructuredTaskScope.open(Joiner.<String>anySuccessfulResultOrThrow())) {
            scope.fork(a);
            scope.fork(b);
            return scope.join();
        } catch (StructuredTaskScope.FailedException e) {
            throw new ProfileUnavailableException(e.getCause());
        }
    }
}
