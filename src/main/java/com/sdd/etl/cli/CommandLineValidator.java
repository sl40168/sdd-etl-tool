package com.sdd.etl.cli;

import com.sdd.etl.ETLException;
import com.sdd.etl.config.ETConfiguration;
import com.sdd.etl.context.ETLContext;
import com.sdd.etl.context.SubprocessType;
import com.sdd.etl.model.CommandLineArguments;
import com.sdd.etl.model.DailyProcessResult;
import com.sdd.etl.model.SubprocessResult;
import com.sdd.etl.model.WorkflowResult;
import com.sdd.etl.subprocess.ExtractSubprocess;
import com.sdd.etl.subprocess.LoadSubprocess;
import com.sdd.etl.subprocess.SubprocessInterface;
import com.sdd.etl.subprocess.ValidateSubprocess;
import com.sdd.etl.subprocess.CleanSubprocess;
import com.sdd.etl.util.DateRangeGenerator;
import com.sdd.etl.util.ConcurrentExecutionDetector;
import com.sdd.etl.logging.StatusLogger;
import com.sdd.etl.logging.ETLogger;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Validates command-line arguments for ETL tool.
 * Performs validation for date format, date range, and file existence.
 */
public class CommandLineValidator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Validates date format.
     *
     * @param date          date string to validate
     * @param parameterName name of parameter (for error messages)
     * @return true if valid, false otherwise
     */
    public boolean validateDateFormat(String date, String parameterName) {
        if (date == null || date.isEmpty()) {
            return false;
        }

        try {
            LocalDate.parse(date, DATE_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Validates date range (from <= to).
     *
     * @param fromDate start date
     * @param toDate   end date
     * @return true if valid, false otherwise
     */
    public boolean validateDateRange(String fromDate, String toDate) {
        if (!validateDateFormat(fromDate, "from") || !validateDateFormat(toDate, "to")) {
            return false;
        }

        try {
            LocalDate start = LocalDate.parse(fromDate, DATE_FORMATTER);
            LocalDate end = LocalDate.parse(toDate, DATE_FORMATTER);
            return !start.isAfter(end);
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Validates that configuration file exists and is readable.
     *
     * @param configPath path to configuration file
     * @return true if valid, false otherwise
     */
    public boolean validateConfigFileExists(String configPath) {
        if (configPath == null || configPath.isEmpty()) {
            return false;
        }

        java.io.File configFile = new java.io.File(configPath);
        return configFile.exists() && configFile.isFile() && configFile.canRead();
    }

    /**
     * Validates all command-line arguments.
     *
     * @param arguments command-line arguments
     * @return list of validation error messages (empty if valid)
     */
    public List<String> validateAll(CommandLineArguments arguments) {
        List<String> errors = new ArrayList<>();

        // Validate help flag
        if (arguments.isHelpRequested()) {
            return errors; // Help requested, no other validation needed
        }

        // Validate required parameters
        if (arguments.getFromDate() == null || arguments.getFromDate().isEmpty()) {
            errors.add("Missing required parameter: --from");
        } else if (!validateDateFormat(arguments.getFromDate(), "from")) {
            errors.add("Invalid date format for --from parameter. Expected format: YYYYMMDD (e.g., 20250101)");
        }

        if (arguments.getToDate() == null || arguments.getToDate().isEmpty()) {
            errors.add("Missing required parameter: --to");
        } else if (!validateDateFormat(arguments.getToDate(), "to")) {
            errors.add("Invalid date format for --to parameter. Expected format: YYYYMMDD (e.g., 20250101)");
        }

        if (arguments.getConfigPath() == null || arguments.getConfigPath().isEmpty()) {
            errors.add("Missing required parameter: --config");
        } else if (!validateConfigFileExists(arguments.getConfigPath())) {
            errors.add("Configuration file not found or not readable. Path: " + arguments.getConfigPath());
        }

        // Validate date range
        if (errors.isEmpty() && !validateDateRange(arguments.getFromDate(), arguments.getToDate())) {
            errors.add("Invalid date range");
        }

        return errors;
    }
}
