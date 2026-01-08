package com.sdd.etl.logging;

import org.junit.Test;
import org.junit.Before;

import com.sdd.etl.model.SubprocessResult;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.*;

/**
 * Unit tests for StatusLogger.
 */
public class StatusLoggerTest {

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

    @org.junit.After
    public void tearDown() {
        // Restore original System.out and System.err
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    public void testLogSubprocessCompletion_WithSuccess_LogsSuccess() {
        StatusLogger logger = new StatusLogger();
        String date = "20250101";
        String subprocess = "EXTRACT";
        SubprocessResult result = new SubprocessResult(100);

        logger.logSubprocessCompletion(date, subprocess, result);

        String output = outContent.toString();
        assertTrue("Should log success", output.contains("Success"));
        assertTrue("Should include data count", output.contains("100"));
        assertTrue("Should include date", output.contains(date));
        assertTrue("Should include subprocess", output.contains(subprocess));
    }

    @Test
    public void testLogSubprocessCompletion_WithFailure_LogsError() {
        StatusLogger logger = new StatusLogger();
        String date = "20250101";
        String subprocess = "EXTRACT";
        String errorMessage = "Connection failed";
        SubprocessResult result = new SubprocessResult(errorMessage);

        logger.logSubprocessCompletion(date, subprocess, result);

        String output = errContent.toString();
        assertTrue("Should log failure", output.contains("Failed"));
        assertTrue("Should include error message", output.contains(errorMessage));
        assertTrue("Should include date", output.contains(date));
        assertTrue("Should include subprocess", output.contains(subprocess));
    }

    @Test
    public void testLogDayCompletion_WithSuccess_LogsSuccess() {
        StatusLogger logger = new StatusLogger();
        String date = "20250101";
        int subprocessCount = 5;
        boolean success = true;

        logger.logDayCompletion(date, subprocessCount, success);

        String output = outContent.toString();
        assertTrue("Should log success", output.contains("successfully"));
        assertTrue("Should include date", output.contains(date));
        assertTrue("Should include subprocess count", output.contains("5"));
    }

    @Test
    public void testLogDayCompletion_WithFailure_LogsFailure() {
        StatusLogger logger = new StatusLogger();
        String date = "20250101";
        int subprocessCount = 3;
        boolean success = false;

        logger.logDayCompletion(date, subprocessCount, success);

        String output = errContent.toString();
        assertTrue("Should log failure", output.contains("failed"));
        assertTrue("Should include date", output.contains(date));
    }

    @Test
    public void testLogError_WithSubprocess_LogsError() {
        StatusLogger logger = new StatusLogger();
        String date = "20250101";
        String subprocess = "EXTRACT";
        String error = "Connection timeout";

        logger.logError(date, subprocess, error);

        String output = errContent.toString();
        assertTrue("Should log error", output.contains("Error"));
        assertTrue("Should include error message", output.contains(error));
        assertTrue("Should include date", output.contains(date));
        assertTrue("Should include subprocess", output.contains(subprocess));
    }

    @Test
    public void testLogError_WithoutSubprocess_LogsError() {
        StatusLogger logger = new StatusLogger();
        String date = "20250101";
        String subprocess = null;
        String error = "General error";

        logger.logError(date, subprocess, error);

        String output = errContent.toString();
        assertTrue("Should log error", output.contains("Error"));
        assertTrue("Should include error message", output.contains(error));
        assertTrue("Should include date", output.contains(date));
    }

    @Test
    public void testLogSummary_LogsAllStatistics() {
        StatusLogger logger = new StatusLogger();
        int totalDays = 7;
        int successfulDays = 7;
        int failedDays = 0;
        long durationMillis = 363000; // 6 minutes 3 seconds

        logger.logSummary(totalDays, successfulDays, failedDays, durationMillis);

        String output = outContent.toString();
        assertTrue("Should log total days", output.contains("7"));
        assertTrue("Should log successful days", output.contains("7"));
        assertTrue("Should log failed days", output.contains("0"));
        assertTrue("Should log duration", output.contains("00:06:03"));
    }
}
