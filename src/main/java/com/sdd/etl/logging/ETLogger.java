package com.sdd.etl.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Facade for logging operations using SLF4J.
 * Provides simple methods for info, warning, and error logging.
 */
public class ETLogger {

    private static final Logger logger = LoggerFactory.getLogger(ETLogger.class);

    /**
     * Logs an informational message.
     *
     * @param message message to log
     */
    public static void info(String message) {
        logger.info(message);
    }

    /**
     * Logs a warning message.
     *
     * @param message warning message to log
     */
    public static void warn(String message) {
        logger.warn(message);
    }

    /**
     * Logs an error message.
     *
     * @param message error message to log
     */
    public static void error(String message) {
        logger.error(message);
    }

    /**
     * Logs an error message with exception.
     *
     * @param message error message to log
     * @param e       exception that occurred
     */
    public static void error(String message, Exception e) {
        logger.error(message, e);
    }

    /**
     * Gets the underlying SLF4J logger.
     *
     * @return Logger instance
     */
    public static Logger getLogger() {
        return logger;
    }
}
