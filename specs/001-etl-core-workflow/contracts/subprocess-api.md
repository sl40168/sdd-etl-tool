# Subprocess API Contract

**Feature Branch**: `001-etl-core-workflow`
**Date**: 2026-01-08
**Status**: Phase 1 Design

## Overview

This document defines the Subprocess API for the five ETL subprocesses. **NOTE**: This phase defines API contracts only; concrete implementations are NOT required.

## API Overview

### Subprocess Types

1. **ExtractSubprocess** - Extracts data from multiple sources
2. **TransformSubprocess** - Transforms extracted data for targets
3. **LoadSubprocess** - Loads transformed data to multiple targets
4. **ValidateSubprocess** - Validates loaded data quality
5. **CleanSubprocess** - Cleans up temporary resources

**Scope**: API definitions only; concrete implementations will be provided in future phases.

---

## Common SubprocessInterface

All subprocesses implement this common interface (defined in workflow-api.md):

```java
package com.sdd.etl.subprocess;

import com.sdd.etl.context.ETLContext;

public interface SubprocessInterface {
    SubprocessResult execute(ETLContext context) throws ETLException;
    boolean validateContext(ETLContext context);
    SubprocessType getType();
}
```

---

## ExtractSubprocess (API Definition)

### Purpose

Extracts data from multiple data sources configured in the INI file and writes the extracted data count to context.

### Class Definition

```java
package com.sdd.etl.subprocess;

import com.sdd.etl.context.ETLContext;
import com.sdd.etl.config.ETConfiguration;
import com.sdd.etl.config.SourceConfig;

/**
 * ExtractSubprocess - Extracts data from multiple data sources.
 * NOTE: This phase defines the API only; concrete implementation is not required.
 */
public abstract class ExtractSubprocess implements SubprocessInterface {

    /**
     * Executes data extraction from all configured sources.
     *
     * Context MUST contain:
     * - currentSubprocess: EXTRACT
     * - config: ETConfiguration with sources list
     *
     * Context MUST be updated with:
     * - extractedData: Actual extracted data objects (List<SourceDataModel> or similar)
     * - extractedDataCount: Total records extracted from all sources
     * - currentSubprocess: TRANSFORM (if successful)
     *
     * @param context ETLContext with configuration
     * @return SubprocessResult with success status and extracted count
     * @throws ETLException If extraction fails from any source
     */
    @Override
    public abstract SubprocessResult execute(ETLContext context) throws ETLException;

    /**
     * Validates context before extraction.
     * Checks:
     * - config is not null
     * - config.sources is not empty
     * - Each source has valid connection string
     *
     * @param context Context to validate
     * @return true if valid, false otherwise
     */
    @Override
    public boolean validateContext(ETLContext context);

    /**
     * Gets the subprocess type.
     * @return SubprocessType.EXTRACT
     */
    @Override
    public SubprocessType getType();

    // Abstract methods (to be implemented in future phases)

    /**
     * Extracts data from a single source.
     * @param source Source configuration
     * @return Number of records extracted
     * @throws ETLException If extraction fails
     */
    protected abstract int extractFromSource(SourceConfig source) throws ETLException;

    /**
     * Validates that all sources can be connected to.
     * @param sources List of source configurations
     * @throws ETLException If any source is unreachable
     */
    protected abstract void validateSourceConnections(List<SourceConfig> sources) throws ETLException;
}
```

### Context Contract

**Input Context**:
- `currentSubprocess`: EXTRACT
- `config`: ETConfiguration with sources

**Output Context** (if successful):
- `extractedDataCount`: Sum of records from all sources
- `currentSubprocess`: TRANSFORM

**Error Conditions**:
- No sources configured
- Source connection failure
- Extraction failure from any source

---

## TransformSubprocess (API Definition)

### Purpose

Transforms extracted data from source format to target format and writes the transformed data count to context.

### Class Definition

