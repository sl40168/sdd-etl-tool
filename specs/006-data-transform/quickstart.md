# Quickstart Guide: Data Transformation Pipeline

**Feature**: 006-data-transform
**Date**: 2026-01-11

## Overview

This guide helps developers get started with implementing data transformation pipeline. It provides setup instructions, code examples, and testing guidelines.

---

## 1. Prerequisites

### 1.1 Environment Setup

Ensure you have:
- Java 8 JDK installed
- Maven 3.x installed
- IDE configured for Java 8 (IntelliJ IDEA, Eclipse, etc.)
- Git repository cloned to local machine

### 1.2 Project Setup

```bash
# Navigate to project root
cd c:/Users/sl401/workspace/quantconnect/sdd-etl-tool

# Switch to feature branch
git checkout 006-data-transform

# Verify branch
git branch
```

### 1.3 Build Verification

```bash
# Run Maven clean compile
./mvnw clean compile

# Run tests
./mvnw test
```

---

## 2. Project Structure

### 2.1 New Packages

The feature adds following new packages:

```
src/main/java/com/sdd/etl/loader/transformer/
├── Transformer.java                    # Common API interface
├── TransformerFactory.java             # Factory for selecting transformers
├── XbondQuoteTransformer.java         # Concrete transformer (83 fields)
├── XbondTradeTransformer.java         # Concrete transformer (15 fields)
├── BondFutureQuoteTransformer.java    # Concrete transformer (96 fields)
└── exceptions/
    └── TransformationException.java    # Custom exception

src/test/java/com/sdd/etl/loader/transformer/
├── XbondQuoteTransformerTest.java
├── XbondTradeTransformerTest.java
└── BondFutureQuoteTransformerTest.java
```

### 2.2 Updated Classes

```
src/main/java/com/sdd/etl/subprocess/
└── TransformSubprocess.java            # Update existing stub implementation
```

---

## 3. Implementation Steps

### 3.1 Step 1: Create Transformer Exception

**File**: `src/main/java/com/sdd/etl/loader/transformer/exceptions/TransformationException.java`

```java
package com.sdd.etl.loader.transformer.exceptions;

import com.sdd.etl.ETLException;

/**
 * Exception thrown when data transformation fails.
 */
public class TransformationException extends ETLException {

    private final String sourceDataType;
    private final int recordCount;

    public TransformationException(String sourceDataType, int recordCount, String message) {
        super("TRANSFORM", message);
        this.sourceDataType = sourceDataType;
        this.recordCount = recordCount;
    }

    public TransformationException(String sourceDataType, int recordCount,
                                   String message, Throwable cause) {
        super("TRANSFORM", message, cause);
        this.sourceDataType = sourceDataType;
        this.recordCount = recordCount;
    }

    public String getSourceDataType() {
        return sourceDataType;
    }

    public int getRecordCount() {
        return recordCount;
    }
}
```

---

### 3.2 Step 2: Create Transformer Interface

**File**: `src/main/java/com/sdd/etl/loader/transformer/Transformer.java`

```java
package com.sdd.etl.loader.transformer;

import com.sdd.etl.loader.transformer.exceptions.TransformationException;
import com.sdd.etl.model.SourceDataModel;
import com.sdd.etl.model.TargetDataModel;
import java.util.List;

/**
 * Transformer API for converting source data models to target data models.
 */
public interface Transformer<S extends SourceDataModel, T extends TargetDataModel> {

    /**
     * Transforms a list of source records to target records.
     *
     * @param sourceRecords List of source records (same type)
     * @return List of transformed target records (same type)
     * @throws TransformationException if transformation fails
     */
    List<T> transform(List<S> sourceRecords) throws TransformationException;

    /**
     * Gets the source data model class this transformer supports.
     *
     * @return Source data model class
     */
    Class<S> getSourceType();

    /**
     * Gets the target data model class this transformer produces.
     *
     * @return Target data model class
     */
    Class<T> getTargetType();
}
```

---

### 3.3 Step 3: Create Abstract Base Transformer

**File**: `src/main/java/com/sdd/etl/loader/transformer/AbstractTransformer.java`

