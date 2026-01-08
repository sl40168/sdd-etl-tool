package com.sdd.etl.model;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for CommandLineArguments POJO.
 */
public class CommandLineArgumentsTest {

    @Test
    public void testConstructorWithValidArguments() {
        CommandLineArguments args = new CommandLineArguments("20250101", "20250110", "/path/to/config.ini", false);

        assertEquals("20250101", args.getFromDate());
        assertEquals("20250110", args.getToDate());
        assertEquals("/path/to/config.ini", args.getConfigPath());
        assertFalse(args.isHelpRequested());
    }

    @Test
    public void testConstructorWithHelpRequested() {
        CommandLineArguments args = new CommandLineArguments(null, null, null, true);

        assertNull(args.getFromDate());
        assertNull(args.getToDate());
        assertNull(args.getConfigPath());
        assertTrue(args.isHelpRequested());
    }

    @Test
    public void testGetterFromDate() {
        CommandLineArguments args = new CommandLineArguments("20251231", "20251231", "config.ini", false);

        assertEquals("20251231", args.getFromDate());
    }

    @Test
    public void testGetterToDate() {
        CommandLineArguments args = new CommandLineArguments("20250101", "20251231", "config.ini", false);

        assertEquals("20251231", args.getToDate());
    }

    @Test
    public void testGetterConfigPath() {
        CommandLineArguments args = new CommandLineArguments("20250101", "20250110", "/home/user/.etlconfig.ini", false);

        assertEquals("/home/user/.etlconfig.ini", args.getConfigPath());
    }

    @Test
    public void testGetterHelpRequested() {
        CommandLineArguments args = new CommandLineArguments(null, null, null, true);

        assertTrue(args.isHelpRequested());
    }

    @Test
    public void testSetterFromDate() {
        CommandLineArguments args = new CommandLineArguments(null, null, null, false);
        args.setFromDate("20251231");

        assertEquals("20251231", args.getFromDate());
    }

    @Test
    public void testSetterToDate() {
        CommandLineArguments args = new CommandLineArguments(null, null, null, false);
        args.setToDate("20251231");

        assertEquals("20251231", args.getToDate());
    }

    @Test
    public void testSetterConfigPath() {
        CommandLineArguments args = new CommandLineArguments(null, null, null, false);
        args.setConfigPath("/home/user/.etlconfig.ini");

        assertEquals("/home/user/.etlconfig.ini", args.getConfigPath());
    }

    @Test
    public void testSetterHelpRequested() {
        CommandLineArguments args = new CommandLineArguments(null, null, null, false);
        args.setHelpRequested(true);

        assertTrue(args.isHelpRequested());
    }

    @Test
    public void testMultipleSetters() {
        CommandLineArguments args = new CommandLineArguments(null, null, null, false);
        args.setFromDate("20250101");
        args.setToDate("20250105");
        args.setConfigPath("/test/config.ini");
        args.setHelpRequested(false);

        assertEquals("20250101", args.getFromDate());
        assertEquals("20250105", args.getToDate());
        assertEquals("/test/config.ini", args.getConfigPath());
        assertFalse(args.isHelpRequested());
    }
}
