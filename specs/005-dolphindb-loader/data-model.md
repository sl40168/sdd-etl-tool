# Data Model: DolphinDB Data Loader

## Overview
This document defines the data entities and relationships for the DolphinDB data loader implementation. The model supports loading transformed data to DolphinDB with temporary table management and extensible loader architecture.

## Core Entities

### TargetDataModel (Abstract Base)
**Purpose**: Base class for all transformed data ready for loading to DolphinDB.

**Attributes**:
- `dataType` (String): Identifier for the specific data model type (e.g., "XbondQuote", "XbondTrade", "BondFutureQuote")
- `fieldOrder` (Map<String, Integer>): Mapping of field names to column order in DolphinDB tables
- `validationErrors` (List<String>): Accumulated validation errors during transformation

**Constraints**:
- All concrete implementations must define field order mapping
- Field names must match DolphinDB column names
- Data types must be compatible with DolphinDB types

### Concrete Data Model Classes

Three concrete classes extend `TargetDataModel`, each corresponding to a specific data type and target table configuration.

**1. XbondQuoteDataModel**
- **Data Type**: `"XbondQuote"`
- **Target Table**: `xbond_quote_stream_temp` (configurable via `LoaderConfiguration.targetTableMappings`)
- **Field Count**: 83 fields
- **Key Fields**: `business_date` (DATE), `exch_product_id` (SYMBOL), `product_type` (SYMBOL), `exchange` (SYMBOL), `source` (SYMBOL), `settle_speed` (INT), `level` (SYMBOL), `status` (SYMBOL), `pre_close_price` (DOUBLE), `pre_settle_price` (DOUBLE), `pre_interest` (DOUBLE), `open_price` (DOUBLE), `high_price` (DOUBLE), `low_price` (DOUBLE), `close_price` (DOUBLE), `settle_price` (DOUBLE), `upper_limit` (DOUBLE), `lower_limit` (DOUBLE), `total_volume` (DOUBLE), `total_turnover` (DOUBLE), `open_interest` (DOUBLE), plus 60 additional fields for bid/offer levels 0‑5, ending with `event_time` (TIMESTAMP), `receive_time` (TIMESTAMP)
- **Full Schema**: See Plan.md Section V (lines 28‑117)

**2. XbondTradeDataModel**
- **Data Type**: `"XbondTrade"`
- **Target Table**: `xbond_trade_stream_temp` (configurable via `LoaderConfiguration.targetTableMappings`)
- **Field Count**: 15 fields
- **Key Fields**: `business_date` (DATE), `exch_product_id` (SYMBOL), `product_type` (SYMBOL), `exchange` (SYMBOL), `source` (SYMBOL), `settle_speed` (INT), `last_trade_price` (DOUBLE), `last_trade_yield` (DOUBLE), `last_trade_yield_type` (SYMBOL), `last_trade_volume` (DOUBLE), `last_trade_turnover` (DOUBLE), `last_trade_interest` (DOUBLE), `last_trade_side` (SYMBOL), `event_time` (TIMESTAMP), `receive_time` (TIMESTAMP)
- **Full Schema**: See Plan.md Section VI (lines 118‑139)

**3. BondFutureQuoteDataModel**
- **Data Type**: `"BondFutureQuote"`
- **Target Table**: `fut_market_price_stream_temp` (configurable via `LoaderConfiguration.targetTableMappings`)
- **Field Count**: 96 fields
- **Key Fields**: `business_date` (DATE), `exch_product_id` (SYMBOL), `product_type` (SYMBOL), `exchange` (SYMBOL), `source` (SYMBOL), `settle_speed` (INT), plus 90 additional fields for last trade details, level/status, price/yield/volume fields, and six timestamp fields (`event_time_trade`, `receive_time_trade`, `create_time_trade`, `event_time_quote`, `receive_time_quote`, `create_time_quote`, `tick_type`, `receive_time`)
- **Full Schema**: See Plan.md Section VII (lines 140‑242)

**Implementation Notes**:
- The single `DolphinDBLoader` accepts a list containing mixed instances of these three classes.
- Records are grouped by `dataType` field, then loaded into the corresponding target table as per configuration mapping.
- Column order mapping (`fieldOrder`) must be defined for each concrete class according to the Plan.md tables.
- **Primitive Number Field Initialization**: All primitive numeric fields (int, double, long, etc.) in concrete TargetDataModel classes MUST be explicitly initialized, avoiding default zero values. This ensures data integrity and complies with constitution principle PR‑11.

### TemporaryTable
**Purpose**: Intermediate table in DolphinDB used during the loading process.

**Attributes**:
- `tableName` (String): Unique name for the temporary table (immutable after creation)
- `schemaDefinition` (String): SQL definition of table columns and types
- `creationTimestamp` (Instant): Time when table was created
- `rowCount` (int): Number of rows loaded into the table

**Constraints**:
- Table names must be unique to avoid conflicts
- Must be created before data loading begins
- Must be deleted after data validation completes

### TargetTable
**Purpose**: Final destination table in DolphinDB where data persists for downstream analytics.

