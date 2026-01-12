package com.sdd.etl.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration loaded from INI file containing all ETL process settings.
 * Contains nested classes for different configuration sections.
 */
public class ETConfiguration {

    private List<SourceConfig> sources;
    private List<TargetConfig> targets;
    private List<TransformationConfig> transformations;
    private List<ValidationConfig> validationRules;
    private LoggingConfig logging;

    /**
     * Constructs a new ETConfiguration.
     */
    public ETConfiguration() {
        this.sources = new ArrayList<>();
        this.targets = new ArrayList<>();
        this.transformations = new ArrayList<>();
        this.validationRules = new ArrayList<>();
        this.logging = new LoggingConfig();
    }

    /**
     * Checks whether a string value is non-null and not blank.
     *
     * @param value input value
     * @return true if value is non-null and has non-whitespace characters
     */
    private static boolean isNonEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Gets list of source configurations.
     *
     * @return list of SourceConfig objects
     */
    public List<SourceConfig> getSources() {
        return sources;
    }

    /**
     * Sets list of source configurations.
     *
     * @param sources list of SourceConfig objects
     */
    public void setSources(List<SourceConfig> sources) {
        this.sources = sources;
    }

    /**
     * Finds the first source configuration matching the given type.
     *
     * @param type source type to match (e.g., "cos")
     * @return matching source configuration, or null if not found
     */
    public SourceConfig findSourceConfig(String type) {
        if (sources == null) {
            return null;
        }
        for (SourceConfig source : sources) {
            if (source != null && type.equals(source.getType())) {
                return source;
            }
        }
        return null;
    }

    /**
     * Finds a source configuration matching both type and category property.
     * The category is expected to be stored in the configuration's properties
     * map under the key "category".
     *
     * @param type source type to match (e.g., "cos")
     * @param category category value to match (e.g., "AllPriceDepth")
     * @return matching source configuration, or null if not found
     */
    public SourceConfig findSourceConfigByCategory(String type, String category) {
        if (sources == null || category == null) {
            return null;
        }
        for (SourceConfig source : sources) {
            if (source != null && type.equals(source.getType())) {
                String configCategory = source.getProperty("category");
                if (category.equals(configCategory)) {
                    return source;
                }
            }
        }
        return null;
    }

    /**
     * Adds a source configuration.
     *
     * @param source SourceConfig to add
     */
    public void addSource(SourceConfig source) {
        this.sources.add(source);
    }

    /**
     * Gets list of target configurations.
     *
     * @return list of TargetConfig objects
     */
    public List<TargetConfig> getTargets() {
        return targets;
    }

    /**
     * Sets list of target configurations.
     *
     * @param targets list of TargetConfig objects
     */
    public void setTargets(List<TargetConfig> targets) {
        this.targets = targets;
    }

    /**
     * Adds a target configuration.
     *
     * @param target TargetConfig to add
     */
    public void addTarget(TargetConfig target) {
        this.targets.add(target);
    }

    /**
     * Gets list of transformation configurations.
     *
     * @return list of TransformationConfig objects
     */
    public List<TransformationConfig> getTransformations() {
        return transformations;
    }

    /**
     * Sets list of transformation configurations.
     *
     * @param transformations list of TransformationConfig objects
     */
    public void setTransformations(List<TransformationConfig> transformations) {
        this.transformations = transformations;
    }

    /**
     * Adds a transformation configuration.
     *
     * @param transformation TransformationConfig to add
     */
    public void addTransformation(TransformationConfig transformation) {
        this.transformations.add(transformation);
    }

    /**
     * Gets list of validation rule configurations.
     *
     * @return list of ValidationConfig objects
     */
    public List<ValidationConfig> getValidationRules() {
        return validationRules;
    }

    /**
     * Sets list of validation rule configurations.
     *
     * @param validationRules list of ValidationConfig objects
     */
    public void setValidationRules(List<ValidationConfig> validationRules) {
        this.validationRules = validationRules;
    }

    /**
     * Adds a validation rule configuration.
     *
     * @param validationRule ValidationConfig to add
     */
    public void addValidationRule(ValidationConfig validationRule) {
        this.validationRules.add(validationRule);
    }

