package com.sdd.etl.loader.dolphin;

import com.sdd.etl.loader.api.exceptions.ConnectionException;
import com.sdd.etl.loader.config.LoaderConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.xxdb.DBConnection;

import java.io.IOException;

/**
 * Wrapper for DolphinDB connection management.
 * Handles connection lifecycle and reconnection logic.
 *
 * Note: Uses DBConnection from com.xxdb package provided by DolphinDB Java API.
 */
public class DolphinDBConnection {

    private static final Logger logger = LoggerFactory.getLogger(DolphinDBConnection.class);

    private final LoaderConfiguration config;
    private DBConnection connection;
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

            // Create DBConnection instance
            connection = new DBConnection();

            // Connect with or without authentication
            if (config.getUsername() != null && !config.getUsername().isEmpty()) {
                connection.connect(config.getHost(), config.getPort(),
                    config.getUsername(), config.getPassword());
            } else {
                connection.connect(config.getHost(), config.getPort());
            }

            connected = true;
            logger.info("Successfully connected to DolphinDB");
        } catch (IOException e) {
            connected = false;
            throw new ConnectionException(
                "Failed to connect to DolphinDB at " + config.getHost() + ":" + config.getPort() +
                ". Check if server is running and credentials are correct.", e);
        }
    }

    /**
     * Returns the underlying DolphinDB connection.
     * Ensures connection is established first.
     *
     * @return the DBConnection instance
     * @throws ConnectionException if not connected
     */
    public synchronized DBConnection getConnection() throws ConnectionException {
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
                connection.close();
                logger.info("Disconnected from DolphinDB");
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
        return connected && connection != null && connection.isConnected();
    }

    /**
     * Closes resources. Safe to call multiple times.
     */
    public synchronized void shutdown() {
        disconnect();
    }
}
