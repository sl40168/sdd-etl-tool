package com.sdd.etl.loader.dolphin;

import com.sdd.etl.loader.api.Loader;
import com.sdd.etl.loader.api.exceptions.LoaderException;
import com.sdd.etl.loader.api.exceptions.DataLoadingException;
import com.sdd.etl.loader.config.LoaderConfiguration;
import com.sdd.etl.model.TargetDataModel;
import com.sdd.etl.loader.dolphin.sort.ExternalSorter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Main DolphinDB loader implementation.
 * Sorts data and loads it to the appropriate tables based on dataType.
 */
public class DolphinDBLoader implements Loader {

    private static final Logger logger = LoggerFactory.getLogger(DolphinDBLoader.class);

    private static final String TABLE_XBOND_QUOTE = "xbond_quote_stream_temp";
    private static final String TABLE_XBOND_TRADE = "xbond_trade_stream_temp";
    private static final String TABLE_BOND_FUTURE_QUOTE = "fut_market_price_stream_temp";

    private final DolphinDBConnection connection;
    private final DolphinDBScriptExecutor scriptExecutor;
    private final ExternalSorter externalSorter;

    private boolean shutdown;

    public DolphinDBLoader(LoaderConfiguration config) {
        if (config == null) {
            throw new IllegalArgumentException("Configuration cannot be null");
        }
        this.connection = new DolphinDBConnection(config);
        this.scriptExecutor = new DolphinDBScriptExecutor(connection);
        this.externalSorter = new ExternalSorter(512 * 1024 * 1024, null); // 512MB memory limit
        this.shutdown = false;
    }

    @Override
    public void sortData(List<? extends TargetDataModel> data, String sortFieldName) throws LoaderException {
        if (shutdown) {
            throw new LoaderException("Loader has been shut down");
        }

        if (data == null) {
            throw new LoaderException("Data cannot be null");
        }

        if (sortFieldName == null || sortFieldName.trim().isEmpty()) {
            throw new LoaderException("Sort field name cannot be null or empty");
        }

        if (data.isEmpty()) {
            logger.info("No data to sort");
            return;
        }

        logger.info("Sorting {} records by field '{}'", data.size(), sortFieldName);

        try {
            // Estimate memory requirement
            long estimatedSize = data.size() * 500L; // 500 bytes per record

            if (estimatedSize < externalSorter.getMemoryLimit()) {
                // In-memory sort
                inMemorySort(data, sortFieldName);
            } else {
                // External sort (disk-based)
                externalSorter.sort(data, sortFieldName);
            }

            logger.info("Sort completed for {} records", data.size());
        } catch (Exception e) {
            throw new LoaderException("Failed to sort data by field: " + sortFieldName, e);
        }
    }

    @Override
    public void loadData(List<? extends TargetDataModel> data) throws LoaderException {
        if (shutdown) {
            throw new LoaderException("Loader has been shut down");
        }

        if (data == null) {
            throw new LoaderException("Data cannot be null");
        }

        if (data.isEmpty()) {
            logger.info("No data to load");
            return;
        }

        logger.info("Loading {} records to DolphinDB", data.size());

        try {
            // Load each record directly to its corresponding table
            for (TargetDataModel record : data) {
                String dataType = record.getDataType();
                String tableName = getTableNameForDataType(dataType);
                loadSingleRecordToTable(record, tableName);
            }

            logger.info("Data loading completed. Total records loaded: {}", data.size());
        } catch (Exception e) {
            throw new DataLoadingException("Failed to load data to DolphinDB: " + e.getMessage(), e);
        }
    }

    @Override
    public void shutdown() {
        if (shutdown) {
            return;
        }

        logger.info("Shutting down DolphinDB loader");
        shutdown = true;

        if (scriptExecutor != null) {
            // Script executor cleanup if needed
        }

        if (connection != null) {
            connection.shutdown();
        }

        logger.info("DolphinDB loader shutdown complete");
    }

    /**
     * Loads a single record to the specified table.
     */
    private void loadSingleRecordToTable(TargetDataModel record, String tableName) throws LoaderException {
        if (record == null) {
            return;
        }

        try {
            Object conn = connection.getConnection();

            // Convert to column-based format
            Map<String, Object> columns = DataConverter.convertSingleRecordToColumns(record);

            // Build insert statement
            StringBuilder insertSql = new StringBuilder();
            insertSql.append("tableInsert(").append(tableName);

            // Convert columns to DolphinDB format and build argument list
            List<Object> args = new ArrayList<>();

            for (Map.Entry<String, Object> entry : columns.entrySet()) {
                args.add(entry.getValue());
            }

            // Execute insert using reflection to avoid compile error with DBConnection
            if (!args.isEmpty()) {
                String argStr = args.toString();
                insertSql.append(", ").append(argStr.substring(1, argStr.length() - 1));
                insertSql.append(")");

                java.lang.reflect.Method method = conn.getClass().getMethod("run", String.class);
                method.invoke(conn, insertSql.toString());
                logger.debug("Inserted 1 record to table {}", tableName);
            }
        } catch (Exception e) {
            throw new DataLoadingException("Failed to load data to table " + tableName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Gets the target table name for a given data type.
     */
    private String getTableNameForDataType(String dataType) throws LoaderException {
        switch (dataType) {
            case "XbondQuote":
                return TABLE_XBOND_QUOTE;
            case "XbondTrade":
                return TABLE_XBOND_TRADE;
            case "BondFutureQuote":
                return TABLE_BOND_FUTURE_QUOTE;
            default:
                throw new LoaderException("Unknown data type: " + dataType);
        }
    }

    /**
     * In-memory sorting for smaller datasets.
     */
    private void inMemorySort(List<? extends TargetDataModel> data, String sortFieldName) throws LoaderException {
        try {
            Collections.sort(data, (a, b) -> {
                try {
                    return compareByField(a, b, sortFieldName);
                } catch (Exception e) {
                    logger.debug("Error comparing items: {}", e.getMessage());
                    return 0;
                }
            });
        } catch (Exception e) {
            throw new LoaderException("In-memory sort failed", e);
        }
    }

    @SuppressWarnings("unchecked")
    private int compareByField(TargetDataModel a, TargetDataModel b, String fieldName)
            throws Exception {
        Object valueA = getFieldValue(a, fieldName);
        Object valueB = getFieldValue(b, fieldName);

        if (valueA == null && valueB == null) {
            return 0;
        } else if (valueA == null) {
            return -1;
        } else if (valueB == null) {
            return 1;
        }

        if (valueA instanceof Comparable && valueB instanceof Comparable) {
            return ((Comparable<Object>) valueA).compareTo(valueB);
        }

        return 0;
    }

    private Object getFieldValue(TargetDataModel model, String fieldName) throws Exception {
        try {
            java.lang.reflect.Field field = model.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(model);
        } catch (NoSuchFieldException e) {
            // Try superclass
            Class<?> superClass = model.getClass().getSuperclass();
            if (superClass != null) {
                try {
                    java.lang.reflect.Field field = superClass.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    return field.get(model);
                } catch (NoSuchFieldException ex) {
                    return null;
                }
            }
            return null;
        }
    }
}
