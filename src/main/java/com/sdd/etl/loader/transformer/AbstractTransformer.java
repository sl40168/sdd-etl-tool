package com.sdd.etl.loader.transformer;

import com.sdd.etl.loader.transformer.exceptions.TransformationException;
import com.sdd.etl.model.SourceDataModel;
import com.sdd.etl.model.TargetDataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract base class for transformers providing common reflection-based field mapping logic.
 * <p>
 * This class implements the common {@link Transformer} interface with:
 * <ul>
 *   <li>Reflection-based field mapping by name</li>
 *   <li>Automatic type conversion between compatible types</li>
 *   <li>Null-safe value handling with sentinel values</li>
 *   <li>Field access caching for performance</li>
 *   <li>Graceful handling of missing fields</li>
 * </ul>
 * </p>
 * <p>
 * Subclasses only need to implement:
 * <ul>
 *   <li>{@link #getSourceType()} - Return the source data model class</li>
 *   <li>{@link #getTargetType()} - Return the target data model class</li>
 *   <li>{@link #createTargetRecord()} - Create a new target instance</li>
 * </ul>
 * </p>
 * 
 * @param <S> Source data model type (must extend SourceDataModel)
 * @param <T> Target data model type (must extend TargetDataModel)
 * @since 1.0.0
 */
public abstract class AbstractTransformer<S extends SourceDataModel, T extends TargetDataModel>
        implements Transformer<S, T> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractTransformer.class);
    
    // Field access caches (thread-safe for concurrent use)
    protected Map<String, Field> sourceFieldsCache = new ConcurrentHashMap<>();
    protected Map<String, Field> targetFieldsCache = new ConcurrentHashMap<>();

    /**
     * Transforms a list of source records to target records.
     * <p>
     * This implementation:
     * <ol>
     *   <li>Validates input list is not null</li>
     *   <li>Initializes field caches on first call</li>
     *   <li>Iterates through sources and transforms each one</li>
     *   <li>Collects results in list</li>
     *   <li>Returns list of transformed records</li>
     * </ol>
     * </p>
     *
     * @param sourceRecords List of source records (must be non-null, same type S)
     * @return List of transformed target records (same type T, same size as input)
     * @throws TransformationException if transformation fails
     * @throws IllegalArgumentException if sourceRecords is null
     */
    @Override
    public List<T> transform(List<S> sourceRecords) throws TransformationException {
        if (sourceRecords == null) {
            throw new IllegalArgumentException("Source records list cannot be null");
        }

        // Initialize field caches if first call
        if (sourceFieldsCache.isEmpty()) {
            initializeFieldCaches();
        }

        List<T> targetRecords = new ArrayList<>(sourceRecords.size());

        for (int i = 0; i < sourceRecords.size(); i++) {
            S sourceRecord = sourceRecords.get(i);

            if (sourceRecord == null) {
                logger.debug("Skipping null source record at index {}", i);
                continue;
            }

            try {
                T targetRecord = transformSingle(sourceRecord);
                targetRecords.add(targetRecord);
            } catch (Exception e) {
                throw new TransformationException(
                    getSourceType().getSimpleName(),
                    i,
                    "Failed to transform record at index " + i,
                    e
                );
            }
        }

        logger.info("Transformation complete: {} records transformed from {} to {}",
                targetRecords.size(), getSourceType().getSimpleName(), getTargetType().getSimpleName());

        return targetRecords;
    }

    /**
     * Initializes field access caches for both source and target classes.
     * <p>
     * Caches all declared fields (excluding synthetic fields) for faster access.
     * </p>
     */
    protected void initializeFieldCaches() {
        Field[] sourceFields = getSourceType().getDeclaredFields();
        for (Field field : sourceFields) {
            if (!field.isSynthetic()) {
                field.setAccessible(true);
                sourceFieldsCache.put(field.getName(), field);
            }
        }

        Field[] targetFields = getTargetType().getDeclaredFields();
        for (Field field : targetFields) {
            if (!field.isSynthetic()) {
                field.setAccessible(true);
                targetFieldsCache.put(field.getName(), field);
            }
        }

        logger.debug("Initialized field caches: {} source fields, {} target fields",
                sourceFieldsCache.size(), targetFieldsCache.size());
    }

    /**
     * Transforms a single source record to target record using reflection-based field mapping.
     * <p>
     * This method:
     * <ol>
     *   <li>Creates a new target instance</li>
     *   <li>Iterates through all source fields</li>
     *   <li>Finds matching target field by name</li>
     *   <li>Gets source value and converts to target type</li>
     *   <li>Sets converted value in target instance</li>
     * </ol>
     * </p>
     * <p>
     * Missing fields are handled gracefully - if a target field doesn't exist
     * in the source, it is left unassigned (keeps its default sentinel value).
     * </p>
     *
     * @param sourceRecord Source record to transform
     * @return Transformed target record
     * @throws RuntimeException if field mapping fails
     */
    protected T transformSingle(S sourceRecord) {
        try {
            T targetRecord = createTargetRecord();

            // Get all fields from source
            for (String fieldName : sourceFieldsCache.keySet()) {
                Field sourceField = sourceFieldsCache.get(fieldName);
                Field targetField = targetFieldsCache.get(fieldName);

                // Skip if target field doesn't exist
                if (targetField == null) {
                    logger.debug("Target field not found: {}, skipping", fieldName);
                    continue;
                }

                // Get source value
                Object sourceValue = sourceField.get(sourceRecord);

                // Convert and set value
                Object convertedValue = convertValue(sourceValue, targetField.getType());
                if (convertedValue != null) {
                    targetField.set(targetRecord, convertedValue);
                }

                logger.debug("Mapped field: {} ({}) → {} ({})",
                        fieldName, sourceValue != null ? sourceValue.getClass().getSimpleName() : "null",
                        fieldName, convertedValue != null ? convertedValue.getClass().getSimpleName() : "null");
            }

            return targetRecord;

        } catch (Exception e) {
            throw new RuntimeException("Field mapping failed", e);
        }
    }

    /**
     * Converts source value to target type with null safety and sentinel values.
     * <p>
     * Type conversion rules:
     * <table border="1">
     *   <tr><th>Source Type</th><th>Target Type</th><th>Action</th></tr>
     *   <tr><td>null</td><td>int</td><td>Return -1 (sentinel)</td></tr>
     *   <tr><td>Integer</td><td>int</td><td>Auto-unbox, null→-1</td></tr>
     *   <tr><td>Long</td><td>long</td><td>Auto-unbox, null→-1L</td></tr>
     *   <tr><td>Double</td><td>double</td><td>Auto-unbox, null→NaN</td></tr>
     *   <tr><td>String</td><td>LocalDate</td><td>Parse YYYYMMDD, null→null</td></tr>
     *   <tr><td>LocalDateTime</td><td>Instant</td><td>Convert to system timezone, null→null</td></tr>
     *   <tr><td>Same type</td><td>Same type</td><td>Return as-is, null→null</td></tr>
     *   <tr><td>Other</td><td>Any</td><td>Log warning, return null</td></tr>
     * </table>
     * </p>
     *
     * @param sourceValue Source value to convert (may be null)
     * @param targetType Target field type to convert to
     * @return Converted value, or null if source is null or types incompatible
     */
    protected Object convertValue(Object sourceValue, Class<?> targetType) {
        // Handle null source value
        if (sourceValue == null) {
            // Return appropriate sentinel for primitive types
            if (targetType == int.class) {
                return -1;  // Sentinel for int (Constitution Principle 11)
            } else if (targetType == long.class) {
                return -1L;  // Sentinel for long
            } else if (targetType == double.class) {
                return Double.NaN;  // Sentinel for double
            }
            return null;  // For object types
        }

        Class<?> sourceType = sourceValue.getClass();

        // Same type - direct assignment
        if (targetType.isAssignableFrom(sourceType)) {
            return sourceValue;
        }

        // String to LocalDate (format: YYYY.MM.DD → YYYYMMDD)
        if (sourceType == String.class && targetType == LocalDate.class) {
            try {
                String dateStr = ((String) sourceValue).replace(".", "");
                return LocalDate.parse(dateStr, DateTimeFormatter.BASIC_ISO_DATE);
            } catch (Exception e) {
                logger.warn("Failed to parse date: {}, error: {}", sourceValue, e.getMessage());
                return null;
            }
        }

        // LocalDateTime to Instant (convert to system timezone)
        if (sourceType == LocalDateTime.class && targetType == Instant.class) {
            LocalDateTime ldt = (LocalDateTime) sourceValue;
            return ldt.atZone(ZoneId.systemDefault()).toInstant();
        }

        // Incompatible types - log warning and skip
        logger.warn("Incompatible type: {} → {}, skipping",
                sourceType.getSimpleName(), targetType.getSimpleName());
        return null;
    }

    /**
     * Gets the source data model class this transformer supports.
     *
     * @return Source data model class (never null)
     */
    @Override
    public abstract Class<S> getSourceType();

    /**
     * Gets the target data model class this transformer produces.
     *
     * @return Target data model class (never null)
     */
    @Override
    public abstract Class<T> getTargetType();

    /**
     * Creates a new instance of the target record.
     * <p>
     * Subclasses must implement this to provide the correct target type.
     * </p>
     *
     * @return New target record instance
     */
    protected abstract T createTargetRecord();
}
