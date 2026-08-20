import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RequestContext")
class RequestContextTest {

    @Test
    @DisplayName("currentUser should throw when nothing is bound")
    void currentUserThrowsWhenUnbound() {
        assertThrows(NoSuchElementException.class, RequestContext::currentUser);
    }

    @Test
    @DisplayName("hasCurrentUser should be false when unbound and true while bound")
    void hasCurrentUserReflectsBindingState() {
        assertFalse(RequestContext.hasCurrentUser());

        RequestContext.runAs("alice", () -> assertTrue(RequestContext.hasCurrentUser()));

        assertFalse(RequestContext.hasCurrentUser());
    }

    @Test
    @DisplayName("currentUserOrDefault should return the fallback when unbound, and the bound value when bound")
    void currentUserOrDefaultUsesFallbackOnlyWhenUnbound() {
        assertEquals("guest", RequestContext.currentUserOrDefault("guest"));

        RequestContext.runAs("alice", () -> assertEquals("alice", RequestContext.currentUserOrDefault("guest")));
    }

    @Test
    @DisplayName("runAs(Callable) should bind for the call's duration, return its result, and unbind afterwards")
    void runAsCallableBindsForDurationAndReturnsResult() throws Exception {
        String result = RequestContext.runAs("alice", RequestContext::currentUser);

        assertEquals("alice", result);
        assertFalse(RequestContext.hasCurrentUser(), "binding should not survive past runAs() returning");
    }

    @Test
    @DisplayName("runAs(Runnable) should bind for the run's duration and unbind afterwards")
    void runAsRunnableBindsForDuration() {
        AtomicReference<String> seenInsideRun = new AtomicReference<>();

        RequestContext.runAs("bob", () -> seenInsideRun.set(RequestContext.currentUser()));

        assertEquals("bob", seenInsideRun.get());
        assertFalse(RequestContext.hasCurrentUser());
    }

    @Test
    @DisplayName("a nested runAs should shadow the outer binding, then restore it once the nested call returns")
    void nestedRunAsShadowsThenRestores() {
        RequestContext.runAs("alice", () -> {
            assertEquals("alice", RequestContext.currentUser());

            RequestContext.runAs("admin", () -> assertEquals("admin", RequestContext.currentUser()));

            assertEquals("alice", RequestContext.currentUser());
        });
    }

    @Test
    @DisplayName("two concurrent threads should never observe each other's binding")
    void concurrentThreadsNeverSeeEachOthersBinding() throws InterruptedException {
        int iterationsPerThread = 500;
        CopyOnWriteArrayList<String> mismatches = new CopyOnWriteArrayList<>();
        CountDownLatch done = new CountDownLatch(2);

        Runnable checkAlice = () -> {
            for (int i = 0; i < iterationsPerThread; i++) {
                if (!"alice".equals(RequestContext.currentUser())) {
                    mismatches.add("expected alice, saw " + RequestContext.currentUser());
                }
                Thread.onSpinWait();
            }
        };
        Runnable checkBob = () -> {
            for (int i = 0; i < iterationsPerThread; i++) {
                if (!"bob".equals(RequestContext.currentUser())) {
                    mismatches.add("expected bob, saw " + RequestContext.currentUser());
                }
                Thread.onSpinWait();
            }
        };

        CopyOnWriteArrayList<Throwable> failures = new CopyOnWriteArrayList<>();
        new Thread(() -> {
            try {
                RequestContext.runAs("alice", checkAlice);
            } catch (Throwable t) {
                failures.add(t);
            } finally {
                done.countDown();
            }
        }).start();
        new Thread(() -> {
            try {
                RequestContext.runAs("bob", checkBob);
            } catch (Throwable t) {
                failures.add(t);
            } finally {
                done.countDown();
            }
        }).start();

        done.await(5, TimeUnit.SECONDS);
        if (!failures.isEmpty()) {
            fail(failures.get(0));
        }
        assertTrue(mismatches.isEmpty(), "threads observed each other's binding: " + mismatches);
    }

    @Test
    @DisplayName("a plain child thread started inside runAs does NOT inherit the binding - only structured-concurrency forking does")
    void plainChildThreadDoesNotInheritBinding() throws InterruptedException {
        AtomicReference<Throwable> seenInChild = new AtomicReference<>();

        RequestContext.runAs("alice", () -> {
            Thread child = new Thread(() -> {
                try {
                    RequestContext.currentUser();
                } catch (NoSuchElementException e) {
                    seenInChild.set(e);
                }
            });
            child.start();
            try {
                child.join(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        assertNotNull(seenInChild.get(),
                "a thread you start yourself runs outside the binding's extent - the value does not follow it "
                        + "automatically, unlike a StructuredTaskScope subtask");
    }

    @Test
    @DisplayName("runAs(user, locale) should bind both values for the run's duration")
    void runAsUserAndLocaleBindsBothForDuration() {
        AtomicReference<String> seenUser = new AtomicReference<>();
        AtomicReference<String> seenLocale = new AtomicReference<>();

        RequestContext.runAs("alice", "pt-PT", () -> {
            seenUser.set(RequestContext.currentUser());
            seenLocale.set(RequestContext.currentLocale());
        });

        assertEquals("alice", seenUser.get());
        assertEquals("pt-PT", seenLocale.get());
        assertFalse(RequestContext.hasCurrentUser());
        assertEquals("en-US", RequestContext.currentLocale(), "locale should fall back once the binding ends");
    }
}
