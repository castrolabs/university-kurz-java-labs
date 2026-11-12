namespace Dispatch;

// Hands out ticket numbers for the dispatch desk. The counter is process-wide on purpose: the desk
// has one numbering sequence, and several parts of the program take tickets from it.
public static class TicketCounter
{
    private static int _last;

    public static int Last => _last;

    public static int Next() => ++_last;

    // Starts a new numbering sequence (the desk does this at midnight).
    public static void Reset() => _last = 0;
}
