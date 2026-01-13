package com.sdd.etl.loader.dolphin;

import cn.hutool.core.collection.CollUtil;
import com.sdd.etl.model.TargetDataModel;
import com.xxdb.data.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
    public static List<Entity> convertSingleRecordToColumns(TargetDataModel record) throws Exception {
        if (record == null) {
            return Collections.emptyList();
        }

        return convertToColumns(record);
    }

    private static List<Entity> convertToColumns(TargetDataModel record) throws Exception {
        List<String> orderedFieldNames = record.getOrderedFieldNames();
        if (CollUtil.isEmpty(orderedFieldNames)) {
            return Collections.emptyList();
        }
        List<Entity> result = new ArrayList<>(orderedFieldNames.size());
        for (String fieldName : orderedFieldNames) {
            Value value = getFieldValue(record, fieldName);
            Entity entity = new BasicStringVector(1);
            if (value.getType().equals(Double.class) || value.getType().equals(double.class)) {
                entity = new BasicDoubleVector(1);
                if (null != value.getValue() && !Double.isNaN((Double) value.getValue())) {
                    ((BasicDoubleVector) entity).set(0, new BasicDouble((Double) value.getValue()));
                } else {
                    ((BasicDoubleVector) entity).setNull(0);
                }
            } else if (value.getType().equals(Integer.class) || value.getType().equals(int.class)) {
                entity = new BasicIntVector(1);
                if (null != value.getValue() && Integer.MIN_VALUE != (Integer) value.getValue() && Integer.MAX_VALUE != (Integer) value.getValue()) {
                    ((BasicIntVector) entity).set(0, new BasicInt((Integer) value.getValue()));
                } else {
                    ((BasicIntVector) entity).setNull(0);
                }
            } else if (value.getType().equals(Long.class) || value.getType().equals(long.class)) {
                entity = new BasicLongVector(1);
                if (null != value.getValue() && Long.MIN_VALUE != (Long) value.getValue() && Long.MAX_VALUE != (Long) value.getValue()) {
                    ((BasicLongVector) entity).set(0, new BasicLong((Long) value.getValue()));
                } else {
                    ((BasicLongVector) entity).setNull(0);
                }
            } else if (value.getType().equals(Short.class) || value.getType().equals(short.class)) {
                entity = new BasicShortVector(1);
                if (null != value.getValue() && Short.MIN_VALUE != (Short) value.getValue() && Short.MAX_VALUE != (Short) value.getValue()) {
                    ((BasicShortVector) entity).set(0, new BasicShort((Short) value.getValue()));
                } else {
                    ((BasicShortVector) entity).setNull(0);
                }
            } else if (value.getType().equals(Float.class) || value.getType().equals(float.class)) {
                entity = new BasicFloatVector(1);
                if (null != value.getValue() && !Float.isNaN((Float) value.getValue())) {
                    ((BasicFloatVector) entity).set(0, new BasicFloat((Float) value.getValue()));
                } else {
                    ((BasicFloatVector) entity).setNull(0);
                }
            } else if (value.getType().equals(Boolean.class) || value.getType()
                .equals(boolean.class)) {
                entity = new BasicBooleanVector(1);
                if (null != value.getValue()) {
                    ((BasicBooleanVector) entity).set(0, new BasicBoolean((Boolean) value.getValue()));
                } else {
                    ((BasicBooleanVector) entity).setNull(0);
                }
            } else if (value.getType().equals(Byte.class) || value.getType().equals(byte.class)) {
                entity = new BasicByteVector(1);
                if (null != value.getValue() && Byte.MIN_VALUE != (Byte) value.getValue() && Byte.MAX_VALUE != (Byte) value.getValue()) {
                    ((BasicByteVector) entity).set(0, new BasicByte((Byte) value.getValue()));
                } else {
                    ((BasicByteVector) entity).setNull(0);
                }
            } else if (value.getType().equals(LocalDate.class)) {
                entity = new BasicDateVector(1);
                if (null != value.getValue()) {
                    ((BasicDateVector) entity).set(0, new BasicDate((LocalDate) value.getValue()));
                } else {
                    ((BasicDateVector) entity).setNull(0);
                }
            } else if (value.getType().equals(Instant.class)) {
                entity = new BasicTimestampVector(1);
                if (null != value.getValue()) {
                    // Convert Instant to LocalDateTime
                    LocalDateTime localDateTime = LocalDateTime.ofInstant((Instant) value.getValue(), ZoneId.systemDefault());
                    ((BasicTimestampVector) entity).set(0, new BasicTimestamp(localDateTime));
                } else {
                    ((BasicTimestampVector) entity).setNull(0);
                }
            } else if (value.getType().equals(LocalDateTime.class)) {
                entity = new BasicTimestampVector(1);
                if (null != value.getValue()) {
                    ((BasicTimestampVector) entity).set(0, new BasicTimestamp((LocalDateTime) value.getValue()));
                } else {
                    ((BasicTimestampVector) entity).setNull(0);
                }
            } else {
                entity = new BasicStringVector(1);
                if (null != value.getValue()) {
                    ((BasicStringVector) entity).set(0, new BasicString(value.getValue()
                        .toString()));
                } else {
                    ((BasicStringVector) entity).setNull(0);
                }
            }
            result.add(entity);
        }
        return result;
    }

    private static Value getFieldValue(TargetDataModel record, String fieldName) throws IllegalAccessException {
        Field field = findField(record.getClass(), fieldName);
        if (field != null) {
            field.setAccessible(true);
            return new Value(field.getType(), field.get(record));
        } else {
            throw new IllegalAccessException("Field not found: " + fieldName);
        }
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

    static class Value {
        Object value;
        Class type;

        Value(Class type, Object value) {
            this.type = type;
            this.value = value;
        }

        public Class getType() {
            return type;
        }

        public Object getValue() {
            return value;
        }
    }
}
