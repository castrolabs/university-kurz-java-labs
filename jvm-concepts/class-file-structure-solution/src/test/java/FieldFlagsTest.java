import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("FieldFlags")
class FieldFlagsTest {

    @Test
    @DisplayName("value carries ACC_PUBLIC")
    void valueIsPublic() throws NoSuchFieldException {
        assertTrue(FieldFlags.isPublic(Counter.class, "value"));
    }

    @Test
    @DisplayName("value does not carry ACC_STATIC")
    void valueIsNotStatic() throws NoSuchFieldException {
        assertFalse(FieldFlags.isStatic(Counter.class, "value"));
    }

    @Test
    @DisplayName("valueInstance carries ACC_STATIC")
    void valueInstanceIsStatic() throws NoSuchFieldException {
        assertTrue(FieldFlags.isStatic(Counter.class, "valueInstance"));
    }

    @Test
    @DisplayName("valueInstance does not carry ACC_PUBLIC")
    void valueInstanceIsNotPublic() throws NoSuchFieldException {
        assertFalse(FieldFlags.isPublic(Counter.class, "valueInstance"));
    }
}