```java
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
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for transformers providing common field mapping logic.
 */
public abstract class AbstractTransformer<S extends SourceDataModel, T extends TargetDataModel>
        implements Transformer<S, T> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractTransformer.class);

    @Override
    public List<T> transform(List<S> sourceRecords) throws TransformationException {
        if (sourceRecords == null) {
            throw new IllegalArgumentException("Source records list cannot be null");
        }

        List<T> targetRecords = new ArrayList<>(sourceRecords.size());

        for (int i = 0; i < sourceRecords.size(); i++) {
            S sourceRecord = sourceRecords.get(i);

            if (sourceRecord == null) {
                logger.debug("Skipping null source record at index {}", i);
                continue;
            }

            try {
                T targetRecord = transformRecord(sourceRecord);
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

        return targetRecords;
    }

    /**
     * Transforms a single source record to target record using reflection-based field mapping.
     */
    protected T transformRecord(S sourceRecord) {
        try {
            T targetRecord = createTargetRecord();

            // Get all fields from source and target
            Field[] sourceFields = sourceRecord.getClass().getDeclaredFields();
            Field[] targetFields = targetRecord.getClass().getDeclaredFields();

            for (Field sourceField : sourceFields) {
                String fieldName = sourceField.getName();

                // Skip synthetic fields (compiler-generated)
                if (sourceField.isSynthetic()) {
                    continue;
                }

                // Find matching target field
                Field targetField = findField(targetFields, fieldName);
                if (targetField == null) {
                    logger.debug("Target field not found: {}, skipping", fieldName);
                    continue;
                }

                // Access fields
                sourceField.setAccessible(true);
                targetField.setAccessible(true);

                // Get source value
                Object sourceValue = sourceField.get(sourceRecord);

                if (sourceValue == null) {
                    // Field is null, leave target unassigned (already has sentinel value)
                    logger.debug("Source field is null: {}, leaving target unassigned", fieldName);
                    continue;
                }

                // Convert and set value
                Object convertedValue = convertValue(sourceValue, targetField.getType());
                targetField.set(targetRecord, convertedValue);

                logger.debug("Mapped field: {} ({}) → {} ({})",
                    fieldName, sourceValue.getClass().getSimpleName(),
                    fieldName, convertedValue.getClass().getSimpleName());
            }

            return targetRecord;

        } catch (Exception e) {
            throw new RuntimeException("Field mapping failed", e);
        }
    }

    /**
     * Finds a field by name in the array of fields.
     */
    private Field findField(Field[] fields, String name) {
        for (Field field : fields) {
            if (field.isSynthetic()) {
                continue;
            }
            if (field.getName().equals(name)) {
                return field;
            }
        }
        return null;
    }

    /**
     * Converts source value to target type.
     */
    private Object convertValue(Object sourceValue, Class<?> targetType) {
        if (sourceValue == null) {
            return null;
        }

        Class<?> sourceType = sourceValue.getClass();

        // Same type
        if (targetType.isAssignableFrom(sourceType)) {
            return sourceValue;
        }

        // String to LocalDate
        if (sourceType == String.class && targetType == LocalDate.class) {
            String dateStr = (String) sourceValue;
            return LocalDate.parse(dateStr.replace("-", ""));
        }

        // LocalDateTime to Instant
        if (sourceType == LocalDateTime.class && targetType == Instant.class) {
            LocalDateTime ldt = (LocalDateTime) sourceValue;
            return ldt.atZone(ZoneId.systemDefault()).toInstant();
        }

        // Incompatible types
        logger.warn("Incompatible type: {} → {}, skipping field",
            sourceType.getSimpleName(), targetType.getSimpleName());
        return null;
    }

    /**
     * Creates a new instance of the target record.
     */
    protected abstract T createTargetRecord();
}
```

---

### 3.4 Step 4: Create XbondQuoteTransformer

**File**: `src/main/java/com/sdd/etl/loader/transformer/XbondQuoteTransformer.java`

```java
package com.sdd.etl.loader.transformer;

import com.sdd.etl.loader.model.XbondQuoteDataModel;
import com.sdd.etl.model.XbondQuoteDataModel;

/**
 * Transformer for Xbond Quote data.
 */
public class XbondQuoteTransformer extends AbstractTransformer<
        com.sdd.etl.model.XbondQuoteDataModel,
        com.sdd.etl.loader.model.XbondQuoteDataModel> {

    @Override
    protected com.sdd.etl.loader.model.XbondQuoteDataModel createTargetRecord() {
        return new com.sdd.etl.loader.model.XbondQuoteDataModel();
    }

    @Override
    public Class<com.sdd.etl.model.XbondQuoteDataModel> getSourceType() {
        return com.sdd.etl.model.XbondQuoteDataModel.class;
    }

    @Override
    public Class<com.sdd.etl.loader.model.XbondQuoteDataModel> getTargetType() {
        return com.sdd.etl.loader.model.XbondQuoteDataModel.class;
    }
}
```

