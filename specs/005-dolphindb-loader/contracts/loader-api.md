# Loader API Contract

**Feature Branch**: `005-dolphindb-loader`
**Date**: 2026-01-11
**Status**: Phase 1 Design

## Overview

This document defines the common Loader API contract for the ETL tool. The Loader API provides an extensible interface for loading transformed data into target systems (databases, data warehouses, etc.). The first implementation will target DolphinDB using its Java API, with future implementations possible for MySQL, PostgreSQL, or other systems.

## Core Interface

### `com.sdd.etl.loader.api.Loader`

```java
public interface Loader {
    /**
     * Initialize the loader with configuration.
     * @param config LoaderConfiguration containing connection details, sorting fields, etc.
     * @throws LoaderConfigurationException if configuration is invalid
     */
    void init(LoaderConfiguration config) throws LoaderConfigurationException;

    /**
     * Sort the provided TargetDataModel instances according to the configured sorting fields.
     * Sorting is performed externally (disk-based) if data exceeds memory limits.
     * @param dataModels List of TargetDataModel instances to be sorted
     * @return List of sorted TargetDataModel instances (may be same instances if already sorted)
     * @throws SortingException if sorting fails due to IO errors or insufficient resources
     */
    List<TargetDataModel> sortData(List<TargetDataModel> dataModels) throws SortingException;

    /**
     * Load sorted data into target tables based on record type.
     * Each record is loaded into its corresponding target table based on dataType.
     * @param sortedData List of sorted TargetDataModel instances
     * @throws LoadingException if any loading operation fails
     */
    void loadData(List<TargetDataModel> sortedData) throws LoadingException;

    /**
     * Shutdown the loader, releasing any resources (e.g., database connections).
     */
    void shutdown();
}
```

**IMPORTANT**: Temporary table creation and deletion are handled by LoadSubprocess and CleanSubprocess respectively, NOT by the Loader interface.
- LoadSubprocess executes `temporary_table_creation.dos` script via DolphinDB Java API
- CleanSubprocess executes `temporary_table_deletion.dos` script via DolphinDB Java API
- Scripts are loaded from resources at runtime

## Supporting Classes

### `com.sdd.etl.loader.api.LoaderConfiguration`

```java
public class LoaderConfiguration {
    private String connectionUrl;
    private String username;
    private String password;
    private List<String> sortFields;          // Field names to sort by (e.g., ["date", "symbol"])
    private long maxMemoryBytes = 256 * 1024 * 1024; // 256 MB default for in‑memory sorting
    private String temporaryTablePrefix = "temp_";
    private Map<String, String> targetTableMappings; // dataType -> tableName (immutable during ETL)
    private Map<String, String> additionalProperties;

    // Constructors, getters, setters, validation methods
}
```

### `com.sdd.etl.loader.api.TargetDataModel`

Abstract base class for all transformed data ready for loading to DolphinDB.

```java
public abstract class TargetDataModel {
    protected String dataType;
    protected Map<String, Integer> fieldOrder;
    protected List<String> validationErrors;

    // Constructors, getters, setters, validation methods
    
    /**
     * Get the data type identifier for this model.
     * @return Data type string (e.g., "XbondQuote", "XbondTrade", "BondFutureQuote")
     */
    public String getDataType();
    
    /**
     * Validate the data model.
     * @return true if valid, false otherwise
     */
    public abstract boolean validate();
    
    /**
     * Get the field names for this model in the correct order.
     * @return List of field names ordered by column position
     */
    public List<String> getOrderedFieldNames();
}
```

**Concrete implementations:**
- `XbondQuoteDataModel` - For Xbond quote data
- `XbondTradeDataModel` - For Xbond trade data  
- `BondFutureQuoteDataModel` - For bond future quote data

Each concrete model defines its own field order mapping and validation rules.

## Exception Hierarchy

All loader exceptions extend `LoaderException` (runtime exception).

```
LoaderException
├── LoaderConfigurationException
├── TemporaryTableCreationException
├── SortingException
├── LoadingException
├── ValidationException
└── CleanupException
```

