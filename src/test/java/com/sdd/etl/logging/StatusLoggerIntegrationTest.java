package com.sdd.etl.logging;

import com.sdd.etl.model.SubprocessResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import static org.junit.Assert.*;

/**
 * Integration tests for StatusLogger.
 * Verifies console output and file logging via Logback configuration.
 */
public class StatusLoggerIntegrationTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @Before
    public void setUp() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));

        // Ensure we use test logback configuration
        System.setProperty("logback.configurationFile", "src/test/resources/logback-test.xml");
    }

    @After
    public void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);

        System.clearProperty("logback.configurationFile");

        // Clean up test log file if created
        File logFile = new File("target/test-etl.log");
        if (logFile.exists()) {
            // best-effort cleanup
            logFile.delete();
        }
    }

    @Test
    public void testLoggingToConsole() {
        StatusLogger logger = new StatusLogger();
        logger.logSubprocessCompletion("20250101", "EXTRACT", new SubprocessResult(1));

        String out = outContent.toString();
        assertTrue("Console output should contain Success", out.contains("Success"));
        assertTrue("Console output should contain EXTRACT", out.contains("EXTRACT"));
    }

    @Test
    public void testLoggingToFile() {
        StatusLogger logger = new StatusLogger();
        logger.logSubprocessCompletion("20250101", "EXTRACT", new SubprocessResult(1));

        File logFile = new File("target/test-etl.log");
        assertTrue("Log file should exist", logFile.exists());
        assertTrue("Log file should not be empty", logFile.length() > 0);
    }
}
