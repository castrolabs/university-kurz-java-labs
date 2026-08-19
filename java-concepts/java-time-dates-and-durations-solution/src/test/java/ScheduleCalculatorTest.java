import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.zone.ZoneOffsetTransition;
import java.time.zone.ZoneRules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ScheduleCalculator")
class ScheduleCalculatorTest {

    private final ScheduleCalculator calculator = new ScheduleCalculator();
    private final ZoneId lisbon = ZoneId.of("Europe/Lisbon");

    @Test
    @DisplayName("should add exactly 24 elapsed hours on an ordinary day")
    void shouldAddExactlyTwentyFourElapsedHoursOnAnOrdinaryDay() {
        ZonedDateTime start = ZonedDateTime.of(2026, 7, 15, 12, 0, 0, 0, lisbon);

        ZonedDateTime result = calculator.plusOneElapsedDay(start);

        assertEquals(24, calculator.elapsedTimeAcross(start, result).toHours());
    }

    @Test
    @DisplayName("should agree with the calendar day away from any DST transition")
    void shouldMatchOnAnOrdinaryDayAwayFromDst() {
        ZonedDateTime start = ZonedDateTime.of(2026, 7, 15, 12, 0, 0, 0, lisbon);

        ZonedDateTime elapsed = calculator.plusOneElapsedDay(start);
        ZonedDateTime calendarDay = calculator.plusOneCalendarDay(start);

        assertEquals(elapsed, calendarDay);
    }

    @Test
    @DisplayName("should diverge across a spring-forward gap: elapsed time shifts the wall clock, the calendar day does not")
    void shouldDivergeAcrossSpringForwardGap() {
        ZoneOffsetTransition gap = findNextTransition(lisbon, Instant.parse("2026-01-01T00:00:00Z"), true);
        ZonedDateTime start = ZonedDateTime.of(
                gap.getDateTimeBefore().toLocalDate().minusDays(1), LocalTime.NOON, lisbon);

        ZonedDateTime elapsedResult = calculator.plusOneElapsedDay(start);
        ZonedDateTime calendarResult = calculator.plusOneCalendarDay(start);

        assertNotEquals(elapsedResult, calendarResult);
        assertTrue(calculator.landsOnSameWallClockTime(start, calendarResult));
        assertFalse(calculator.landsOnSameWallClockTime(start, elapsedResult));
        assertEquals(23, calculator.elapsedTimeAcross(start, calendarResult).toHours());
        assertEquals(24, calculator.elapsedTimeAcross(start, elapsedResult).toHours());
    }

    @Test
    @DisplayName("should diverge across a fall-back overlap: the calendar day takes 25 elapsed hours, not 24")
    void shouldDivergeAcrossFallBackOverlap() {
        ZoneOffsetTransition overlap = findNextTransition(lisbon, Instant.parse("2026-06-01T00:00:00Z"), false);
        ZonedDateTime start = ZonedDateTime.of(
                overlap.getDateTimeBefore().toLocalDate().minusDays(1), LocalTime.NOON, lisbon);

        ZonedDateTime elapsedResult = calculator.plusOneElapsedDay(start);
        ZonedDateTime calendarResult = calculator.plusOneCalendarDay(start);

        assertNotEquals(elapsedResult, calendarResult);
        assertTrue(calculator.landsOnSameWallClockTime(start, calendarResult));
        assertFalse(calculator.landsOnSameWallClockTime(start, elapsedResult));
        assertEquals(25, calculator.elapsedTimeAcross(start, calendarResult).toHours());
        assertEquals(24, calculator.elapsedTimeAcross(start, elapsedResult).toHours());
    }

    @Test
    @DisplayName("should clamp to the last day of the month when the source day doesn't exist there")
    void shouldClampWhenAddingACalendarMonthPastMonthEnd() {
        ZonedDateTime start = ZonedDateTime.of(2026, 1, 31, 12, 0, 0, 0, lisbon);

        ZonedDateTime result = calculator.plusOneCalendarMonthClamped(start);

        assertEquals(LocalDate.of(2026, 2, 28), result.toLocalDate());
    }

    /**
     * Walks the zone's real transition rules forward from {@code from} to find the next
     * transition of the requested kind - a spring-forward gap ({@code gap == true}) or a
     * fall-back overlap ({@code gap == false}) - instead of assuming a remembered calendar date.
     */
    private static ZoneOffsetTransition findNextTransition(ZoneId zone, Instant from, boolean gap) {
        ZoneRules rules = zone.getRules();
        Instant instant = from;
        while (true) {
            ZoneOffsetTransition transition = rules.nextTransition(instant);
            if (transition == null) {
                throw new IllegalStateException("No further transitions found for " + zone);
            }
            if (transition.isGap() == gap) {
                return transition;
            }
            instant = transition.getInstant();
        }
    }
}
