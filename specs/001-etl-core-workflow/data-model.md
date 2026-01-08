# Data Model: ETL Core Workflow

**Feature**: ETL Core Workflow  
**Date**: January 8, 2026  
**Purpose**: Define data models, entities, and their relationships for ETL core workflow

## Overview

This document defines the data models for the ETL core workflow, including:
- Configuration entity
- DailyProcessContext entity (concrete implementation)
- Source Data Model API (abstract)
- Target Data Model API (abstract)
- Supporting enums and value objects

## Entity Definitions

### 1. Configuration

**Purpose**: Stores all configuration settings for the ETL process, including data sources, targets, and transformation rules.

**Package**: `com.sdd.etl.config`

**Fields**:
| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| sources | List<SourceConfig> | List of data source configurations | At least one source required |
| targets | List<TargetConfig> | List of data target configurations | At least one target required |
| transforms | List<TransformConfig> | List of transformation rules | Optional, default transformations if empty |
| validation | ValidationConfig | Validation criteria and rules | Required |
| logging | LoggingConfig | Logging configuration | Required, with defaults |

**State Transitions**: Immutable (loaded once at startup)

**Validation Rules**:
- `sources` cannot be empty
- `targets` cannot be empty
- Each `source` must have unique name
- Each `target` must have unique name
- Source and target names must be unique across both lists

---

### 2. SourceConfig

**Purpose**: Configuration for a single data source.

**Package**: `com.sdd.etl.config`

**Fields**:
| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| name | String | Unique identifier for this source | Required, alphanumeric + underscore |
| type | String | Source type (e.g., "database", "file", "api") | Required |
| connectionString | String | Connection string or URI | Required |
| credentials | Credentials | Authentication credentials | Optional (anonymous sources) |
| primaryKeyField | String | Field name used for unique record identification | Required |
| additionalProperties | Map<String, String> | Additional source-specific properties | Optional |

**State Transitions**: Immutable (loaded from INI file)

**Validation Rules**:
- `name` must be unique across all sources
- `name` must match regex: `^[a-zA-Z0-9_]+$`
- `type` must be a recognized source type
- `connectionString` must be non-empty
- If `credentials` provided, must be valid for source type

---

### 3. TargetConfig

**Purpose**: Configuration for a single data target.

**Package**: `com.sdd.etl.config`

**Fields**:
| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| name | String | Unique identifier for this target | Required, alphanumeric + underscore |
| type | String | Target type (e.g., "database", "file", "api") | Required |
| connectionString | String | Connection string or URI | Required |
| credentials | Credentials | Authentication credentials | Optional (anonymous targets) |
| additionalProperties | Map<String, String> | Additional target-specific properties | Optional |

**State Transitions**: Immutable (loaded from INI file)

**Validation Rules**:
- `name` must be unique across all targets
- `name` must match regex: `^[a-zA-Z0-9_]+$`
- `type` must be a recognized target type
- `connectionString` must be non-empty
- If `credentials` provided, must be valid for target type

---

### 4. Credentials

**Purpose**: Stores authentication credentials for data sources or targets.

**Package**: `com.sdd.etl.config`

**Fields**:
| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| username | String | Username for authentication | Optional |
| password | String | Password for authentication | Optional (may use token) |
| token | String | Authentication token (alternative to username/password) | Optional |
| additionalAuthFields | Map<String, String> | Additional authentication parameters | Optional |

**State Transitions**: Immutable

**Validation Rules**:
- Either (`username` + `password`) OR `token` must be provided
- Cannot have both `password` and `token` simultaneously

---

### 5. TransformConfig

**Purpose**: Configuration for data transformation rules.

**Package**: `com.sdd.etl.config`

**Fields**:
| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| sourceName | String | Name of the source this transform applies to | Required, must match existing source |
| targetName | String | Name of the target this transform applies to | Required, must match existing target |
| fieldMappings | Map<String, String> | Source field to target field mappings | Required |
| filters | List<FilterConfig> | Data filtering rules | Optional |

**State Transitions**: Immutable

**Validation Rules**:
- `sourceName` must exist in `Configuration.sources`
- `targetName` must exist in `Configuration.targets`
- `fieldMappings` cannot be empty

---

### 6. FilterConfig

**Purpose**: Configuration for data filtering rules.

**Package**: `com.sdd.etl.config`

**Fields**:
| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| fieldName | String | Field name to filter on | Required |
| operator | String | Comparison operator (eq, ne, gt, lt, ge, le, contains) | Required |
| value | String | Value to compare against | Required |

**State Transitions**: Immutable

**Validation Rules**:
- `operator` must be one of: "eq", "ne", "gt", "lt", "ge", "le", "contains"

---

### 7. ValidationConfig

