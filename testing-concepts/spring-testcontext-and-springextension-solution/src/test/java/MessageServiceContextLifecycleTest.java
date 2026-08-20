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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AppConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MessageServiceContextLifecycleTest {

    @Autowired
    private MessageService messageService;

    @Test
    @Order(1)
    @DisplayName("1: the context is built once, creationCount is 1")
    void firstTestCreatesContextOnce() {
        assertEquals(1, AppConfig.creationCount.get());

        messageService.addMessage("first");

        assertEquals(1, messageService.getHistory().size());
    }

    @Test
    @Order(2)
    @DisplayName("2: the SAME cached context/bean is reused - history leaked from test 1")
    void secondTestReusesSameCachedContext() {
        assertEquals(1, AppConfig.creationCount.get());
        assertTrue(messageService.getHistory().contains("first"));
    }

    @Test
    @Order(3)
    @DirtiesContext
    @DisplayName("3: mutate state again, then mark the context dirty for what follows")
    void thirdTestMutatesAndDirtiesTheContext() {
        messageService.addMessage("third");
    }

    @Test
    @Order(4)
    @DisplayName("4: @DirtiesContext on test 3 forced a fresh context here")
    void fourthTestGetsAFreshContext() {
        assertEquals(2, AppConfig.creationCount.get());
        assertTrue(messageService.getHistory().isEmpty());
    }

    @Test
    @Order(5)
    @DisplayName("bonus: getHistory() returns an unmodifiable view")
    void historyIsUnmodifiable() {
        assertThrows(UnsupportedOperationException.class, () -> messageService.getHistory().add("x"));
    }
}
