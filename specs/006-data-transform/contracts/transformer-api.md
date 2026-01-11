# Transformer API Contract

**Feature**: 006-data-transform
**Date**: 2026-01-11
**Version**: 1.0

## Overview

This document defines the API contract for the Transformer interface and its implementations. It specifies method signatures, behavior, input/output contracts, and exception handling.

---

## 1. Transformer Interface

### 1.1 Interface Definition

```java
package com.sdd.etl.loader.transformer;

import com.sdd.etl.loader.transformer.exceptions.TransformationException;
import java.util.List;

/**
 * Transformer API for converting source data models to target data models.
 * <p>
 * Implementations MUST follow these contracts:
 * <ul>
 *   <li>One-to-one transformation: Each source record produces exactly one target record</li>
 *   <li>Field mapping based on field names (case-sensitive)</li>
 *   <li>Missing source fields result in unassigned target fields</li>
 *   <li>Type conversion between compatible types</li>
 *   <li>Throw TransformationException on any failure</li>
 * </ul>
 *
 * @param <S> Source data model type (extends SourceDataModel)
 * @param <T> Target data model type (extends TargetDataModel)
 */
public interface Transformer<S extends SourceDataModel, T extends TargetDataModel> {

    /**
     * Transforms a list of source records to target records.
     * Records are transformed one by one (not batch transformation).
     *
     * @param sourceRecords List of source records (must be non-null, same type S)
     * @return List of transformed target records (same type T, same size as input)
     * @throws TransformationException if any record transformation fails
     * @throws IllegalArgumentException if sourceRecords is null
     */
    List<T> transform(List<S> sourceRecords) throws TransformationException;

    /**
     * Gets the source data model class this transformer supports.
     *
     * @return Source data model class (never null)
     */
    Class<S> getSourceType();

    /**
     * Gets the target data model class this transformer produces.
     *
     * @return Target data model class (never null)
     */
    Class<T> getTargetType();
}
```

### 1.2 Method Contracts

#### transform(List<S> sourceRecords)

**Input Contract**:
- `sourceRecords` MUST be non-null
- `sourceRecords` MUST contain only instances of type S (same class)
- `sourceRecords` MAY be empty (returns empty list)
- Individual records MAY be null (skip null records with warning log)

**Output Contract**:
- Returns a non-null List<T>
- Returned list size equals input list size (excluding null records)
- Each target record is a new instance (not shared)
- Target records preserve input order

**Exception Contract**:
- Throws `IllegalArgumentException` if `sourceRecords` is null
- Throws `TransformationException` if any record transformation fails
- On exception, no partial results are returned (all-or-nothing)

**Performance Contract**:
- Time complexity: O(n * f) where n = record count, f = average fields per record
- Space complexity: O(n) for output list
- No external I/O (in-memory operation)

---

## 2. TransformerFactory Contract

### 2.1 Factory Definition

```java
package com.sdd.etl.loader.transformer;

import com.sdd.etl.loader.transformer.exceptions.TransformationException;

/**
 * Factory for selecting appropriate Transformer based on source data type.
 */
public class TransformerFactory {

    /**
     * Gets the transformer for the specified source data type.
     *
     * @param sourceType Source data model class (must be non-null)
     * @return Transformer instance (never null)
     * @throws TransformationException if no transformer is registered for sourceType
     * @throws IllegalArgumentException if sourceType is null
     */
    public static Transformer<?, ?> getTransformer(Class<?> sourceType)
            throws TransformationException;
}
```

### 2.2 Registered Transformers

| Source Type | Transformer | Target Type |
|-------------|-------------|-------------|
| com.sdd.etl.model.XbondQuoteDataModel | XbondQuoteTransformer | com.sdd.etl.loader.model.XbondQuoteDataModel |
| com.sdd.etl.model.XbondTradeDataModel | XbondTradeTransformer | com.sdd.etl.loader.model.XbondTradeDataModel |
| com.sdd.etl.source.extract.db.quote.BondFutureQuoteDataModel | BondFutureQuoteTransformer | com.sdd.etl.loader.model.BondFutureQuoteDataModel |

---

## 3. Exception Contract

### 3.1 TransformationException

