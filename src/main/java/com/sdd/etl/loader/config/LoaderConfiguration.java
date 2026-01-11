package com.sdd.etl.loader.config;

/**
 * Configuration parameters for data loading operations.
 * This class encapsulates all configuration needed by Loader implementations.
 */
public class LoaderConfiguration {

    private String host;
    private int port;
    private String username;
    private String password;
    private int batchSize;
    private String database;
    private String tableCreationScript;
    private String tableDeletionScript;

    /**
     * Creates a new LoaderConfiguration with default values.
     */
    public LoaderConfiguration() {
        this.host = "localhost";
        this.port = 8848;
        this.username = "";
        this.password = "";
        this.batchSize = 1000;
        this.database = "";
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getTableCreationScript() {
        return tableCreationScript;
    }

    public void setTableCreationScript(String tableCreationScript) {
        this.tableCreationScript = tableCreationScript;
    }

    public String getTableDeletionScript() {
        return tableDeletionScript;
    }

    public void setTableDeletionScript(String tableDeletionScript) {
        this.tableDeletionScript = tableDeletionScript;
    }

    @Override
    public String toString() {
        return "LoaderConfiguration{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", username='" + username + '\'' +
                ", batchSize=" + batchSize +
                ", database='" + database + '\'' +
                ", tableCreationScript='" + tableCreationScript + '\'' +
                ", tableDeletionScript='" + tableDeletionScript + '\'' +
                '}';
    }
}
