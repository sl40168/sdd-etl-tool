package com.sdd.etl.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract base class representing data structure for target systems.
 * Concrete implementations will be provided in future phases.
 */
public abstract class TargetDataModel {

    protected Map<String, Object> metadata;
    protected List<Map<String, Object>> records;

    /**
     * Constructs a new TargetDataModel.
     */
    public TargetDataModel() {
        this.metadata = new HashMap<>();
        this.records = null;
    }

    /**
     * Validates data integrity for target system.
     *
     * @return true if data is valid, false otherwise
     */
    public abstract boolean validate();

    /**
     * Converts data to target-specific format.
     *
     * @return data in target format
     */
    public abstract Object toTargetFormat();

    /**
     * Gets target type identifier.
     *
     * @return target type (e.g., "database", "api", "file")
     */
    public abstract String getTargetType();

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
     * Gets data records formatted for target.
     *
     * @return list of record maps, or null if not set
     */
    public List<Map<String, Object>> getRecords() {
        return records;
    }

    /**
     * Sets data records formatted for target.
     *
     * @param records list of record maps
     */
    public void setRecords(List<Map<String, Object>> records) {
        this.records = records;
    }
}
