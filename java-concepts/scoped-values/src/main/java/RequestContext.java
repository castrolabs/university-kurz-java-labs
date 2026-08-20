/**
 * Carries per-request context (the current user, and optionally a locale)
 * through arbitrarily deep call chains without threading a parameter
 * through every method, using ScopedValue instead of ThreadLocal.
 */
public class RequestContext {

    private static final ScopedValue<String> CURRENT_USER = ScopedValue.newInstance();
    private static final ScopedValue<String> CURRENT_LOCALE = ScopedValue.newInstance();

    private RequestContext() {
    }

    public static String currentUser() {
        // TODO-00: Return the bound value of CURRENT_USER. There is no
        // set() to reach for - use ScopedValue.get(). Let it throw
        // NoSuchElementException unmodified when nothing is bound.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public static boolean hasCurrentUser() {
        // TODO-01: Return whether CURRENT_USER is currently bound on this
        // thread, using ScopedValue.isBound().
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public static String currentUserOrDefault(String fallback) {
        // TODO-02: Return CURRENT_USER's bound value, or `fallback` if
        // nothing is bound, using ScopedValue.orElse(...).
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public static <T, X extends Throwable> T runAs(String user, ScopedValue.CallableOp<T, X> action) throws X {
        // TODO-03: Bind CURRENT_USER to `user` for the duration of
        // action.call() and return its result, using
        // ScopedValue.where(...).call(...). The binding must not be visible
        // before this method is called or after it returns.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public static void runAs(String user, Runnable action) {
        // TODO-04: Same idea as the Callable overload, but for a Runnable
        // with no result, using ScopedValue.where(...).run(...).
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public static String currentLocale() {
        return CURRENT_LOCALE.orElse("en-US");
    }

    public static void runAs(String user, String locale, Runnable action) {
        // TODO-05 (optional): Bind BOTH CURRENT_USER and CURRENT_LOCALE for
        // the duration of `action`, by chaining a second .where(...) onto
        // the Carrier returned by the first, then calling .run(action).
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
