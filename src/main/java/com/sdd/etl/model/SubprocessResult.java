package com.sdd.etl.model;

/**
 * Result returned by each subprocess execution.
 * Contains success status, data count, error message, and timestamp.
 */
public class SubprocessResult {

    private boolean success;
    private int dataCount;
    private String errorMessage;
    private long timestamp;

    /**
     * Constructs a successful SubprocessResult with data count.
     *
     * @param dataCount number of records processed
     */
    public SubprocessResult(int dataCount) {
        this.success = true;
        this.dataCount = dataCount;
        this.errorMessage = null;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Constructs a failed SubprocessResult with error message.
     *
     * @param errorMessage description of error that occurred
     */
    public SubprocessResult(String errorMessage) {
        this.success = false;
        this.dataCount = 0;
        this.errorMessage = errorMessage;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Gets whether subprocess completed successfully.
     *
     * @return true if successful, false otherwise
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Sets success status.
     *
     * @param success success status
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * Gets number of records processed.
     *
     * @return data count
     */
    public int getDataCount() {
        return dataCount;
    }

    /**
     * Sets data count.
     *
     * @param dataCount number of records processed
     */
    public void setDataCount(int dataCount) {
        this.dataCount = dataCount;
    }

    /**
     * Gets error message if subprocess failed.
     *
     * @return error message, or null if successful
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets error message and marks result as failed.
     *
     * @param errorMessage description of error
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        this.success = false;
    }

    /**
     * Gets execution timestamp.
     *
     * @return timestamp in milliseconds since epoch
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Sets execution timestamp.
     *
     * @param timestamp timestamp in milliseconds since epoch
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
