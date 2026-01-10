package com.sdd.etl.context;

import com.sdd.etl.config.ETConfiguration;

import java.time.LocalDate;
import com.sdd.etl.util.DateUtils;
import java.util.HashMap;
import java.util.Map;

/**
 * Manager for ETL context lifecycle.
 * Provides static methods for creating, validating, and managing context state.
 */
public class ContextManager {

    /**
     * Creates a new ETL context with date and configuration.
     *
     * @param date   processing date as LocalDate object
     * @param config ETL configuration object
     * @return initialized ETLContext
     * @throws IllegalArgumentException if date or config is null
     */
    public static ETLContext createContext(LocalDate date, ETConfiguration config) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        if (config == null) {
            throw new IllegalArgumentException("Config cannot be null");
        }

        ETLContext context = new ETLContext();
        context.setCurrentDate(date);
        context.setConfig(config);
        context.setCurrentSubprocess(SubprocessType.EXTRACT); // Start with EXTRACT

        // Initialize counters to 0
        context.setExtractedDataCount(0);
        context.setTransformedDataCount(0);
        context.setLoadedDataCount(0);

        // Initialize flags to false
        context.setValidationPassed(false);
        context.setCleanupPerformed(false);

        // Initialize error list
        context.setValidationErrors(new java.util.ArrayList<String>());

        return context;
    }

    /**
     * Validates context state before subprocess execution.
     *
     * @param context ETL context to validate
     * @throws IllegalArgumentException if context is invalid
     */
    public static void validateContext(ETLContext context) {
        try {
            if (context == null) {
                throw new IllegalArgumentException("Context cannot be null");
            }

            if (context.getCurrentDate() == null) {
                throw new IllegalArgumentException("Current date must be set");
            }

            if (context.getConfig() == null) {
                throw new IllegalArgumentException("Configuration must be set");
            }

            if (context.getCurrentSubprocess() == null) {
                throw new IllegalArgumentException("Current subprocess must be set");
            }

            // Validate counters are non-negative
            if (context.getExtractedDataCount() < 0) {
                throw new IllegalArgumentException("Extracted data count must be >= 0");
            }

            if (context.getTransformedDataCount() < 0) {
                throw new IllegalArgumentException("Transformed data count must be >= 0");
            }

            if (context.getLoadedDataCount() < 0) {
                throw new IllegalArgumentException("Loaded data count must be >= 0");
            }
        } catch (IllegalArgumentException e) {
            com.sdd.etl.logging.ETLogger.error("Context validation failed: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Creates an immutable snapshot of the current context state.
     *
     * @param context ETL context to snapshot
     * @return copy of context data
     */
    public static Map<String, Object> snapshot(ETLContext context) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }

        // Return a deep copy of the context data
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.putAll(context.getAll());

        return snapshot;
    }

    /**
     * Logs current context state for troubleshooting.
     *
     * @param context ETL context to log
     */
    public static void logContextState(ETLContext context) {
        if (context == null) {
            com.sdd.etl.logging.ETLogger.error("Cannot log context state: context is null");
            return;
        }

        com.sdd.etl.logging.ETLogger.info("=== Context State ===");
        com.sdd.etl.logging.ETLogger.info("Date: " + DateUtils.formatDate(context.getCurrentDate()));
        com.sdd.etl.logging.ETLogger.info("Subprocess: " + context.getCurrentSubprocess());
        com.sdd.etl.logging.ETLogger.info("Extracted Data Count: " + context.getExtractedDataCount());
        com.sdd.etl.logging.ETLogger.info("Transformed Data Count: " + context.getTransformedDataCount());
        com.sdd.etl.logging.ETLogger.info("Loaded Data Count: " + context.getLoadedDataCount());
        com.sdd.etl.logging.ETLogger.info("Validation Passed: " + context.isValidationPassed());
        com.sdd.etl.logging.ETLogger.info("Cleanup Performed: " + context.isCleanupPerformed());

        if (context.getValidationErrors() != null && !context.getValidationErrors().isEmpty()) {
            com.sdd.etl.logging.ETLogger.info("Validation Errors:");
            for (String error : context.getValidationErrors()) {
                com.sdd.etl.logging.ETLogger.info("  - " + error);
            }
        }

        com.sdd.etl.logging.ETLogger.info("=====================");
    }
}
