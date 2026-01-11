# Research: Data Transformation Pipeline

**Feature**: 006-data-transform
**Date**: 2026-01-11

## Overview

This document consolidates research findings for the data transformation pipeline feature. All technical decisions and alternatives evaluated are documented here.

## Decision Summary

| Decision | Chosen Approach | Key Reason |
|----------|----------------|------------|
| Field Mapping | Java Reflection | Flexible, type-safe, eliminates code duplication |
| Concurrency | ExecutorService | Built-in exception handling, proven pattern |
| Error Handling | Immediate Failure Halt | Data integrity, meets FR-014 requirement |
| Type Conversion | Null Safety with Sentinels | Constitution Principle 11 compliance |

---

## 1. Field Mapping Strategy

### Requirements
- FR-006: Transformer MUST map fields based on field names between source and target models
- FR-007: Missing source fields result in unassigned (null) target fields
- FR-003: Transform data one by one (not batch transformation)

### Decision: Java Reflection

**Implementation Details**:
```java
// Pseudo-code for field mapping
Field sourceField = sourceModel.getClass().getDeclaredField(fieldName);
Field targetField = targetModel.getClass().getDeclaredField(fieldName);
sourceField.setAccessible(true);
targetField.setAccessible(true);
Object value = sourceField.get(sourceModel);
if (value != null) {
    targetField.set(targetModel, value);
}
```

**Advantages**:
- Eliminates 194 manual field mappings (83 + 15 + 96 fields)
- Type-safe through Java reflection API
- Automatic type conversion (wrapper to wrapper, primitive to primitive)
- Future-proof: Adding new fields requires no mapping code changes
- Aligns with "one-by-one" transformation requirement

**Performance Considerations**:
- Reflection overhead is acceptable for record-by-record transformation
- Can optimize with field access caching if needed (measured in implementation)
- Benchmark: Expected < 0.001ms per field access

**Alternatives Rejected**:

1. **Manual Field-by-Field Mapping**
   - High code duplication (194 mappings across 3 transformers)
   - Maintenance burden: Field changes require updating mapping code
   - Violates DRY principle

2. **Map-Based Intermediate Representation**
   - Adds unnecessary indirection layer
   - Loses type safety
   - Additional object creation overhead

3. **Annotation-Based Mapping (@Mapping)**
   - Adds complexity without clear benefit for simple name-based mapping
   - Requires additional dependency (e.g., MapStruct, ModelMapper)
   - Overkill for one-to-one field matching

### Type Conversion Rules

| Source Type | Target Type | Conversion | Null Handling |
|------------|-------------|------------|---------------|
| Integer | int | Auto-unboxing | Use sentinel value (-1) |
| Long | long | Auto-unboxing | Use sentinel value (-1L) |
| Double | double | Auto-unboxing | Use sentinel value (Double.NaN) |
| String | String | Direct copy | Keep as null |
| LocalDateTime | LocalDateTime | Direct copy | Keep as null |
| LocalDate | LocalDate | Direct copy | Keep as null |

---

## 2. Concurrent Processing Implementation

### Requirements
- FR-011: TransformSubprocess MUST select and execute transformers concurrently
- FR-014: TransformSubprocess MUST immediately halt if any exception occurs
- SC-002: 40%+ speedup with concurrent processing across all three data types

### Decision: ExecutorService with Fixed Thread Pool

**Implementation Details**:
```java
// Pseudo-code for concurrent transformation
ExecutorService executor = Executors.newFixedThreadPool(dataTypes.size());
List<Callable<TransformResult>> tasks = createTransformerTasks(dataGroups);

try {
    List<Future<TransformResult>> futures = executor.invokeAll(tasks);
    for (Future<TransformResult> future : futures) {
        future.get(); // This will throw ExecutionException if any task failed
    }
} catch (ExecutionException e) {
    // One of the transformations failed
    throw new TransformationException("Transformation failed", e.getCause());
} finally {
    executor.shutdown();
}
```

**Thread Pool Configuration**:
- Fixed thread pool size = number of data types (3 for this feature)
- Avoids resource contention while enabling parallelism
- Predictable behavior: No more threads than data types to transform

