# Data Model: ETL Core Workflow

**Feature Branch**: `001-etl-core-workflow`
**Date**: 2026-01-08
**Status**: Phase 1 Design

## Overview

This document defines the data entities and their relationships for the ETL Core Workflow feature. The data model supports day-by-day ETL processing with context-based data transfer between subprocesses.

## Entities

### 1. CommandLineArguments

**Description**: Parsed command-line input from user.

**Fields**:
- `fromDate` (String, required) - Start date in YYYYMMDD format (inclusive)
- `toDate` (String, required) - End date in YYYYMMDD format (inclusive)
- `configPath` (String, required) - Absolute path to INI configuration file
- `helpRequested` (boolean) - True if user requested help (--help flag)

**Validation Rules**:
- `fromDate` must be valid date format (YYYYMMDD)
- `toDate` must be valid date format (YYYYMMDD)
- `fromDate` must be ≤ `toDate` (edge case: invalid date range)
- `configPath` must exist and be readable (edge case: missing config file)
- Either all three required parameters are provided OR help is requested

**Source**: FR-001, FR-002

---

### 2. ETConfiguration

**Description**: Configuration loaded from INI file containing all ETL process settings.

**Fields**:
- `sources` (List<SourceConfig>) - Data source configurations
- `targets` (List<TargetConfig>) - Data target configurations
- `transformations` (List<TransformationConfig>) - Transformation rules
- `validationRules` (List<ValidationConfig>) - Validation criteria
- `logging` (LoggingConfig) - Logging settings

**Sub-Entities**:

#### SourceConfig
- `name` (String, required) - Source identifier
- `type` (String, required) - Source type (e.g., "database", "api", "file")
- `connectionString` (String, required) - Connection details including credentials
- `primaryKeyField` (String, required) - Primary key field name for unique identification
- `extractQuery` (String, optional) - Query or fetch pattern for extraction
- `dateField` (String, optional) - Field name used for date filtering

#### TargetConfig
- `name` (String, required) - Target identifier
- `type` (String, required) - Target type (e.g., "database", "api", "file")
- `connectionString` (String, required) - Connection details including credentials
- `batchSize` (int, optional) - Batch size for loading (default: 1000)

#### TransformationConfig
- `name` (String, required) - Transformation rule identifier
- `sourceField` (String, required) - Source field name
- `targetField` (String, required) - Target field name
- `transformType` (String, required) - Transformation type (e.g., "map", "aggregate", "filter")

#### ValidationConfig
- `name` (String, required) - Validation rule identifier
- `field` (String, required) - Field to validate
- `ruleType` (String, required) - Validation rule type (e.g., "not_null", "range", "pattern")
- `ruleValue` (String, optional) - Expected value or range (depends on ruleType)

#### LoggingConfig
- `logFilePath` (String, optional) - Path to log file (default: "./etl.log")
- `logLevel` (String, optional) - Log level (default: "INFO")

**Validation Rules**:
- At least one source and one target must be configured
- All connection strings must be non-empty
- Primary key fields must be specified for all sources

**Source**: FR-008, FR-012, FR-026, Key Entities section

---

### 3. ETLContext

**Description**: Runtime context containing state for a single day's ETL process. All data transfer between subprocesses occurs through this context.

**Fields**:
- `currentDate` (String, required) - Processing date in YYYYMMDD format
- `currentSubprocess` (SubprocessType, required) - Currently executing subprocess
- `config` (ETConfiguration, required) - Reference to configuration for this day
- `extractedDataCount` (int) - Count of records extracted from all sources
- `extractedData` (Object) - Actual extracted data objects (List<SourceDataModel> or Map<String, Object>)
- `transformedDataCount` (int) - Count of records transformed
- `transformedData` (Object) - Actual transformed data objects (List<TargetDataModel> or Map<String, Object>)
- `loadedDataCount` (int) - Count of records loaded to all targets
- `validationPassed` (boolean) - Validation result (true if all rules pass)
- `validationErrors` (List<String>) - Validation error messages (if any)
- `cleanupPerformed` (boolean) - Whether cleanup subprocess has executed

**Enum**: SubprocessType
- `EXTRACT`
- `TRANSFORM`
- `LOAD`
- `VALIDATE`
- `CLEAN`

**Validation Rules**:
- `currentDate` must be valid date format (YYYYMMDD)
- `config` must not be null
- `extractedDataCount`, `transformedDataCount`, `loadedDataCount` must be ≥ 0
- `extractedData` must be null before Extract, non-null after Extract (if count > 0)
- `transformedData` must be null before Transform, non-null after Transform (if count > 0)
- `validationPassed` is false if `validationErrors` is non-empty

