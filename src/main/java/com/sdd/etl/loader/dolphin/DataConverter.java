package com.sdd.etl.loader.dolphin;

import com.sdd.etl.model.TargetDataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Converts record-oriented data to column-oriented arrays for DolphinDB.
 * Uses reflection to extract fields in correct order based on @ColumnOrder.
 */
public class DataConverter {

    private static final Logger logger = LoggerFactory.getLogger(DataConverter.class);

    /**
     * Converts a list of data models to column-oriented arrays.
     *
     * @param data the list of data models
     * @return map of field names to column arrays
     */
    public static Map<String, Object> convertToColumns(List<? extends TargetDataModel> data) {
        if (data == null || data.isEmpty()) {
            return new HashMap<>();
        }

        // Group by data type
        Map<String, List<TargetDataModel>> groupedData = new HashMap<>();
        for (TargetDataModel model : data) {
            String dataType = model.getDataType();
            groupedData.computeIfAbsent(dataType, k -> new ArrayList<>()).add(model);
        }

        // Convert each group
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, List<TargetDataModel>> entry : groupedData.entrySet()) {
            Map<String, Object> columns = convertGroupToColumns(entry.getValue());
            result.putAll(columns);
        }

        return result;
    }

    /**
     * Converts a single data model to column-oriented arrays.
     * Each column array contains exactly one element.
     *
     * @param record the data model
     * @return map of field names to single-element column arrays
     */
    public static Map<String, Object> convertSingleRecordToColumns(TargetDataModel record) {
        if (record == null) {
            return new HashMap<>();
        }

        List<TargetDataModel> singleList = Collections.singletonList(record);
        return convertGroupToColumns(singleList);
    }

    /**
     * Converts a group of same-type models to column arrays.
     */
    private static Map<String, Object> convertGroupToColumns(List<TargetDataModel> group) {
        if (group.isEmpty()) {
            return new HashMap<>();
        }

        // Get ordered field names from the first model
        TargetDataModel firstModel = group.get(0);
        List<String> fieldNames = firstModel.getOrderedFieldNames();

        Map<String, Object> columns = new HashMap<>();

        // Extract each field's values into an array
        for (String fieldName : fieldNames) {
            try {
                Object[] columnArray = extractFieldArray(group, fieldName);
                columns.put(fieldName, columnArray);
            } catch (Exception e) {
                logger.warn("Failed to extract field {}: {}", fieldName, e.getMessage());
            }
        }

        return columns;
    }

    /**
     * Extracts values of a specific field from all models into an array.
     */
    private static Object[] extractFieldArray(List<TargetDataModel> group, String fieldName) {
        Object[] array = new Object[group.size()];

        for (int i = 0; i < group.size(); i++) {
            try {
                Field field = findField(group.get(i).getClass(), fieldName);
                if (field != null) {
                    field.setAccessible(true);
                    array[i] = field.get(group.get(i));
                }
            } catch (Exception e) {
                array[i] = null;
            }
        }

        return array;
    }

    /**
     * Finds a field in the class hierarchy.
     */
    private static Field findField(Class<?> clazz, String fieldName) {
        while (clazz != null && clazz != Object.class) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }
}
