import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class FieldFlags {

    public static boolean isPublic(Class<?> type, String fieldName) throws NoSuchFieldException {
        // TODO-03: Return true if the field's access_flags include ACC_PUBLIC.
        // Field.getModifiers() returns the same bits the class file stores;
        // Modifier decodes them.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public static boolean isStatic(Class<?> type, String fieldName) throws NoSuchFieldException {
        // TODO-04: Return true if the field's access_flags include ACC_STATIC.
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
