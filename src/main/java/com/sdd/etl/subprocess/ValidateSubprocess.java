package com.sdd.etl.subprocess;

import com.sdd.etl.ETLException;
import com.sdd.etl.context.ETLContext;
import com.sdd.etl.context.SubprocessType;
import com.sdd.etl.util.DateUtils;
import com.sdd.etl.loader.dolphin.DolphinDBConnection;
import com.xxdb.data.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Concrete implementation of Validate subprocess for DolphinDB.
 * Validates loaded data by querying DolphinDB and comparing counts.
 */
public class ValidateSubprocess implements SubprocessInterface {

    private static final Logger logger = LoggerFactory.getLogger(ValidateSubprocess.class);

    /**
     * Date formatter for DolphinDB DATE type (YYYY.MM.DD format).
     */
    private static final DateTimeFormatter DOLPHINDB_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy.MM.dd");

    /**
     * Executes validation operation.
     * Validates loaded data counts against DolphinDB query results.
     *
     * @param context ETL context containing execution state
     * @return number of records validated (0 if validation fails)
     * @throws ETLException if validation process fails
     */
    @Override
    public int execute(ETLContext context) throws ETLException {
        try {
            logger.info("ValidateSubprocess: Starting validation for date {}", context.getCurrentDate());

            // Get DolphinDB connection from context (shared from LoadSubprocess)
            Object connectionObj = context.getDolphinDBConnection();
            if (connectionObj == null) {
                throw new ETLException("VALIDATE", DateUtils.formatDate(context.getCurrentDate()),
                        "No DolphinDB connection found in context. LoadSubprocess must run first.");
            }

            if (!(connectionObj instanceof com.sdd.etl.loader.dolphin.DolphinDBConnection)) {
                throw new ETLException("VALIDATE", DateUtils.formatDate(context.getCurrentDate()),
                        "Invalid DolphinDB connection type in context.");
            }

            DolphinDBConnection connection = (DolphinDBConnection) connectionObj;
            com.xxdb.DBConnection dbConn = connection.getConnection();

            // Format current date for DolphinDB query (YYYY.MM.DD format)
            String businessDate = context.getCurrentDate().format(DOLPHINDB_DATE_FORMATTER);

            logger.info("ValidateSubprocess: Querying DolphinDB for business_date = {}", businessDate);

            // Execute validation query
            // Note: Using `market_price` as table name (backticks are for DolphinDB identifier)
            String query = String.format(
                    "select count(*) from loadTable(\"dfs://Zing_MDS\", `market_price) " +
                            "where business_date = %s",
                    businessDate);

            logger.debug("ValidateSubprocess: Executing query: {}", query);

            Entity result = dbConn.run(query);

            // Extract count from result
            int actualCount = extractCountFromResult(result);
            logger.info("ValidateSubprocess: Actual count from DolphinDB: {}", actualCount);

            // Get expected counts from context
            int transformedCount = context.getTransformedDataCount();
            int extractedCount = context.getExtractedDataCount();

            logger.info("ValidateSubprocess: Expected counts - transformed={}, extracted={}",
                    transformedCount, extractedCount);

            // Validate counts
            List<String> errors = new ArrayList<>();
            boolean validationPassed = true;

            // Check against transformed count
            if (actualCount != transformedCount) {
                errors.add(String.format(
                        "DolphinDB count mismatch: actual=%d, expected (transformed)=%d",
                        actualCount, transformedCount));
                validationPassed = false;
            } else {
                logger.info("ValidateSubprocess: Transformed count matches (count={})", transformedCount);
            }

            // Check against extracted count
            if (actualCount != extractedCount) {
                errors.add(String.format(
                        "DolphinDB count mismatch: actual=%d, expected (extracted)=%d",
                        actualCount, extractedCount));
                validationPassed = false;
            } else {
                logger.info("ValidateSubprocess: Extracted count matches (count={})", extractedCount);
            }

            // Update context with validation results
            context.setValidationPassed(validationPassed);
            context.setValidationErrors(errors);

            if (!validationPassed) {
                String errorMsg = "Validation failed. Errors: " + String.join("; ", errors);
                logger.error("ValidateSubprocess: {}", errorMsg);
                throw new ETLException("VALIDATE", DateUtils.formatDate(context.getCurrentDate()),
                        errorMsg);
            }

            logger.info("ValidateSubprocess: Validation passed successfully");
            return actualCount;

        } catch (ETLException e) {
            throw e;
        } catch (Exception e) {
            String errorMsg = "Failed to validate data: " + e.getMessage();
            logger.error("ValidateSubprocess: {}", errorMsg, e);
            throw new ETLException("VALIDATE", DateUtils.formatDate(context.getCurrentDate()),
                    errorMsg, e);
        }
    }

