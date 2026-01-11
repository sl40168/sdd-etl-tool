package com.sdd.etl.source.extract.db;

import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Singleton manager for database connection pooling using DBCP2.
 */
public class DatabaseConnectionManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectionManager.class);
    private static volatile DatabaseConnectionManager instance;
    private DataSource dataSource;

    private DatabaseConnectionManager() {
        // Private constructor
    }

    public static DatabaseConnectionManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnectionManager.class) {
                if (instance == null) {
                    instance = new DatabaseConnectionManager();
                }
            }
        }
        return instance;
    }

    /**
     * Initializes the connection pool if not already initialized.
     * 
     * @param url            Database JDBC URL
     * @param user           Database username
     * @param password       Database password
     * @param minIdle        Minimum idle connections
     * @param maxTotal       Maximum total connections
     * @param timeoutSeconds Connection timeout in seconds
     */
    public synchronized void initialize(String url, String user, String password, int minIdle, int maxTotal,
            int timeoutSeconds) {
        if (dataSource != null) {
            // Check if closed? DataSource generic interface doesn't have isClosed.
            // We assume if set, it's valid unless closed explicitly.
            // For now, simple check.
            logger.warn("DataSource already initialized. Ignoring re-initialization.");
            return;
        }

        try {
            BasicDataSource ds = new BasicDataSource();
            ds.setUrl(url);
            ds.setUsername(user);
            ds.setPassword(password);
            ds.setMinIdle(minIdle);
            ds.setMaxTotal(maxTotal);
            ds.setMaxWaitMillis(timeoutSeconds * 1000L);

            // Validation settings
            ds.setValidationQuery("SELECT 1");
            ds.setTestOnBorrow(true);
            ds.setTestWhileIdle(true);

            this.dataSource = ds;
            logger.info("Initialized database connection pool: URL={}, MinIdle={}, MaxTotal={}", url, minIdle,
                    maxTotal);
        } catch (Exception e) {
            logger.error("Failed to initialize DataSource", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    /**
     * Sets a custom DataSource (e.g., for testing).
     * 
     * @param dataSource DataSource instance
     */
    public synchronized void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Gets the DataSource instance.
     * 
     * @return DataSource instance
     * @throws IllegalStateException if not initialized
     */
    public DataSource getDataSource() {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource not initialized. Call initialize() first.");
        }
        return dataSource;
    }

    /**
     * Closes the connection pool.
     */
    public synchronized void close() {
        if (dataSource != null) {
            try {
                if (dataSource instanceof BasicDataSource) {
                    ((BasicDataSource) dataSource).close();
                } else if (dataSource instanceof AutoCloseable) {
                    ((AutoCloseable) dataSource).close();
                }
                logger.info("Database connection pool closed.");
            } catch (Exception e) {
                logger.error("Error closing DataSource", e);
            }
        }
    }
}
