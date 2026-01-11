package com.sdd.etl.loader.config;

import com.sdd.etl.loader.api.exceptions.LoaderException;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.File;

/**
 * Parses INI configuration files for loader settings.
 * Expected INI structure:
 *
 * [dolphindb]
 * host=localhost
 * port=8848
 * username=admin
 * password=password
 * batch_size=1000
 * database=mydb
 * table_creation_script=scripts/temporary_table_creation.dos
 * table_deletion_script=scripts/temporary_table_deletion.dos
 */
public class ConfigParser {

    private static final String SECTION = "dolphindb";
    private static final String HOST = "host";
    private static final String PORT = "port";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String BATCH_SIZE = "batch_size";
    private static final String DATABASE = "database";
    private static final String TABLE_CREATION_SCRIPT = "table_creation_script";
    private static final String TABLE_DELETION_SCRIPT = "table_deletion_script";

    /**
     * Parses a loader configuration from an INI file.
     *
     * @param configPath the path to the INI configuration file
     * @return the parsed LoaderConfiguration
     * @throws LoaderException if the file cannot be read or contains invalid configuration
     */
    public static LoaderConfiguration parse(String configPath) throws LoaderException {
        if (configPath == null || configPath.isEmpty()) {
            throw new LoaderException("Configuration path cannot be null or empty");
        }

        File configFile = new File(configPath);
        if (!configFile.exists()) {
            throw new LoaderException("Configuration file not found: " + configPath);
        }

        try {
            Configurations configs = new Configurations();
            FileBasedConfigurationBuilder<INIConfiguration> builder =
                    configs.iniBuilder(configFile);
            Configuration config = builder.getConfiguration();

            LoaderConfiguration loaderConfig = new LoaderConfiguration();

            if (config.containsKey(SECTION + "." + HOST)) {
                loaderConfig.setHost(config.getString(SECTION + "." + HOST));
            }

            if (config.containsKey(SECTION + "." + PORT)) {
                String portValue = config.getString(SECTION + "." + PORT);
                if (portValue != null && !portValue.trim().isEmpty()) {
                    loaderConfig.setPort(config.getInt(SECTION + "." + PORT));
                }
            }

            if (config.containsKey(SECTION + "." + USERNAME)) {
                loaderConfig.setUsername(config.getString(SECTION + "." + USERNAME));
            }

            if (config.containsKey(SECTION + "." + PASSWORD)) {
                loaderConfig.setPassword(config.getString(SECTION + "." + PASSWORD));
            }

            if (config.containsKey(SECTION + "." + BATCH_SIZE)) {
                String batchSizeValue = config.getString(SECTION + "." + BATCH_SIZE);
                if (batchSizeValue != null && !batchSizeValue.trim().isEmpty()) {
                    loaderConfig.setBatchSize(config.getInt(SECTION + "." + BATCH_SIZE));
                }
            }

            if (config.containsKey(SECTION + "." + DATABASE)) {
                loaderConfig.setDatabase(config.getString(SECTION + "." + DATABASE));
            }

            if (config.containsKey(SECTION + "." + TABLE_CREATION_SCRIPT)) {
                loaderConfig.setTableCreationScript(
                        config.getString(SECTION + "." + TABLE_CREATION_SCRIPT));
            }

            if (config.containsKey(SECTION + "." + TABLE_DELETION_SCRIPT)) {
                loaderConfig.setTableDeletionScript(
                        config.getString(SECTION + "." + TABLE_DELETION_SCRIPT));
            }

            return loaderConfig;

        } catch (ConfigurationException e) {
            throw new LoaderException("Failed to parse configuration file: " + configPath, e);
        }
    }

    /**
     * Parses a loader configuration from a Configuration object.
     * Useful for configurations already loaded from INI or other sources.
     *
     * @param config the Configuration object
     * @return the parsed LoaderConfiguration
     * @throws LoaderException if required fields are missing
     */
    public static LoaderConfiguration parse(Configuration config) throws LoaderException {
        if (config == null) {
            throw new LoaderException("Configuration cannot be null");
        }

        LoaderConfiguration loaderConfig = new LoaderConfiguration();

        if (config.containsKey(HOST)) {
            loaderConfig.setHost(config.getString(HOST));
        }

        if (config.containsKey(PORT)) {
            loaderConfig.setPort(config.getInt(PORT));
        }

        if (config.containsKey(USERNAME)) {
            loaderConfig.setUsername(config.getString(USERNAME));
        }

        if (config.containsKey(PASSWORD)) {
            loaderConfig.setPassword(config.getString(PASSWORD));
        }

        if (config.containsKey(BATCH_SIZE)) {
            loaderConfig.setBatchSize(config.getInt(BATCH_SIZE));
        }

        if (config.containsKey(DATABASE)) {
            loaderConfig.setDatabase(config.getString(DATABASE));
        }

        if (config.containsKey(TABLE_CREATION_SCRIPT)) {
            loaderConfig.setTableCreationScript(config.getString(TABLE_CREATION_SCRIPT));
        }

        if (config.containsKey(TABLE_DELETION_SCRIPT)) {
            loaderConfig.setTableDeletionScript(config.getString(TABLE_DELETION_SCRIPT));
        }

        return loaderConfig;
    }
}
