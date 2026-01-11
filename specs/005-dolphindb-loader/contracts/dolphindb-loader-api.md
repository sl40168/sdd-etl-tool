# DolphinDB Loader API Contract

**Feature Branch**: `005-dolphindb-loader`
**Date**: 2026-01-11
**Status**: Phase 1 Design

## Overview

This document defines the DolphinDB‑specific implementation contract for the common Loader API. The `DolphinDBLoader` uses DolphinDB's Java API to connect to a DolphinDB server, create temporary tables, sort data externally if needed, load sorted data into target tables sequentially by data type, validate the load, and clean up temporary tables.

## Implementation Class

### `com.sdd.etl.loader.dolphin.DolphinDBLoader`

```java
public class DolphinDBLoader implements Loader {
    private DolphinDBConnection connection;
    private LoaderConfiguration config;
    private ExternalSorter sorter;
    private Map<String, String> temporaryTableNames; // target table -> temporary table name

    @Override
    public void init(LoaderConfiguration config) throws LoaderConfigurationException {
        // Validate config (connection URL, credentials, sort fields)
        // Establish connection using DolphinDB Java API's DBConnection
        // Set connection timeout and other parameters
        // Initialize external sorter with maxMemoryBytes
    }

    @Override
    public void createTemporaryTables(List<TargetTable> targetTables)
            throws TemporaryTableCreationException {
        // For each target table:
        // 1. Generate immutable temporary table name (e.g., "temp_quote_20250111_123456")
        // 2. Execute DolphinDB script to create temporary table with same schema
        //    (using `table(schema)` or `createTempTable`)
        // 3. Store mapping in temporaryTableNames
    }

    @Override
    public TargetDataModel sortData(TargetDataModel dataModel) throws SortingException {
        // Extract records from dataModel
        // Skip records with null values in any configured sort field (log warning)
        // Sort by config.getSortFields() using external sorter if data exceeds memory limit
        // Return sorted data model (may be new instance)
    }

    @Override
    public void loadData(TargetDataModel sortedData, List<TargetTable> targetTables)
            throws LoadingException {
        // 1. Insert sorted records into temporary tables using `tableInsert`
        // 2. Sort target tables by dataType (e.g., "quote" before "trade")
        // 3. For each target table in sorted order:
        //    a. Execute DolphinDB script to move data from temporary table to target table
        //       (e.g., `targetTable.append!(temporaryTable)`)
        //    b. Commit after each table (or batch) – ensure atomicity per table
        // 4. If any table fails, stop loading and throw LoadingException
        //    (previous successful tables remain loaded)
    }

    @Override
    public void validateLoad(List<TargetTable> targetTables) throws ValidationException {
        // For each target table:
        // 1. Query row count of temporary table
        // 2. Query row count of target table (since last load)
        // 3. Compare counts; mismatch throws ValidationException
        // 4. Optionally run integrity checks (no duplicates, foreign keys)
    }

    @Override
    public void cleanupTemporaryTables(List<TargetTable> targetTables)
            throws CleanupException {
        // For each target table, drop its temporary table using `dropTable`
        // Ignore "table does not exist" errors
        // Clear temporaryTableNames mapping
    }

    @Override
    public void shutdown() {
        // Close DolphinDB connection
        // Release external sorter resources
    }
}
```

## Supporting Classes

### `com.sdd.etl.loader.dolphin.DolphinDBConnection`

Wrapper around DolphinDB's `DBConnection` with logging and error translation.

```java
public class DolphinDBConnection {
    private DBConnection dbConn;
    private String url;
    private String username;
    private String password;

    public void connect(String url, String username, String password, int timeout)
            throws ConnectionException;

    public void executeScript(String script) throws ScriptExecutionException;

    public Table executeQuery(String query) throws QueryException;

    public void close();
}
```

### `com.sdd.etl.loader.dolphin.DolphinDBScriptExecutor`

Manages script execution with retry and error handling.

```java
public class DolphinDBScriptExecutor {
    public void createTable(String tableName, Schema schema) throws ScriptExecutionException;
    public void insertData(String tableName, List<Record> records) throws ScriptExecutionException;
    public void appendTable(String sourceTable, String targetTable) throws ScriptExecutionException;
    public void dropTable(String tableName) throws ScriptExecutionException;
    public long getRowCount(String tableName) throws QueryException;
}
```

### `com.sdd.etl.loader.dolphin.sort.ExternalSorter`

Performs external (disk‑based) sorting when data exceeds memory limit.

