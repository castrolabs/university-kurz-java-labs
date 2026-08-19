# Dates and Durations with java.time

## Goal

Prove to yourself that `Duration` and `Period` are not interchangeable ways
of saying "add a day" - one respects elapsed real time, the other respects
the calendar, and a daylight-saving transition is exactly where the two
stop agreeing.

## Prerequisites

- `ZonedDateTime` vs `Instant` vs `LocalDateTime`
- Basic familiarity with time zones and DST in general terms

## Task

`ScheduleCalculator` will implement two different ways of "adding a day" to
a `ZonedDateTime`: one that adds a fixed amount of elapsed time
(`Duration`), and one that adds a calendar day
(`Period`). On an ordinary day, in the middle of a stable stretch with no
clock change, both give the same answer. Across a DST transition, they do
not - and the tests prove it using the zone's *real* transition rules
rather than a hardcoded date.

## Instructions

Complete the following TODOs in `ScheduleCalculator`:

- TODO-00: Implement `plusOneElapsedDay(start)` by adding exactly 24 hours
  of elapsed time, using `Duration`.
- TODO-01: Implement `plusOneCalendarDay(start)` by adding one calendar day,
  using `Period`, so the result preserves the wall-clock time.
- TODO-02: Implement `elapsedTimeAcross(start, end)`, returning the exact
  `Duration` between the two.
- TODO-03: Implement `landsOnSameWallClockTime(start, shifted)`, comparing
  only the local time-of-day of the two arguments.

Run the tests until they all pass.

## Running the Lab

From the project root:

```bash
mvn -pl java-concepts/java-time-dates-and-durations test
```

Or from the lab directory:

```bash
cd java-concepts/java-time-dates-and-durations
mvn test
```

## Bonus (Optional)

- TODO-04 (optional): Implement `plusOneCalendarMonthClamped(start)` by
  adding one calendar month with `Period`. Verify it clamps to the last day
  of the following month instead of overflowing (adding a month to January
  31st should not land on March 3rd).
