package com.sdd.etl.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Result of processing entire multi-day ETL workflow.
 * Contains summary statistics and per-day results.
 */
public class WorkflowResult {

    private boolean success;
    private int processedDays;
    private int successfulDays;
    private int failedDays;
    private Map<String, DailyProcessResult> dailyResults;
    private String startDate;
    private String endDate;

    /**
     * Constructs a new WorkflowResult.
     */
    public WorkflowResult() {
        this.success = false;
        this.processedDays = 0;
        this.successfulDays = 0;
        this.failedDays = 0;
        this.dailyResults = new HashMap<>();
        this.startDate = null;
        this.endDate = null;
    }

    /**
     * Gets whether all days completed successfully.
     *
     * @return true if all days succeeded, false otherwise
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Sets overall success status.
     *
     * @param success true if all days succeeded
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * Gets total number of days processed.
     *
     * @return total days processed
     */
    public int getProcessedDays() {
        return processedDays;
    }

    /**
     * Sets total number of days processed.
     *
     * @param processedDays total days processed
     */
    public void setProcessedDays(int processedDays) {
        this.processedDays = processedDays;
    }

    /**
     * Gets number of successful days.
     *
     * @return count of successful days
     */
    public int getSuccessfulDays() {
        return successfulDays;
    }

    /**
     * Sets number of successful days.
     *
     * @param successfulDays count of successful days
     */
    public void setSuccessfulDays(int successfulDays) {
        this.successfulDays = successfulDays;
    }

    /**
     * Gets number of failed days.
     *
     * @return count of failed days
     */
    public int getFailedDays() {
        return failedDays;
    }

    /**
     * Sets number of failed days.
     *
     * @param failedDays count of failed days
     */
    public void setFailedDays(int failedDays) {
        this.failedDays = failedDays;
    }

    /**
     * Gets map of daily results.
     *
     * @return map with date as key and DailyProcessResult as value
     */
    public Map<String, DailyProcessResult> getDailyResults() {
        return dailyResults;
    }

    /**
     * Adds a daily result to the map.
     *
     * @param date   processing date in YYYYMMDD format
     * @param result DailyProcessResult for the date
     */
    public void addDailyResult(String date, DailyProcessResult result) {
        this.dailyResults.put(date, result);
    }

    /**
     * Gets result for a specific date.
     *
     * @param date processing date in YYYYMMDD format
     * @return DailyProcessResult, or null if not found
     */
    public DailyProcessResult getDailyResult(String date) {
        return this.dailyResults.get(date);
    }

    /**
     * Gets first processed date.
     *
     * @return start date in YYYYMMDD format
     */
    public String getStartDate() {
        return startDate;
    }

    /**
     * Sets first processed date.
     *
     * @param startDate start date in YYYYMMDD format
     */
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    /**
     * Gets last processed date.
     *
     * @return end date in YYYYMMDD format
     */
    public String getEndDate() {
        return endDate;
    }

    /**
     * Sets last processed date.
     *
     * @param endDate end date in YYYYMMDD format
     */
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
}
