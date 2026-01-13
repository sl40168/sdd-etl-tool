package com.sdd.etl.subprocess;

import com.sdd.etl.ETLException;
import com.sdd.etl.config.ETConfiguration;
import com.sdd.etl.context.ETLContext;
import com.sdd.etl.context.SubprocessType;
import com.sdd.etl.loader.config.LoaderConfiguration;
import com.sdd.etl.loader.dolphin.DolphinDBLoader;
import com.sdd.etl.loader.dolphin.DolphinDBScriptExecutor;
import com.sdd.etl.model.TargetDataModel;
import com.sdd.etl.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
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
            logger.info("LoadSubprocess: Loader configuration - host={}, port={}, username={}",
                    loaderConfig.getHost(), loaderConfig.getPort(), loaderConfig.getUsername());

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
            List<TargetDataModel> transformedData = context.getTransformedData();

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
     * Reads DolphinDB settings from the first target configuration of type 'dolphindb'.
     * Extracts DolphinDB-specific properties from the target's properties map.
     *
     * @param context ETL context
     * @return parsed LoaderConfiguration
     * @throws Exception if parsing fails or no DolphinDB target found
     */
    private LoaderConfiguration parseLoaderConfiguration(ETLContext context) throws Exception {
        logger.debug("LoadSubprocess: Parsing loader configuration");

        // Get the first DolphinDB target from configuration
        ETConfiguration.TargetConfig targetConfig = findDolphinDBTarget(context);

        if (targetConfig == null) {
            throw new Exception("No DolphinDB target found in configuration. "
                    + "Please add a [target] section with type=dolphindb in config file.");
        }

        logger.debug("LoadSubprocess: Found target config - name={}, type={}, connectionString={}",
                targetConfig.getName(), targetConfig.getType(), targetConfig.getConnectionString());

        LoaderConfiguration config = new LoaderConfiguration();

        // Load standard fields
        if (targetConfig.getConnectionString() != null && !targetConfig.getConnectionString().isEmpty()) {
            // If connectionString is provided, parse it (format: host:port)
            String connString = targetConfig.getConnectionString();
            String[] parts = connString.split(":");
            if (parts.length >= 1) {
                config.setHost(parts[0]);
            }
            if (parts.length >= 2) {
                try {
                    config.setPort(Integer.parseInt(parts[1]));
                } catch (NumberFormatException e) {
                    // Keep default port
                }
            }
        }

        config.setBatchSize(targetConfig.getBatchSize());

        // Load DolphinDB-specific properties from the properties map
        // These are stored as extra properties in TargetConfig
        java.util.Map<String, String> properties = targetConfig.getProperties();

        logger.debug("LoadSubprocess: Properties map size: {}", properties != null ? properties.size() : 0);

        if (properties != null) {
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                logger.debug("LoadSubprocess: Property - {} = {}", entry.getKey(), entry.getValue());
            }

            // Check for DolphinDB-specific properties with ddb. prefix
            String ddbHost = properties.get("ddb.host");
            if (ddbHost != null && !ddbHost.trim().isEmpty()) {
                config.setHost(ddbHost);
            }

            String ddbPort = properties.get("ddb.port");
            if (ddbPort != null && !ddbPort.trim().isEmpty()) {
                try {
                    config.setPort(Integer.parseInt(ddbPort));
                } catch (NumberFormatException e) {
                    // Keep default or use connectionString port
                }
            }

            String ddbUser = properties.get("ddb.user");
            if (ddbUser != null && !ddbUser.trim().isEmpty()) {
                config.setUsername(ddbUser);
            }

            String ddbPassword = properties.get("ddb.password");
            if (ddbPassword != null && !ddbPassword.trim().isEmpty()) {
                config.setPassword(ddbPassword);
            }

            String ddbDatabase = properties.get("ddb.database");
            if (ddbDatabase != null && !ddbDatabase.trim().isEmpty()) {
                config.setDatabase(ddbDatabase);
            }
        }

        logger.info("LoadSubprocess: Loader configuration - host={}, port={}, username={}",
                config.getHost(), config.getPort(), config.getUsername());

        return config;
    }
    
    /**
     * Finds the first DolphinDB target from the ETL configuration.
     *
     * @param context ETL context containing configuration
     * @return first target with type='dolphindb', or null if not found
     */
    private ETConfiguration.TargetConfig findDolphinDBTarget(ETLContext context) {
        if (context.getConfig() == null || context.getConfig().getTargets() == null) {
            logger.warn("LoadSubprocess: Config or targets is null");
            return null;
        }

        logger.debug("LoadSubprocess: Searching for DolphinDB target among {} targets",
                context.getConfig().getTargets().size());

        for (ETConfiguration.TargetConfig target : context.getConfig().getTargets()) {
            logger.debug("LoadSubprocess: Checking target - name={}, type={}",
                    target.getName(), target.getType());
            if ("dolphindb".equalsIgnoreCase(target.getType())) {
                logger.info("LoadSubprocess: Found DolphinDB target: {}", target.getName());
                return target;
            }
        }

        logger.warn("LoadSubprocess: No DolphinDB target found. Available types: {}",
                context.getConfig().getTargets().stream()
                        .map(ETConfiguration.TargetConfig::getType)
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("none"));
        return null;
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
