package com.sdd.etl.workflow;

import com.sdd.etl.ETLException;
import com.sdd.etl.config.ETConfiguration;
import com.sdd.etl.context.ContextManager;
import com.sdd.etl.context.ETLContext;
import com.sdd.etl.logging.StatusLogger;
import com.sdd.etl.logging.ETLogger;
import com.sdd.etl.model.DailyProcessResult;
import com.sdd.etl.model.WorkflowResult;
import com.sdd.etl.util.DateRangeGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * Main workflow coordinator for multi-day ETL processing.
 * Iterates through date range and orchestrates daily workflows.
 */
public class WorkflowEngine {

    private final StatusLogger statusLogger;
    private final DailyETLWorkflow dailyWorkflow;

    /**
     * Constructs a new WorkflowEngine.
     *
     * @param statusLogger logger for status updates
     * @param dailyWorkflow workflow executor for single days
     */
    public WorkflowEngine(StatusLogger statusLogger, DailyETLWorkflow dailyWorkflow) {
        this.statusLogger = statusLogger;
        this.dailyWorkflow = dailyWorkflow;
    }

    /**
     * Generates list of dates between from and to dates inclusive.
     *
     * @param fromDate start date in YYYYMMDD format
     * @param toDate end date in YYYYMMDD format
     * @return list of dates in YYYYMMDD format
     */
    List<String> generateDateRange(String fromDate, String toDate) {
        return DateRangeGenerator.generate(fromDate, toDate);
    }
    /**
     * Executes the entire multi-day ETL workflow.
     *
     * @param fromDate start date in YYYYMMDD format
     * @param toDate   end date in YYYYMMDD format
     * @param config   ETL configuration object
     * @return WorkflowResult with summary statistics and per-day results
     */
    public WorkflowResult execute(String fromDate, String toDate, ETConfiguration config) {
        WorkflowResult result = new WorkflowResult();

        try {
            long startTime = System.currentTimeMillis();

            // Generate date range
            List<String> dates = DateRangeGenerator.generate(fromDate, toDate);
            result.setStartDate(fromDate);
            result.setEndDate(toDate);

            ETLogger.info("Processing " + dates.size() + " days");

            // Process each day sequentially
            for (String date : dates) {
                DailyProcessResult dailyResult = executeDay(date, config);
                result.addDailyResult(date, dailyResult);

                // Update statistics
                result.setProcessedDays(result.getProcessedDays() + 1);

                if (dailyResult.isSuccess()) {
                    result.setSuccessfulDays(result.getSuccessfulDays() + 1);
                } else {
                    result.setFailedDays(result.getFailedDays() + 1);

                    // Stop execution - day failed
                    logSummary(result, startTime);
                    result.setSuccess(false);
                    return result;
                }
            }

            // All days processed successfully
            result.setSuccess(true);

            // Log final summary
            logSummary(result, startTime);

            return result;

        } catch (Exception e) {
            ETLogger.error("Workflow execution failed: " + e.getMessage(), e);
            result.setSuccess(false);
            logSummary(result, System.currentTimeMillis());
            return result;
        }
    }

    /**
     * Executes daily workflow for a single date.
     *
     * @param date   processing date in YYYYMMDD format
     * @param config ETL configuration object
     * @return DailyProcessResult for the date
     */
    DailyProcessResult executeDay(String date, ETConfiguration config) {
        return dailyWorkflow.execute(date, config);
    }

    /**
     * Aggregates DailyProcessResult into WorkflowResult.
     *
     * @param dailyResults list of daily results to aggregate
     * @return WorkflowResult with summary statistics
     */
    private WorkflowResult aggregateResults(List<DailyProcessResult> dailyResults) {
        WorkflowResult result = new WorkflowResult();

        int successfulDays = 0;
        int failedDays = 0;

        for (DailyProcessResult dailyResult : dailyResults) {
            result.addDailyResult(dailyResult.getDate(), dailyResult);

            if (dailyResult.isSuccess()) {
                successfulDays++;
            } else {
                failedDays++;
            }
        }

        result.setProcessedDays(dailyResults.size());
        result.setSuccessfulDays(successfulDays);
        result.setFailedDays(failedDays);
        result.setSuccess(failedDays == 0);

        return result;
    }

    /**
     * Logs final workflow summary with statistics.
     *
     * @param result          WorkflowResult to summarize
     * @param startTimeMillis  start time in milliseconds
     */
    private void logSummary(WorkflowResult result, long startTimeMillis) {
        long durationMillis = System.currentTimeMillis() - startTimeMillis;

        statusLogger.logSummary(
                result.getProcessedDays(),
                result.getSuccessfulDays(),
                result.getFailedDays(),
                durationMillis
        );
    }
}
