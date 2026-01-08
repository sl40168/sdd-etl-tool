package com.sdd.etl.logging;

import org.junit.Before;
import org.junit.Test;
import org.junit.After;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.*;

/**
 * Unit tests for ETLogger facade.
 */
public class ETLoggerTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @Before
    public void setUp() {
        // Redirect System.out and System.err to capture output
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @After
    public void tearDown() {
        // Restore original System.out and System.err
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    public void testInfo_LogsMessage() {
        String message = "Test info message";
        ETLogger.info(message);

        String output = outContent.toString();
        assertTrue("Info message should be logged", output.contains(message));
    }

    @Test
    public void testWarn_LogsWarningMessage() {
        String message = "Test warning message";
        ETLogger.warn(message);

        String output = outContent.toString();
        assertTrue("Warning message should be logged", output.contains(message));
    }

    @Test
    public void testError_LogsErrorMessage() {
        String message = "Test error message";
        ETLogger.error(message);

        String output = errContent.toString();
        assertTrue("Error message should be logged", output.contains(message));
    }

    @Test
    public void testError_LogsErrorMessageWithException() {
        String message = "Test error with exception";
        Exception exception = new RuntimeException("Test exception");
        ETLogger.error(message, exception);

        String output = errContent.toString();
        assertTrue("Error message should be logged", output.contains(message));
        assertTrue("Exception should be logged", output.contains(exception.getMessage()));
    }

    @Test
    public void testGetLogger_ReturnsLoggerInstance() {
        assertNotNull("Logger should not be null", ETLogger.getLogger());
    }
}