```java
package com.sdd.etl.loader.transformer.exceptions;

import com.sdd.etl.ETLException;

/**
 * Exception thrown when data transformation fails.
 */
public class TransformationException extends ETLException {

    private final String sourceDataType;
    private final int recordCount;

    /**
     * Constructs a new TransformationException.
     *
     * @param sourceDataType Name of source data type that failed
     * @param recordCount Number of records processed before failure
     * @param message Detailed error message
     */
    public TransformationException(String sourceDataType, int recordCount, String message) {
        super("TRANSFORM", message);
        this.sourceDataType = sourceDataType;
        this.recordCount = recordCount;
    }

    /**
     * Constructs a new TransformationException with cause.
     *
     * @param sourceDataType Name of source data type that failed
     * @param recordCount Number of records processed before failure
     * @param message Detailed error message
     * @param cause Root cause exception
     */
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

### 3.2 Exception Scenarios

| Scenario | Exception Type | Error Message Format |
|----------|----------------|---------------------|
| Null sourceRecords list | IllegalArgumentException | "Source records list cannot be null" |
| No transformer for source type | TransformationException | "No transformer found for source type: {typeName}" |
| Field mapping error | TransformationException | "Field mapping failed: {fieldName}, source type: {typeName}" |
| Type conversion error | TransformationException | "Type conversion failed: {sourceType} → {targetType}, field: {fieldName}" |
| Reflection access error | TransformationException | "Reflection access error: {className}, field: {fieldName}" |

---

## 4. Field Mapping Contract

### 4.1 Mapping Rules

| Source Field | Target Field | Mapped? | If Not Found |
|--------------|--------------|---------|--------------|
| Same name, same type | Same name, same type | ✅ Yes | N/A |
| Same name, compatible types | Same name, compatible types | ✅ Yes | Convert automatically |
| Same name, incompatible types | Same name, incompatible types | ❌ No | Skip with warning |
| Different name | Any | ❌ No | Skip (no mapping) |
| Missing in source | Any | ❌ No | Leave unassigned |
| Extra in source | N/A | ❌ No | Ignore |

### 4.2 Type Conversion Matrix

| Source Type | Target Type | Valid? | Conversion |
|------------|-------------|--------|------------|
| Integer | int | ✅ Yes | Auto-unboxing (null → -1) |
| Long | long | ✅ Yes | Auto-unboxing (null → -1L) |
| Double | double | ✅ Yes | Auto-unboxing (null → Double.NaN) |
| Float | double | ✅ Yes | Auto-unboxing (null → Double.NaN) |
| String | String | ✅ Yes | Direct copy (null → null) |
| LocalDateTime | LocalDateTime | ✅ Yes | Direct copy (null → null) |
| LocalDateTime | Instant | ✅ Yes | Convert (null → null) |
| LocalDate | LocalDate | ✅ Yes | Direct copy (null → null) |
| String | int | ❌ No | Skip with warning |
| int | String | ❌ No | Skip with warning |

### 4.3 Sentinel Values

| Target Type | Sentinel Value | Meaning |
|-------------|----------------|---------|
| int | -1 | Not set (Constitution Principle 11) |
| long | -1L | Not set (Constitution Principle 11) |
| double | Double.NaN | Not set (Constitution Principle 11) |
| String | null | Not set |
| LocalDateTime | null | Not set |
| Instant | null | Not set |

---

## 5. Concrete Transformer Contracts

### 5.1 XbondQuoteTransformer

**Source**: `com.sdd.etl.model.XbondQuoteDataModel`
**Target**: `com.sdd.etl.loader.model.XbondQuoteDataModel`
**Field Count**: 83 fields

**Special Conversions**:
- `businessDate`: String → LocalDate (format: YYYYMMDD)
- All numeric wrapper types → primitives with sentinel values

**Validation**:
- Source businessDate must match YYYYMMDD format
- At least one price field must be non-null
- eventTime must not be null

### 5.2 XbondTradeTransformer

**Source**: `com.sdd.etl.model.XbondTradeDataModel`
**Target**: `com.sdd.etl.loader.model.XbondTradeDataModel`
**Field Count**: 15 fields

**Special Conversions**:
- `businessDate`: String → LocalDate (format: YYYYMMDD)
- `eventTime`: LocalDateTime → Instant
- `receiveTime`: LocalDateTime → Instant
- All numeric wrapper types → primitives with sentinel values

**Validation**:
- Source businessDate must match YYYYMMDD format
- price must not be null
- volume must be positive (> 0)
- eventTime must not be null

### 5.3 BondFutureQuoteTransformer

**Source**: `com.sdd.etl.source.extract.db.quote.BondFutureQuoteDataModel`
**Target**: `com.sdd.etl.loader.model.BondFutureQuoteDataModel`
**Field Count**: 96 fields

**Special Conversions**:
- Same as XbondQuoteTransformer for common fields
- Additional bond future-specific fields handled with same rules

**Validation**:
- Source businessDate must match YYYYMMDD format
- At least one price field must be non-null
- eventTime must not be null

---

## 6. TransformSubprocess Contract

### 6.1 Subprocess Interface Implementation

```java
package com.sdd.etl.subprocess;

import com.sdd.etl.ETLException;
import com.sdd.etl.context.ETLContext;
import com.sdd.etl.context.SubprocessType;

public abstract class TransformSubprocess implements SubprocessInterface {

    /**
     * Executes transform operation.
     *
     * Process:
     * 1. Retrieve all extracted data from ETLContext
     * 2. Group extracted data by source type
     * 3. Select appropriate transformer for each group
     * 4. Execute transformations concurrently using ExecutorService
     * 5. Consolidate all transformed data into single list
     * 6. Transfer transformed data to ETLContext
     * 7. Halt immediately on any exception
     *
     * @param context ETL context containing execution state (must be non-null)
     * @return Number of records transformed
     * @throws ETLException if transformation fails or context is invalid
     */
    @Override
    public abstract int execute(ETLContext context) throws ETLException;