```java
package com.sdd.etl.subprocess;

import com.sdd.etl.context.ETLContext;
import com.sdd.etl.config.ETConfiguration;

/**
 * TransformSubprocess - Transforms extracted data for targets.
 * NOTE: This phase defines the API only; concrete implementation is not required.
 */
public abstract class TransformSubprocess implements SubprocessInterface {

    /**
     * Executes data transformation.
     *
     * Context MUST contain:
     * - currentSubprocess: TRANSFORM
     * - extractedData: Actual extracted data objects (List<SourceDataModel> or similar)
     * - extractedDataCount: Number of records extracted
     * - config: ETConfiguration with transformations
     *
     * Context MUST be updated with:
     * - transformedData: Actual transformed data objects (List<TargetDataModel> or similar)
     * - transformedDataCount: Number of records transformed
     * - currentSubprocess: LOAD (if successful)
     *
     * @param context ETLContext with extracted data
     * @return SubprocessResult with success status and transformed count
     * @throws ETLException If transformation fails
     */
    @Override
    public abstract SubprocessResult execute(ETLContext context) throws ETLException;

    /**
     * Validates context before transformation.
     * Checks:
     * - extractedDataCount > 0 (extract completed)
     * - config.transformations is not empty (if required)
     *
     * @param context Context to validate
     * @return true if valid, false otherwise
     */
    @Override
    public boolean validateContext(ETLContext context);

    /**
     * Gets the subprocess type.
     * @return SubprocessType.TRANSFORM
     */
    @Override
    public SubprocessType getType();

    // Abstract methods (to be implemented in future phases)

    /**
     * Transforms extracted data to target format.
     * @param extractedCount Number of records to transform
     * @param config Configuration with transformation rules
     * @return Number of records transformed
     * @throws ETLException If transformation fails
     */
    protected abstract int transform(int extractedCount, ETConfiguration config) throws ETLException;
}
```

### Context Contract

**Input Context**:
- `currentSubprocess`: TRANSFORM
- `extractedDataCount`: >0 (extract completed)
- `config`: ETConfiguration with transformations

**Output Context** (if successful):
- `transformedDataCount`: Number of records transformed
- `currentSubprocess`: LOAD

