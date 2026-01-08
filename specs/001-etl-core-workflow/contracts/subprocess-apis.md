# Subprocess APIs Contract

**Feature**: ETL Core Workflow  
**Date**: January 8, 2026  
**Purpose**: Define the Subprocess API interfaces for extract, transform, load, validate, and clean operations

## Overview

This document defines the API interfaces for all subprocesses in the ETL workflow. These are API definitions only - no concrete implementations are required in this phase.

**CRITICAL REQUIREMENT**: All subprocesses (Extract, Transform, Load, Validate, Clean) **MUST** use the DailyProcessContext to transfer data. No subprocess should store data externally or access another subprocess's data directly.

## Subprocess Interfaces

### 1. Extract Process API

### 2. Transform Process API

### 3. Load Process API

### 4. Validate Process API

### 5. Clean Process API

---

## API Contract

### Interface: Subprocess

```java
package com.sdd.etl.api.subprocess;

import com.sdd.etl.context.DailyProcessContext;
import com.sdd.etl.enums.SubprocessType;
import com.sdd.etl.exception.SubprocessException;

/**
 * Base interface for all ETL subprocesses.
 * 
 * All subprocesses must:
 * - Accept a DailyProcessContext as input
 * - Return an updated DailyProcessContext
 * - MUST use context to transfer data between subprocesses
 * - Store extracted data in context for transform subprocess to use
 * - Store transformed data in context for load subprocess to use
 * - Store loaded data counts in context for validation subprocess to use
 * - Execute successfully or throw SubprocessException
 * - Be idempotent (safe to retry if supported)
 * - Have no side effects outside the context (except resource cleanup)
 * 
 * Subprocess execution order:
 * 1. Extract (stores extracted data in context)
 * 2. Transform (reads extracted from context, stores transformed in context)
 * 3. Load (reads transformed from context, updates counts in context)
 * 4. Validate (reads counts from context)
 * 5. Clean (reads context for cleanup information)
 * 
 * CRITICAL: All data transfer between subprocesses MUST occur through the
 * DailyProcessContext. No subprocess should store data externally or access
 * another subprocess's data directly.
 */
public interface Subprocess {
    
    /**
     * Gets the name of this subprocess.
     * 
     * @return The subprocess name (e.g., "Extract", "Transform")
     */
    String getName();
    
    /**
     * Gets the subprocess type.
     * 
     * @return The subprocess type
     */
    SubprocessType getType();
    
    /**
     * Executes the subprocess with the given context.
     * 
     * This method should:
     * - Perform the subprocess logic
     * - Update the context with results
     * - Return the updated context
     * 
     * @param context The current process context
     * @return Updated context after subprocess execution
     * @throws SubprocessException if subprocess execution fails
     */
    DailyProcessContext execute(DailyProcessContext context) 
            throws SubprocessException;
}
```

---

### Interface: ExtractProcess

