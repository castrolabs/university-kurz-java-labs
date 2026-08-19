import java.time.Duration;
import java.time.Period;
import java.time.ZonedDateTime;

public class ScheduleCalculator {

    public ZonedDateTime plusOneElapsedDay(ZonedDateTime start) {
        return start.plus(Duration.ofDays(1));
    }

    public ZonedDateTime plusOneCalendarDay(ZonedDateTime start) {
        return start.plus(Period.ofDays(1));
    }

    public Duration elapsedTimeAcross(ZonedDateTime start, ZonedDateTime end) {
        return Duration.between(start, end);
    }

    public boolean landsOnSameWallClockTime(ZonedDateTime start, ZonedDateTime shifted) {
        return start.toLocalTime().equals(shifted.toLocalTime());
    }

    public ZonedDateTime plusOneCalendarMonthClamped(ZonedDateTime start) {
        return start.plus(Period.ofMonths(1));
    }
}
