package com.sdd.etl.cli;

import com.sdd.etl.model.CommandLineArguments;
import org.junit.Test;
import org.junit.After;
import static org.junit.Assert.*;
import org.apache.commons.cli.ParseException;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Unit tests for ETLCommandLine.
 */
public class ETLCommandLineTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @org.junit.Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @After
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    public void testHelpCommandArgumentParsing() throws ParseException {
        String[] args = {"--help"};
        CommandLineArguments result = ETLCommandLine.parseArguments(args);

        assertNotNull("Help argument should be parsed", result);
        assertTrue("Help requested flag should be true", result.isHelpRequested());
    }

    @Test
    public void testHelpCommandArgumentParsing_ShortForm() throws ParseException {
        String[] args = {"-h"};
        CommandLineArguments result = ETLCommandLine.parseArguments(args);

        assertNotNull("Short help argument should be parsed", result);
        assertTrue("Help requested flag should be true", result.isHelpRequested());
    }

    @Test
    public void testRequiredParameterParsing_AllPresent() throws ParseException {
        String[] args = {"--from", "20250101", "--to", "20250110", "--config", "config.ini"};
        CommandLineArguments result = ETLCommandLine.parseArguments(args);

        assertNotNull("Arguments should be parsed", result);
        assertEquals("From date should match", "20250101", result.getFromDate());
        assertEquals("To date should match", "20250110", result.getToDate());
        assertEquals("Config path should match", "config.ini", result.getConfigPath());
        assertFalse("Help requested should be false", result.isHelpRequested());
    }

    @Test
    public void testRequiredParameterParsing_ShortForm() throws ParseException {
        String[] args = {"-f", "20250101", "-t", "20250110", "-c", "config.ini"};
        CommandLineArguments result = ETLCommandLine.parseArguments(args);

        assertNotNull("Arguments should be parsed", result);
        assertEquals("From date should match", "20250101", result.getFromDate());
        assertEquals("To date should match", "20250110", result.getToDate());
        assertEquals("Config path should match", "config.ini", result.getConfigPath());
    }

    @Test
    public void testRequiredParameterParsing_PartialArguments() throws ParseException {
        String[] args = {"--from", "20250101", "--to", "20250110"};
        CommandLineArguments result = ETLCommandLine.parseArguments(args);

        assertNotNull("Arguments should be parsed", result);
        assertEquals("From date should match", "20250101", result.getFromDate());
        assertEquals("To date should match", "20250110", result.getToDate());
        assertNull("Missing config should be null", result.getConfigPath());
    }

    @Test
    public void testInvalidParameterErrorHandling_UnknownOption() throws ParseException {
        String[] args = {"--unknown", "value"};
        CommandLineArguments result = ETLCommandLine.parseArguments(args);

        // Unknown options should either return null or an object with null fields
        // The exact behavior depends on Apache Commons CLI configuration
        assertNotNull("Should parse arguments", result);
    }

    @Test
    public void testInvalidParameterErrorHandling_MissingRequired() throws ParseException {
        String[] args = {}; // Empty arguments
        CommandLineArguments result = ETLCommandLine.parseArguments(args);

        assertNotNull("Should parse arguments", result);
        assertNull("From date should be null", result.getFromDate());
        assertNull("To date should be null", result.getToDate());
        assertNull("Config path should be null", result.getConfigPath());
    }

    @Test
    public void testInvalidParameterErrorHandling_DuplicateFrom() throws ParseException {
        String[] args = {"--from", "20250101", "--from", "20250102", "--to", "20250110", "--config", "config.ini"};
        CommandLineArguments result = ETLCommandLine.parseArguments(args);

        // Apache Commons CLI typically handles duplicates by using last value
        assertNotNull("Should parse arguments", result);
        assertEquals("Should use last from value", "20250102", result.getFromDate());
    }

    @Test
    public void testInvalidParameterErrorHandling_ConfigWithSpaces() throws ParseException {
        String[] args = {"--from", "20250101", "--to", "20250110", "--config", "/path/to/my config.ini"};
        CommandLineArguments result = ETLCommandLine.parseArguments(args);

        assertNotNull("Should parse arguments", result);
        // Path with spaces should be handled (may need quotes in actual usage)
    }
}