**Error Conditions**:
- `extractedDataCount` is 0 (extract didn't complete)
- Transformation rule failure

---

## LoadSubprocess (API Definition)

### Purpose

Loads transformed data to multiple targets configured in the INI file and writes the loaded data count to context.

### Class Definition

```java
package com.sdd.etl.subprocess;

import com.sdd.etl.context.ETLContext;
import com.sdd.etl.config.ETConfiguration;
import com.sdd.etl.config.TargetConfig;

/**
 * LoadSubprocess - Loads transformed data to multiple targets.
 * NOTE: This phase defines the API only; concrete implementation is not required.
 */
public abstract class LoadSubprocess implements SubprocessInterface {

    /**
     * Executes data loading to all configured targets.
     *
     * Context MUST contain:
     * - currentSubprocess: LOAD
     * - transformedData: Actual transformed data objects (List<TargetDataModel> or similar)
     * - transformedDataCount: Number of records transformed
     * - config: ETConfiguration with targets list
     *
     * Context MUST be updated with:
     * - loadedDataCount: Total records loaded to all targets
     * - currentSubprocess: VALIDATE (if successful)
     *
     * @param context ETLContext with transformed data
     * @return SubprocessResult with success status and loaded count
     * @throws ETLException If loading fails to any target
     */
    @Override
    public abstract SubprocessResult execute(ETLContext context) throws ETLException;

    /**
     * Validates context before loading.
     * Checks:
     * - transformedDataCount > 0 (transform completed)
     * - config.targets is not empty
     * - Each target has valid connection string
     *
     * @param context Context to validate
     * @return true if valid, false otherwise
     */
    @Override
    public boolean validateContext(ETLContext context);

    /**
     * Gets the subprocess type.
     * @return SubprocessType.LOAD
     */
    @Override
    public SubprocessType getType();

    // Abstract methods (to be implemented in future phases)

    /**
     * Loads data to a single target.
     * @param target Target configuration
     * @param count Number of records to load
     * @return Number of records loaded
     * @throws ETLException If loading fails
     */
    protected abstract int loadToTarget(TargetConfig target, int count) throws ETLException;

    /**
     * Validates that all targets can be connected to.
     * @param targets List of target configurations
     * @throws ETLException If any target is unreachable
     */
    protected abstract void validateTargetConnections(List<TargetConfig> targets) throws ETLException;
}
```

### Context Contract

**Input Context**:
- `currentSubprocess`: LOAD
- `transformedDataCount`: >0 (transform completed)
- `config`: ETConfiguration with targets

**Output Context** (if successful):
- `loadedDataCount`: Sum of records loaded to all targets
- `currentSubprocess`: VALIDATE

**Error Conditions**:
- `transformedDataCount` is 0 (transform didn't complete)
- No targets configured
- Target connection failure
- Loading failure to any target

---

## ValidateSubprocess (API Definition)

### Purpose

Validates loaded data against validation rules and writes validation results to context.

### Class Definition

```java
package com.sdd.etl.subprocess;

import com.sdd.etl.context.ETLContext;
import com.sdd.etl.config.ETConfiguration;

/**
 * ValidateSubprocess - Validates loaded data quality.
 * NOTE: This phase defines the API only; concrete implementation is not required.
 */
public abstract class ValidateSubprocess implements SubprocessInterface {

    /**
     * Executes data validation.
     *
     * Context MUST contain:
     * - currentSubprocess: VALIDATE
     * - loadedDataCount: Number of records loaded
     * - config: ETConfiguration with validation rules
     *
     * Context MUST be updated with:
     * - validationPassed: True if all rules pass, false otherwise
     * - validationErrors: List of error messages (if any)
     * - currentSubprocess: CLEAN (if successful)
     *
     * @param context ETLContext with loaded data
     * @return SubprocessResult with success status and validation results
     * @throws ETLException If validation encounters critical error
     */
    @Override
    public abstract SubprocessResult execute(ETLContext context) throws ETLException;

    /**
     * Validates context before validation.
     * Checks:
     * - loadedDataCount > 0 (load completed)
     * - config.validationRules is not empty (if required)
     *
     * @param context Context to validate
     * @return true if valid, false otherwise
     */
    @Override
    public boolean validateContext(ETLContext context);

    /**
     * Gets the subprocess type.
     * @return SubprocessType.VALIDATE
     */
    @Override
    public SubprocessType getType();

    // Abstract methods (to be implemented in future phases)

    /**
     * Validates loaded data against rules.
     * @param loadedCount Number of records to validate
     * @param config Configuration with validation rules
     * @return Pair of (passed, errorMessages)
     * @throws ETLException If validation encounters critical error
     */
    protected abstract Pair<Boolean, List<String>> validate(int loadedCount, ETConfiguration config) throws ETLException;
}
```

### Context Contract

**Input Context**:
- `currentSubprocess`: VALIDATE
- `loadedDataCount`: >0 (load completed)
- `config`: ETConfiguration with validation rules

**Output Context** (if successful):
- `validationPassed`: True if all rules pass
- `validationErrors`: Empty list if all pass, non-empty if any fail
- `currentSubprocess`: CLEAN

**Error Conditions**:
- `loadedDataCount` is 0 (load didn't complete)
- Critical validation error (not rule failure, but system error)

**Note**: Validation rule failures are NOT errors; they set `validationPassed = false` with error messages.

---

## CleanSubprocess (API Definition)

### Purpose

Cleans up temporary resources and marks cleanup as performed in context.

### Class Definition

```java
package com.sdd.etl.subprocess;

import com.sdd.etl.context.ETLContext;

/**
 * CleanSubprocess - Cleans up temporary resources.
 * NOTE: This phase defines the API only; concrete implementation is not required.
 */
public abstract class CleanSubprocess implements SubprocessInterface {

    /**
     * Executes cleanup of temporary resources.
     *
     * Context MUST contain:
     * - currentSubprocess: CLEAN
     * - validationPassed: Validation result
     * - validationErrors: Validation error messages (if any)
     *
     * Context MUST be updated with:
     * - cleanupPerformed: True (always, even if validation failed)
     *
     * @param context ETLContext with validation results
     * @return SubprocessResult with success status
     * @throws ETLException If cleanup encounters critical error
     */
    @Override
    public abstract SubprocessResult execute(ETLContext context) throws ETLException;

    /**
     * Validates context before cleanup.
     * Checks:
     * - validationPassed is set (validate completed or failed)
     *
     * @param context Context to validate
     * @return true if valid, false otherwise
     */
    @Override
    public boolean validateContext(ETLContext context);

    /**
     * Gets the subprocess type.
     * @return SubprocessType.CLEAN
     */
    @Override
    public SubprocessType getType();

    // Abstract methods (to be implemented in future phases)

    /**
     * Cleans up temporary resources.
     * @param context Context with cleanup state
     * @throws ETLException If cleanup encounters critical error
     */
    protected abstract void cleanup(ETLContext context) throws ETLException;
}
```

### Context Contract

**Input Context**:
- `currentSubprocess`: CLEAN
- `validationPassed`: Set (validate completed)
- `validationErrors`: Set (may be empty)

**Output Context** (if successful):
- `cleanupPerformed`: True (always, regardless of validation result)

**Error Conditions**:
- Critical cleanup error (unable to delete files, etc.)

**Note**: CleanSubprocess executes even if validation failed. It's always called as the last subprocess.

---

## Context-Based Data Flow

### Complete Flow Across All Subprocesses

```
[Initial Context]
  currentDate: "20250101"
  currentSubprocess: EXTRACT
  extractedDataCount: 0
  extractedData: null
  transformedDataCount: 0
  transformedData: null
  loadedDataCount: 0
  validationPassed: false
  cleanupPerformed: false

[After Extract]
  extractedData: List<SourceDataModel> (size: 1000)
  extractedDataCount: 1000
  currentSubprocess: TRANSFORM

[After Transform]
  transformedData: List<TargetDataModel> (size: 1000)
  transformedDataCount: 1000
  currentSubprocess: LOAD

[After Load]
  loadedDataCount: 1000
  currentSubprocess: VALIDATE

[After Validate]
  validationPassed: true
  validationErrors: []
  currentSubprocess: CLEAN

[After Clean]
  cleanupPerformed: true
  currentSubprocess: CLEAN
```

---

## Feature Requirements Mapping

| FR # | Requirement | API Coverage |
|------|-------------|--------------|
| FR-008 | Extract supports multiple sources | ✅ ExtractSubprocess.execute() |
| FR-009 | Extract complete only when all sources complete | ✅ ExtractSubprocess success condition |
| FR-010 | Transform supports multiple sources to multiple targets | ✅ TransformSubprocess.execute() |
| FR-011 | Transform complete only when all data transformed | ✅ TransformSubprocess success condition |
| FR-012 | Load supports multiple targets | ✅ LoadSubprocess.execute() |
| FR-013 | Load complete only when all targets loaded | ✅ LoadSubprocess success condition |
| FR-022 | Provide component API for each subprocess | ✅ All 5 subprocess interfaces defined |
| FR-027 | All sub-components use context for data transfer | ✅ Context contracts for all subprocesses |
| FR-028 | Extract writes to context, Transform reads from context | ✅ Extract output, Transform input context |
| FR-029 | Transform writes to context, Load reads from context | ✅ Transform output, Load input context |
| FR-030 | Load writes to context, Validate reads from context | ✅ Load output, Validate input context |
| FR-031 | Validate writes to context, Clean reads from context | ✅ Validate output, Clean input context |

---

## Source

- Feature Specification: FR-008 through FR-013, FR-022, FR-027 through FR-031
- User Stories: US3 (Subprocess Sequential Execution), US4 (Multi-Source Extraction), US5 (Multi-Target Loading)
- Data Model: SubprocessResult, SourceConfig, TargetConfig
- Research: Orchestrator Pattern, Context Pattern
- Scope: docs/v1/Plan.md - "NO Concrete implementation is required for all sub processes, ONLY DEFINE API"
