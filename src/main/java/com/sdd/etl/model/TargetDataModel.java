package com.sdd.etl.model;

import com.sdd.etl.loader.annotation.ColumnOrder;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

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
     * Gets data type identifier.
     *
     * @return data type (e.g., "XBOND_QUOTE", "XBOND_TRADE", "BOND_FUTURE_QUOTE")
     */
    public abstract String getDataType();

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

    /**
     * Gets field names in the order specified by @ColumnOrder annotations.
     * This is critical for DolphinDB column-based storage where field order
     * must match the database schema definition.
     *
     * Fields without @ColumnOrder annotation are appended at the end in natural order.
     * Synthetic fields (compiler-generated) are excluded.
     *
     * @return ordered list of field names
     */
    public List<String> getOrderedFieldNames() {
        Class<?> clazz = getClass();
        Field[] fields = clazz.getDeclaredFields();

        // Collect fields with their order values
        Map<Integer, String> orderedFields = new TreeMap<>();
        List<String> unorderedFields = new ArrayList<>();

        for (Field field : fields) {
            // Skip synthetic fields (compiler-generated)
            if (field.isSynthetic()) {
                continue;
            }

            ColumnOrder annotation = field.getAnnotation(ColumnOrder.class);
            if (annotation != null) {
                orderedFields.put(annotation.value(), field.getName());
            } else {
                unorderedFields.add(field.getName());
            }
        }

        // Combine ordered and unordered fields
        List<String> result = new ArrayList<>(orderedFields.values());
        result.addAll(unorderedFields);

        return result;
    }
}
