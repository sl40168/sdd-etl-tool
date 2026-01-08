# Context API Contract

**Feature Branch**: `001-etl-core-workflow`
**Date**: 2026-01-08
**Status**: Phase 1 Design

## Overview

This document defines the Context API for the ETL tool. Context provides a centralized mechanism for transferring data between subprocesses, ensuring loose coupling and maintainable data flow architecture.

## API Overview

### Core Interface

**Package**: `com.sdd.etl.context`

**Primary Classes**:
- `ETLContext` - Main context implementation
- `ContextManager` - Context lifecycle management
- `ContextConstants` - Context key constants

---

## ETLContext Class

### Purpose

Provides thread-safe storage for runtime state of a single day's ETL process. All subprocesses read from and write to this context.

### Class Definition

```java
package com.sdd.etl.context;

import java.util.List;
import java.util.Map;

/**
 * ETLContext - Runtime context for a single day's ETL process.
 * All data transfer between subprocesses occurs through this context.
 */
public class ETLContext {

    // Context keys (defined in ContextConstants)
    public static final String KEY_CURRENT_DATE = "currentDate";
    public static final String KEY_CURRENT_SUBPROCESS = "currentSubprocess";
    public static final String KEY_CONFIG = "config";
    public static final String KEY_EXTRACTED_COUNT = "extractedDataCount";
    public static final String KEY_EXTRACTED_DATA = "extractedData";
    public static final String KEY_TRANSFORMED_COUNT = "transformedDataCount";
    public static final String KEY_TRANSFORMED_DATA = "transformedData";
    public static final String KEY_LOADED_COUNT = "loadedDataCount";
    public static final String KEY_VALIDATION_PASSED = "validationPassed";
    public static final String KEY_VALIDATION_ERRORS = "validationErrors";
    public static final String KEY_CLEANUP_PERFORMED = "cleanupPerformed";

    private final Map<String, Object> data;

    /**
     * Constructor - Initializes empty context.
     */
    public ETLContext();

    /**
     * Constructor - Initializes context with required fields.
     * @param currentDate Processing date (YYYYMMDD format)
     * @param config Configuration for this ETL run
     */
    public ETLContext(String currentDate, ETConfiguration config);

    // Getter methods with type safety
    public String getCurrentDate();
    public SubprocessType getCurrentSubprocess();
    public ETConfiguration getConfig();
    public int getExtractedDataCount();
    public Object getExtractedData();
    public int getTransformedDataCount();
    public Object getTransformedData();
    public int getLoadedDataCount();
    public boolean isValidationPassed();
    public List<String> getValidationErrors();
    public boolean isCleanupPerformed();

    // Setter methods (state transitions)
    public void setCurrentDate(String date);
    public void setCurrentSubprocess(SubprocessType subprocess);
    public void setConfig(ETConfiguration config);
    public void setExtractedDataCount(int count);
    public void setExtractedData(Object data);
    public void setTransformedDataCount(int count);
    public void setTransformedData(Object data);
    public void setLoadedDataCount(int count);
    public void setValidationPassed(boolean passed);
    public void setValidationErrors(List<String> errors);
    public void setCleanupPerformed(boolean performed);

    // Generic get/set for extensibility
    public <T> T get(String key, Class<T> type);
    public void set(String key, Object value);

    // Utility methods
    public Map<String, Object> getAll();
    public void clear();
}
```

---

## SubprocessType Enum

### Purpose

Represents the five subprocess types in the ETL pipeline.

### Enum Definition

```java
package com.sdd.etl.context;

public enum SubprocessType {
    EXTRACT,
    TRANSFORM,
    LOAD,
    VALIDATE,
    CLEAN
}
```

---

## ContextManager Class

### Purpose

Manages context lifecycle: creation, validation, and cleanup.

### Class Definition

