package com.sdd.etl.source.extract.db;

import com.sdd.etl.ETLException;
import com.sdd.etl.config.ETConfiguration;
import com.sdd.etl.context.ETLContext;
import com.sdd.etl.model.SourceDataModel;
import com.sdd.etl.source.extract.Extractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Abstract base class for database-based extractors.
 * 
 * <p>
 * Encapsulates common database operations:
 * <ol>
 * <li>Connection pooling via {@link DatabaseConnectionManager}</li>
 * <li>Configuration loading</li>
 * <li>Retry logic with exponential backoff for connection failures</li>
 * <li>Query execution with timeout and streaming</li>
 * </ol>
 */
public abstract class DatabaseExtractor implements Extractor {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseExtractor.class);

    // Configuration properties
    protected ETConfiguration.SourceConfig sourceConfig;
    protected int queryTimeoutSeconds = 300; // Default 5 minutes

    /**
     * Maps a single ResultSet row to a domain model.
     * 
     * @param rs ResultSet positioned at current row
     * @return SourceDataModel instance
     * @throws SQLException if column access fails
     */
    protected abstract SourceDataModel mapRow(ResultSet rs) throws SQLException;

    /**
     * Gets the configuration property key for the SQL template.
     * 
     * @return property key (e.g., "sql.template")
     */
    protected abstract String getSqlTemplateConfigKey();

    @Override
    public void setup(ETLContext context) throws ETLException {
        validate(context);

        // Initialize connection pool if needed sourceConfig populated in validate
        String url = sourceConfig.getProperty("db.url");
        String user = sourceConfig.getProperty("db.user");
        String password = sourceConfig.getProperty("db.password");

        int minIdle = getIntProperty(sourceConfig, "db.pool.min", 1);
        int maxTotal = getIntProperty(sourceConfig, "db.pool.max", 5);
        int poolTimeout = getIntProperty(sourceConfig, "db.pool.timeout", 30);

        this.queryTimeoutSeconds = getIntProperty(sourceConfig, "db.timeout.seconds", 300);

        // Initialize singleton pool
        DatabaseConnectionManager.getInstance().initialize(url, user, password, minIdle, maxTotal, poolTimeout);
    }

    private int getIntProperty(ETConfiguration.SourceConfig config, String key, int defaultValue) {
        String val = config.getProperty(key);
        if (val != null) {
            try {
                return Integer.parseInt(val);
            } catch (NumberFormatException e) {
                logger.warn("Invalid integer for property {}: {}", key, val);
            }
        }
        return defaultValue;
    }

    @Override
    public List<SourceDataModel> extract(ETLContext context) throws ETLException {
        String dateStr = context.getCurrentDate().format(DateTimeFormatter.BASIC_ISO_DATE);

        String sqlTemplate = sourceConfig.getProperty(getSqlTemplateConfigKey());
        if (sqlTemplate == null || sqlTemplate.isEmpty()) {
            throw new ETLException("EXTRACT", dateStr, "SQL template not found for key: " + getSqlTemplateConfigKey());
        }

        // 1. Prepare SQL
        String businessDateStr = context.getCurrentDate().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sql = sqlTemplate.replace("{BUSINESS_DATE}", businessDateStr);

        logger.info("[{}] Starting extraction. Date: {}, SQL: {}", getName(), businessDateStr, sql);

        // 2. Execute with Retry
        int maxRetries = 3;
        int attempt = 0;
        Exception lastException = null;

        while (attempt < maxRetries) {
            try {
                return executeQuery(sql);
            } catch (SQLException e) {
                lastException = e;
                attempt++;
                if (attempt < maxRetries) {
                    long delayMillis = (long) Math.pow(2, attempt - 1) * 1000;
                    logger.warn("[{}] Extraction failed (attempt {}/{}). Retrying in {} ms. Error: {}",
                            getName(), attempt, maxRetries, delayMillis, e.getMessage());
                    try {
                        TimeUnit.MILLISECONDS.sleep(delayMillis);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new ETLException("EXTRACT", dateStr, "Extraction interrupted during retry backoff", ie);
                    }
                }
            } catch (Exception e) {
                throw new ETLException("EXTRACT", dateStr, "Extraction failed with unexpected error", e);
            }
        }

        throw new ETLException("EXTRACT", dateStr, "Extraction failed after " + maxRetries + " attempts",
                lastException);
    }

    private List<SourceDataModel> executeQuery(String sql) throws SQLException {
        List<SourceDataModel> results = new ArrayList<>();
        DataSource ds = DatabaseConnectionManager.getInstance().getDataSource();

        try (Connection conn = ds.getConnection();
                Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {

            stmt.setFetchSize(Integer.MIN_VALUE); // MySQL specific: stream result set row-by-row
            stmt.setQueryTimeout(queryTimeoutSeconds);

            try (ResultSet rs = stmt.executeQuery(sql)) {
                int rowCount = 0;
                while (rs.next()) {
                    try {
                        SourceDataModel model = mapRow(rs);
                        if (model != null) {
                            if (model.validate()) {
                                results.add(model);
                            } else {
                                logger.warn("[{}] Skipped invalid record at row {}.", getName(), rowCount + 1);
                            }
                        }
                    } catch (Exception e) {
                        // FR-018: Log warning and continue
                        logger.warn("[{}] Error mapping row {}: {}", getName(), rowCount + 1, e.getMessage());
                    }
                    rowCount++;
                }
                logger.info("[{}] Extracted {} records.", getName(), results.size());
            }
        }
        return results;
    }

    @Override
    public void cleanup() throws ETLException {
        // Connection is closed (returned to pool) automatically in try-with-resources.
        // We do NOT close the DatabaseConnectionManager here as it may be shared.
    }

    @Override
    public void validate(ETLContext context) throws ETLException {
        String dateStr = context.getCurrentDate() != null
                ? context.getCurrentDate().format(DateTimeFormatter.BASIC_ISO_DATE)
                : "Unknown";

        ETConfiguration config = context.getConfig();
        // Use property based category lookup if available, or fallback to type
        this.sourceConfig = config.findSourceConfigByCategory("database", getCategory());

        if (this.sourceConfig == null) {
            // Fallback
            this.sourceConfig = config.findSourceConfig("database");
        }

        if (this.sourceConfig == null) {
            throw new ETLException("EXTRACT", dateStr, "Configuration not found for category: " + getCategory());
        }

        if (!this.sourceConfig.isValid()) {
            throw new ETLException("EXTRACT", dateStr, "Invalid source configuration for category: " + getCategory());
        }

        if (!this.sourceConfig.hasProperty("db.url"))
            throw new ETLException("EXTRACT", dateStr, "Missing property: db.url");
        if (!this.sourceConfig.hasProperty("db.user"))
            throw new ETLException("EXTRACT", dateStr, "Missing property: db.user");
        if (!this.sourceConfig.hasProperty("db.password"))
            throw new ETLException("EXTRACT", dateStr, "Missing property: db.password");
        if (!this.sourceConfig.hasProperty(getSqlTemplateConfigKey()))
            throw new ETLException("EXTRACT", dateStr, "Missing property: " + getSqlTemplateConfigKey());
    }
}
