package com.sdd.etl.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utility class for date parsing and formatting operations.
 * Provides consistent date handling using the "YYYYMMDD" format across the ETL system.
 */
public final class DateUtils {

    /**
     * Date formatter for "YYYYMMDD" format (e.g., 20250101).
     */
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Private constructor to prevent instantiation.
     */
    private DateUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Parses a date string in "YYYYMMDD" format to a LocalDate object.
     *
     * @param dateString the date string to parse (must be non-null and non-empty)
     * @return the parsed LocalDate object
     * @throws IllegalArgumentException if the date string is null, empty, or malformed
     */
    public static LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            throw new IllegalArgumentException("Date string cannot be null or empty");
        }

        try {
            return LocalDate.parse(dateString, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                "Invalid date format. Expected YYYYMMDD format, got: " + dateString, e);
        }
    }

    /**
     * Formats a LocalDate object to a string in "YYYYMMDD" format.
     *
     * @param date the LocalDate to format (must be non-null)
     * @return the formatted date string
     * @throws IllegalArgumentException if the date is null
     */
    public static String formatDate(LocalDate date) {
        if (date == null) {
            return null;
        }

        return date.format(DATE_FORMATTER);
    }
}