**Purpose**: Configuration for data validation rules.

**Package**: `com.sdd.etl.config`

**Fields**:
| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| checkCompleteness | boolean | Whether to check for missing required fields | Required, default true |
| checkQuality | boolean | Whether to perform data quality checks | Required, default true |
| checkConsistency | boolean | Whether to check for data consistency | Required, default true |
| completenessRules | List<String> | Field names that must be non-null | Optional |
| qualityRules | Map<String, String> | Field name to validation pattern regex | Optional |

**State Transitions**: Immutable

**Validation Rules**:
- If `checkCompleteness` is true, `completenessRules` cannot be empty (all fields required)
- If `checkQuality` is true, `qualityRules` cannot be empty

---

### 8. LoggingConfig

**Purpose**: Configuration for logging settings.

**Package**: `com.sdd.etl.config`

**Fields**:
| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| logDirectory | String | Directory for log files | Required, must be valid path |
| consoleLogging | boolean | Whether to log to console | Required, default true |
| fileLogging | boolean | Whether to log to file | Required, default true |
| logLevel | String | Logging level (DEBUG, INFO, WARN, ERROR) | Required, default INFO |

**State Transitions**: Immutable

**Validation Rules**:
- `logDirectory` must be a valid directory path
- `logLevel` must be one of: "DEBUG", "INFO", "WARN", "ERROR"

---

### 9. DailyProcessContext

**Purpose**: Concrete implementation of context that holds runtime state for each day's ETL process.

**Package**: `com.sdd.etl.context`

**Fields**:
| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| date | LocalDate | The date being processed | Required |
| currentSubprocess | SubprocessType | Current subprocess being executed | Required, initial value is NOT_STARTED |
| recordsExtracted | int | Count of records extracted from sources | Required, initial value 0 |
| recordsTransformed | int | Count of records transformed | Required, initial value 0 |
| recordsLoaded | int | Count of records loaded to targets | Required, initial value 0 |
| configuration | Configuration | The configuration for the ETL process | Required |
| startTime | LocalDateTime | Process start time | Required |
| endTime | LocalDateTime | Process end time (null if not completed) | Optional |
| extractedData | List<SourceDataModel> | Data extracted by Extract subprocess (stored for Transform) | Optional, populated by Extract |
| transformedData | List<TargetDataModel> | Data transformed by Transform subprocess (stored for Load) | Optional, populated by Transform |
| tempFiles | List<Path> | Temporary files created during execution (for cleanup) | Optional, populated by Extract/Transform/Load |

**State Transitions**:
- **Created**: Context created with date, config, startTime; currentSubprocess = NOT_STARTED
- **Extract**: currentSubprocess = EXTRACT; recordsExtracted updated; extractedData populated with SourceDataModel list
- **Transform**: currentSubprocess = TRANSFORM; recordsTransformed updated; transformedData populated with TargetDataModel list (read from extractedData)
- **Load**: currentSubprocess = LOAD; recordsLoaded updated (uses transformedData)
- **Validate**: currentSubprocess = VALIDATE (reads recordsLoaded from context)
- **Clean**: currentSubprocess = CLEAN (uses tempFiles to clean up)
- **Completed**: currentSubprocess = COMPLETED; endTime set

**IMPORTANT**: All data transfer between subprocesses MUST occur through DailyProcessContext:
- Extract stores data in `extractedData` field
- Transform reads from `extractedData`, stores in `transformedData`
- Load reads from `transformedData`, updates `recordsLoaded`
- Validate reads `recordsLoaded` for validation
- Clean reads `tempFiles` for cleanup

No subprocess should store data externally or access another subprocess's data directly.

**Builder Pattern**:
```java
DailyProcessContext context = DailyProcessContext.builder()
    .date(LocalDate.of(2025, 1, 1))
    .configuration(config)
    .startTime(LocalDateTime.now())
    .build();

DailyProcessContext updated = DailyProcessContext.builder(context)
    .currentSubprocess(SubprocessType.TRANSFORM)
    .recordsExtracted(1000)
    .build();
```

**Validation Rules**:
- `date` cannot be null
- `configuration` cannot be null
- `startTime` cannot be null
- Record counts must be non-negative
- `recordsTransformed` <= `recordsExtracted`
- `recordsLoaded` <= `recordsTransformed`

---

## Enums

### SubprocessType

**Purpose**: Enumerates subprocess types in the ETL workflow.

**Package**: `com.sdd.etl.enums`

**Values**:
| Value | Description |
|-------|-------------|
| NOT_STARTED | Process has not started |
| EXTRACT | Extract subprocess |
| TRANSFORM | Transform subprocess |
| LOAD | Load subprocess |
| VALIDATE | Validate subprocess |
| CLEAN | Clean subprocess |
| COMPLETED | All subprocesses completed successfully |
| FAILED | Process failed |

