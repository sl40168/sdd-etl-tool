package com.sdd.etl.model;

/**
 * Parsed command-line arguments from user.
 * Contains date range, configuration path, and help flag.
 */
public class CommandLineArguments {

    private String fromDate;
    private String toDate;
    private String configPath;
    private boolean helpRequested;

    /**
     * Constructs a new CommandLineArguments with all fields.
     *
     * @param fromDate       start date in YYYYMMDD format (inclusive)
     * @param toDate         end date in YYYYMMDD format (inclusive)
     * @param configPath     absolute path to INI configuration file
     * @param helpRequested  true if user requested help
     */
    public CommandLineArguments(String fromDate, String toDate, String configPath, boolean helpRequested) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.configPath = configPath;
        this.helpRequested = helpRequested;
    }

    /**
     * Gets start date.
     *
     * @return from date in YYYYMMDD format
     */
    public String getFromDate() {
        return fromDate;
    }

    /**
     * Sets start date.
     *
     * @param fromDate from date in YYYYMMDD format
     */
    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    /**
     * Gets end date.
     *
     * @return to date in YYYYMMDD format
     */
    public String getToDate() {
        return toDate;
    }

    /**
     * Sets end date.
     *
     * @param toDate to date in YYYYMMDD format
     */
    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    /**
     * Gets path to configuration file.
     *
     * @return absolute path to INI configuration file
     */
    public String getConfigPath() {
        return configPath;
    }

    /**
     * Sets path to configuration file.
     *
     * @param configPath absolute path to INI configuration file
     */
    public void setConfigPath(String configPath) {
        this.configPath = configPath;
    }

    /**
     * Gets whether user requested help.
     *
     * @return true if help was requested
     */
    public boolean isHelpRequested() {
        return helpRequested;
    }

    /**
     * Sets help requested flag.
     *
     * @param helpRequested true if help was requested
     */
    public void setHelpRequested(boolean helpRequested) {
        this.helpRequested = helpRequested;
    }
}
