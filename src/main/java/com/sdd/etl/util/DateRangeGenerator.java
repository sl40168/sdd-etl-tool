package com.sdd.etl.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates list of dates between from and to dates.
 * Supports iterating through date ranges for multi-day ETL processing.
 */
public class DateRangeGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Generates list of dates from from date to to date inclusive.
     *
     * @param fromDate start date in YYYYMMDD format
     * @param toDate   end date in YYYYMMDD format
     * @return list of dates in YYYYMMDD format
     * @throws IllegalArgumentException if dates are invalid or range is invalid
     */
    public static List<String> generate(String fromDate, String toDate) {
        if (fromDate == null || fromDate.isEmpty()) {
            throw new IllegalArgumentException("From date cannot be null or empty");
        }

        if (toDate == null || toDate.isEmpty()) {
            throw new IllegalArgumentException("To date cannot be null or empty");
        }

        LocalDate start;
        LocalDate end;

        try {
            start = LocalDate.parse(fromDate, DATE_FORMATTER);
            end = LocalDate.parse(toDate, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Expected YYYYMMDD format.", e);
        }

        if (start.isAfter(end)) {
            throw new IllegalArgumentException("From date must be before or equal to to date");
        }

        List<String> dates = new ArrayList<>();
        LocalDate current = start;

        while (!current.isAfter(end)) {
            dates.add(current.format(DATE_FORMATTER));
            current = current.plusDays(1);
        }

        return dates;
    }
}