```java
public class ExternalSorter {
    public ExternalSorter(long maxMemoryBytes, Path tempDir);

    public List<Record> sort(List<Record> records, List<String> sortFields)
            throws IOException;

    public void cleanUp();
}
```

## Concrete Data Model Classes

Three concrete classes extend the abstract `TargetDataModel`, each corresponding to a specific data type and target table. The single `DolphinDBLoader` accepts a list containing mixed instances of these classes, groups records by `dataType`, and loads each group into the corresponding target table as configured in `LoaderConfiguration.targetTableMappings`.

**1. XbondQuoteDataModel**
- **Data Type**: `"XbondQuote"`
- **Target Table**: `xbond_quote_stream_temp` (configurable via `target.table.mappings`)
- **Field Count**: 83 fields
- **Key Fields**: `business_date` (DATE), `exch_product_id` (SYMBOL), `product_type` (SYMBOL), `exchange` (SYMBOL), `source` (SYMBOL), `settle_speed` (INT), `level` (SYMBOL), `status` (SYMBOL), price/yield/volume fields for levels 0‑5, `event_time` (TIMESTAMP), `receive_time` (TIMESTAMP)
- **Full Schema**: See Plan.md Section V

**2. XbondTradeDataModel**
- **Data Type**: `"XbondTrade"`
- **Target Table**: `xbond_trade_stream_temp` (configurable via `target.table.mappings`)
- **Field Count**: 15 fields
- **Key Fields**: `business_date` (DATE), `exch_product_id` (SYMBOL), `product_type` (SYMBOL), `exchange` (SYMBOL), `source` (SYMBOL), `settle_speed` (INT), `last_trade_price` (DOUBLE), `last_trade_yield` (DOUBLE), `last_trade_yield_type` (SYMBOL), `last_trade_volume` (DOUBLE), `last_trade_turnover` (DOUBLE), `last_trade_interest` (DOUBLE), `last_trade_side` (SYMBOL), `event_time` (TIMESTAMP), `receive_time` (TIMESTAMP)
- **Full Schema**: See Plan.md Section VI

**3. BondFutureQuoteDataModel**
- **Data Type**: `"BondFutureQuote"`
- **Target Table**: `fut_market_price_stream_temp` (configurable via `target.table.mappings`)
- **Field Count**: 96 fields
- **Key Fields**: `business_date` (DATE), `exch_product_id` (SYMBOL), `product_type` (SYMBOL), `exchange` (SYMBOL), `source` (SYMBOL), `settle_speed` (INT), plus last trade details, level/status, price/yield/volume fields, and six timestamp fields (`event_time_trade`, `receive_time_trade`, `create_time_trade`, `event_time_quote`, `receive_time_quote`, `create_time_quote`, `tick_type`, `receive_time`)
- **Full Schema**: See Plan.md Section VII

**Implementation Notes**:
- Each concrete class must define `fieldOrder` mapping according to the Plan.md tables.
- The `dataType` field must match the configured mapping key in `targetTableMappings`.
- The loader groups records by `dataType` before sorting and loading.

## Data Type Mapping

| DolphinDB Type | Java Class (DolphinDB API) | Example Value |
|----------------|----------------------------|---------------|
| DATE           | LocalDate                  | 2025‑01‑01    |
| SYMBOL         | String                     | "AAPL"        |
| INT            | Integer                    | 100           |
| DOUBLE         | Double                     | 123.45        |
| TIMESTAMP      | LocalDateTime              | 2025‑01‑01 10:30:00.000 |

The loader must convert `TargetDataModel` records to the appropriate Java objects before inserting into DolphinDB.

## Temporary Table Naming Convention

Temporary table names are generated once per ETL run and remain immutable. Format:

```
{prefix}_{targetTableName}_{date}_{randomSuffix}
```

Example: `temp_quote_20250111_abc123`

- **prefix**: from `LoaderConfiguration.temporaryTablePrefix` (default `temp_`)
- **targetTableName**: name of the target table (e.g., `quote`)
- **date**: processing date in `YYYYMMDD` format
- **randomSuffix**: 6‑character alphanumeric string to avoid collisions

The mapping is stored in `temporaryTableNames` and used throughout the loading lifecycle.

## Sorting Strategy

1. **In‑Memory Sorting**: Used when total record size ≤ `maxMemoryBytes`.
   - Java's `Collections.sort` with a custom comparator.
2. **External Sorting**: Automatically triggered when memory limit exceeded.
   - Divides records into sorted runs stored on disk.
   - Merges runs using a priority queue.
   - Temporary files stored in `java.io.tmpdir/etl_sort_*`.

Records with `null` values in any configured sort field are **skipped** (logged as warning). They are not included in the sorted output and will not be loaded.