**Advantages**:
- Built-in exception handling via `Future.get()`
- Thread pool managed by Java (no manual thread lifecycle)
- Timeout support if needed (not required by spec)
- Well-documented pattern with extensive community resources
- Meets FR-014 requirement: First exception propagates immediately

**Performance Expectations**:
- Sequential baseline: ~3x time for 3 data types (sum of individual times)
- Concurrent target: ~1.1-1.5x time (dominant task + overhead)
- Expected speedup: ~40-60% (exceeds SC-002 requirement of 40%)

**Alternatives Rejected**:

1. **Java 8 Parallel Streams**
   - Exception handling is less controllable
   - `parallelStream()` continues on exception in some cases
   - Cannot easily cancel pending tasks on first failure
   - Violates FR-014 immediate halt requirement

2. **Fork/Join Framework**
   - Overkill for simple parallel task execution
   - Designed for recursive task decomposition, not simple parallelism
   - More complex than necessary

3. **Manual Thread Creation**
   - No built-in thread management
   - Requires manual exception aggregation
   - Potential for thread leaks if not properly managed

### Exception Handling Strategy

**Requirements**:
- FR-014: Immediate halt on any exception
- FR-015: Notify user for manual investigation

**Implementation**:
```java
try {
    // ... execute transformations ...
} catch (ExecutionException e) {
    // Cancel all pending tasks
    futures.forEach(f -> f.cancel(true));

    // Extract root cause
    Throwable rootCause = e.getCause();

    // Log detailed error
    logger.error("Transformation failed: {}", rootCause.getMessage(), rootCause);

    // Propagate to caller
    throw new TransformationException("Transformation failed", rootCause);
}
```

**Key Behaviors**:
- First exception thrown triggers immediate cancellation
- Pending tasks receive interrupt signal
- All exceptions logged with full stack trace
- User receives clear error message for manual investigation

---

## 3. Error Handling Patterns

### Requirements
- FR-014: TransformSubprocess MUST immediately halt on any exception
- FR-015: System MUST notify user for manual investigation
- Edge Case: Missing transformer for source data type → clear error message

### Decision: Custom TransformationException

**Implementation**:
```java
public class TransformationException extends ETLException {
    private final String sourceDataType;
    private final int recordCount;

    public TransformationException(String sourceDataType, int recordCount,
                                   String message, Throwable cause) {
        super("TRANSFORM", message, cause);
        this.sourceDataType = sourceDataType;
        this.recordCount = recordCount;
    }

    // Getters...
}
```

**Exception Categories**:

| Category | Example | Handling |
|----------|---------|----------|
| Missing Transformer | Unknown source type | Fail fast with clear message |
| Field Mapping Error | Type mismatch | Log field name, propagate exception |
| Data Validation Error | Invalid data format | Log record details, propagate exception |
| Concurrency Error | Thread interrupted | Log thread info, propagate exception |

**Logging Strategy**:
- ERROR level: All transformation failures (with stack trace)
- WARN level: Missing optional fields (no action required)
- INFO level: Transformation start/completion with record counts
- DEBUG level: Field-by-field mapping details (development only)

**User Notification**:
- Exception message includes:
  - Source data type that failed
  - Number of records processed before failure
  - Root cause description
  - Suggested next steps (e.g., "Check data format for source: XbondQuote")

---

## 4. Type Conversion Strategy

### Requirements
- Constitution Principle 11: No default zero initialization for primitive number fields
- Target models use sentinel values: -1 for int/long, Double.NaN for double
- Source models may have null values for optional fields

### Decision: Null Safety with Sentinel Values

**Implementation Rules**:

1. **Null Source → Sentinel Target**
   ```java
   if (sourceValue == null) {
       if (targetFieldType == int.class) {
           targetField.setInt(target, -1);
       } else if (targetFieldType == double.class) {
           targetField.setDouble(target, Double.NaN);
       } else if (targetFieldType == long.class) {
           targetField.setLong(target, -1L);
       }
   }
   ```

2. **Non-Null Source → Direct Assignment**
   ```java
   else {
       targetField.set(target, sourceValue);
   }
   ```

3. **Type Mismatch → Log Warning and Skip**
   ```java
   else if (!targetFieldType.isAssignableFrom(sourceValue.getClass())) {
       logger.warn("Type mismatch: source={}, target={}, value={}",
                   sourceType, targetType, sourceValue);
       // Leave target field as default sentinel value
   }
   ```

