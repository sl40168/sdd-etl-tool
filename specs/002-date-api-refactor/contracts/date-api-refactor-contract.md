# Date API Refactor Contract

**Feature Branch**: `002-date-api-refactor`  
**Date**: 2026-01-10  
**Status**: Phase 1 Design

## Overview

This contract defines the changes to date-related APIs in the ETL tool. Three core classes are being refactored to use `LocalDate` instead of `String` for date representations. All dependent code must be updated accordingly.

## Affected Classes

### 1. ETLContext

**Package**: `com.sdd.etl.context`

**Changes**:
- `getCurrentDate()` return type changes from `String` to `LocalDate`
- `setCurrentDate(String date)` parameter type changes to `LocalDate`

**Updated Signatures**:
```java
public class ETLContext {
    // ...
    public LocalDate getCurrentDate();
    public void setCurrentDate(LocalDate date);
    // ...
}
```

**Behavior**:
- Returns `LocalDate` object representing the current processing date
- `setCurrentDate` expects a `LocalDate` parameter, not a string
- Internal storage still uses `String` keys but stores `LocalDate` objects

### 2. WorkflowResult

**Package**: `com.sdd.etl.model`

**Changes**:
- `startDate` field type changes from `String` to `LocalDate`
- `endDate` field type changes from `String` to `LocalDate`
- Getter/setter signatures updated accordingly

**Updated Signatures**:
```java
public class WorkflowResult {
    // ...
    private LocalDate startDate;
    private LocalDate endDate;
    // ...
    public LocalDate getStartDate();
    public void setStartDate(LocalDate date);
    public LocalDate getEndDate();
    public void setEndDate(LocalDate date);
    // ...
}
```

**Behavior**:
- Date fields store `LocalDate` objects for type-safe operations
- Serialization to string format may be needed for external interfaces

### 3. DateRangeGenerator

**Package**: `com.sdd.etl.util`

**Changes**:
- `generate(String fromDate, String toDate)` return type changes from `List<String>` to `List<LocalDate>`

**Updated Signature**:
```java
public class DateRangeGenerator {
    // ...
    public static List<LocalDate> generate(String fromDate, String toDate);
    // ...
}
```

**Behavior**:
- Returns a list of `LocalDate` objects for the specified inclusive range
- Input parameters remain `String` for backward compatibility with CLI
- Parsing errors throw `IllegalArgumentException`

## New Entity: ConfigurationFile

### Overview
Default INI configuration file demonstrating all existing components.

### Location
`src/main/resources/config.ini`

### Required Sections
```ini
[database]
url = jdbc:postgresql://localhost:5432/etl_db
username = etl_user
password = etl_password
driver = org.postgresql.Driver

[logging]
level = INFO
file = /var/log/etl-tool/etl.log
pattern = %d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n

[etl]
batch_size = 1000
timeout_seconds = 300
retry_attempts = 3

[subprocess]
max_concurrent = 5
timeout_per_process = 60
```

### Validation Rules
1. All required sections must be present
2. Required properties within sections must be non-empty
3. Property values must match expected type (string, integer)

## Migration Strategy

### Step 1: Update Method Signatures
- Change return types from `String` to `LocalDate`
- Change parameter types from `String` to `LocalDate` where appropriate

### Step 2: Update Internal Implementations
- Convert string parsing to `LocalDate` operations
- Update internal logic to work with `LocalDate` objects

### Step 3: Update Dependent Code
- Find all callers of affected methods using IDE refactoring tools
- Update callers to handle `LocalDate` return types
- Add conversion helpers where needed (e.g., `formatDate(LocalDate)`)

### Step 4: Validate Backward Compatibility
- Ensure all existing tests pass with new types
- Add new tests for `LocalDate` behavior

## Integration Points

### Date Formatting Helper
```java
public class DateUtils {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    public static LocalDate parseDate(String dateString) {
        return LocalDate.parse(dateString, DATE_FORMATTER);
    }
    
    public static String formatDate(LocalDate date) {
        return date.format(DATE_FORMATTER);
    }
}
```

### Configuration Loading
- `ConfigurationLoader` loads INI file from classpath or filesystem
- `ETConfiguration` POJO provides typed access to configuration properties

## Feature Requirements Mapping

| FR # | Requirement | Contract Coverage |
|------|-------------|-------------------|
| FR-001 | ETLContext.getCurrentDate() returns LocalDate | ✅ Updated signature |
| FR-002 | WorkflowResult startDate/endDate are LocalDate | ✅ Updated fields |
| FR-003 | DateRangeGenerator.generate() returns List<LocalDate> | ✅ Updated signature |
| FR-004 | All dependent code updated | ✅ Migration strategy |
| FR-005 | Default INI configuration file created | ✅ ConfigurationFile entity |
| FR-006 | Config file loadable from standard location | ✅ Location and validation |

## Notes

- This refactoring improves type safety and reduces parsing overhead
- Existing tests serve as regression safety net
- No change to external interfaces (CLI arguments remain "YYYYMMDD" strings)
- All date operations should use `java.time` APIs for consistency