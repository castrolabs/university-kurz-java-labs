public class RequestContext {

    private static final ScopedValue<String> CURRENT_USER = ScopedValue.newInstance();
    private static final ScopedValue<String> CURRENT_LOCALE = ScopedValue.newInstance();

    private RequestContext() {
    }

    public static String currentUser() {
        return CURRENT_USER.get();
    }

    public static boolean hasCurrentUser() {
        return CURRENT_USER.isBound();
    }

    public static String currentUserOrDefault(String fallback) {
        return CURRENT_USER.orElse(fallback);
    }

    public static <T, X extends Throwable> T runAs(String user, ScopedValue.CallableOp<T, X> action) throws X {
        return ScopedValue.where(CURRENT_USER, user).call(action);
    }

    public static void runAs(String user, Runnable action) {
        ScopedValue.where(CURRENT_USER, user).run(action);
    }

    public static String currentLocale() {
        return CURRENT_LOCALE.orElse("en-US");
    }

    public static void runAs(String user, String locale, Runnable action) {
        ScopedValue.where(CURRENT_USER, user)
                .where(CURRENT_LOCALE, locale)
                .run(action);
    }
}
