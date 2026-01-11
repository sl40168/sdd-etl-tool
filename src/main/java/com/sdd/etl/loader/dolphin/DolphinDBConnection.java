package com.sdd.etl.loader.dolphin;

import com.sdd.etl.loader.api.exceptions.ConnectionException;
import com.sdd.etl.loader.config.LoaderConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for DolphinDB connection management.
 * Handles connection lifecycle and reconnection logic.
 * 
 * Note: DBConnection is from com.xxdb package provided by DolphinDB Java API.
 */
public class DolphinDBConnection {

    private static final Logger logger = LoggerFactory.getLogger(DolphinDBConnection.class);

    private final LoaderConfiguration config;
    private Object connection; // DBConnection from DolphinDB API
    private boolean connected;

    public DolphinDBConnection(LoaderConfiguration config) {
        if (config == null) {
            throw new IllegalArgumentException("Configuration cannot be null");
        }
        this.config = config;
        this.connected = false;
    }

    /**
     * Establishes connection to DolphinDB server.
     *
     * @throws ConnectionException if connection fails
     */
    public synchronized void connect() throws ConnectionException {
        if (connected && connection != null && isConnected()) {
            logger.debug("Already connected to DolphinDB");
            return;
        }

        try {
            logger.info("Connecting to DolphinDB at {}:{}", config.getHost(), config.getPort());

            // Use reflection to create DBConnection instance
            Class<?> dbConnectionClass = Class.forName("com.xxdb.DBConnection");
            connection = dbConnectionClass.newInstance();

            // Connect with or without authentication
            if (config.getUsername() != null && !config.getUsername().isEmpty()) {
                java.lang.reflect.Method connectMethod = dbConnectionClass.getMethod(
                    "connect", String.class, int.class, String.class, String.class);
                connectMethod.invoke(connection, config.getHost(), config.getPort(),
                    config.getUsername(), config.getPassword());
            } else {
                java.lang.reflect.Method connectMethod = dbConnectionClass.getMethod(
                    "connect", String.class, int.class);
                connectMethod.invoke(connection, config.getHost(), config.getPort());
            }

            connected = true;
            logger.info("Successfully connected to DolphinDB");
        } catch (ClassNotFoundException e) {
            connected = false;
            throw new ConnectionException(
                "DolphinDB Java API not found. Please add dolphindb-java dependency to pom.xml", e);
        } catch (Exception e) {
            connected = false;
            throw new ConnectionException("Failed to connect to DolphinDB at " + config.getHost() + ":" + config.getPort(), e);
        }
    }

    /**
     * Returns the underlying DolphinDB connection.
     * Ensures connection is established first.
     *
     * @return the DBConnection instance
     * @throws ConnectionException if not connected
     */
    public synchronized Object getConnection() throws ConnectionException {
        if (!connected || connection == null) {
            connect();
        }
        return connection;
    }

    /**
     * Disconnects from DolphinDB server.
     */
    public synchronized void disconnect() {
        if (connection != null) {
            try {
                if (checkConnected()) {
                    java.lang.reflect.Method closeMethod = connection.getClass().getMethod("close");
                    closeMethod.invoke(connection);
                    logger.info("Disconnected from DolphinDB");
                }
            } catch (Exception e) {
                logger.warn("Error while disconnecting from DolphinDB: {}", e.getMessage());
            } finally {
                connected = false;
            }
        }
    }

    /**
     * Checks if currently connected to DolphinDB.
     *
     * @return true if connected, false otherwise
     */
    public synchronized boolean isConnected() {
        if (!connected || connection == null) {
            return false;
        }
        return checkConnected();
    }

    /**
     * Internal method to check connection status using reflection.
     */
    private boolean checkConnected() {
        try {
            // Use reflection to check isConnected() to avoid compile error
            java.lang.reflect.Method method = connection.getClass().getMethod("isConnected");
            return (Boolean) method.invoke(connection);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Closes resources. Safe to call multiple times.
     */
    public synchronized void shutdown() {
        disconnect();
    }
}