---

### 3.5 Step 5: Create XbondTradeTransformer

**File**: `src/main/java/com/sdd/etl/loader/transformer/XbondTradeTransformer.java`

```java
package com.sdd.etl.loader.transformer;

import com.sdd.etl.loader.model.XbondTradeDataModel;

/**
 * Transformer for Xbond Trade data.
 */
public class XbondTradeTransformer extends AbstractTransformer<
        com.sdd.etl.model.XbondTradeDataModel,
        com.sdd.etl.loader.model.XbondTradeDataModel> {

    @Override
    protected com.sdd.etl.loader.model.XbondTradeDataModel createTargetRecord() {
        return new com.sdd.etl.loader.model.XbondTradeDataModel();
    }

    @Override
    public Class<com.sdd.etl.model.XbondTradeDataModel> getSourceType() {
        return com.sdd.etl.model.XbondTradeDataModel.class;
    }

    @Override
    public Class<com.sdd.etl.loader.model.XbondTradeDataModel> getTargetType() {
        return com.sdd.etl.loader.model.XbondTradeDataModel.class;
    }
}
```

---

### 3.6 Step 6: Create BondFutureQuoteTransformer

**File**: `src/main/java/com/sdd/etl/loader/transformer/BondFutureQuoteTransformer.java`

```java
package com.sdd.etl.loader.transformer;

import com.sdd.etl.loader.model.BondFutureQuoteDataModel;

/**
 * Transformer for Bond Future Quote data.
 */
public class BondFutureQuoteTransformer extends AbstractTransformer<
        com.sdd.etl.source.extract.db.quote.BondFutureQuoteDataModel,
        com.sdd.etl.loader.model.BondFutureQuoteDataModel> {

    @Override
    protected com.sdd.etl.loader.model.BondFutureQuoteDataModel createTargetRecord() {
        return new com.sdd.etl.loader.model.BondFutureQuoteDataModel();
    }

    @Override
    public Class<com.sdd.etl.source.extract.db.quote.BondFutureQuoteDataModel> getSourceType() {
        return com.sdd.etl.source.extract.db.quote.BondFutureQuoteDataModel.class;
    }

    @Override
    public Class<com.sdd.etl.loader.model.BondFutureQuoteDataModel> getTargetType() {
        return com.sdd.etl.loader.model.BondFutureQuoteDataModel.class;
    }
}
```

---

### 3.7 Step 7: Create TransformerFactory

**File**: `src/main/java/com/sdd/etl/loader/transformer/TransformerFactory.java`

```java
package com.sdd.etl.loader.transformer;

import com.sdd.etl.loader.transformer.exceptions.TransformationException;
import java.util.HashMap;
import java.util.Map;

/**
 * Factory for selecting appropriate Transformer based on source data type.
 */
public class TransformerFactory {

    private static final Map<Class<?>, Transformer<?, ?>> TRANSFORMERS = new HashMap<>();

    static {
        // Register all transformers
        register(new XbondQuoteTransformer());
        register(new XbondTradeTransformer());
        register(new BondFutureQuoteTransformer());
    }

    /**
     * Gets the transformer for the specified source data type.
     *
     * @param sourceType Source data model class
     * @return Transformer instance
     * @throws TransformationException if no transformer is registered
     */
    public static Transformer<?, ?> getTransformer(Class<?> sourceType)
            throws TransformationException {

        if (sourceType == null) {
            throw new IllegalArgumentException("Source type cannot be null");
        }

        Transformer<?, ?> transformer = TRANSFORMERS.get(sourceType);

        if (transformer == null) {
            throw new TransformationException(
                sourceType.getSimpleName(),
                0,
                "No transformer found for source type: " + sourceType.getName()
            );
        }

        return transformer;
    }

    /**
     * Registers a transformer in the factory.
     */
    private static void register(Transformer<?, ?> transformer) {
        TRANSFORMERS.put(transformer.getSourceType(), transformer);
    }
}
```

---

### 3.8 Step 8: Update TransformSubprocess

**File**: `src/main/java/com/sdd/etl/subprocess/TransformSubprocess.java`