```java
package com.sdd.etl.api.subprocess;

import com.sdd.etl.api.model.SourceDataModel;
import com.sdd.etl.config.Configuration;
import com.sdd.etl.context.DailyProcessContext;
import com.sdd.etl.exception.SubprocessException;

import java.util.List;

/**
 * Extract subprocess API.
 *
 * This subprocess extracts data from all configured data sources.
 *
 * Responsibilities:
 * - Read data from each configured source
 * - Apply source-specific filtering if configured
 * - Count total records extracted
 * - Update context with records extracted count
 * - **MUST store all extracted data in context for Transform subprocess to use**
 *
 * Execution Rules:
 * - All sources must be extracted successfully
 * - Process fails immediately if any source fails
 * - No retry mechanism (fail-fast on errors)
 * - Records must conform to SourceDataModel API
 * - **All extracted data MUST be stored in DailyProcessContext.extractedData field**
 *
 * Context Updates:
 * - currentSubprocess = EXTRACT
 * - recordsExtracted = total records from all sources
 * - extractedData = list of SourceDataModel from all sources (stored in context for Transform to use)
 *
 * CRITICAL: Extract subprocess MUST store all extracted data in the context.
 * The Transform subprocess will read this data from the context.
 *
 * **IMPORTANT**: DO NOT store extracted data in any external location (files, databases, static variables).
 * ALL data must be passed through the DailyProcessContext.
 */
public interface ExtractProcess extends Subprocess {
    
    /**
     * Extracts data from all configured sources.
     * 
     * Implementation should:
     * 1. Iterate through all sources in configuration
     * 2. For each source:
     *    - Connect to source
     *    - Extract data for the specified date
     *    - Apply filters if configured
     *    - Validate data conforms to SourceDataModel
     *    - Count records extracted
     *    - Disconnect from source
     * 3. Sum records from all sources
     * 4. Update context with total count
     * 
     * @param context The current process context
     * @return Updated context with records extracted count
     * @throws SubprocessException if extraction from any source fails
     */
    @Override
    DailyProcessContext execute(DailyProcessContext context) 
            throws SubprocessException;
    
    /**
     * Extracts data from a single source.
     * 
     * @param sourceName The name of the source
     * @param date The date to extract data for
     * @param configuration The ETL configuration
     * @return List of extracted data models
     * @throws SubprocessException if extraction fails
     */
    List<SourceDataModel> extractFromSource(String sourceName, 
                                             LocalDate date,
                                             Configuration configuration)
            throws SubprocessException;
    
    /**
     * Validates that extracted data conforms to SourceDataModel.
     * 
     * @param data The extracted data
     * @param sourceName The source name
     * @return true if valid, false otherwise
     */
    boolean validateData(List<SourceDataModel> data, String sourceName);
    
    /**
     * Applies filters to extracted data.
     * 
     * @param data The extracted data
     * @param configuration The configuration containing filter rules
     * @return Filtered data
     */
    List<SourceDataModel> applyFilters(List<SourceDataModel> data,
                                      Configuration configuration);
    
    /**
     * Gets the total count of extracted records.
     * 
     * @param allExtractedData Data from all sources
     * @return Total record count
     */
    int getTotalRecordCount(List<List<SourceDataModel>> allExtractedData);
}
```

---

### Interface: TransformProcess

```java
package com.sdd.etl.api.subprocess;

import com.sdd.etl.api.model.SourceDataModel;
import com.sdd.etl.api.model.TargetDataModel;
import com.sdd.etl.config.Configuration;
import com.sdd.etl.context.DailyProcessContext;
import com.sdd.etl.exception.SubprocessException;

import java.util.List;
import java.util.Map;

/**
 * Transform subprocess API.
 *
 * This subprocess transforms source data to target data formats.
 *
 * Responsibilities:
 * - Apply field mappings from configuration
 * - Apply data transformations (type conversion, formatting)
 * - Transform data from multiple sources to multiple targets
 * - Count total records transformed
 * - Update context with records transformed count
 * - **MUST read extracted data from context and store transformed data in context**
 *
 * Execution Rules:
 * - All configured transforms must be applied
 * - Process fails immediately if any transform fails
 * - Each source-target pair has independent transform rules
 * - No retry mechanism (fail-fast on errors)
 * - **All data MUST flow through DailyProcessContext (read extractedData, write transformedData)**
 *
 * Context Updates:
 * - currentSubprocess = TRANSFORM
 * - recordsTransformed = total records transformed
 * - transformedData = list of TargetDataModel (stored in context for Load to use)
 *
 * CRITICAL: Transform subprocess MUST:
 * - Read extracted data from context (stored by Extract)
 * - Store transformed data in context for Load subprocess to use
 *
 * **IMPORTANT**: DO NOT access extracted data from any source other than DailyProcessContext.extractedData.
 * DO NOT store transformed data externally. ALL data must be passed through the DailyProcessContext.
 */
public interface TransformProcess extends Subprocess {
    
    /**
     * Transforms source data to target data formats.
     * 
     * Implementation should:
     * 1. Iterate through all transform configurations
     * 2. For each transform (source -> target):
     *    - Get source data
     *    - Apply field mappings
     *    - Apply data type conversions
     *    - Create target data models
     *    - Validate target data conforms to TargetDataModel
     * 3. Sum transformed records
     * 4. Update context with total count
     * 
     * @param context The current process context
     * @return Updated context with records transformed count
     * @throws SubprocessException if transformation fails
     */
    @Override
    DailyProcessContext execute(DailyProcessContext context) 
            throws SubprocessException;
    
    /**
     * Transforms source data to target data format.
     * 
     * @param sourceData The source data to transform
     * @param fieldMappings Field name mappings (source field -> target field)
     * @param targetType The target type
     * @return Transformed target data
     * @throws SubprocessException if transformation fails
     */
    TargetDataModel transform(SourceDataModel sourceData,
                            Map<String, String> fieldMappings,
                            String targetType)
            throws SubprocessException;
    
    /**
     * Applies field mappings to source data.
     * 
     * @param sourceData The source data
     * @param mappings The field mappings
     * @return Mapped field values
     */
    Map<String, Object> applyFieldMappings(SourceDataModel sourceData,
                                         Map<String, String> mappings);
    
    /**
     * Converts data types according to target requirements.
     * 
     * @param value The value to convert
     * @param targetType The target type
     * @return Converted value
     * @throws SubprocessException if conversion fails
     */
    Object convertType(Object value, String targetType) 
            throws SubprocessException;
    
    /**
     * Validates that transformed data conforms to TargetDataModel.
     * 
     * @param data The transformed data
     * @param targetType The target type
     * @return true if valid, false otherwise
     */
    boolean validateData(List<TargetDataModel> data, String targetType);
}
```