    /**
     * Gets logging configuration.
     *
     * @return LoggingConfig object
     */
    public LoggingConfig getLogging() {
        return logging;
    }

    /**
     * Sets logging configuration.
     *
     * @param logging LoggingConfig object
     */
    public void setLogging(LoggingConfig logging) {
        this.logging = logging;
    }

    /**
     * Configuration for a data source.
     */
    public static class SourceConfig {
        private String name;
        private String type;
        private String connectionString;
        private String extractQuery;
        private String dateField;
        private Map<String, String> properties;

        /**
         * Gets the source name.
         *
         * @return source name
         */
        public String getName() {
            return name;
        }

        /**
         * Sets the source name.
         *
         * @param name source name
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * Gets the source type identifier.
         *
         * @return source type
         */
        public String getType() {
            return type;
        }

        /**
         * Sets the source type identifier.
         *
         * @param type source type
         */
        public void setType(String type) {
            this.type = type;
        }

        /**
         * Gets the connection string for this source.
         *
         * @return connection string
         */
        public String getConnectionString() {
            return connectionString;
        }

        /**
         * Sets the connection string for this source.
         *
         * @param connectionString connection string
         */
        public void setConnectionString(String connectionString) {
            this.connectionString = connectionString;
        }

        /**
         * Gets the optional extract query template.
         *
         * @return extract query (may be null)
         */
        public String getExtractQuery() {
            return extractQuery;
        }

        /**
         * Sets the optional extract query template.
         *
         * @param extractQuery extract query (may be null)
         */
        public void setExtractQuery(String extractQuery) {
            this.extractQuery = extractQuery;
        }

        /**
         * Gets the optional date field name used by the extractor.
         *
         * @return date field name (may be null)
         */
        public String getDateField() {
            return dateField;
        }

        /**
         * Sets the optional date field name used by the extractor.
         *
         * @param dateField date field name (may be null)
         */
        public void setDateField(String dateField) {
            this.dateField = dateField;
        }

        /**
         * Constructs a new SourceConfig with default values.
         */
        public SourceConfig() {
            this.properties = new HashMap<>();
        }

        /**
         * Validates that required fields are present.
         * Required: name, type.
         * Optional: connectionString, primaryKeyField (for informational purposes only).
         *
         * @return true if required fields are non-empty
         */
        public boolean isValid() {
            return ETConfiguration.isNonEmpty(name)
                    && ETConfiguration.isNonEmpty(type);
        }

        /**
         * Gets the properties map for this source configuration.
         *
         * @return properties map (never null)
         */
        public Map<String, String> getProperties() {
            return properties;
        }

        /**
         * Sets the properties map for this source configuration.
         *
         * @param properties properties map
         */
        public void setProperties(Map<String, String> properties) {
            this.properties = properties;
        }

        /**
         * Gets a property value by key.
         *
         * @param key property key
         * @return property value or null if not found
         */
        public String getProperty(String key) {
            return properties.get(key);
        }

        /**
         * Sets a property value.
         *
         * @param key property key
         * @param value property value
         */
        public void setProperty(String key, String value) {
            properties.put(key, value);
        }

        /**
         * Checks if a property exists.
         *
         * @param key property key
         * @return true if property exists
         */
        public boolean hasProperty(String key) {
            return properties.containsKey(key);
        }
    }

    /**
     * Configuration for a data target.
     */
    public static class TargetConfig {
        private String name;
        private String type;
        private String connectionString;
        private int batchSize;

        /**
         * Gets the target name.
         *
         * @return target name
         */
        public String getName() {
            return name;
        }

        /**
         * Sets the target name.
         *
         * @param name target name
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * Gets the target type identifier.
         *
         * @return target type
         */
        public String getType() {
            return type;
        }

        /**
         * Sets the target type identifier.
         *
         * @param type target type
         */
        public void setType(String type) {
            this.type = type;
        }

        /**
         * Gets the connection string for this target.
         *
         * @return connection string
         */
        public String getConnectionString() {
            return connectionString;
        }

        /**
         * Sets the connection string for this target.
         *
         * @param connectionString connection string
         */
        public void setConnectionString(String connectionString) {
            this.connectionString = connectionString;
        }

