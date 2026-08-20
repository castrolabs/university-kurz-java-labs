import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * MessageService and AppConfig are already fully implemented in src/main/java -
 * your job is to write the four tests below, IN THE ORDER GIVEN (@Order fixes the
 * execution order so the story is deterministic), that together demonstrate two
 * things about the Spring TestContext framework: it caches and reuses a context
 * across test methods with identical configuration, and @DirtiesContext is what
 * forces it to build a fresh one.
 *
 * AppConfig.creationCount only goes up when Spring actually invokes the
 * @Bean messageService() method - i.e. only when a NEW context (and therefore a
 * new MessageService) is built.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AppConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MessageServiceContextLifecycleTest {

    @Autowired
    private MessageService messageService;

    // TODO-00: Assert AppConfig.creationCount.get() == 1 (the context was just built
    // for the first time in this class). Then call messageService.addMessage("first")
    // and assert messageService.getHistory() has size 1.
    @Test
    @Order(1)
    @DisplayName("1: the context is built once, creationCount is 1")
    void firstTestCreatesContextOnce() {
        fail("TODO-00: not implemented yet");
    }

    // TODO-01: Assert AppConfig.creationCount.get() is STILL 1 (no new context was
    // built for this second test method), and that messageService.getHistory()
    // already contains "first" - the message the PREVIOUS test added. This proves the
    // exact same MessageService instance served both tests: that leakage is exactly
    // what @DirtiesContext exists to stop.
    @Test
    @Order(2)
    @DisplayName("2: the SAME cached context/bean is reused - history leaked from test 1")
    void secondTestReusesSameCachedContext() {
        fail("TODO-01: not implemented yet");
    }

    // TODO-02: Call messageService.addMessage("third"). No assertion on creationCount
    // is needed here - this test's only job is to mutate shared state one more time
    // while @DirtiesContext (already applied to this method, see below) marks the
    // context for replacement once this test finishes.
    @Test
    @Order(3)
    @DirtiesContext
    @DisplayName("3: mutate state again, then mark the context dirty for what follows")
    void thirdTestMutatesAndDirtiesTheContext() {
        fail("TODO-02: not implemented yet");
    }

    // TODO-03: Assert AppConfig.creationCount.get() == 2 (a NEW MessageService, in a
    // NEW context, was built - because test 3 above was annotated @DirtiesContext),
    // and that messageService.getHistory() is empty. If this test saw "first" or
    // "third" in the history, it would mean @DirtiesContext failed to force a fresh
    // context.
    @Test
    @Order(4)
    @DisplayName("4: @DirtiesContext on test 3 forced a fresh context here")
    void fourthTestGetsAFreshContext() {
        fail("TODO-03: not implemented yet");
    }

    // TODO-04 (optional): Add a fifth @Test, @Order(5), asserting that calling
    // messageService.getHistory().add("x") throws UnsupportedOperationException -
    // getHistory() returns an unmodifiable view, so mutating it directly (instead of
    // through addMessage(...)) must fail.
}
