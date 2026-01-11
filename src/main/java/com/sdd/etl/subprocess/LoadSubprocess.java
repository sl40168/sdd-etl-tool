package com.sdd.etl.subprocess;

import com.sdd.etl.ETLException;
import com.sdd.etl.context.ETLContext;
import com.sdd.etl.context.SubprocessType;
import com.sdd.etl.util.DateUtils;
import com.sdd.etl.loader.config.LoaderConfiguration;
import com.sdd.etl.loader.config.ConfigParser;
import com.sdd.etl.loader.dolphin.DolphinDBLoader;
import com.sdd.etl.loader.dolphin.DolphinDBScriptExecutor;
import com.sdd.etl.loader.api.Loader;
import com.sdd.etl.model.TargetDataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

/**
 * Concrete implementation of Load subprocess for DolphinDB.
 * Loads transformed data to DolphinDB using Java API.
 */
public class LoadSubprocess implements SubprocessInterface {

    private static final Logger logger = LoggerFactory.getLogger(LoadSubprocess.class);
    private static final String SCRIPT_CREATION = "scripts/temporary_table_creation.dos";

    /**
     * Executes load operation.
     * This implementation:
     * 1. Parses loader configuration from ETConfiguration
     * 2. Establishes shared DolphinDB connection
     * 3. Executes temporary table creation script
     * 4. Instantiates DolphinDBLoader with shared connection
     * 5. Sorts data by receive_time
     * 6. Loads data to appropriate tables based on dataType
     *
     * @param context ETL context containing execution state
     * @return number of records loaded
     * @throws ETLException if loading fails
     */
    @Override
    public int execute(ETLContext context) throws ETLException {
        try {
            logger.info("LoadSubprocess: Starting load operation for date {}", context.getCurrentDate());

            // Get loader configuration from ETL config
            LoaderConfiguration loaderConfig = parseLoaderConfiguration(context);

            // Establish DolphinDB connection (will be shared with CleanSubprocess)
            com.sdd.etl.loader.dolphin.DolphinDBConnection connection =
                    new com.sdd.etl.loader.dolphin.DolphinDBConnection(loaderConfig);
            connection.connect();

            // Store connection in context for sharing
            context.setDolphinDBConnection(connection);

            // Create script executor
            DolphinDBScriptExecutor scriptExecutor = new DolphinDBScriptExecutor(connection);

            // Execute temporary table creation script (LoadSubprocess responsibility per spec)
            String creationScript = readScriptResource(SCRIPT_CREATION);
            logger.info("LoadSubprocess: Executing temporary table creation script");
            scriptExecutor.executeScript(creationScript);

            // Instantiate DolphinDBLoader with the shared connection
            DolphinDBLoader loader = new DolphinDBLoader(loaderConfig);
            context.setDolphinDBLoader(loader);

            // Get transformed data from context
            @SuppressWarnings("unchecked")
            List<TargetDataModel> transformedData = (List<TargetDataModel>) context.getTransformedData();

            if (transformedData == null || transformedData.isEmpty()) {
                logger.warn("LoadSubprocess: No transformed data to load");
                return 0;
            }

            // Sort data by receive_time (FR-003)
            logger.info("LoadSubprocess: Sorting {} records by receive_time", transformedData.size());
            loader.sortData(transformedData, "receiveTime");

            // Load data to DolphinDB (FR-001, FR-004)
            logger.info("LoadSubprocess: Loading {} records to DolphinDB", transformedData.size());
            loader.loadData(transformedData);

            // Update context with loaded count
            context.setLoadedDataCount(transformedData.size());

            logger.info("LoadSubprocess: Load operation completed. Loaded {} records", transformedData.size());
            return transformedData.size();

        } catch (Exception e) {
            String errorMsg = "Failed to load data to DolphinDB: " + e.getMessage();
            logger.error("LoadSubprocess: {}", errorMsg, e);
            throw new ETLException("LOAD", DateUtils.formatDate(context.getCurrentDate()),
                    errorMsg, e);
        }
    }

    /**
     * Validates context state before loading.
     *
     * @param context ETL context to validate
     * @throws ETLException if context is invalid
     */
    @Override
    public void validateContext(ETLContext context) throws ETLException {
        logger.debug("LoadSubprocess: Validating context");

        // Ensure context has transformed data
        if (context.getTransformedData() == null) {
            throw new ETLException("LOAD", DateUtils.formatDate(context.getCurrentDate()),
                    "No transformed data found in context. Cannot load data.");
        }

        // Ensure configuration exists
        if (context.getConfig() == null) {
            throw new ETLException("LOAD", DateUtils.formatDate(context.getCurrentDate()),
                    "No ETL configuration found in context.");
        }

        // Parse loader config to validate it
        try {
            parseLoaderConfiguration(context);
        } catch (Exception e) {
            throw new ETLException("LOAD", DateUtils.formatDate(context.getCurrentDate()),
                    "Failed to parse loader configuration: " + e.getMessage(), e);
        }
    }

    /**
     * Gets type of this subprocess.
     *
     * @return SubprocessType.LOAD
     */
    @Override
    public SubprocessType getType() {
        return SubprocessType.LOAD;
    }

    /**
     * Parses LoaderConfiguration from ETConfiguration.
     * Reads DolphinDB settings from the main configuration.
     *
     * @param context ETL context
     * @return parsed LoaderConfiguration
     * @throws Exception if parsing fails
     */
    private LoaderConfiguration parseLoaderConfiguration(ETLContext context) throws Exception {
        // For now, create a basic configuration from ETConfiguration
        // In production, this would read from an INI file
        LoaderConfiguration config = new LoaderConfiguration();
        // Use defaults from config if available
        return config;
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
