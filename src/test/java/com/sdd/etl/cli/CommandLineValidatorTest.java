package com.sdd.etl.cli;

import com.sdd.etl.model.CommandLineArguments;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.util.List;

/**
 * Unit tests for CommandLineValidator.
 */
public class CommandLineValidatorTest {

    private CommandLineValidator validator;

    @Before
    public void setUp() {
        validator = new CommandLineValidator();
    }

    @Test
    public void testValidateDateFormat_ValidFormat() {
        boolean result = validator.validateDateFormat("20250101", "from");
        assertTrue("Valid date format should return true", result);
    }

    @Test
    public void testValidateDateFormat_ValidFormat_December31() {
        boolean result = validator.validateDateFormat("20251231", "from");
        assertTrue("Valid date format for December 31 should return true", result);
    }

    @Test
    public void testValidateDateFormat_ValidFormat_January1() {
        boolean result = validator.validateDateFormat("20250101", "to");
        assertTrue("Valid date format for January 1 should return true", result);
    }

    @Test
    public void testValidateDateFormat_InvalidFormat_MissingLeadingZero() {
        boolean result = validator.validateDateFormat("2025011", "from");
        assertFalse("Invalid date format (7 digits) should return false", result);
    }

    @Test
    public void testValidateDateFormat_InvalidFormat_9Digits() {
        boolean result = validator.validateDateFormat("202501011", "from");
        assertFalse("Invalid date format (9 digits) should return false", result);
    }

    @Test
    public void testValidateDateFormat_InvalidFormat_Slashes() {
        boolean result = validator.validateDateFormat("2025/01/01", "from");
        assertFalse("Invalid date format (slashes) should return false", result);
    }

    @Test
    public void testValidateDateFormat_InvalidFormat_Dashes() {
        boolean result = validator.validateDateFormat("2025-01-01", "to");
        assertFalse("Invalid date format (dashes) should return false", result);
    }

    @Test
    public void testValidateDateFormat_InvalidFormat_Alphabetic() {
        boolean result = validator.validateDateFormat("January", "from");
        assertFalse("Invalid date format (alphabetic) should return false", result);
    }

    @Test
    public void testValidateDateFormat_EmptyString() {
        boolean result = validator.validateDateFormat("", "from");
        assertFalse("Empty string should return false", result);
    }

    @Test
    public void testValidateDateFormat_Null() {
        boolean result = validator.validateDateFormat(null, "from");
        assertFalse("Null value should return false", result);
    }

    @Test
    public void testValidateDateRange_ValidRange() {
        boolean result = validator.validateDateRange("20250101", "20250110");
        assertTrue("Valid date range (from < to) should return true", result);
    }

    @Test
    public void testValidateDateRange_EqualDates() {
        boolean result = validator.validateDateRange("20250101", "20250101");
        assertTrue("Valid date range (from = to, single day) should return true", result);
    }

    @Test
    public void testValidateDateRange_InvalidRange_FromAfterTo() {
        boolean result = validator.validateDateRange("20250110", "20250101");
        assertFalse("Invalid date range (from > to) should return false", result);
    }

    @Test
    public void testValidateConfigFileExists_ExistingFile() {
        boolean result = validator.validateConfigFileExists("src/main/resources/default-config.ini");
        assertTrue("Existing config file should return true", result);
    }

    @Test
    public void testValidateConfigFileExists_NonExistingFile() {
        boolean result = validator.validateConfigFileExists("/nonexistent/config.ini");
        assertFalse("Non-existing config file should return false", result);
    }

    @Test
    public void testValidateConfigFileExists_EmptyPath() {
        boolean result = validator.validateConfigFileExists("");
        assertFalse("Empty path should return false", result);
    }

    @Test
    public void testValidateConfigFileExists_NullPath() {
        boolean result = validator.validateConfigFileExists(null);
        assertFalse("Null path should return false", result);
    }

    @Test
    public void testValidateAll_AllValid() {
        CommandLineArguments args = new CommandLineArguments("20250101", "20250110", "src/main/resources/default-config.ini", false);
        List<String> errors = validator.validateAll(args);
        assertTrue("All valid inputs should produce empty error list", errors.isEmpty());
    }

    @Test
    public void testValidateAll_HelpRequested() {
        CommandLineArguments args = new CommandLineArguments(null, null, null, true);
        List<String> errors = validator.validateAll(args);
        assertTrue("Help requested should produce empty error list", errors.isEmpty());
    }

    @Test
    public void testValidateAll_MissingFromDate() {
        CommandLineArguments args = new CommandLineArguments(null, "20250110", "src/main/resources/default-config.ini", false);
        List<String> errors = validator.validateAll(args);
        assertFalse("Missing from date should produce error", errors.isEmpty());
        assertTrue("Error should mention --from", errors.get(0).contains("--from"));
    }

    @Test
    public void testValidateAll_MissingToDate() {
        CommandLineArguments args = new CommandLineArguments("20250101", null, "src/main/resources/default-config.ini", false);
        List<String> errors = validator.validateAll(args);
        assertFalse("Missing to date should produce error", errors.isEmpty());
        assertTrue("Error should mention --to", errors.get(0).contains("--to"));
    }

    @Test
    public void testValidateAll_MissingConfig() {
        CommandLineArguments args = new CommandLineArguments("20250101", "20250110", null, false);
        List<String> errors = validator.validateAll(args);
        assertFalse("Missing config should produce error", errors.isEmpty());
        assertTrue("Error should mention --config", errors.get(0).contains("--config"));
    }

    @Test
    public void testValidateAll_InvalidDateFormat_From() {
        CommandLineArguments args = new CommandLineArguments("2025-01-01", "20250110", "src/main/resources/default-config.ini", false);
        List<String> errors = validator.validateAll(args);
        assertFalse("Invalid date format should produce error", errors.isEmpty());
        assertTrue("Error should mention date format", errors.get(0).contains("date format") || errors.get(0).contains("YYYYMMDD"));
    }

    @Test
    public void testValidateAll_InvalidDateFormat_To() {
        CommandLineArguments args = new CommandLineArguments("20250101", "2025/01/10", "src/main/resources/default-config.ini", false);
        List<String> errors = validator.validateAll(args);
        assertFalse("Invalid date format should produce error", errors.isEmpty());
    }

    @Test
    public void testValidateAll_InvalidDateRange() {
        CommandLineArguments args = new CommandLineArguments("20250110", "20250101", "src/main/resources/default-config.ini", false);
        List<String> errors = validator.validateAll(args);
        assertFalse("Invalid date range should produce error", errors.isEmpty());
        assertTrue("Error should mention date range", errors.get(0).contains("date range") || errors.get(0).contains("Invalid date range") || errors.get(0).contains("after"));
    }

    @Test
    public void testValidateAll_MissingConfigFile() {
        CommandLineArguments args = new CommandLineArguments("20250101", "20250110", "/nonexistent/config.ini", false);
        List<String> errors = validator.validateAll(args);
        assertFalse("Missing config file should produce error", errors.isEmpty());
        assertTrue("Error should mention configuration file", errors.get(0).contains("config") || errors.get(0).contains("file"));
    }

    @Test
    public void testValidateAll_MultipleErrors() {
        CommandLineArguments args = new CommandLineArguments("invalid", "20250101", "/nonexistent/config.ini", false);
        List<String> errors = validator.validateAll(args);
        assertFalse("Multiple errors should produce error list", errors.isEmpty());
        assertTrue("Should have multiple errors", errors.size() >= 2);
    }
}