---

### Interface: LoadProcess

```java
package com.sdd.etl.api.subprocess;

import com.sdd.etl.api.model.TargetDataModel;
import com.sdd.etl.config.Configuration;
import com.sdd.etl.context.DailyProcessContext;
import com.sdd.etl.exception.SubprocessException;

import java.util.List;
import java.util.Map;

/**
 * Load subprocess API.
 *
 * This subprocess loads transformed data to all configured targets.
 *
 * Responsibilities:
 * - Load data to each configured target
 * - Handle target-specific data formatting
 * - Count total records loaded
 * - Update context with records loaded count
 * - **MUST read transformed data from context**
 *
 * Execution Rules:
 * - All targets must be loaded successfully
 * - Process fails immediately if any target fails
 * - Each target has independent load logic
 * - No retry mechanism (fail-fast on errors)
 * - **All data MUST be read from DailyProcessContext.transformedData**
 *
 * Context Updates:
 * - currentSubprocess = LOAD
 * - recordsLoaded = total records loaded
 *
 * CRITICAL: Load subprocess MUST:
 * - Read transformed data from context (stored by Transform)
 * - Update recordsLoaded count in context for Validate subprocess to use
 *
 * **IMPORTANT**: DO NOT access transformed data from any source other than DailyProcessContext.transformedData.
 */
public interface LoadProcess extends Subprocess {
    
    /**
     * Loads transformed data to all configured targets.
     * 
     * Implementation should:
     * 1. Iterate through all targets in configuration
     * 2. For each target:
     *    - Get transformed data for this target
     *    - Connect to target
     *    - Format data for target
     *    - Load data to target
     *    - Count records loaded
     *    - Disconnect from target
     * 3. Sum records from all targets
     * 4. Update context with total count
     * 
     * @param context The current process context
     * @return Updated context with records loaded count
     * @throws SubprocessException if loading to any target fails
     */
    @Override
    DailyProcessContext execute(DailyProcessContext context) 
            throws SubprocessException;
    
    /**
     * Loads data to a single target.
     * 
     * @param targetName The name of the target
     * @param data The data to load
     * @param configuration The ETL configuration
     * @return Count of records loaded
     * @throws SubprocessException if loading fails
     */
    int loadToTarget(String targetName,
                    List<TargetDataModel> data,
                    Configuration configuration)
            throws SubprocessException;
    
    /**
     * Formats data for the target.
     * 
     * @param data The data to format
     * @param targetType The target type
     * @return Formatted data ready for loading
     * @throws SubprocessException if formatting fails
     */
    Object formatForTarget(List<TargetDataModel> data, String targetType)
            throws SubprocessException;
    
    /**
     * Gets the total count of loaded records.
     * 
     * @param allLoadedCounts Record counts from all targets
     * @return Total record count
     */
    int getTotalRecordCount(List<Integer> allLoadedCounts);
}
```

---

### Interface: ValidateProcess