**Attributes**:
- `tableName` (String): Name of the target table in DolphinDB
- `dataModelType` (String): Type of TargetDataModel this table accepts
- `partitioningStrategy` (String): Optional partitioning scheme for distributed tables
- `indexes` (List<String>): Optional column indexes for performance

**Constraints**:
- Each TargetTable accepts a specific type of TargetDataModel
- Table schemas must match the field definitions in corresponding TargetDataModel
- Partitioning must be compatible with DolphinDB's distributed architecture

### LoaderConfiguration
**Purpose**: Configuration settings for the DolphinDB data loader.

**Attributes**:
- `dolphinDBHost` (String): DolphinDB server hostname or IP
- `dolphinDBPort` (int): DolphinDB server port
- `username` (String): Authentication username
- `password` (String): Authentication password
- `sortFields` (List<String>): Fields to sort by before loading (e.g., ["receive_time"])
- `tableMappings` (Map<String, String>): Mapping from data model types to target table names
- `memoryLimitMB` (int): Maximum memory for sorting operations (default: 512MB)
- `temporaryTablePrefix` (String): Prefix for temporary table names (default: "etl_temp_")
- `batchSize` (int): Number of records to process in a batch (default: 1000)

**Constraints**:
- Authentication credentials must be provided for secure connections
- Sort fields must exist in the TargetDataModel being processed
- Memory limit must be positive integer

## Data Type Mapping

### DolphinDB → Java Mapping
| DolphinDB Type | Java Class | Java Type | Conversion Notes |
|----------------|------------|-----------|------------------|
| DATE           | `BasicDate` | `LocalDate` | Use `LocalDate.parse()` for string conversion |
| SYMBOL         | `BasicString` | `String` | Direct string assignment |
| INT            | `BasicInt` | `int` | Use Java primitive int |
| DOUBLE         | `BasicDouble` | `double` | Use Java primitive double |
| TIMESTAMP      | `BasicTimestamp` | `Instant` | Requires nanosecond precision |

### Implementation Notes
- All DolphinDB data type classes reside in `com.xxdb.data` package
- For missing `receive_time` fields, records are skipped with warning
- Null values in sort fields cause record exclusion during sorting
- Data volume exceeding memory limit triggers external (disk-based) sorting

## Data Conversion for Column-Based Insertion

DolphinDB is a column-based database where data insertion typically requires converting record-oriented data into column-oriented arrays. Each field's values across all records must be transformed into a single array per column.

### Conversion Process

1. **Input**: List of `TargetDataModel` instances sorted by configured fields.
2. **Grouping**: Records grouped by data type (e.g., XbondQuote, XbondTrade, BondFutureQuote).
3. **Column Extraction**: For each field in the data model schema, extract values across all records into a Java array.
4. **Type Mapping**: Convert Java primitives/wrappers to DolphinDB API scalar types (`BasicDate`, `BasicString`, `BasicInt`, `BasicDouble`, `BasicTimestamp`).
5. **Output**: Column‑wise arrays ready for `tableInsert` or appender operations.

### Example: XbondQuote Conversion

Given 1000 XbondQuote records, the loader must create:

```java
// Column arrays (simplified)
LocalDate[] businessDate = extractField("business_date");
String[] exchProductId = extractField("exch_product_id");
// ... for all 83 fields
Instant[] receiveTime = extractField("receive_time");
```

Each array length equals the number of records (1000). The arrays are then passed to DolphinDB's `tableInsert` method for bulk insertion.

### Performance Considerations

- **Batch Size**: Configurable batch size for memory‑efficient conversion.
- **Direct Buffer**: Use `java.nio` buffers for large numeric arrays to reduce heap pressure.
- **Parallel Conversion**: For large datasets, consider parallel extraction of independent fields.

## Relationships

### TargetDataModel → TargetTable (1:1)
- Each concrete TargetDataModel maps to exactly one TargetTable
- Mapping defined in LoaderConfiguration.tableMappings

### LoaderConfiguration → Data Loader (1:1)
- Each loader instance uses a single configuration
- Configuration provides connection parameters and runtime settings



## Validation Rules

### Data Integrity
1. All fields in TargetDataModel must have corresponding columns in TargetTable
2. Field types must be compatible between Java and DolphinDB
3. Required fields (e.g., `receive_time`) must not be null

### Operational Constraints
1. Temporary tables must be uniquely named to prevent collisions
2. Sorting memory usage must not exceed configured limit
3. Failed loads must stop the ETL process for manual intervention

## Performance Considerations

### Sorting Strategy
- **In-memory sort**: For datasets under configured memory limit
- **External sort**: For datasets exceeding memory limit (disk-based)
- **Configurable memory limit**: Allows tuning for different data volumes

### Connection Management
- Connection pooling for efficient reuse
- Configurable timeouts for network operations
- Automatic reconnection handling for transient failures

## Extensibility Patterns

### Adding New Data Model Types
1. Extend `TargetDataModel` abstract class
2. Define field order mapping
3. Add table mapping to LoaderConfiguration
4. Implement data conversion logic

### Supporting New Target Systems
1. Implement common Loader API interfaces
2. Extend LoaderConfiguration for system-specific settings
3. Create system-specific data conversion utilities