```java
package com.sdd.etl.context;

/**
 * ContextManager - Manages lifecycle of ETLContext instances.
 */
public class ContextManager {

    /**
     * Creates a new context for a single day's ETL process.
     * @param date Processing date (YYYYMMDD format)
     * @param config ETL configuration
     * @return Initialized ETLContext
     * @throws IllegalArgumentException If date or config is invalid
     */
    public static ETLContext createContext(String date, ETConfiguration config);

    /**
     * Validates context integrity before subprocess execution.
     * @param context Context to validate
     * @param expectedSubprocess Expected current subprocess
     * @return true if context is valid, false otherwise
     */
    public static boolean validateContext(ETLContext context, SubprocessType expectedSubprocess);

    /**
     * Creates an immutable snapshot of context for debugging.
     * @param context Context to snapshot
     * @return Immutable map containing context data
     */
    public static Map<String, Object> snapshot(ETLContext context);

    /**
     * Logs current context state for troubleshooting.
     * @param context Context to log
     * @param logger Logger instance
     */
    public static void logContextState(ETLContext context, ETLogger logger);
}
```

---

## ContextConstants Class

### Purpose

Defines all context keys as constants for type safety and consistency.

### Class Definition

```java
package com.sdd.etl.context;

/**
 * ContextConstants - Defines all context key constants.
 */
public final class ContextConstants {

    // Prevent instantiation
    private ContextConstants() {}

    // Required fields (set at context creation)
    public static final String CURRENT_DATE = "currentDate";
    public static final String CONFIG = "config";

    // Runtime state fields (set during subprocess execution)
    public static final String CURRENT_SUBPROCESS = "currentSubprocess";
    public static final String EXTRACTED_DATA_COUNT = "extractedDataCount";
    public static final String EXTRACTED_DATA = "extractedData";
    public static final String TRANSFORMED_DATA_COUNT = "transformedDataCount";
    public static final String TRANSFORMED_DATA = "transformedData";
    public static final String LOADED_DATA_COUNT = "loadedDataCount";
    public static final String VALIDATION_PASSED = "validationPassed";
    public static final String VALIDATION_ERRORS = "validationErrors";
    public static final String CLEANUP_PERFORMED = "cleanupPerformed";

    // Metadata fields (optional)
    public static final String START_TIME = "startTime";
    public static final String END_TIME = "endTime";
    public static final String DURATION_MS = "durationMs";
}
```

---

## Context State Machine

### State Transitions

```
[Initial]
  currentDate: set
  currentSubprocess: EXTRACT
  config: set
  extractedDataCount: 0
  extractedData: null
  transformedDataCount: 0
  transformedData: null
  loadedDataCount: 0
  validationPassed: false
  cleanupPerformed: false

  ↓ (After Extract)

[Extracted]
  currentSubprocess: TRANSFORM
  extractedDataCount: >0
  extractedData: List<SourceDataModel> or Map<String, Object>

  ↓ (After Transform)

[Transformed]
  currentSubprocess: LOAD
  transformedDataCount: >0
  transformedData: List<TargetDataModel> or Map<String, Object>

  ↓ (After Load)

[Loaded]
  currentSubprocess: VALIDATE
  loadedDataCount: >0

  ↓ (After Validate)

[Validated]
  currentSubprocess: CLEAN
  validationPassed: true/false

  ↓ (After Clean)

[Complete]
  currentSubprocess: CLEAN
  cleanupPerformed: true
```

---

## Context Usage Examples

### 1. Create Context for a Day

```java
// Load configuration
ETConfiguration config = ConfigurationLoader.load("/path/to/config.ini");

// Create context for a specific date
ETLContext context = ContextManager.createContext("20250101", config);

// Context is initialized with required fields
assert context.getCurrentDate().equals("20250101");
assert context.getConfig() == config;
assert context.getCurrentSubprocess() == SubprocessType.EXTRACT;
assert context.getExtractedDataCount() == 0;
```

---

### 2. Subprocess Writes to Context

```java
// Extract subprocess writes extracted data and count to context
public class ExtractSubprocess {
    public SubprocessResult execute(ETLContext context) {
        // Extract actual data from sources
        List<SourceDataModel> extractedData = extractFromSources(context.getConfig());
        int count = extractedData.size();

        // Write both data and count to context (FR-028)
        context.setExtractedData(extractedData);
        context.setExtractedDataCount(count);
        context.setCurrentSubprocess(SubprocessType.TRANSFORM);

        return new SubprocessResult(true, count, null, System.currentTimeMillis());
    }
}
```