```java
package com.sdd.etl.subprocess;

import com.sdd.etl.ETLException;
import com.sdd.etl.context.ContextConstants;
import com.sdd.etl.context.ETLContext;
import com.sdd.etl.loader.transformer.Transformer;
import com.sdd.etl.loader.transformer.TransformerFactory;
import com.sdd.etl.loader.transformer.exceptions.TransformationException;
import com.sdd.etl.model.SourceDataModel;
import com.sdd.etl.model.TargetDataModel;
import com.sdd.etl.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Concrete implementation of Transform subprocess.
 * Groups extracted data by type and transforms concurrently.
 */
public class TransformSubprocess extends TransformSubprocessBase {

    private static final Logger logger = LoggerFactory.getLogger(TransformSubprocess.class);

    @Override
    public int execute(ETLContext context) throws ETLException {
        logger.info("TransformSubprocess: Starting transformation");

        // Retrieve extracted data from context
        List<SourceDataModel> extractedData = context.getExtractedData();

        if (extractedData == null || extractedData.isEmpty()) {
            logger.info("TransformSubprocess: No extracted data found");
            context.setTransformedData(new ArrayList<TargetDataModel>());
            context.setTransformedDataCount(0);
            return 0;
        }

        // Group extracted data by type
        Map<Class<? extends SourceDataModel>, List<? extends SourceDataModel>> dataGroups =
            groupByType(extractedData);

        logger.info("TransformSubprocess: Found {} data type(s) to transform", dataGroups.size());

        // Execute transformations concurrently
        List<TargetDataModel> allTransformedData = transformConcurrently(dataGroups, context);

        // Transfer transformed data to context
        context.setTransformedData(allTransformedData);
        context.setTransformedDataCount(allTransformedData.size());

        logger.info("TransformSubprocess: Completed transformation, {} records transformed",
            allTransformedData.size());

        return allTransformedData.size();
    }

    /**
     * Groups extracted data by source data type.
     */
    private Map<Class<? extends SourceDataModel>, List<? extends SourceDataModel>> groupByType(
            List<SourceDataModel> extractedData) {

        Map<Class<? extends SourceDataModel>, List<SourceDataModel>> grouped = new HashMap<>();

        for (SourceDataModel record : extractedData) {
            Class<? extends SourceDataModel> recordType = record.getClass();

            if (!grouped.containsKey(recordType)) {
                grouped.put(recordType, new ArrayList<>());
            }

            grouped.get(recordType).add(record);
        }

        return grouped;
    }

    /**
     * Executes transformations for all data groups concurrently.
     */
    private List<TargetDataModel> transformConcurrently(
            Map<Class<? extends SourceDataModel>, List<? extends SourceDataModel>> dataGroups,
            ETLContext context) throws ETLException {

        int numGroups = dataGroups.size();
        ExecutorService executor = Executors.newFixedThreadPool(numGroups);

        try {
            List<Callable<TransformResult>> tasks = new ArrayList<>();
            List<Future<TransformResult>> futures;

            // Create transformation tasks
            for (Map.Entry<Class<? extends SourceDataModel>, List<? extends SourceDataModel>> entry :
                 dataGroups.entrySet()) {

                Class<?> sourceType = entry.getKey();
                List<? extends SourceDataModel> records = entry.getValue();

                tasks.add(() -> transformGroup(sourceType, records));
            }

            // Execute concurrently
            logger.debug("Starting {} concurrent transformations", numGroups);
            futures = executor.invokeAll(tasks);

            // Wait for all tasks and collect results
            List<TargetDataModel> allTransformed = new ArrayList<>();

            for (Future<TransformResult> future : futures) {
                try {
                    TransformResult result = future.get();
                    allTransformed.addAll(result.data);
                } catch (ExecutionException e) {
                    // One of the transformations failed
                    throw new ETLException("TRANSFORM",
                        DateUtils.formatDate(context.getCurrentDate()),
                        "Transformation failed: " + e.getCause().getMessage(),
                        e.getCause());
                }
            }

            return allTransformed;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ETLException("TRANSFORM",
                DateUtils.formatDate(context.getCurrentDate()),
                "Transformation interrupted", e);
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Transforms a group of records using the appropriate transformer.
     */
    private TransformResult transformGroup(Class<?> sourceType,
                                           List<? extends SourceDataModel> records)
            throws TransformationException {

        long startTime = System.currentTimeMillis();
        logger.info("Transforming: {}, record count: {}", sourceType.getSimpleName(), records.size());

        Transformer<?, ?> transformer = TransformerFactory.getTransformer(sourceType);
        List<TargetDataModel> transformedRecords = transformer.transform(records);

        long duration = System.currentTimeMillis() - startTime;
        logger.info("Transformation complete: {}, {} records in {} ms",
            sourceType.getSimpleName(), transformedRecords.size(), duration);

        return new TransformResult(sourceType, transformedRecords);
    }

    /**
     * Helper class to hold transformation results.
     */
    private static class TransformResult {
        final Class<?> sourceType;
        final List<TargetDataModel> data;

        TransformResult(Class<?> sourceType, List<TargetDataModel> data) {
            this.sourceType = sourceType;
            this.data = data;
        }
    }
}
```

