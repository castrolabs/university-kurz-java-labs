import java.time.Duration;
import java.time.Period;
import java.time.ZonedDateTime;

public class ScheduleCalculator {

    public ZonedDateTime plusOneElapsedDay(ZonedDateTime start) {
        // TODO-00: Add exactly 24 hours of elapsed time to `start`, using
        // Duration. This adds to the underlying instant, so it can land on a
        // different wall-clock time across a DST transition.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public ZonedDateTime plusOneCalendarDay(ZonedDateTime start) {
        // TODO-01: Add one calendar day to `start`, using Period. This adds
        // to the local date-time and re-resolves against the zone's rules,
        // so it lands on the same wall-clock time the next day - even when
        // that day is not exactly 24 hours long.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public Duration elapsedTimeAcross(ZonedDateTime start, ZonedDateTime end) {
        // TODO-02: Return the exact elapsed time between `start` and `end`.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public boolean landsOnSameWallClockTime(ZonedDateTime start, ZonedDateTime shifted) {
        // TODO-03: Return true when `shifted` has the same local
        // time-of-day as `start` (ignore the date and the zone offset).
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public ZonedDateTime plusOneCalendarMonthClamped(ZonedDateTime start) {
        // TODO-04 (optional): Add one calendar month to `start`, using
        // Period. On a day that does not exist in the following month (for
        // example, adding a month to January 31st), the result should clamp
        // to that month's last day instead of overflowing into the month
        // after.
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
