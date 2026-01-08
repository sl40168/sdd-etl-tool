package com.sdd.etl;

/**
 * Custom exception for ETL process errors.
 * Contains information about the subprocess type, date, and root cause.
 */
public class ETLException extends Exception {

    private final String subprocessType;
    private final String date;
    private final Throwable rootCause;

    /**
     * Constructs a new ETLException with the specified details.
     *
     * @param subprocessType the type of subprocess where the error occurred
     * @param date the date being processed when the error occurred (YYYYMMDD format)
     * @param message the error message
     */
    public ETLException(String subprocessType, String date, String message) {
        super(message);
        this.subprocessType = subprocessType;
        this.date = date;
        this.rootCause = null;
    }

    /**
     * Constructs a new ETLException with the specified details and root cause.
     *
     * @param subprocessType the type of subprocess where the error occurred
     * @param date the date being processed when the error occurred (YYYYMMDD format)
     * @param message the error message
     * @param rootCause the root cause of the exception
     */
    public ETLException(String subprocessType, String date, String message, Throwable rootCause) {
        super(message, rootCause);
        this.subprocessType = subprocessType;
        this.date = date;
        this.rootCause = rootCause;
    }

    /**
     * Gets the subprocess type where the error occurred.
     *
     * @return the subprocess type (e.g., "EXTRACT", "TRANSFORM", "LOAD", "VALIDATE", "CLEAN")
     */
    public String getSubprocessType() {
        return subprocessType;
    }

    /**
     * Gets the date being processed when the error occurred.
     *
     * @return the date in YYYYMMDD format
     */
    public String getDate() {
        return date;
    }

    /**
     * Gets the root cause of the exception.
     *
     * @return the root cause Throwable, or null if not provided
     */
    public Throwable getRootCause() {
        return rootCause;
    }
}
