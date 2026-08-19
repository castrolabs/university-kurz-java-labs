# Dates and Durations with java.time - Solution

## Overview

This is the official solution for the Dates and Durations lab.
`ScheduleCalculator` implements the same "add a day" operation two ways -
once with `Duration`, once with `Period` - to make the difference between
elapsed time and calendar time directly observable across a real DST
transition.

## Key Concepts

### plusOneElapsedDay: Duration adds to the instant

```java
public ZonedDateTime plusOneElapsedDay(ZonedDateTime start) {
    return start.plus(Duration.ofDays(1));
}
```

`Duration` is time-based: one of its days is exactly 86,400 seconds,
always. Adding it to a `ZonedDateTime` adds to the underlying instant and
then re-renders that instant in the zone - so if a DST transition falls
inside those 24 hours, the *wall-clock* time shifts by however many hours
the clocks moved. This is equivalent to `start.plusHours(24)`.

### plusOneCalendarDay: Period adds to the local date-time

```java
public ZonedDateTime plusOneCalendarDay(ZonedDateTime start) {
    return start.plus(Period.ofDays(1));
}
```

`Period` is calendar-based: it adds to the local date-time first, then
re-resolves the result against the zone's rules. That is what makes it
land on the *same wall-clock time* the next day, even on a day that was
only 23 or 25 real hours long. This is equivalent to `start.plusDays(1)`.

### Why the two diverge only across a transition

```java
ZonedDateTime start = ...;                                  // noon, the day before a spring-forward gap
ZonedDateTime elapsed = calculator.plusOneElapsedDay(start); // 24 real hours later -> 13:00
ZonedDateTime calendarDay = calculator.plusOneCalendarDay(start); // same wall time -> 12:00, but only 23 real hours later
```

On an ordinary day, with no clock change between `start` and `start + 1
day`, both methods produce the exact same `ZonedDateTime` - "24 elapsed
hours" and "the same time tomorrow" are the same statement when every day
is 24 hours long. A DST transition is the only place that stops being
true, because on that one day it isn't 24 hours long. Neither result is
wrong; they answer different questions, and the test suite finds a real,
current transition for `Europe/Lisbon` via `ZoneRules.nextTransition(...)`
rather than trusting a written-down date, since the exact day shifts from
year to year.

### plusOneCalendarMonthClamped: the same Period behavior, over months

```java
public ZonedDateTime plusOneCalendarMonthClamped(ZonedDateTime start) {
    return start.plus(Period.ofMonths(1));
}
```

The same date-based resolution that preserves wall-clock time across a DST
gap also clamps an out-of-range day-of-month rather than overflowing:
adding a month to January 31st lands on February 28th (or 29th, in a leap
year), not March 3rd, because February simply has no 31st day to land on.

## Summary

- `Duration` measures elapsed, clock-based time; `Period` measures
  calendar-based time. Neither can do the other's job.
- On a `ZonedDateTime`, adding a `Duration` moves the instant and lets the
  wall-clock time fall where it falls; adding a `Period` (or calling
  `plusDays`/`plusMonths`/`plusYears`) preserves the wall-clock time and
  lets the elapsed duration fall where it falls.
- The two are interchangeable on an ordinary day and diverge specifically
  across a DST transition - which is exactly why relying on that
  coincidence is a latent bug.
- Trust `ZoneRules`, not a remembered calendar date, when a test needs a
  real transition: `getRules().nextTransition(instant)` finds the next one
  programmatically, and `isGap()`/`isOverlap()` tell you which kind it is.