        /**
         * Gets the batch size (records per batch).
         *
         * @return batch size
         */
        public int getBatchSize() {
            return batchSize;
        }

        /**
         * Sets the batch size (records per batch).
         *
         * @param batchSize batch size (must be > 0)
         */
        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }

        /**
         * Validates that required fields are present.
         * Required: name, type, batchSize (> 0).
         * Optional: connectionString (for informational purposes only).
         *
         * @return true if required fields are valid
         */
        public boolean isValid() {
            return ETConfiguration.isNonEmpty(name)
                    && ETConfiguration.isNonEmpty(type)
                    && batchSize > 0;
        }
    }

    /**
     * Configuration for a transformation rule.
     */
    public static class TransformationConfig {
        private String name;
        private String sourceField;
        private String targetField;
        private String transformType;

        /**
         * Gets the transformation name.
         *
         * @return transformation name
         */
        public String getName() {
            return name;
        }

        /**
         * Sets the transformation name.
         *
         * @param name transformation name
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * Gets the source field name.
         *
         * @return source field name
         */
        public String getSourceField() {
            return sourceField;
        }

        /**
         * Sets the source field name.
         *
         * @param sourceField source field name
         */
        public void setSourceField(String sourceField) {
            this.sourceField = sourceField;
        }

        /**
         * Gets the target field name.
         *
         * @return target field name
         */
        public String getTargetField() {
            return targetField;
        }

        /**
         * Sets the target field name.
         *
         * @param targetField target field name
         */
        public void setTargetField(String targetField) {
            this.targetField = targetField;
        }

        /**
         * Gets the transformation type identifier.
         *
         * @return transformation type
         */
        public String getTransformType() {
            return transformType;
        }

        /**
         * Sets the transformation type identifier.
         *
         * @param transformType transformation type
         */
        public void setTransformType(String transformType) {
            this.transformType = transformType;
        }
    }

    /**
     * Configuration for a validation rule.
     */
    public static class ValidationConfig {
        private String name;
        private String field;
        private String ruleType;
        private String ruleValue;

        /**
         * Gets the validation rule name.
         *
         * @return rule name
         */
        public String getName() {
            return name;
        }

        /**
         * Sets the validation rule name.
         *
         * @param name rule name
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * Gets the field this rule applies to.
         *
         * @return field name
         */
        public String getField() {
            return field;
        }

        /**
         * Sets the field this rule applies to.
         *
         * @param field field name
         */
        public void setField(String field) {
            this.field = field;
        }

        /**
         * Gets the rule type identifier.
         *
         * @return rule type
         */
        public String getRuleType() {
            return ruleType;
        }

        /**
         * Sets the rule type identifier.
         *
         * @param ruleType rule type
         */
        public void setRuleType(String ruleType) {
            this.ruleType = ruleType;
        }

        /**
         * Gets the rule value.
         *
         * @return rule value (may be empty)
         */
        public String getRuleValue() {
            return ruleValue;
        }

        /**
         * Sets the rule value.
         *
         * @param ruleValue rule value
         */
        public void setRuleValue(String ruleValue) {
            this.ruleValue = ruleValue;
        }
    }

    /**
     * Configuration for logging settings.
     */
    public static class LoggingConfig {
        private String logFilePath;
        private String logLevel;

        /**
         * Constructs logging configuration with default values.
         */
        public LoggingConfig() {
            this.logFilePath = "./etl.log";
            this.logLevel = "INFO";
        }

        /**
         * Gets the log file path.
         *
         * @return log file path
         */
        public String getLogFilePath() {
            return logFilePath;
        }

        /**
         * Sets the log file path.
         *
         * @param logFilePath log file path
         */
        public void setLogFilePath(String logFilePath) {
            this.logFilePath = logFilePath;
        }

        /**
         * Gets the log level (e.g., INFO).
         *
         * @return log level
         */
        public String getLogLevel() {
            return logLevel;
        }

        /**
         * Sets the log level (e.g., INFO).
         *
         * @param logLevel log level
         */
        public void setLogLevel(String logLevel) {
            this.logLevel = logLevel;
        }
    }

    public static final String SOURCE_TYPE_COS = "cos";
}
