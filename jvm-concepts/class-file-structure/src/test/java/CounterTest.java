import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Counter")
class CounterTest {

    @Test
    @DisplayName("instance fields are independent per object, but the static field is shared")
    void instanceFieldsAreIndependentWhileStaticFieldIsShared() {
        var a = new Counter(10);
        assertEquals(10, a.value);
        assertEquals(10, Counter.valueInstance);

        var b = new Counter(12);
        assertEquals(10, a.value);
        assertEquals(12, b.value);
        assertEquals(12, Counter.valueInstance);
    }
}
