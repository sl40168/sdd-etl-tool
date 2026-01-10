package com.sdd.etl.workflow;

import com.sdd.etl.ETLException;
import com.sdd.etl.context.ETLContext;
import com.sdd.etl.context.SubprocessType;
import com.sdd.etl.logging.StatusLogger;
import com.sdd.etl.model.SubprocessResult;
import com.sdd.etl.subprocess.SubprocessInterface;
import com.sdd.etl.util.DateUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Executes subprocesses in strict sequence with dependency checks.
 * Ensures each subprocess completes successfully before triggering the next.
 */
public class SubprocessExecutor {

    private final StatusLogger statusLogger;

    /**
     * Constructs a new SubprocessExecutor.
     *
     * @param statusLogger logger for subprocess status
     */
    public SubprocessExecutor(StatusLogger statusLogger) {
        this.statusLogger = statusLogger;
    }

    /**
     * Validates context state before subprocess execution.
     *
     * @param subprocess subprocess to validate context for
     * @param context    ETL context to validate
     * @throws ETLException if context validation fails
     */
    private void validateContextBeforeExecution(SubprocessInterface subprocess, ETLContext context) throws ETLException {
        subprocess.validateContext(context);
    }

    /**
     * Executes a single subprocess with context validation.
     *
     * @param subprocess subprocess to execute
     * @param context    ETL context containing execution state
     * @return subprocess result (success or failure)
     */
    public SubprocessResult execute(SubprocessInterface subprocess, ETLContext context) {
        try {
            // Set current subprocess in context
            context.setCurrentSubprocess(subprocess.getType());

            // Validate context before execution
            validateContextBeforeExecution(subprocess, context);

            // Execute subprocess
            int dataCount = subprocess.execute(context);

            // Return success result
            return new SubprocessResult(dataCount);
        } catch (ETLException e) {
            // Return failure result with error message
            return new SubprocessResult(e.getMessage());
        }
    }

    /**
     * Executes all subprocesses in strict sequence.
     * Sequence: EXTRACT → TRANSFORM → LOAD → VALIDATE → CLEAN
     *
     * @param subprocesses list of subprocesses to execute in order
     * @param context       ETL context containing execution state
     * @return map of subprocess results keyed by subprocess type
     * @throws ETLException if any subprocess fails
     */
    public Map<String, SubprocessResult> executeAll(List<SubprocessInterface> subprocesses, ETLContext context)
            throws ETLException {

        Map<String, SubprocessResult> results = new HashMap<>();

        for (SubprocessInterface subprocess : subprocesses) {
            // Execute subprocess
            SubprocessResult result = execute(subprocess, context);
            results.put(subprocess.getType().getValue(), result);

            // Log subprocess completion
            statusLogger.logSubprocessCompletion(DateUtils.formatDate(context.getCurrentDate()),
                    subprocess.getType().getValue(), result);

            // Check if subprocess succeeded
            if (!result.isSuccess()) {
                // Stop execution - subprocess failed
                throw new ETLException(subprocess.getType().getValue(),
                        DateUtils.formatDate(context.getCurrentDate()),
                        result.getErrorMessage());
            }

            // Validate previous subprocess completed successfully
            validatePreviousSubprocessCompletion(subprocess.getType(), context);
        }

        return results;
    }

    /**
     * Validates that the previous subprocess in the sequence completed successfully.
     *
     * @param currentSubprocess the subprocess about to execute
     * @param context           ETL context to validate
     * @throws ETLException if previous subprocess did not complete
     */
    private void validatePreviousSubprocessCompletion(SubprocessType currentSubprocess, ETLContext context)
            throws ETLException {

        // Check previous subprocess based on current subprocess type
        switch (currentSubprocess) {
            case TRANSFORM:
                // Require EXTRACT to have completed
                if (context.getExtractedDataCount() == 0 && context.getExtractedData() == null) {
                    throw new ETLException("TRANSFORM", DateUtils.formatDate(context.getCurrentDate()),
                            "Extract subprocess did not complete successfully");
                }
                break;

            case LOAD:
                // Require TRANSFORM to have completed
                if (context.getTransformedDataCount() == 0 && context.getTransformedData() == null) {
                    throw new ETLException("LOAD", DateUtils.formatDate(context.getCurrentDate()),
                            "Transform subprocess did not complete successfully");
                }
                break;

            case VALIDATE:
                // Require LOAD to have completed
                if (context.getLoadedDataCount() == 0) {
                    throw new ETLException("VALIDATE", DateUtils.formatDate(context.getCurrentDate()),
                            "Load subprocess did not complete successfully");
                }
                break;

            case CLEAN:
                // Require VALIDATE to have completed
                if (!context.isValidationPassed()) {
                    throw new ETLException("CLEAN", DateUtils.formatDate(context.getCurrentDate()),
                            "Validate subprocess did not complete successfully");
                }
                break;

            case EXTRACT:
                // No previous subprocess for EXTRACT
                break;
        }
    }
}