**Data Flow** (per FR-028 through FR-031):
1. **Before Extract**: All counts = 0, `extractedData` = null, `transformedData` = null, `validationPassed` = false, `cleanupPerformed` = false
2. **After Extract**: `extractedData` and `extractedDataCount` set, `currentSubprocess` = TRANSFORM
3. **After Transform**: `transformedData` and `transformedDataCount` set, `currentSubprocess` = LOAD
4. **After Load**: `loadedDataCount` set, `currentSubprocess` = VALIDATE
5. **After Validate**: `validationPassed` and `validationErrors` set, `currentSubprocess` = CLEAN
6. **After Clean**: `cleanupPerformed` = true, process complete

**Source**: FR-014 through FR-031, Key Entities section

---

### 4. SubprocessResult

**Description**: Result returned by each subprocess execution.

**Fields**:
- `success` (boolean, required) - True if subprocess completed successfully
- `dataCount` (int) - Number of records processed (depends on subprocess type)
- `errorMessage` (String) - Error message if subprocess failed
- `timestamp` (long) - Execution timestamp (milliseconds since epoch)

**Validation Rules**:
- `dataCount` must be ≥ 0 if `success` is true
- `errorMessage` must be non-empty if `success` is false
- `errorMessage` must be null if `success` is true

**Source**: FR-005, FR-006, FR-007

---

### 5. DailyProcessResult

**Description**: Result of processing a single day's ETL workflow.

**Fields**:
- `date` (String, required) - Processing date in YYYYMMDD format
- `success` (boolean, required) - True if all subprocesses completed successfully
- `extractResult` (SubprocessResult) - Extract subprocess result
- `transformResult` (SubprocessResult) - Transform subprocess result
- `loadResult` (SubprocessResult) - Load subprocess result
- `validateResult` (SubprocessResult) - Validate subprocess result
- `cleanResult` (SubprocessResult) - Clean subprocess result
- `context` (ETLContext) - Final context state (for debugging)

**Validation Rules**:
- `success` is true only if all subprocess results have `success` = true
- All subprocess results must be non-null
- `context` must reflect final state (after all subprocesses)

**Source**: FR-004, FR-007, Key Entities section

---

### 6. WorkflowResult

**Description**: Result of processing the entire multi-day ETL workflow.

**Fields**:
- `success` (boolean, required) - True if all days completed successfully
- `processedDays` (int, required) - Number of days processed
- `successfulDays` (int) - Number of successful days
- `failedDays` (int) - Number of failed days
- `dailyResults` (Map<String, DailyProcessResult>) - Result per date (key: YYYYMMDD)
- `startDate` (String) - First processed date
- `endDate` (String) - Last processed date

**Validation Rules**:
- `processedDays` = `successfulDays` + `failedDays`
- `success` is true only if `failedDays` = 0
- `dailyResults` keys are in ascending date order

**Source**: FR-004, Success Criteria section

---

### 7. StatusLogEntry

**Description**: Single log entry for status updates to file and console.

**Fields**:
- `timestamp` (String, required) - ISO 8601 timestamp
- `date` (String) - Processing date (YYYYMMDD)
- `subprocess` (SubprocessType) - Subprocess being logged
- `level` (LogLevel) - Log level (INFO, WARN, ERROR)
- `message` (String, required) - Log message
- `dataCount` (int) - Optional data count for subprocess completion

**Enum**: LogLevel
- `INFO`
- `WARN`
- `ERROR`

**Validation Rules**:
- `timestamp` must be valid ISO 8601 format
- `message` must be non-empty
- `dataCount` is only valid when `level` is INFO

**Source**: FR-016, FR-017, User Story 6

---

### 8. SourceDataModel (API Definition Only)

**Description**: Abstract base class representing data structure from various sources.

**Fields** (protected):
- `metadata` (Map<String, Object>) - Metadata about data fields and types
- `records` (List<Map<String, Object>>) - Data records with field-value pairs

**Abstract Methods**:
- `validate()` (boolean) - Validate data integrity and completeness
- `getPrimaryKey()` (Object) - Get primary key value for this record

**Subclasses** (to be implemented in future phases):
- `DatabaseSourceData` - Database-specific data model
- `APISourceData` - API-specific data model
- `FileSourceData` - File-specific data model

**Source**: FR-018, FR-020, Key Entities section

---

### 9. TargetDataModel (API Definition Only)

**Description**: Abstract base class representing data structure for target systems.

