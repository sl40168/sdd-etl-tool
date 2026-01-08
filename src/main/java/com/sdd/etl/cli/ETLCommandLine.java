package com.sdd.etl.cli;

import com.sdd.etl.ETLException;
import com.sdd.etl.config.ConfigurationLoader;
import com.sdd.etl.config.ETConfiguration;
import com.sdd.etl.logging.ETLogger;
import com.sdd.etl.logging.StatusLogger;
import com.sdd.etl.model.CommandLineArguments;
import com.sdd.etl.model.WorkflowResult;
import com.sdd.etl.subprocess.ExtractSubprocess;
import com.sdd.etl.subprocess.TransformSubprocess;
import com.sdd.etl.subprocess.LoadSubprocess;
import com.sdd.etl.subprocess.ValidateSubprocess;
import com.sdd.etl.subprocess.CleanSubprocess;
import com.sdd.etl.util.ConcurrentExecutionDetector;
import com.sdd.etl.util.DateRangeGenerator;
import com.sdd.etl.workflow.DailyETLWorkflow;
import com.sdd.etl.workflow.SubprocessExecutor;
import com.sdd.etl.workflow.WorkflowEngine;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.util.Arrays;
import java.util.List;

/**
 * Main entry point for ETL command-line tool.
 * Parses command-line arguments, validates input, and executes ETL workflow.
 */
public class ETLCommandLine {

    private static final String VERSION = "1.0.0";

    /**
     * Main method - entry point for CLI tool.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        try {
            // Parse command-line arguments
            CommandLineArguments arguments = parseArguments(args);

            // Check if help was requested
            if (arguments.isHelpRequested()) {
                displayHelp();
                System.exit(0);
            }

            // Validate and execute
            validateAndExecute(arguments);

        } catch (Exception e) {
            ETLogger.error("Unexpected error: " + e.getMessage(), e);
            System.exit(5);
        }
    }

    /**
     * Parses command-line arguments.
     *
     * @param args command-line arguments
     * @return parsed CommandLineArguments object
     * @throws ParseException if parsing fails
     */
    static CommandLineArguments parseArguments(String[] args) throws ParseException {
        Options options = createOptions();
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            // Return empty arguments object for invalid input
            return new CommandLineArguments(null, null, null, false);
        }

        // Extract arguments, handle duplicates by taking last value
        String fromDate = null;
        String[] fromValues = cmd.getOptionValues("from");
        if (fromValues != null && fromValues.length > 0) {
            fromDate = fromValues[fromValues.length - 1];
        }
        
        String toDate = null;
        String[] toValues = cmd.getOptionValues("to");
        if (toValues != null && toValues.length > 0) {
            toDate = toValues[toValues.length - 1];
        }
        
        String configPath = null;
        String[] configValues = cmd.getOptionValues("config");
        if (configValues != null && configValues.length > 0) {
            configPath = configValues[configValues.length - 1];
        }
        
        boolean helpRequested = cmd.hasOption("help");

