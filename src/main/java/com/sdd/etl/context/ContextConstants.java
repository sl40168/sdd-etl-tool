package com.sdd.etl.context;

/**
 * Constants for ETL context keys.
 * Provides a centralized location for all context key constants used throughout the ETL process.
 */
public class ContextConstants {

    // Current execution context
    /**
     * Key for the current processing date (YYYYMMDD format)
     */
    public static final String CURRENT_DATE = "currentDate";

    /**
     * Key for the currently executing subprocess type
     */
    public static final String CURRENT_SUBPROCESS = "currentSubprocess";

    // Configuration
    /**
     * Key for the ETL configuration object
     */
    public static final String CONFIG = "config";

    // Extract subprocess data
    /**
     * Key for the count of extracted records
     */
    public static final String EXTRACTED_DATA_COUNT = "extractedDataCount";

    /**
     * Key for the actual extracted data objects
     */
    public static final String EXTRACTED_DATA = "extractedData";

    // Transform subprocess data
    /**
     * Key for the count of transformed records
     */
    public static final String TRANSFORMED_DATA_COUNT = "transformedDataCount";

    /**
     * Key for the actual transformed data objects
     */
    public static final String TRANSFORMED_DATA = "transformedData";

    // Load subprocess data
    /**
     * Key for the count of loaded records
     */
    public static final String LOADED_DATA_COUNT = "loadedDataCount";

    // Validate subprocess data
    /**
     * Key for whether validation passed
     */
    public static final String VALIDATION_PASSED = "validationPassed";

    /**
     * Key for the list of validation errors
     */
    public static final String VALIDATION_ERRORS = "validationErrors";

    // Clean subprocess data
    /**
     * Key for whether cleanup has been performed
     */
    public static final String CLEANUP_PERFORMED = "cleanupPerformed";

    // Private constructor to prevent instantiation
    private ContextConstants() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}