    /**
     * Validates context state before transformation.
     *
     * @param context ETL context to validate (must be non-null)
     * @throws ETLException if context is invalid or missing required data
     */
    @Override
    public void validateContext(ETLContext context) throws ETLException;

    /**
     * Gets the type of this subprocess.
     *
     * @return SubprocessType.TRANSFORM
     */
    @Override
    public SubprocessType getType();
}
```

### 6.2 Execution Flow

```
┌─────────────────────────────────────────────────────────┐
│ TransformSubprocess.execute(context)                     │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼
              ┌───────────────────────┐
              │ validateContext()    │
              └───────────────────────┘
                          │
                          ▼
              ┌───────────────────────┐
              │ context.getExtractedData() │
              └───────────────────────┘
                          │
                          ▼
              ┌───────────────────────┐
              │ Group by source type   │
              └───────────────────────┘
                          │
                          ▼
              ┌───────────────────────┐
              │ Create transformer    │
              │ tasks (one per type) │
              └───────────────────────┘
                          │
                          ▼
              ┌───────────────────────┐
              │ Execute concurrently  │
              │ (ExecutorService)    │
              └───────────────────────┘
                          │
                          ▼
              ┌───────────────────────┐
              │ Wait for all tasks   │
              │ OR fail on first error│
              └───────────────────────┘
                          │
                          ▼
              ┌───────────────────────┐
              │ Consolidate results  │
              └───────────────────────┘
                          │
                          ▼
              ┌───────────────────────┐
              │ context.setTransformedData() │
              └───────────────────────┘
                          │
                          ▼
              ┌───────────────────────┐
              │ Return record count   │
              └───────────────────────┘
```

### 6.3 Concurrency Contract

**Thread Pool**:
- Fixed thread pool (size = number of data types)
- Maximum 3 threads (for XbondQuote, XbondTrade, BondFutureQuote)
- Thread naming: "transformer-{sourceType}-{index}"

**Error Handling**:
- First exception cancels all pending tasks
- `ExecutionException` wrapped in `TransformationException`
- All errors logged at ERROR level

**Resource Management**:
- ExecutorService shutdown in finally block
- 60-second timeout for graceful shutdown
- Force shutdown if timeout exceeded

---

## 7. Logging Contract

### 7.1 Log Levels

| Level | When to Use | Example |
|-------|-------------|---------|
| ERROR | Transformation failures | "Transformation failed: Field mapping error: price, source type: XbondQuoteDataModel" |
| WARN | Non-critical issues | "Skipping field with incompatible type: String → int, field: settleSpeed" |
| INFO | Major milestones | "Transformation started: 1000 records, source type: XbondQuoteDataModel" |
| DEBUG | Detailed operations | "Field mapped: bid0Price (Double) → bid0Price (double), value: 102.5" |

### 7.2 Log Messages

**Start Transformation**:
```
INFO - TransformSubprocess: Starting transformation for data type: XbondQuoteDataModel, record count: 1000
```

**Field Mapping**:
```
DEBUG - XbondQuoteTransformer: Mapped field: businessDate (String → LocalDate), value: "20260111" → 2026-01-11
```

**Missing Field**:
```
DEBUG - XbondQuoteTransformer: Source field not found: preClosePrice, leaving target unassigned
```

**Type Conversion Warning**:
```
WARN - XbondQuoteTransformer: Incompatible type: settleSpeed (String) → int, skipping
```

**Transformation Complete**:
```
INFO - TransformSubprocess: Transformation complete: XbondQuoteDataModel, 1000 records transformed in 1.23s
```

**Transformation Failed**:
```
ERROR - TransformSubprocess: Transformation failed for XbondQuoteDataModel, processed 500/1000 records
ERROR - TransformSubprocess: Root cause: java.lang.NumberFormatException: For input string: "invalid"
```

---

## 8. Testing Contract

### 8.1 Unit Tests

**Transformer Interface**:
- Test with empty list
- Test with single record
- Test with multiple records
- Test with null records in list
- Test with all fields present
- Test with missing optional fields
- Test field name matching
- Test type conversion

**TransformerFactory**:
- Test getTransformer for each registered type
- Test exception for unregistered type
- Test thread safety (if applicable)

**Concrete Transformers**:
- Test field mapping for all fields
- Test special conversions (String → LocalDate, LocalDateTime → Instant)
- Test sentinel value assignment (-1, NaN)
- Test null handling

### 8.2 Integration Tests

**TransformSubprocess**:
- Test with single data type
- Test with multiple data types (concurrent)
- Test error propagation (first exception halts all)
- Test empty extracted data
- Test context integration

### 8.3 Performance Tests

- 10,000 records < 30 seconds per data type
- Concurrent speedup > 40% vs sequential
- Memory usage monitoring
- Benchmark field reflection access

---

## 9. Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2026-01-11 | Initial version |

---

## 10. References

- Feature Specification: [../spec.md](../spec.md)
- Data Model: [../data-model.md](../data-model.md)
- Research: [../research.md](../research.md)
- Implementation Plan: [../plan.md](../plan.md)
- Java Reflection API: https://docs.oracle.com/javase/8/docs/api/java/lang/reflect/package-summary.html
- ExecutorService: https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ExecutorService.html
