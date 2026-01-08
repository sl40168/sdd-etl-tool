package com.sdd.etl.model;

import java.util.HashMap;
import java.util.Map;

import com.sdd.etl.context.ETLContext;

/**
 * Result of processing a single day's ETL workflow.
 * Contains success status, date, subprocess results, and final context state.
 */
public class DailyProcessResult {

    private String date;
    private boolean success;
    private Map<String, SubprocessResult> subprocessResults;
    private ETLContext context;

    /**
     * Constructs a new DailyProcessResult for specified date.
     *
     * @param date processing date in YYYYMMDD format
     */
    public DailyProcessResult(String date) {
        this.date = date;
        this.success = false;
        this.subprocessResults = new HashMap<>();
        this.context = null;
    }

    /**
     * Gets processing date.
     *
     * @return date in YYYYMMDD format
     */
    public String getDate() {
        return date;
    }

    /**
     * Sets processing date.
     *
     * @param date date in YYYYMMDD format
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * Gets whether all subprocesses completed successfully.
     *
     * @return true if all subprocesses succeeded, false otherwise
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Sets overall success status for the day.
     *
     * @param success true if all subprocesses succeeded
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * Gets map of subprocess results.
     *
     * @return map with subprocess type as key and SubprocessResult as value
     */
    public Map<String, SubprocessResult> getSubprocessResults() {
        return subprocessResults;
    }

    /**
     * Adds a subprocess result to the map.
     *
     * @param subprocessType type of subprocess (e.g., "EXTRACT", "TRANSFORM")
     * @param result      subprocess result
     */
    public void addSubprocessResult(String subprocessType, SubprocessResult result) {
        this.subprocessResults.put(subprocessType, result);
    }

    /**
     * Gets result for a specific subprocess.
     *
     * @param subprocessType type of subprocess
     * @return SubprocessResult, or null if not found
     */
    public SubprocessResult getSubprocessResult(String subprocessType) {
        return this.subprocessResults.get(subprocessType);
    }

    /**
     * Gets final context state after processing.
     *
     * @return ETLContext with final state, or null if not set
     */
    public ETLContext getContext() {
        return context;
    }

    /**
     * Sets final context state.
     *
     * @param context ETLContext with final state
     */
    public void setContext(ETLContext context) {
        this.context = context;
    }
}
