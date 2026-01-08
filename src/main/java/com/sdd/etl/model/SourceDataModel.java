package com.sdd.etl.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract base class representing data structure from various sources.
 * Concrete implementations will be provided in future phases.
 */
public abstract class SourceDataModel {

    protected Map<String, Object> metadata;
    protected List<Map<String, Object>> records;

    /**
     * Constructs a new SourceDataModel.
     */
    public SourceDataModel() {
        this.metadata = new HashMap<>();
        this.records = null;
    }

    /**
     * Validates data integrity and completeness.
     *
     * @return true if data is valid, false otherwise
     */
    public abstract boolean validate();

    /**
     * Gets primary key value for this data model.
     *
     * @return primary key value, or null if not set
     */
    public abstract Object getPrimaryKey();

    /**
     * Gets source type identifier.
     *
     * @return source type (e.g., "database", "api", "file")
     */
    public abstract String getSourceType();

    /**
     * Gets metadata about data fields and types.
     *
     * @return map of metadata
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Sets metadata about data fields and types.
     *
     * @param metadata map of metadata
     */
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    /**
     * Gets data records with field-value pairs.
     *
     * @return list of record maps, or null if not set
     */
    public List<Map<String, Object>> getRecords() {
        return records;
    }

    /**
     * Sets data records with field-value pairs.
     *
     * @param records list of record maps
     */
    public void setRecords(List<Map<String, Object>> records) {
        this.records = records;
    }
}