```java
package com.sdd.etl.api.subprocess;

import com.sdd.etl.api.model.TargetDataModel;
import com.sdd.etl.config.Configuration;
import com.sdd.etl.config.ValidationConfig;
import com.sdd.etl.context.DailyProcessContext;
import com.sdd.etl.exception.SubprocessException;

import java.util.List;
import java.util.Map;

/**
 * Validate subprocess API.
 *
 * This subprocess validates loaded data against configured rules.
 *
 * Responsibilities:
 * - Check data completeness (required fields)
 * - Check data quality (format, range, pattern)
 * - Check data consistency (referential integrity)
 * - Generate validation report
 * - Fail process if validation rules are violated
 * - **MUST read validation data from context**
 *
 * Execution Rules:
 * - All validation checks must pass
 * - Process fails immediately if any check fails
 * - Validation rules are configurable
 * - No retry mechanism (fail-fast on errors)
 * - **All validation information MUST be read from DailyProcessContext**
 *
 * Context Updates:
 * - currentSubprocess = VALIDATE
 * - No count updates (validation does not change data)
 *
 * CRITICAL: Validate subprocess MUST:
 * - Read recordsLoaded count from context
 * - Validate data integrity based on context information
 *
 * **IMPORTANT**: DO NOT access any external data sources for validation. All required information
 * MUST be available in the DailyProcessContext.
 */
public interface ValidateProcess extends Subprocess {
    
    /**
     * Validates loaded data against configured rules.
     * 
     * Implementation should:
     * 1. Get validation configuration
     * 2. If completeness check enabled:
     *    - Check all required fields are present
     *    - Fail if any required field is missing
     * 3. If quality check enabled:
     *    - Check field formats against patterns
     *    - Check field value ranges
     *    - Fail if any quality check fails
     * 4. If consistency check enabled:
     *    - Check referential integrity
     *    - Check data relationships
     *    - Fail if any consistency check fails
     * 5. Update context subprocess status
     * 
     * @param context The current process context
     * @return Updated context with validation status
     * @throws SubprocessException if validation fails
     */
    @Override
    DailyProcessContext execute(DailyProcessContext context) 
            throws SubprocessException;
    
    /**
     * Validates data completeness.
     * 
     * Checks that all required fields are present and non-null.
     * 
     * @param data The data to validate
     * @param config The validation configuration
     * @return true if complete, false otherwise
     */
    boolean validateCompleteness(List<TargetDataModel> data,
                                ValidationConfig config);
    
    /**
     * Validates data quality.
     * 
     * Checks field formats, patterns, and value ranges.
     * 
     * @param data The data to validate
     * @param config The validation configuration
     * @return true if quality passes, false otherwise
     */
    boolean validateQuality(List<TargetDataModel> data,
                          ValidationConfig config);
    
    /**
     * Validates data consistency.
     * 
     * Checks referential integrity and data relationships.
     * 
     * @param data The data to validate
     * @param config The validation configuration
     * @return true if consistent, false otherwise
     */
    boolean validateConsistency(List<TargetDataModel> data,
                                ValidationConfig config);
    
    /**
     * Validates a single field against a regex pattern.
     * 
     * @param value The field value
     * @param pattern The regex pattern
     * @return true if matches, false otherwise
     */
    boolean validatePattern(Object value, String pattern);
    
    /**
     * Validates a single field against a value range.
     * 
     * @param value The field value
     * @param minValue Minimum value (inclusive)
     * @param maxValue Maximum value (inclusive)
     * @return true if in range, false otherwise
     */
    boolean validateRange(Object value, Object minValue, Object maxValue);
    
    /**
     * Generates a validation report.
     * 
     * @param data The validated data
     * @param config The validation configuration
     * @return Validation report as string
     */
    String generateValidationReport(List<TargetDataModel> data,
                                  ValidationConfig config);
}
```

---

### Interface: CleanProcess

