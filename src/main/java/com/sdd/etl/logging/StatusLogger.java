package com.sdd.etl.logging;

import com.sdd.etl.model.SubprocessResult;

/**
 * Logger for status updates during ETL process.
 * Provides methods for logging subprocess and day completion status.
 */
public class StatusLogger {

    /**
     * Logs completion of a subprocess.
     *
     * @param date      processing date in YYYYMMDD format
     * @param subprocess type of subprocess (e.g., "EXTRACT", "TRANSFORM")
     * @param result    subprocess execution result
     */
    public void logSubprocessCompletion(String date, String subprocess, SubprocessResult result) {
        if (result.isSuccess()) {
            ETLogger.info(String.format("[%s] %s Success (%d records)",
                    date, subprocess, result.getDataCount()));
        } else {
            ETLogger.error(String.format("[%s] %s Failed: %s",
                    date, subprocess, result.getErrorMessage()));
        }
    }

    /**
     * Logs completion of a day's ETL process.
     *
     * @param date          processing date in YYYYMMDD format
     * @param subprocessCount number of subprocesses executed
     * @param success        whether the day completed successfully
     */
    public void logDayCompletion(String date, int subprocessCount, boolean success) {
        if (success) {
            ETLogger.info(String.format("[%s] Day completed successfully (%d subprocesses)", date, subprocessCount));
        } else {
            ETLogger.error(String.format("[%s] Day failed (%d subprocesses)", date, subprocessCount));
        }
    }

    /**
     * Logs an error during subprocess or day execution.
     *
     * @param date       processing date in YYYYMMDD format
     * @param subprocess type of subprocess (or null for day-level error)
     * @param error      error message
     */
    public void logError(String date, String subprocess, String error) {
        if (subprocess != null) {
            ETLogger.error(String.format("[%s] Error in %s: %s", date, subprocess, error));
        } else {
            ETLogger.error(String.format("[%s] Error: %s", date, error));
        }
    }

    /**
     * Logs workflow summary.
     *
     * @param totalDays      total days processed
     * @param successfulDays number of successful days
     * @param failedDays     number of failed days
     * @param durationMillis  total execution time in milliseconds
     */
    public void logSummary(int totalDays, int successfulDays, int failedDays, long durationMillis) {
        String duration = formatDuration(durationMillis);
        ETLogger.info(String.format("ETL Process Completed"));
        ETLogger.info(String.format("  Total Days: %d", totalDays));
        ETLogger.info(String.format("  Successful: %d", successfulDays));
        ETLogger.info(String.format("  Failed: %d", failedDays));
        ETLogger.info(String.format("  Duration: %s", duration));
    }

    /**
     * Formats duration in milliseconds to HH:mm:ss format.
     *
     * @param durationMillis duration in milliseconds
     * @return formatted duration string
     */
    private String formatDuration(long durationMillis) {
        long seconds = durationMillis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;
        long remainingSeconds = seconds % 60;

        return String.format("%02d:%02d:%02d", hours, remainingMinutes, remainingSeconds);
    }
}