---

## API Definitions (Abstract Models)

### 10. SourceDataModel (API Only)

**Purpose**: Abstract API representing the structure of data from various sources. Concrete implementations will extend this model according to real data structure.

**Package**: `com.sdd.etl.api.model`

**Methods** (to be implemented by concrete subclasses):
| Method | Signature | Description |
|--------|-----------|-------------|
| getMetadata | `Map<String, Object> getMetadata()` | Returns metadata about data fields and types |
| getPrimaryKey | `Object getPrimaryKey()` | Returns primary key value for this record |
| getSourceName | `String getSourceName()` | Returns name of source system |
| getField | `Object getField(String fieldName)` | Returns value of a specific field |
| getFields | `Map<String, Object> getFields()` | Returns all field values |

**Validation Rules**:
- `getPrimaryKey()` must return non-null value
- `getSourceName()` must match a configured source name

---

### 11. TargetDataModel (API Only)

**Purpose**: Abstract API representing the structure of data in target systems. Concrete implementations will extend this model according to real data structure.

**Package**: `com.sdd.etl.api.model`

**Methods** (to be implemented by concrete subclasses):
| Method | Signature | Description |
|--------|-----------|-------------|
| getMetadata | `Map<String, Object> getMetadata()` | Returns metadata about data fields and types |
| setField | `void setField(String fieldName, Object value)` | Sets value of a specific field |
| getFields | `Map<String, Object> getFields()` | Returns all field values |
| getTargetName | `String getTargetName()` | Returns name of target system |
| validate | `boolean validate(ValidationConfig config)` | Validates data against target constraints |

**Validation Rules**:
- `setField()` must enforce target-specific constraints
- `getTargetName()` must match a configured target name

---

## Relationships

```
Configuration (1)
├── SourceConfig (1..*)
│   └── Credentials (0..1)
├── TargetConfig (1..*)
│   └── Credentials (0..1)
├── TransformConfig (0..*)
│   ├── references SourceConfig (1)
│   ├── references TargetConfig (1)
│   └── FilterConfig (0..*)
├── ValidationConfig (1)
│   ├── CompletenessRules (0..*)
│   └── QualityRules (0..*)
└── LoggingConfig (1)

DailyProcessContext (many, one per day)
├── references Configuration (1)
├── references extractedData (List<SourceDataModel>, populated by Extract)
├── references transformedData (List<TargetDataModel>, populated by Transform)
├── references tempFiles (List<Path>, populated by Extract/Transform/Load)
└── uses SubprocessType (1 at a time)

SourceDataModel (API)
└── implemented by concrete source data models

TargetDataModel (API)
└── implemented by concrete target data models
```

## INI File Structure (Configuration Source)

```ini
[sources]
count=3

[source.0]
name=source_database
type=database
connectionString=jdbc:postgresql://localhost:5432/sourcedb
credentials.username=etl_user
credentials.password=secure_password
primaryKeyField=id

[source.1]
name=source_api
type=api
connectionString=https://api.example.com/data
credentials.token=api_key_12345
primaryKeyField=record_id

[source.2]
name=source_file
type=file
connectionString=/data/input/file.csv
primaryKeyField=row_id

[targets]
count=2

[target.0]
name=target_warehouse
type=database
connectionString=jdbc:mysql://localhost:3306/warehouse
credentials.username=etl_user
credentials.password=secure_password

[target.1]
name=target_archive
type=file
connectionString=/data/output/archive.csv

[transforms]
count=2

[transform.0]
sourceName=source_database
targetName=target_warehouse

[transform.0.fieldMapping]
source_field1=target_field1
source_field2=target_field2

[transform.0.filter]
0.fieldName=source_field1
0.operator=eq
0.value=active

[transform.1]
sourceName=source_api
targetName=target_archive

[transform.1.fieldMapping]
record_id=archive_id
data=archive_data

[validation]
checkCompleteness=true
checkQuality=true
checkConsistency=true
completenessRules=id,name,timestamp
qualityRules.id=^[0-9]+$
qualityRules.name=^[a-zA-Z ]+$

[logging]
logDirectory=./logs
consoleLogging=true
fileLogging=true
logLevel=INFO
```

## Summary

This data model provides:
- Concrete implementations for Configuration and DailyProcessContext
- API definitions (not implementations) for SourceDataModel and TargetDataModel
- Clear entity relationships and validation rules
- Immutable design for thread safety
- Builder pattern for flexible context construction
- **Context-based data transfer** between subprocesses (critical requirement)

All models align with:
- Java 8 compatibility
- INI configuration format
- Component boundary clarity
- Single-process execution constraints
- Subprocess data transfer through DailyProcessContext