## Script Execution

All DolphinDB operations are performed via scripts executed through `DBConnection.run(String script)`. Example scripts:

### Create Temporary Table
```python
tempTable = table(
    1:0,  // initial rows
    `col1`col2`col3,  // column names
    [DATE, SYMBOL, INT]  // column types
)
share tempTable as temp_quote_20250111_abc123
```

### Insert Data
```python
data = [[2025.01.01, "AAPL", 100], [2025.01.02, "GOOG", 200]]
t = temp_quote_20250111_abc123
t.append!(data)
```

### Append to Target Table
```python
target = loadTable("dfs://db", "quote")
target.append!(temp_quote_20250111_abc123)
```

### Drop Temporary Table
```python
dropTable("temp_quote_20250111_abc123")
```

## Error Handling

### Connection Errors
- `ConnectionException` – server unreachable, wrong credentials.
- **User action**: Verify server status and credentials.

### Script Execution Errors
- `ScriptExecutionException` – syntax error, insufficient permissions.
- **User action**: Check DolphinDB logs, review script.

### Sorting Errors
- `SortingException` – IO error, insufficient disk space.
- **User action**: Free disk space, check file permissions.

### Loading Errors
- `LoadingException` – duplicate key, constraint violation, partial failure.
- **User action**: Examine temporary table data, fix duplicates, restart.

### Validation Errors
- `ValidationException` – row count mismatch, data corruption.
- **User action**: Compare temporary and target table contents.

### Cleanup Errors
- `CleanupException` – table lock, permission denied.
- **User action**: Manually drop leftover temporary tables.

All exceptions extend `LoaderException` (runtime). The ETL process stops immediately, temporary tables are left intact for forensic analysis.

## Integration with LoadSubprocess and CleanSubprocess

### LoadSubprocess
1. Obtain `Loader` instance from `LoaderFactory.getLoader(config)`.
2. Call `init(config)`.
3. Call `createTemporaryTables(targetTables)`.
4. Call `sortData(transformedDataModel)`.
5. Call `loadData(sortedData, targetTables)`.
6. Call `validateLoad(targetTables)`.
7. Propagate any exception → day fails, ETL stops.

### CleanSubprocess
1. Obtain same `Loader` instance (via context).
2. Call `cleanupTemporaryTables(targetTables)`.
3. Call `shutdown()`.

## Configuration Details

### INI Section `[loader]`
| Key | Required | Default | Description |
|-----|----------|---------|-------------|
| `connection.url` | Yes | – | DolphinDB server host:port (e.g., `localhost:8848`) |
| `connection.username` | Yes | – | Authentication username |
| `connection.password` | Yes | – | Authentication password |
| `sort.fields` | Yes | – | Comma‑separated field names (e.g., `date,symbol`) |
| `max.memory.mb` | No | `256` | Memory limit (MB) for in‑memory sorting |
| `temporary.table.prefix` | No | `temp_` | Prefix for temporary table names |

### INI Section `[targetX]` (for DolphinDB targets)
| Key | Required | Default | Description |
|-----|----------|---------|-------------|
| `type` | Yes | – | Must be `dolphindb` |
| `dataType` | Yes | – | Determines loading order (e.g., `quote`, `trade`) |
| `schemaTable` | No | – | Existing table whose schema is copied for temporary table |

## Performance Considerations

- **Batch insertion**: Use `tableInsert` with batches of records (size configurable).
- **Connection pooling**: Single connection per loader instance (DolphinDB Java API does not support pooling).
- **External sort tuning**: Adjust `maxMemoryBytes` and temporary directory location.
- **Network latency**: Minimize round‑trips by combining scripts where possible.

## Success Criteria Mapping

| SC # | Success Criterion | Contract Coverage |
|------|-------------------|-------------------|
| SC-001 | Load 1M records in ≤30 minutes | ✅ Performance considerations, batch insertion |
| SC-002 | Zero orphaned temporary tables after successful run | ✅ Cleanup guarantees, immutable naming |
| SC-003 | Error diagnosis within 5 minutes | ✅ Descriptive exceptions, suggested actions |
| SC-004 | <200 lines of new code for new target system | ✅ Common Loader API design, extensibility |

## Source

- Feature Specification: `specs/005-dolphindb-loader/spec.md`
- Research: `specs/005-dolphindb-loader/research.md`
- Data Model: `specs/005-dolphindb-loader/data-model.md`
- Common Loader API Contract: `specs/005-dolphindb-loader/contracts/loader-api.md`
- DolphinDB Java API documentation: https://www.dolphindb.com/help/