---

### 3. Subprocess Reads from Context

```java
// Transform subprocess reads extracted data from context (FR-028)
public class TransformSubprocess {
    public SubprocessResult execute(ETLContext context) {
        // Read actual extracted data from context
        Object extractedDataObj = context.getExtractedData();

        // Validate that previous subprocess completed
        if (extractedDataObj == null) {
            throw new IllegalStateException("No extracted data found in context");
        }

        // Cast to expected type and transform
        List<SourceDataModel> extractedData = (List<SourceDataModel>) extractedDataObj;
        List<TargetDataModel> transformedData = transform(extractedData);

        // Write both data and count to context (FR-029)
        context.setTransformedData(transformedData);
        context.setTransformedDataCount(transformedData.size());
        context.setCurrentSubprocess(SubprocessType.LOAD);

        return new SubprocessResult(true, transformedData.size(), null, System.currentTimeMillis());
    }
}
```

---

### 4. Context Validation

```java
// Before executing a subprocess, validate context state
ETLContext context = ...;
boolean isValid = ContextManager.validateContext(context, SubprocessType.TRANSFORM);
if (!isValid) {
    throw new IllegalStateException("Context is invalid for TRANSFORM subprocess");
}
```

---

### 5. Context Snapshot for Debugging

```java
// Create immutable snapshot for logging/debugging
ETLContext context = ...;
Map<String, Object> snapshot = ContextManager.snapshot(context);

// Snapshot can be safely logged without modifying context
logger.info("Context state: " + snapshot);
```

---

## Thread Safety

**Guarantee**: ETLContext is thread-safe for single-process execution.

**Implementation**: Uses `ThreadLocal` pattern for context storage, ensuring each thread has its own context instance.

**Concurrent Executions**: The CLI tool detects and prevents concurrent executions (FR-024), so only one context instance is active at any time.

---

## Error Handling

### Invalid Context State

**Error**: `IllegalStateException`

**Condition**: Context is in invalid state for requested operation.

**Example**:
```java
ETLContext context = new ETLContext("20250101", config);

// Extract not yet completed, but trying to read transformed count
int count = context.getTransformedDataCount(); // Returns 0 (not error)

// Trying to execute Transform before Extract
context.setCurrentSubprocess(SubprocessType.TRANSFORM); // Valid
// But extractedDataCount is still 0, indicating previous subprocess didn't complete
if (context.getExtractedDataCount() == 0) {
    throw new IllegalStateException("Extract subprocess did not complete");
}
```

---

### Context Corruption

**Error**: `IllegalStateException`

**Condition**: Context data is corrupted or contains invalid values.

**Detection**: Context validation before each subprocess execution.

**Recovery**: Process stops, user must investigate and restart manually (FR-025).

---

## Feature Requirements Mapping

| FR # | Requirement | API Coverage |
|------|-------------|--------------|
| FR-014 | Context creation with date, subprocess, counts, config | ✅ ETLContext constructor, getters |
| FR-015 | Pass context to each subprocess | ✅ ContextManager.createContext() |
| FR-027 | All sub-components use context for data transfer | ✅ ETLContext get/set methods |
| FR-028 | Extract writes to context, Transform reads from context | ✅ setExtractedDataCount(), getExtractedDataCount() |
| FR-029 | Transform writes to context, Load reads from context | ✅ setTransformedDataCount(), getTransformedDataCount() |
| FR-030 | Load writes to context, Validate reads from context | ✅ setLoadedDataCount(), getLoadedDataCount() |
| FR-031 | Validate writes to context, Clean reads from context | ✅ setValidationPassed(), isValidationPassed() |

---

## Source

- Feature Specification: FR-014 through FR-031, User Story 7 (Context-Based Data Transfer)
- Data Model: ETLContext entity definition
- Research: Context implementation pattern (ThreadLocal + HashMap)