**Type Conversion Matrix**:

| Source | Target | Valid? | Action |
|--------|--------|--------|--------|
| null | int | Yes | Set to -1 |
| Integer | int | Yes | Unbox and set |
| Double | double | Yes | Unbox and set |
| Long | long | Yes | Unbox and set |
| String | String | Yes | Direct copy |
| null | String | Yes | Keep as null |
| LocalDateTime | LocalDateTime | Yes | Direct copy |
| null | LocalDateTime | Yes | Keep as null |
| Integer | double | Yes | Auto-convert |
| String | int | No | Skip with warning |

**Validation**:
- Unit tests verify all type conversion scenarios
- Integration tests with real data samples
- Warn-only for type mismatches (fail fast for data integrity issues only)

---

## 5. Performance Benchmarks

### Test Scenarios

| Scenario | Record Count | Expected Time | Success Criteria |
|----------|--------------|---------------|------------------|
| Xbond Quote Transformation | 10,000 | < 30s | SC-001 |
| Xbond Trade Transformation | 10,000 | < 10s | SC-001 |
| Bond Future Quote Transformation | 10,000 | < 35s | SC-001 |
| All Three (Concurrent) | 10,000 each | < 75s (40%+ speedup) | SC-002 |
| Hourly Throughput | Mixed | > 1M records | SC-005 |

### Measurement Approach

1. **Unit-Level Benchmarking**
   - JMH (Java Microbenchmark Harness) for field mapping operations
   - Measure per-field access time
   - Validate reflection overhead < 0.001ms/field

2. **Integration-Level Benchmarking**
   - End-to-end transformation time with real data samples
   - Measure concurrent vs sequential execution
   - Validate 40%+ speedup requirement

3. **Stress Testing**
   - Large datasets (100K+ records)
   - Memory usage monitoring
   - Identify bottlenecks

---

## 6. Best Practices Integration

### Java 8 Best Practices

- Use `try-with-resources` for ExecutorService auto-shutdown
- Prefer `CompletableFuture` for async operations (not needed for this feature)
- Use `@Override` annotations consistently
- Document all public APIs with Javadoc

### Concurrency Best Practices

- Immutable data objects (SourceDataModel, TargetDataModel are immutable during transformation)
- No shared mutable state between transformer threads
- Thread-safe collections only when needed (e.g., `ConcurrentHashMap` not required here)
- Proper exception handling in all Callable implementations

### ETL Pipeline Best Practices

- Fail fast on data integrity issues
- Log all transformations with metadata (source type, record count, duration)
- Preserve traceability: Each target record references its source
- Support replayability: Transformation should be deterministic

---

## 7. Technology Dependencies

### Existing Dependencies (No New Dependencies Required)

| Dependency | Version | Purpose | Status |
|------------|---------|---------|--------|
| Java 8 | 1.8.0+ | Platform | ✅ In project |
| SLF4J | 1.7.36 | Logging API | ✅ In project |
| Logback | 1.2.11 | Logging Implementation | ✅ In project |
| JUnit 4 | 4.13.2 | Unit Testing | ✅ In project |
| Mockito | 4.5.1 | Mocking | ✅ In project |
| Apache Commons CLI | 1.4 | CLI parsing | ✅ In project (not used in this feature) |
| Apache Commons Configuration | 2.8.0 | Config parsing | ✅ In project (not used in this feature) |

### Dependencies Considered and Rejected

| Library | Reason for Rejection |
|---------|----------------------|
| MapStruct | Unnecessary complexity for simple name-based mapping |
| ModelMapper | Overkill, reflection is sufficient |
| Apache Commons BeanUtils | Not needed, Java reflection is adequate |
| Guava | No additional features needed beyond Java 8 stdlib |
| Lombok | Not used in project (not in pom.xml) |

---

## 8. Open Research Questions

**None.** All technical decisions have been resolved.

---

## 9. References

- Java 8 Reflection API: https://docs.oracle.com/javase/8/docs/api/java/lang/reflect/package-summary.html
- ExecutorService Guide: https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ExecutorService.html
- JMH Benchmarking: http://openjdk.java.net/projects/code-tools/jmh/
- Project Constitution: `.specify/memory/constitution.md`
- Feature Spec: `./spec.md`
