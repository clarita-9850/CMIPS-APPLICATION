package com.cmips.util;

import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.HashSet;
import java.util.Set;

/**
 * Business day calculator per DSD Section 8 — Escalation Rules & Deadlines.
 * <p>
 * Deadlines are in business days (excludes weekends and California state holidays).
 * Used for task due dates, escalation deadlines, and batch date calculations.
 */
@Component
public class BusinessDayCalculator {

    /**
     * Add N business days to the given date-time (preserves time of day).
     * Skips weekends and California state holidays.
     */
    public LocalDateTime addBusinessDays(LocalDateTime from, int businessDays) {
        if (businessDays <= 0) return from;

        LocalDate date = from.toLocalDate();
        int year = date.getYear();
        Set<LocalDate> holidays = getCaliforniaHolidays(year);

        int added = 0;
        while (added < businessDays) {
            date = date.plusDays(1);
            // If we crossed into a new year, include that year's holidays
            if (date.getYear() != year) {
                year = date.getYear();
                holidays.addAll(getCaliforniaHolidays(year));
            }
            if (isBusinessDay(date, holidays)) {
                added++;
            }
        }

        return date.atTime(from.toLocalTime());
    }

    /**
     * Subtract N business days from the given date-time.
     */
    public LocalDateTime subtractBusinessDays(LocalDateTime from, int businessDays) {
        if (businessDays <= 0) return from;

        LocalDate date = from.toLocalDate();
        int year = date.getYear();
        Set<LocalDate> holidays = getCaliforniaHolidays(year);

        int subtracted = 0;
        while (subtracted < businessDays) {
            date = date.minusDays(1);
            if (date.getYear() != year) {
                year = date.getYear();
                holidays.addAll(getCaliforniaHolidays(year));
            }
            if (isBusinessDay(date, holidays)) {
                subtracted++;
            }
        }

        return date.atTime(from.toLocalTime());
    }

    /**
     * Count business days between two dates (exclusive of start, inclusive of end).
     */
    public int countBusinessDaysBetween(LocalDateTime start, LocalDateTime end) {
        LocalDate startDate = start.toLocalDate();
        LocalDate endDate = end.toLocalDate();
        if (!endDate.isAfter(startDate)) return 0;

        Set<LocalDate> holidays = getCaliforniaHolidays(startDate.getYear());
        if (endDate.getYear() != startDate.getYear()) {
            holidays.addAll(getCaliforniaHolidays(endDate.getYear()));
        }

        int count = 0;
        LocalDate d = startDate.plusDays(1);
        while (!d.isAfter(endDate)) {
            if (isBusinessDay(d, holidays)) {
                count++;
            }
            d = d.plusDays(1);
        }
        return count;
    }

    /**
     * Check if a date is a business day (not weekend, not holiday).
     */
    public boolean isBusinessDay(LocalDate date) {
        return isBusinessDay(date, getCaliforniaHolidays(date.getYear()));
    }

    private boolean isBusinessDay(LocalDate date, Set<LocalDate> holidays) {
        DayOfWeek dow = date.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) return false;
        return !holidays.contains(date);
    }

    /**
     * California state holidays for a given year.
     * Per California Government Code Section 6700.
     */
    public Set<LocalDate> getCaliforniaHolidays(int year) {
        Set<LocalDate> holidays = new HashSet<>();

        // New Year's Day — January 1
        holidays.add(observedDate(LocalDate.of(year, Month.JANUARY, 1)));

        // Martin Luther King Jr. Day — 3rd Monday of January
        holidays.add(nthWeekday(year, Month.JANUARY, DayOfWeek.MONDAY, 3));

        // Presidents' Day — 3rd Monday of February
        holidays.add(nthWeekday(year, Month.FEBRUARY, DayOfWeek.MONDAY, 3));

        // Cesar Chavez Day — March 31
        holidays.add(observedDate(LocalDate.of(year, Month.MARCH, 31)));

        // Memorial Day — Last Monday of May
        holidays.add(lastWeekday(year, Month.MAY, DayOfWeek.MONDAY));

        // Independence Day — July 4
        holidays.add(observedDate(LocalDate.of(year, Month.JULY, 4)));

        // Labor Day — 1st Monday of September
        holidays.add(nthWeekday(year, Month.SEPTEMBER, DayOfWeek.MONDAY, 1));

        // Indigenous Peoples' Day (Columbus Day) — 2nd Monday of October
        holidays.add(nthWeekday(year, Month.OCTOBER, DayOfWeek.MONDAY, 2));

        // Veterans Day — November 11
        holidays.add(observedDate(LocalDate.of(year, Month.NOVEMBER, 11)));

        // Thanksgiving Day — 4th Thursday of November
        holidays.add(nthWeekday(year, Month.NOVEMBER, DayOfWeek.THURSDAY, 4));

        // Day after Thanksgiving — 4th Friday of November
        holidays.add(nthWeekday(year, Month.NOVEMBER, DayOfWeek.FRIDAY, 4));

        // Christmas Day — December 25
        holidays.add(observedDate(LocalDate.of(year, Month.DECEMBER, 25)));

        return holidays;
    }

    /**
     * Federal observed-date rule: if holiday falls on Saturday, observed Friday;
     * if Sunday, observed Monday.
     */
    private LocalDate observedDate(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY) return date.minusDays(1);
        if (dow == DayOfWeek.SUNDAY) return date.plusDays(1);
        return date;
    }

    /**
     * Find the Nth occurrence of a weekday in a month. E.g., 3rd Monday of January.
     */
    private LocalDate nthWeekday(int year, Month month, DayOfWeek dayOfWeek, int n) {
        LocalDate first = LocalDate.of(year, month, 1);
        int daysUntil = (dayOfWeek.getValue() - first.getDayOfWeek().getValue() + 7) % 7;
        return first.plusDays(daysUntil + (long)(n - 1) * 7);
    }

    /**
     * Find the last occurrence of a weekday in a month. E.g., last Monday of May.
     */
    private LocalDate lastWeekday(int year, Month month, DayOfWeek dayOfWeek) {
        LocalDate last = LocalDate.of(year, month, month.length(year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)));
        int daysBack = (last.getDayOfWeek().getValue() - dayOfWeek.getValue() + 7) % 7;
        return last.minusDays(daysBack);
    }
}
