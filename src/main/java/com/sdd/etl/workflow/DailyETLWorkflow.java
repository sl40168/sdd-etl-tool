package com.sdd.etl.workflow;

import com.sdd.etl.ETLException;
import com.sdd.etl.config.ETConfiguration;
import com.sdd.etl.context.ContextManager;
import com.sdd.etl.context.ETLContext;
import com.sdd.etl.logging.StatusLogger;
import com.sdd.etl.logging.ETLogger;
import com.sdd.etl.model.DailyProcessResult;
import com.sdd.etl.model.SubprocessResult;
import com.sdd.etl.subprocess.SubprocessInterface;
import com.sdd.etl.subprocess.ExtractSubprocess.MultiSourceExtractSubprocess;
import com.sdd.etl.subprocess.LoadSubprocess;
import com.sdd.etl.subprocess.CleanSubprocess;
import com.sdd.etl.subprocess.TransformSubprocess;
import com.sdd.etl.subprocess.ValidateSubprocess;
import com.sdd.etl.util.DateUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Orchestrates all subprocesses for a single day.
 * Executes subprocesses in sequence and aggregates results.
 */
public class DailyETLWorkflow {

    private final StatusLogger statusLogger;
    private final SubprocessExecutor subprocessExecutor;

    /**
     * Constructs a new DailyETLWorkflow.
     *
     * @param statusLogger       logger for status updates
     * @param subprocessExecutor executor for subprocesses
     */
    public DailyETLWorkflow(StatusLogger statusLogger, SubprocessExecutor subprocessExecutor) {
        this.statusLogger = statusLogger;
        this.subprocessExecutor = subprocessExecutor;
    }

    /**
     * Executes the daily ETL workflow for a single date.
     *
     * @param date   processing date in YYYYMMDD format
     * @param config ETL configuration object
     * @return DailyProcessResult with all subprocess results
     */
    public DailyProcessResult execute(String date, ETConfiguration config) {
        DailyProcessResult result = new DailyProcessResult(date);

        try {
            // Create context for the day
            ETLContext context = createContext(date, config);

            // Validate initial context state
            validateInitialState(context);

            // Log start of day processing
            ETLogger.info("Processing date: " + date);

            // Execute all subprocesses in sequence
            List<SubprocessInterface> subprocesses = createSubprocesses();
            Map<String, SubprocessResult> subprocessResults =
                    subprocessExecutor.executeAll(subprocesses, context);

            // Set subprocess results in result
            for (Map.Entry<String, SubprocessResult> entry : subprocessResults.entrySet()) {
                result.addSubprocessResult(entry.getKey(), entry.getValue());
            }

            // Set final context state in result
            result.setContext(context);

            // Mark day as successful
            result.setSuccess(true);

            // Log day completion
            logCompletion(result);

            return result;

        } catch (ETLException e) {
            // Mark day as failed
            result.setSuccess(false);

            // Log failure
            statusLogger.logError(date, e.getSubprocessType(), e.getMessage());

            // Log day completion with failure
            logCompletion(result);

            return result;
        }
    }

    /**
     * Validates initial context state before executing day's workflow.
     *
     * @param context ETL context to validate
     * @throws ETLException if context is invalid
     */
    private void validateInitialState(ETLContext context) throws ETLException {
        // Basic validation
        ContextManager.validateContext(context);

        // Ensure context is in correct initial state
        if (context.getExtractedDataCount() != 0 ||
            context.getTransformedDataCount() != 0 ||
            context.getLoadedDataCount() != 0) {
            throw new ETLException("WORKFLOW", DateUtils.formatDate(context.getCurrentDate()),
                    "Context is not in initial state. All counters must be 0 before starting.");
        }
    }

    /**
     * Creates context for the day. Override in tests to provide mock context.
     *
     * @param date processing date in YYYYMMDD format
     * @param config ETL configuration object
     * @return ETL context for the day
     */
    protected ETLContext createContext(String date, ETConfiguration config) {
        return ContextManager.createContext(DateUtils.parseDate(date), config);
    }

    /**
     * Logs completion of day's workflow with subprocess results.
     *
     * @param result      DailyProcessResult with all subprocess results
     */
    private void logCompletion(DailyProcessResult result) {
        boolean success = result.isSuccess();
        String date = result.getDate();
        Map<String, SubprocessResult> subprocessResults = result.getSubprocessResults();
        
        // Log day completion summary
        ETLogger.info("Day " + date + " processing " + (success ? "succeeded" : "failed"));
        ETLogger.info("Total subprocesses executed: " + subprocessResults.size());
        
        // Log each subprocess result
        for (Map.Entry<String, SubprocessResult> entry : subprocessResults.entrySet()) {
            SubprocessResult sr = entry.getValue();
            ETLogger.info("  " + entry.getKey() + ": " + 
                (sr.isSuccess() ? "success (" + sr.getDataCount() + " records)" : 
                                 "failed - " + sr.getErrorMessage()));
        }
        
        // Also log via StatusLogger for structured logging
        statusLogger.logDayCompletion(date, subprocessResults.size(), success);
    }

    /**
     * Creates list of subprocesses to execute.
     * In production, this would instantiate concrete implementations.
     * For this phase, returns placeholder implementations.
     *
     * @return list of subprocesses in execution order
     */
    protected List<SubprocessInterface> createSubprocesses() {
        List<SubprocessInterface> subprocesses = new ArrayList<>();

        // Step 1: Extract - Extract data from source systems
        subprocesses.add(new MultiSourceExtractSubprocess());

        // Step 2: Transform - Transform source data to target format (006-data-transform)
        subprocesses.add(new TransformSubprocess());

        // Step 3: Load - Load transformed data to DolphinDB (005-dolphindb-loader)
        subprocesses.add(new LoadSubprocess());

        // Step 4: Validate - Validate loaded data (abstract - needs concrete implementation)
        // subprocesses.add(new ValidateSubprocess()); // TODO: Add concrete validation implementation

        // Step 5: Clean - Clean up temporary resources (005-dolphindb-loader)
        subprocesses.add(new CleanSubprocess());

        return subprocesses;
    }
}
