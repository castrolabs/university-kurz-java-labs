import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class FieldFlags {

    public static boolean isPublic(Class<?> type, String fieldName) throws NoSuchFieldException {
        Field field = type.getDeclaredField(fieldName);
        return Modifier.isPublic(field.getModifiers());
    }

    public static boolean isStatic(Class<?> type, String fieldName) throws NoSuchFieldException {
        Field field = type.getDeclaredField(fieldName);
        return Modifier.isStatic(field.getModifiers());
    }
}
