package com.sdd.etl.subprocess;

import com.sdd.etl.ETLException;
import com.sdd.etl.context.ETLContext;
import com.sdd.etl.context.SubprocessType;
import com.sdd.etl.util.DateUtils;
import com.sdd.etl.loader.dolphin.DolphinDBScriptExecutor;
import com.sdd.etl.loader.api.Loader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Concrete implementation of Clean subprocess for DolphinDB.
 * Executes temporary table deletion script after validation.
 */
public class CleanSubprocess implements SubprocessInterface {

    private static final Logger logger = LoggerFactory.getLogger(CleanSubprocess.class);
    private static final String SCRIPT_DELETION = "scripts/temporary_table_deletion.dos";

    /**
     * Executes cleanup operation.
     * This implementation:
     * 1. Gets shared DolphinDB connection from context
     * 2. Executes temporary table deletion script
     * 3. Calls loader.shutdown() to release resources
     *
     * @param context ETL context containing execution state
     * @return 0 (cleanup operations typically return 0)
     * @throws ETLException if cleanup fails
     */
    @Override
    public int execute(ETLContext context) throws ETLException {
        try {
            logger.info("CleanSubprocess: Starting cleanup operation for date {}", context.getCurrentDate());

            // Get shared DolphinDB connection from context (established by LoadSubprocess)
            Object connectionObj = context.getDolphinDBConnection();
            if (connectionObj == null) {
                throw new ETLException("CLEAN", DateUtils.formatDate(context.getCurrentDate()),
                        "No DolphinDB connection found in context. Cannot execute cleanup.");
            }

            com.sdd.etl.loader.dolphin.DolphinDBConnection connection =
                    (com.sdd.etl.loader.dolphin.DolphinDBConnection) connectionObj;

            // Create script executor with shared connection
            DolphinDBScriptExecutor scriptExecutor = new DolphinDBScriptExecutor(connection);

            // Execute temporary table deletion script (CleanSubprocess responsibility per spec)
            String deletionScript = readScriptResource(SCRIPT_DELETION);
            logger.info("CleanSubprocess: Executing temporary table deletion script");
            scriptExecutor.executeScript(deletionScript);

            // Get loader from context and shutdown (FR-006)
            Loader loader = (Loader) context.getDolphinDBLoader();
            if (loader != null) {
                logger.info("CleanSubprocess: Shutting down DolphinDB loader");
                loader.shutdown();
            } else {
                logger.warn("CleanSubprocess: No DolphinDB loader found in context");
            }

            // Mark cleanup as performed in context
            context.setCleanupPerformed(true);

            logger.info("CleanSubprocess: Cleanup operation completed successfully");
            return 0;

        } catch (Exception e) {
            String errorMsg = "Failed to execute cleanup: " + e.getMessage();
            logger.error("CleanSubprocess: {}", errorMsg, e);
            throw new ETLException("CLEAN", DateUtils.formatDate(context.getCurrentDate()),
                    errorMsg, e);
        }
    }

    /**
     * Validates context state before cleanup.
     *
     * @param context ETL context to validate
     * @throws ETLException if context is invalid
     */
    @Override
    public void validateContext(ETLContext context) throws ETLException {
        logger.debug("CleanSubprocess: Validating context");

        // Ensure validation has passed before cleanup (FR-007, SC-002)
        if (!context.isValidationPassed()) {
            throw new ETLException("CLEAN", DateUtils.formatDate(context.getCurrentDate()),
                    "Validation failed. Cannot perform cleanup. Manual intervention required.");
        }

        // Ensure DolphinDB connection exists (set by LoadSubprocess)
        if (context.getDolphinDBConnection() == null) {
            throw new ETLException("CLEAN", DateUtils.formatDate(context.getCurrentDate()),
                    "No DolphinDB connection found in context. LoadSubprocess may have failed.");
        }
    }

    /**
     * Gets type of this subprocess.
     *
     * @return SubprocessType.CLEAN
     */
    @Override
    public SubprocessType getType() {
        return SubprocessType.CLEAN;
    }

    /**
     * Reads a script resource file from classpath.
     *
     * @param scriptPath resource path to script
     * @return script content as string
     * @throws Exception if script cannot be read
     */
    private String readScriptResource(String scriptPath) throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(scriptPath);
        if (inputStream == null) {
            throw new Exception("Script resource not found: " + scriptPath);
        }

        try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name())) {
            return scanner.useDelimiter("\\A").next();
        } finally {
            inputStream.close();
        }
    }
}
