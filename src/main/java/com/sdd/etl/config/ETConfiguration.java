package com.sdd.etl.config;

import java.util.ArrayList;
import java.util.List;

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
        private String primaryKeyField;
        private String extractQuery;
        private String dateField;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getConnectionString() {
            return connectionString;
        }

        public void setConnectionString(String connectionString) {
            this.connectionString = connectionString;
        }

        public String getPrimaryKeyField() {
            return primaryKeyField;
        }

        public void setPrimaryKeyField(String primaryKeyField) {
            this.primaryKeyField = primaryKeyField;
        }

        public String getExtractQuery() {
            return extractQuery;
        }

        public void setExtractQuery(String extractQuery) {
            this.extractQuery = extractQuery;
        }

        public String getDateField() {
            return dateField;
        }

        public void setDateField(String dateField) {
            this.dateField = dateField;
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

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getConnectionString() {
            return connectionString;
        }

        public void setConnectionString(String connectionString) {
            this.connectionString = connectionString;
        }

        public int getBatchSize() {
            return batchSize;
        }

        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
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

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSourceField() {
            return sourceField;
        }

        public void setSourceField(String sourceField) {
            this.sourceField = sourceField;
        }

        public String getTargetField() {
            return targetField;
        }

        public void setTargetField(String targetField) {
            this.targetField = targetField;
        }

        public String getTransformType() {
            return transformType;
        }

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

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getRuleType() {
            return ruleType;
        }

        public void setRuleType(String ruleType) {
            this.ruleType = ruleType;
        }

        public String getRuleValue() {
            return ruleValue;
        }

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

        public LoggingConfig() {
            this.logFilePath = "./etl.log";
            this.logLevel = "INFO";
        }

        public String getLogFilePath() {
            return logFilePath;
        }

        public void setLogFilePath(String logFilePath) {
            this.logFilePath = logFilePath;
        }

        public String getLogLevel() {
            return logLevel;
        }

        public void setLogLevel(String logLevel) {
            this.logLevel = logLevel;
        }
    }
}