---

## 4. Testing

### 4.1 Unit Test Example

**File**: `src/test/java/com/sdd/etl/loader/transformer/XbondQuoteTransformerTest.java`

```java
package com.sdd.etl.loader.transformer;

import com.sdd.etl.loader.model.XbondQuoteDataModel;
import com.sdd.etl.loader.transformer.exceptions.TransformationException;
import com.sdd.etl.model.XbondQuoteDataModel;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class XbondQuoteTransformerTest {

    private XbondQuoteTransformer transformer;

    @Before
    public void setUp() {
        transformer = new XbondQuoteTransformer();
    }

    @Test
    public void testTransformEmptyList() throws TransformationException {
        List<com.sdd.etl.model.XbondQuoteDataModel> source = new ArrayList<>();
        List<com.sdd.etl.loader.model.XbondQuoteDataModel> result = transformer.transform(source);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testTransformSingleRecord() throws TransformationException {
        com.sdd.etl.model.XbondQuoteDataModel source = new com.sdd.etl.model.XbondQuoteDataModel();
        source.setBusinessDate("20260111");
        source.setExchProductId("TEST001");
        source.setEventTime(LocalDateTime.now());

        List<com.sdd.etl.model.XbondQuoteDataModel> sourceList = new ArrayList<>();
        sourceList.add(source);

        List<com.sdd.etl.loader.model.XbondQuoteDataModel> result = transformer.transform(sourceList);

        assertNotNull(result);
        assertEquals(1, result.size());

        com.sdd.etl.loader.model.XbondQuoteDataModel target = result.get(0);
        assertNotNull(target);
        assertEquals("2026-01-11", target.getBusinessDate().toString());
        assertEquals("TEST001", target.getExchProductId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTransformNullList() throws TransformationException {
        transformer.transform(null);
    }
}
```

### 4.2 Run Tests

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=XbondQuoteTransformerTest

# Run with coverage
./mvnw clean test jacoco:report
```

---

## 5. Verification

### 5.1 Build

```bash
./mvnw clean package
```

### 5.2 Run Integration Test

```bash
# Run ETL workflow
java -jar target/etl-tool-1.0.0.jar transform --config test-config.ini
```

### 5.3 Performance Test

```bash
# Benchmark transformation time
./mvnw test -Dtest=TransformSubprocessPerformanceTest
```

---

## 6. Troubleshooting

### 6.1 Common Issues

| Issue | Cause | Solution |
|-------|--------|----------|
| No transformer found | Source type not registered | Check TransformerFactory static initializer |
| Field mapping error | Field name mismatch | Verify field names match exactly (case-sensitive) |
| Type conversion error | Incompatible types | Check type conversion rules in AbstractTransformer |
| Concurrent execution fails | Thread pool issue | Check ExecutorService configuration |

### 6.2 Debugging

```java
// Enable debug logging
<logger name="com.sdd.etl.loader.transformer" level="DEBUG"/>

// Add breakpoints
- AbstractTransformer.transformRecord()
- TransformerFactory.getTransformer()
- TransformSubprocess.execute()
```

---

## 7. Next Steps

1. ✅ Create exception class
2. ✅ Create Transformer interface
3. ✅ Create AbstractTransformer base class
4. ✅ Create concrete transformers (XbondQuote, XbondTrade, BondFutureQuote)
5. ✅ Create TransformerFactory
6. ✅ Update TransformSubprocess
7. ⏳ Write unit tests
8. ⏳ Write integration tests
9. ⏳ Run performance benchmarks
10. ⏳ Verify against success criteria

---

## 8. References

- [Transformer API Contract](./contracts/transformer-api.md)
- [Data Model](./data-model.md)
- [Research](./research.md)
- [Feature Spec](./spec.md)