```java
package com.sdd.etl.api.subprocess;

import com.sdd.etl.context.DailyProcessContext;
import com.sdd.etl.exception.SubprocessException;

/**
 * Clean subprocess API.
 * 
 * This subprocess cleans up temporary resources.
 * 
 * Responsibilities:
 * - Delete temporary files
 * - Release database connections
 * - Clear in-memory caches
 * - Close file handles
 * - Log cleanup actions
 * 
 * Execution Rules:
 * - Must not affect production data
 * - Must be safe to execute even if previous subprocesses failed
 * - Should log all cleanup actions
 * - No retry mechanism (fail-fast on errors)
 * 
 * Context Updates:
 * - currentSubprocess = CLEAN
 * - No count updates (cleanup does not change data)
 */
public interface CleanProcess extends Subprocess {
    
    /**
     * Cleans up temporary resources.
     * 
     * Implementation should:
     * 1. Delete temporary files created during extract/transform/load
     * 2. Close any open file handles
     * 3. Release database connections
     * 4. Clear in-memory caches
     * 5. Log all cleanup actions
     * 6. Update context subprocess status
     * 
     * @param context The current process context
     * @return Updated context with cleanup status
     * @throws SubprocessException if cleanup fails (should be rare)
     */
    @Override
    DailyProcessContext execute(DailyProcessContext context) 
            throws SubprocessException;
    
    /**
     * Deletes temporary files.
     * 
     * @param context The process context containing temp file paths
     * @return Count of files deleted
     * @throws SubprocessException if deletion fails
     */
    int deleteTemporaryFiles(DailyProcessContext context) 
            throws SubprocessException;
    
    /**
     * Releases database connections.
     * 
     * @param context The process context
     * @return Count of connections released
     */
    int releaseConnections(DailyProcessContext context);
    
    /**
     * Clears in-memory caches.
     * 
     * @param context The process context
     * @return Count of caches cleared
     */
    int clearCaches(DailyProcessContext context);
    
    /**
     * Closes file handles.
     * 
     * @param context The process context
     * @return Count of file handles closed
     */
    int closeFileHandles(DailyProcessContext context);
}
```

---

## Subprocess Execution Order

```
1. Extract Process
   ↓
2. Transform Process
   ↓
3. Load Process
   ↓
4. Validate Process
   ↓
5. Clean Process
```

## Context Updates Summary

| Subprocess | Context Updates | Data Flow (via Context) |
|------------|-----------------|------------------------|
| Extract | currentSubprocess = EXTRACT, recordsExtracted, extractedData | **WRITES** extractedData → DailyProcessContext |
| Transform | currentSubprocess = TRANSFORM, recordsTransformed, transformedData | **READS** extractedData from Context, **WRITES** transformedData to Context |
| Load | currentSubprocess = LOAD, recordsLoaded | **READS** transformedData from Context, **UPDATES** recordsLoaded in Context |
| Validate | currentSubprocess = VALIDATE | **READS** recordsLoaded from Context |
| Clean | currentSubprocess = CLEAN | **READS** tempFiles from Context |

## Data Flow

```
Sources → Extract → DailyProcessContext.extractedData (SourceDataModel[])
                      ↓
                    (READS from Context)
                      ↓
              Transform → DailyProcessContext.transformedData (TargetDataModel[])
                      ↓
                    (READS from Context)
                      ↓
                   Load → DailyProcessContext.recordsLoaded
                      ↓
                    (READS from Context)
                      ↓
                Validate → Validation Report
                      ↓
                    (READS from Context)
                      ↓
                   Clean → Cleanup (reads tempFiles from Context)
```

**IMPORTANT**: All data MUST flow through DailyProcessContext. No direct subprocess-to-subprocess data access is allowed.

## Testing Requirements

### API Contract Tests

- **ExtractProcessTest**: Verify API contract compliance
- **TransformProcessTest**: Verify API contract compliance
- **LoadProcessTest**: Verify API contract compliance
- **ValidateProcessTest**: Verify API contract compliance
- **CleanProcessTest**: Verify API contract compliance

### Mock Implementation Tests

Create mock implementations to test:
- Subprocess execution with context updates
- Error handling and exception propagation
- Context validation rules

### Edge Cases to Test

- Empty data from extract
- Failed transformation
- Failed load to target
- Validation failures (completeness, quality, consistency)
- Cleanup with no temporary resources
- Concurrent subprocess execution (should not happen, but test isolation)

## Implementation Notes

- These are API definitions only - no concrete implementations required
- Concrete implementations will be provided in future phases
- Mock implementations can be used for testing
- All subprocesses must be thread-safe if context is shared
- Subprocesses should not modify the input context directly
- Return a new context with updates instead
- **CRITICAL: ALL data transfer MUST occur through DailyProcessContext**
  - Extract: WRITE to extractedData field
  - Transform: READ from extractedData, WRITE to transformedData
  - Load: READ from transformedData, UPDATE recordsLoaded
  - Validate: READ recordsLoaded
  - Clean: READ tempFiles
- **DO NOT** use static variables, files, databases, or any other external storage for data transfer
- **DO NOT** allow subprocesses to access each other's data directly
- **ALL** inter-subprocess communication MUST go through the DailyProcessContext object