        return new CommandLineArguments(fromDate, toDate, configPath, helpRequested);
    }

    /**
     * Creates command-line options.
     *
     * @return Options object with all configured options
     */
    private static Options createOptions() {
        Options options = new Options();

        options.addOption(Option.builder("f")
                .longOpt("from")
                .hasArg()
                .argName("YYYYMMDD")
                .desc("Inclusive start date (format: YYYYMMDD)")
                .build());

        options.addOption(Option.builder("t")
                .longOpt("to")
                .hasArg()
                .argName("YYYYMMDD")
                .desc("Inclusive end date (format: YYYYMMDD)")
                .build());

        options.addOption(Option.builder("c")
                .longOpt("config")
                .hasArg()
                .argName("path")
                .desc("Absolute path to INI configuration file")
                .build());

        options.addOption(Option.builder("h")
                .longOpt("help")
                .desc("Display this help message")
                .build());

        return options;
    }

    /**
     * Displays help message with usage information.
     */
    private static void displayHelp() {
        Options options = createOptions();
        HelpFormatter formatter = new HelpFormatter();

        System.out.println("ETL Tool - Extract, Transform, Load data across multiple dates");
        System.out.println();
        System.out.println("Usage:");
        System.out.println("  java -jar etl-tool-1.0.0.jar --from <YYYYMMDD> --to <YYYYMMDD> --config <path>");
        System.out.println();
        System.out.println("Required Parameters:");
        System.out.println("  --from <YYYYMMDD>    Inclusive start date (format: YYYYMMDD)");
        System.out.println("  --to <YYYYMMDD>      Inclusive end date (format: YYYYMMDD)");
        System.out.println("  --config <path>      Absolute path to INI configuration file");
        System.out.println();
        System.out.println("Optional Parameters:");
        System.out.println("  --help               Display this help message");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java -jar etl-tool-1.0.0.jar --from 20250101 --to 20250107 --config /path/to/config.ini");
        System.out.println("  java -jar etl-tool-1.0.0.jar --help");
        System.out.println();
        System.out.println("Exit Codes:");
        System.out.println("  0 - Success");
        System.out.println("  1 - Input validation error");
        System.out.println("  2 - Concurrent execution detected");
        System.out.println("  3 - ETL process error");
        System.out.println("  4 - Configuration error");
        System.out.println("  5 - Unexpected error");
    }

    /**
     * Validates input and executes ETL workflow.
     *
     * @param arguments command-line arguments
     */
    private static void validateAndExecute(CommandLineArguments arguments) {
        CommandLineValidator validator = new CommandLineValidator();

        // Validate all arguments
        List<String> errors = validator.validateAll(arguments);

        if (!errors.isEmpty()) {
            // Display validation errors
            ETLogger.error("Input validation failed:");
            for (String error : errors) {
                ETLogger.error("  " + error);
            }
            System.exit(1);
        }

        // Detect concurrent execution
        ConcurrentExecutionDetector detector = new ConcurrentExecutionDetector();
        if (!detector.acquireLock()) {
            ETLogger.error("Another ETL process is already running.");
            ETLogger.error("Please wait for it to complete before starting a new process.");
            ETLogger.error("Lock file: " + detector.getLockFilePath());
            System.exit(2);
        }

        try {
            // Load configuration
            ConfigurationLoader configLoader = new ConfigurationLoader();
            ETConfiguration config = configLoader.load(arguments.getConfigPath());

            // Log startup banner
            ETLogger.info("ETL Tool v" + VERSION);
            ETLogger.info("Starting ETL process...");
            ETLogger.info("  From: " + arguments.getFromDate());
            ETLogger.info("  To:  " + arguments.getToDate());
            ETLogger.info("  Config: " + arguments.getConfigPath());
            ETLogger.info("");

            // Execute workflow
            StatusLogger statusLogger = new StatusLogger();
            SubprocessExecutor subprocessExecutor = new SubprocessExecutor(statusLogger);
            DailyETLWorkflow dailyWorkflow = new DailyETLWorkflow(statusLogger, subprocessExecutor);
            WorkflowEngine workflowEngine = new WorkflowEngine(statusLogger, dailyWorkflow);

            WorkflowResult result = workflowEngine.execute(
                    arguments.getFromDate(),
                    arguments.getToDate(),
                    config
            );

            // Check final result
            if (result.isSuccess()) {
                System.exit(0);
            } else {
                ETLogger.error("ETL process failed. See logs for details.");
                System.exit(3);
            }

        } catch (ETLException e) {
            ETLogger.error("ETL process error: " + e.getMessage());
            ETLogger.error("  Subprocess: " + e.getSubprocessType());
            ETLogger.error("  Date: " + e.getDate());
            if (e.getRootCause() != null) {
                ETLogger.error("  Root Cause: " + e.getRootCause().getMessage());
            }
            System.exit(3);

        } catch (Exception e) {
            ETLogger.error("Configuration error: " + e.getMessage(), e);
            System.exit(4);

        } finally {
            // Release lock
            detector.releaseLock();
        }
    }
}