Each exception includes:
- A descriptive message
- The target table name (if applicable)
- The original cause (if any)
- Suggested user action

## Integration Points

### LoadSubprocess Integration

The `LoadSubprocess` will:
1. Execute temporary table creation script (`temporary_table_creation.dos`) via DolphinDB Java API.
2. Retrieve transformed data from `ETLContext`.
3. Instantiate the configured `Loader` implementation (e.g., `DolphinDBLoader`).
4. Call `init()` with configuration from the INI file.
5. Call `sortData()` on the list of transformed data models (XbondQuoteDataModel, XbondTradeDataModel, BondFutureQuoteDataModel).
6. Call `loadData()` with sorted data models.
7. If any step fails, propagate the exception (which will break the ETL process).

### CleanSubprocess Integration

The `CleanSubprocess` will:
1. Execute temporary table deletion script (`temporary_table_deletion.dos`) via DolphinDB Java API.
2. Obtain the same `Loader` instance (shared via context).
3. Call `shutdown()` after cleanup.

## Configuration

### INI File Section

```ini
[loader]
connection.url = localhost:8848
connection.username = admin
connection.password = 123456
sort.fields = date,symbol
max.memory.mb = 256
temporary.table.prefix = temp_
target.table.mappings = XbondQuote:xbond_quote_stream_temp,XbondTrade:xbond_trade_stream_temp,BondFutureQuote:fut_market_price_stream_temp
```

## Lifecycle

1. **Initialization**: Loader reads configuration and establishes connection.
2. **Temporary Table Creation** (by LoadSubprocess): Before loading, temporary tables are created by executing `temporary_table_creation.dos` via DolphinDB Java API.
3. **Sorting**: Data is sorted by configured fields (external sort if needed).
4. **Loading**: Sorted data is inserted directly into target tables via `tableInsert`.
5. **Cleanup** (by CleanSubprocess): Temporary tables are dropped by executing `temporary_table_deletion.dos` via DolphinDB Java API.
6. **Shutdown**: Connection is closed.

## Error Handling

- Any exception thrown by the loader will cause the ETL process to stop immediately.
- The exception message must be descriptive enough for manual intervention.
- Temporary tables are left intact on failure to allow forensic analysis.
- The user must resolve the issue and restart the ETL process.

## Extensibility

To add a new target system:
1. Implement the `Loader` interface.
2. Add a factory method in `LoaderFactory`.
3. Update INI configuration parser to recognize the new loader type.
4. Write unit and integration tests.

Example future loader types:
- `MySQLloader`
- `PostgreSQLLoader`
- `BigQueryLoader`

## Feature Requirements Mapping

| FR # | Requirement | Contract Coverage |
|------|-------------|-------------------|
| FR-001 | Common Loader API for extensibility | ✅ Core interface definition |
| FR-002 | Use DolphinDB Java API | ✅ Implementation‑specific contract (separate) |
| FR-003 | Daily ETL process with temporary tables | ✅ createTemporaryTables, cleanupTemporaryTables |
| FR-004 | Sort TargetDataModel by given fields | ✅ sortData method |
| FR-005 | Load data sequentially by data type | ✅ loadData with targetTableMappings |
| FR-006 | Integrate with LoadSubprocess | ✅ Integration points section |
| FR-007 | Integrate with CleanSubprocess | ✅ Integration points section |
| FR-008 | Any loading exception breaks ETL process | ✅ Error handling section |
| FR-009 | Target table names MUST be configurable and remain unchanged during the entire ETL process | ✅ LoaderConfiguration.targetTableMappings field and INI mapping example |

## Source

- Feature Specification: FR-001 through FR-009
- User Stories: US1 (Load Data via Java API), US2 (Integrate with Subprocesses), US3 (Handle Exceptions)
- Edge Cases: DolphinDB unavailability, duplicate data, null values in sort fields, memory limits, partial failures
- Clarifications: Authentication via username/password, skip records with null sort fields, external sorting when memory exceeded, keep successful data on partial failure, temporary table names immutable