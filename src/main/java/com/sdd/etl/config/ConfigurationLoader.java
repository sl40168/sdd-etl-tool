package com.sdd.etl.config;

import com.sdd.etl.ETLException;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.util.ArrayList;
import java.util.List;

/**
 * Loads ETL configuration from INI file.
 * Parses all configuration sections and creates ETConfiguration object.
 */
public class ConfigurationLoader {

    /**
     * Loads configuration from INI file.
     *
     * @param configPath absolute path to INI configuration file
     * @return ETConfiguration object with all settings
     * @throws ETLException if file cannot be loaded or parsed
     */
    public ETConfiguration load(String configPath) throws ETLException {
        if (configPath == null) {
            throw new ETLException("CONFIG", "", "Configuration path cannot be null");
        }

        try {
            // Load INI file using Apache Commons Configuration
            FileBasedConfigurationBuilder<INIConfiguration> builder =
                    new FileBasedConfigurationBuilder<>(INIConfiguration.class)
                            .configure(new Parameters().hierarchical()
                                    .setFileName(configPath));

            INIConfiguration iniConfig = builder.getConfiguration();

            // Create ETConfiguration object
            ETConfiguration config = new ETConfiguration();

            // Parse sources section
            parseSources(iniConfig, config);

            // Parse targets section
            parseTargets(iniConfig, config);

            // Parse transformations section
            parseTransformations(iniConfig, config);

            // Parse validation section
            parseValidation(iniConfig, config);

            // Parse logging section
            parseLogging(iniConfig, config);

            return config;

        } catch (ConfigurationException e) {
            throw new ETLException("CONFIGURATION", "N/A",
                    "Failed to load configuration file: " + configPath, e);
        } catch (Exception e) {
            throw new ETLException("CONFIGURATION", "N/A",
                    "Error parsing configuration: " + e.getMessage(), e);
        }
    }

    /**
     * Parses data source configurations from INI file.
     *
     * @param iniConfig INI configuration object
     * @param config      ETConfiguration object to populate
     * @throws ConfigurationException if parsing fails
     */
    void parseSources(INIConfiguration iniConfig, ETConfiguration config)
            throws ConfigurationException {

        // Get source count
        int sourceCount = iniConfig.getInt("sources.count", 0);

        // Parse each source
        for (int i = 1; i <= sourceCount; i++) {
            String sectionKey = "source" + i;

            ETConfiguration.SourceConfig source = new ETConfiguration.SourceConfig();
            source.setName(iniConfig.getString(sectionKey + ".name"));
            source.setType(iniConfig.getString(sectionKey + ".type"));
            source.setConnectionString(iniConfig.getString(sectionKey + ".connectionString"));
            source.setPrimaryKeyField(iniConfig.getString(sectionKey + ".primaryKeyField"));
            source.setExtractQuery(iniConfig.getString(sectionKey + ".extractQuery"));
            source.setDateField(iniConfig.getString(sectionKey + ".dateField"));

            if (!source.isValid()) {
                throw new ConfigurationException(
                        "Invalid source configuration in section [" + sectionKey + "]: "
                                + "name/type/connectionString/primaryKeyField are required");
            }

            config.addSource(source);
        }
    }

    /**
     * Parses data target configurations from INI file.
     *
     * @param iniConfig INI configuration object
     * @param config      ETConfiguration object to populate
     * @throws ConfigurationException if parsing fails
     */
    void parseTargets(INIConfiguration iniConfig, ETConfiguration config)
            throws ConfigurationException {

        // Get target count
        int targetCount = iniConfig.getInt("targets.count", 0);

        // Parse each target
        for (int i = 1; i <= targetCount; i++) {
            String sectionKey = "target" + i;

            ETConfiguration.TargetConfig target = new ETConfiguration.TargetConfig();
            target.setName(iniConfig.getString(sectionKey + ".name"));
            target.setType(iniConfig.getString(sectionKey + ".type"));
            target.setConnectionString(iniConfig.getString(sectionKey + ".connectionString"));
            target.setBatchSize(iniConfig.getInt(sectionKey + ".batchSize", 1000));

            if (!target.isValid()) {
                throw new ConfigurationException(
                        "Invalid target configuration in section [" + sectionKey + "]: "
                                + "name/type/connectionString are required and batchSize must be > 0");
            }

            config.addTarget(target);
        }
    }

    /**
     * Parses transformation rule configurations from INI file.
     *
     * @param iniConfig INI configuration object
     * @param config      ETConfiguration object to populate
     * @throws ConfigurationException if parsing fails
     */
    void parseTransformations(INIConfiguration iniConfig, ETConfiguration config)
            throws ConfigurationException {

        // Get transformation count
        int transformCount = iniConfig.getInt("transformations.count", 0);

        // Parse each transformation
        for (int i = 1; i <= transformCount; i++) {
            String sectionKey = "transformation" + i;

            ETConfiguration.TransformationConfig transformation = new ETConfiguration.TransformationConfig();
            transformation.setName(iniConfig.getString(sectionKey + ".name"));
            transformation.setSourceField(iniConfig.getString(sectionKey + ".sourceField"));
            transformation.setTargetField(iniConfig.getString(sectionKey + ".targetField"));
            transformation.setTransformType(iniConfig.getString(sectionKey + ".transformType"));

            config.addTransformation(transformation);
        }
    }

    /**
     * Parses validation rule configurations from INI file.
     *
     * @param iniConfig INI configuration object
     * @param config      ETConfiguration object to populate
     * @throws ConfigurationException if parsing fails
     */
    void parseValidation(INIConfiguration iniConfig, ETConfiguration config)
            throws ConfigurationException {

        // Get validation rule count
        int ruleCount = iniConfig.getInt("validation.count", 0);

        // Parse each validation rule
        for (int i = 1; i <= ruleCount; i++) {
            String sectionKey = "rule" + i;

            ETConfiguration.ValidationConfig validationRule = new ETConfiguration.ValidationConfig();
            validationRule.setName(iniConfig.getString(sectionKey + ".name"));
            validationRule.setField(iniConfig.getString(sectionKey + ".field"));
            validationRule.setRuleType(iniConfig.getString(sectionKey + ".ruleType"));
            validationRule.setRuleValue(iniConfig.getString(sectionKey + ".ruleValue"));

            config.addValidationRule(validationRule);
        }
    }

    /**
     * Parses logging configuration from INI file.
     *
     * @param iniConfig INI configuration object
     * @param config      ETConfiguration object to populate
     * @throws ConfigurationException if parsing fails
     */
    void parseLogging(INIConfiguration iniConfig, ETConfiguration config)
            throws ConfigurationException {

        ETConfiguration.LoggingConfig logging = new ETConfiguration.LoggingConfig();

        String logFilePath = iniConfig.getString("logging.logFilePath");
        if (logFilePath != null && !logFilePath.isEmpty()) {
            logging.setLogFilePath(logFilePath);
        }

        String logLevel = iniConfig.getString("logging.logLevel");
        if (logLevel != null && !logLevel.isEmpty()) {
            logging.setLogLevel(logLevel);
        }

        config.setLogging(logging);
    }
}