**Fields** (protected):
- `metadata` (Map<String, Object>) - Metadata about data fields and types
- `records` (List<Map<String, Object>>) - Data records formatted for target

**Abstract Methods**:
- `validate()` (boolean) - Validate data integrity for target system
- `toTargetFormat()` (Object) - Convert to target-specific format

**Subclasses** (to be implemented in future phases):
- `DatabaseTargetData` - Database-specific data model
- `APITargetData` - API-specific data model
- `FileTargetData` - File-specific data model

**Source**: FR-019, FR-021, Key Entities section

---

## Entity Relationships

```
CommandLineArguments
    │
    ├─> ETConfiguration (loaded from configPath)
    │
    └─> WorkflowResult (generated after processing)
            │
            └─> DailyProcessResult (per date)
                    │
                    ├─> ETLContext (shared state)
                    │
                    ├─> SubprocessResult (per subprocess: extract, transform, load, validate, clean)
                    │
                    └─> StatusLogEntry (per subprocess completion)
```

**Data Flow**:
1. User provides `CommandLineArguments`
2. System loads `ETConfiguration` from INI file
3. For each date in range:
   - Create `ETLContext` for the date
   - Execute subprocesses sequentially
   - Each subprocess returns `SubprocessResult` and updates `ETLContext`
   - Log `StatusLogEntry` for each subprocess
   - Aggregate into `DailyProcessResult`
4. Aggregate all `DailyProcessResult` into `WorkflowResult`

---

## State Transitions

### ETLContext State Machine

```
[Initial]
  │
  ├─> Extract Subprocess
  │     ├─> Success → [Extracted] (extractedData and extractedDataCount set)
  │     └─> Fail → [Error State] (stop day's process)
  │
  ├─> Transform Subprocess (only if Extract success)
  │     ├─> Success → [Transformed] (transformedData and transformedDataCount set)
  │     └─> Fail → [Error State] (stop day's process)
  │
  ├─> Load Subprocess (only if Transform success)
  │     ├─> Success → [Loaded] (loadedDataCount set)
  │     └─> Fail → [Error State] (stop day's process)
  │
  ├─> Validate Subprocess (only if Load success)
  │     ├─> Success → [Validated] (validationPassed = true)
  │     └─> Fail → [Error State] (stop day's process)
  │
  ├─> Clean Subprocess (only if Validate success)
  │     ├─> Success → [Complete] (cleanupPerformed = true)
  │     └─> Fail → [Error State] (stop day's process)
  │
  └─> [Error State] (any subprocess fails)
        └─> Stop day's process, do not proceed to next day
```

**Rules** (from FR-006, FR-007):
- Each subprocess must complete successfully before next subprocess starts
- If any subprocess fails, stop the day's process immediately
- Day's process is complete only when all 5 subprocesses complete successfully

---

## Edge Cases Handling

### 1. Empty Data Scenario

- **When**: A subprocess completes but produces no data (dataCount = 0)
- **Handling**: Consider as success unless validation rules require non-zero data
- **Logging**: Log warning message "No data extracted/transformed/loaded"
- **Source**: Edge Cases in spec

### 2. Context Integrity

- **When**: Context data is corrupted or contains invalid values
- **Handling**: Validate context before each subprocess execution; throw exception if invalid
- **Recovery**: Process stops, user must investigate and restart manually
- **Source**: Edge Cases in spec, FR-025

### 3. Concurrent Execution Detection

- **When**: Another ETL process is already running
- **Handling**: Detect file lock, display error message "Another ETL process is running. Please wait for it to complete.", exit with error code
- **Source**: Edge Cases in spec, FR-024

---

## Data Model Completeness

All entities defined in the Key Entities section of the feature specification have been modeled:
- ✅ Configuration
- ✅ Source Data (API only in this phase)
- ✅ Target Data (API only in this phase)
- ✅ Context
- ✅ Subprocess Component (via SubprocessResult)
- ✅ Day Process (via DailyProcessResult)

Additional entities added to support:
- CLI argument parsing and validation
- Multi-day workflow orchestration
- Status logging
- Error handling and recovery

---

## Implementation Scope (This Phase)

**Concrete Implementations** (Required):
- CommandLineArguments
- ETConfiguration
- ETLContext
- SubprocessResult
- DailyProcessResult
- WorkflowResult
- StatusLogEntry

**API Definitions Only** (No implementation):
- SourceDataModel (abstract class with methods)
- TargetDataModel (abstract class with methods)

**Source**: docs/v1/Plan.md scope definition