    /**
     * Validates context state before validation.
     *
     * @param context ETL context to validate
     * @throws ETLException if context is invalid
     */
    @Override
    public void validateContext(ETLContext context) throws ETLException {
        logger.debug("ValidateSubprocess: Validating context");

        // Ensure context has loaded data count (LoadSubprocess must have run)
        if (context.getLoadedDataCount() == 0) {
            throw new ETLException("VALIDATE", DateUtils.formatDate(context.getCurrentDate()),
                    "No loaded data found in context. LoadSubprocess must run first.");
        }

        // Ensure context has DolphinDB connection
        if (context.getDolphinDBConnection() == null) {
            throw new ETLException("VALIDATE", DateUtils.formatDate(context.getCurrentDate()),
                    "No DolphinDB connection found in context. LoadSubprocess must run first.");
        }

        logger.debug("ValidateSubprocess: Context validation passed");
    }

    /**
     * Gets the type of this subprocess.
     *
     * @return SubprocessType.VALIDATE
     */
    @Override
    public SubprocessType getType() {
        return SubprocessType.VALIDATE;
    }

    /**
     * Extracts count value from DolphinDB query result.
     *
     * @param result Entity result from DolphinDB query
     * @return count as integer
     * @throws Exception if result cannot be parsed
     */
    private int extractCountFromResult(Entity result) throws Exception {
        if (result == null) {
            throw new Exception("Query result is null");
        }

        // DolphinDB run() returns Entity, check if it's a scalar number
        if (result instanceof com.xxdb.data.BasicInt) {
            com.xxdb.data.BasicInt intResult = (com.xxdb.data.BasicInt) result;
            return intResult.getInt();
        } else if (result instanceof com.xxdb.data.BasicLong) {
            com.xxdb.data.BasicLong longResult = (com.xxdb.data.BasicLong) result;
            return (int) longResult.getLong();
        } else if (result instanceof com.xxdb.data.BasicDouble) {
            com.xxdb.data.BasicDouble doubleResult = (com.xxdb.data.BasicDouble) result;
            return (int) doubleResult.getDouble();
        } else if (result instanceof com.xxdb.data.Table) {
            // Table result - get first row, first column
            com.xxdb.data.Table table = (com.xxdb.data.Table) result;
            if (table.rows() > 0 && table.columns() > 0) {
                try {
                    Entity cell = table.getColumn(0).get(0);
                    if (cell instanceof com.xxdb.data.BasicInt) {
                        return ((com.xxdb.data.BasicInt) cell).getInt();
                    } else if (cell instanceof com.xxdb.data.BasicLong) {
                        return (int) ((com.xxdb.data.BasicLong) cell).getLong();
                    } else if (cell instanceof com.xxdb.data.BasicDouble) {
                        return (int) ((com.xxdb.data.BasicDouble) cell).getDouble();
                    } else {
                        // Fallback: convert to string and parse
                        String cellStr = cell.getString();
                        return Integer.parseInt(cellStr.trim());
                    }
                } catch (Exception e) {
                    throw new Exception("Failed to extract cell from table: " + e.getMessage(), e);
                }
            }
            throw new Exception("Table result is empty");
        } else {
            // Try to convert to string and parse as integer
            String resultStr = result.getString();
            if (resultStr != null) {
                return Integer.parseInt(resultStr.trim());
            }
            throw new Exception("Cannot convert result to integer. Type: " + result.getClass().getName());
        }
    }
}
