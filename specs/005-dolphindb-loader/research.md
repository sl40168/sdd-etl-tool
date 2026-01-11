# Research: DolphinDB Data Loader Implementation

## Overview
This document summarizes research findings for implementing a data loader to load transformed data to DolphinDB via its Java API. The research focuses on connection management, script execution, data type mapping, sorting strategies, and integration with existing ETL subprocesses.

## 1. DolphinDB Java API

### Key Components
- **DBConnection**: Main class for establishing connections to DolphinDB server
- **Connection Pools**: 
  - `SimpleDBConnectionPool`: Standard connection pool
  - `ExclusiveDBConnectionPool`: Task mechanism connection pool
- **Authentication**: Login with username/password credentials during connection setup

### Basic Operations
- **run()**: Execute scripts or functions on the server
- **tryRun()**: Alternative execution method with error handling
- **upload()**: Upload Java objects to the server
- **tryUpload()**: Alternative upload method

### Data Insertion Methods
1. **AutoFitTableAppender**: Write data to tables with automatic schema fitting
2. **AutoFitTableUpsert**: Update tables with upsert operations
3. **PartitionedTableAppender**: Write data to distributed tables
4. **MultithreadedTableWriter**: Multi-threaded concurrent data writing

### Quick Start Steps
1. Add DolphinDB Java API dependency to Maven
2. Establish connection using `DBConnection` with host, port, username, password
3. Execute scripts via `run()` method
4. Upload data using appropriate appender classes

## 2. Script Execution from Resources

### Reading Scripts from Classpath
```java
public String readScriptFromResources(String scriptPath) throws IOException {
    try (InputStream inputStream = getClass().getResourceAsStream(scriptPath)) {
        if (inputStream == null) {
            throw new IOException("Script not found in resources: " + scriptPath);
        }
        return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    }
}
```

### Execution Flow
1. Load script from `src/main/resources/scripts/`
2. Execute via `DBConnection.run(script)`
3. Handle execution errors with appropriate logging

### Script File Structure
- Temporary table creation script: `temporary_table_creation.dos`
- Temporary table deletion script: `temporary_table_deletion.dos`
- Scripts should be stored in `src/main/resources/scripts/`

## 3. Data Type Mapping

### Inferred Mapping (DolphinDB → Java API)
| DolphinDB Type | Java API Class | Java Type | Notes |
|----------------|----------------|-----------|-------|
| DATE           | `BasicDate`    | `LocalDate` | Stores date without time |
| SYMBOL         | `BasicString`  | `String`    | Symbol type mapped to string |
| INT            | `BasicInt`     | `int`       | 32-bit integer |
| DOUBLE         | `BasicDouble`  | `double`    | 64-bit floating point |
| TIMESTAMP      | `BasicTimestamp` | `Instant` | Nanosecond precision timestamp |

### Implementation Considerations
- All data type classes are in `com.xxdb.data` package
- Scalar types extend `Scalar` interface
- For `SYMBOL` type, use `BasicString` with appropriate conversion
- Timestamp values require nanosecond precision handling

## 4. Sorting Strategy

### Memory Management
- **Configurable memory limit**: Set maximum heap usage for sorting operations
- **Fallback to disk**: Use external sorting when data exceeds memory limit
- **Chunked processing**: Process data in manageable chunks

### External Sorting Implementation
```java
public List<TargetDataModel> sortWithExternalMemory(
    List<TargetDataModel> data, 
    Comparator<TargetDataModel> comparator,
    long maxMemoryBytes) {
    
    if (estimateMemoryUsage(data) <= maxMemoryBytes) {
        // In-memory sort
        data.sort(comparator);
        return data;
    } else {
        // External sort using temporary files
        return externalSort(data, comparator);
    }
}
```

### Null Value Handling
- Records with null values in sort fields are skipped during sorting
- Log warning for skipped records
- Ensure data integrity by excluding incomplete records

## 5. Integration with ETL Subprocesses

### LoadSubprocess Integration
- Extend `LoadSubprocess` for DolphinDB-specific loading
- Read temporary table creation script from resources
- Execute script via DolphinDB Java API before data loading
- Pass transformed data from `ETLContext` to embedded loader

### CleanSubprocess Integration
- Extend `CleanSubprocess` for DolphinDB cleanup
- Read temporary table deletion script from resources
- Execute script via DolphinDB Java API after data validation
- Ensure all temporary tables are removed

### Error Handling
- Stop ETL process on any loading exception
- Provide descriptive error messages for operator diagnosis
- Log detailed error context for debugging
- Support manual intervention for failed loads

## 6. Array Conversion for Column-Based Insertion

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

## 7. Common Implementation Patterns

### Connection Management
```java
public class DolphinDBConnectionManager {
    private DBConnection connection;
    
    public void connect(ETLContext context) {
        LoaderConfig config = context.getLoaderConfig();
        connection = new DBConnection();
        connection.connect(config.getHost(), config.getPort(), 
                          config.getUsername(), config.getPassword());
    }
    
    public void disconnect() {
        if (connection != null) {
            connection.close();
        }
    }
}
```

### Temporary Table Operations
```java
public class DolphinDBTableManager {
    private DBConnection connection;
    
    public void createTemporaryTables() throws IOException {
        String script = readScriptFromResources("/scripts/temporary_table_creation.dos");
        connection.run(script);
    }
    
    public void deleteTemporaryTables() throws IOException {
        String script = readScriptFromResources("/scripts/temporary_table_deletion.dos");
        connection.run(script);
    }
}
```

## 8. Reference Documents

### DolphinDB Java API Documentation
- https://docs.dolphindb.cn/zh/javadoc/newjava.html - Overview and quickstart
- https://docs.dolphindb.cn/zh/javadoc/install.html - Installation guide
- https://docs.dolphindb.cn/zh/javadoc/quickstart.html - Quick start examples
- https://docs.dolphindb.cn/zh/javadoc/java_api_data_types_forms.html - Data types and forms
- https://docs.dolphindb.cn/zh/javadoc/data_types_and_forms/scalar.html - Scalar types
- https://docs.dolphindb.cn/zh/javadoc/connect/create.html - Connection creation
- https://docs.dolphindb.cn/zh/javadoc/connect/connect.html - Connection management
- https://docs.dolphindb.cn/zh/javadoc/connect/login.html - Authentication
- https://docs.dolphindb.cn/zh/javadoc/connect/run.html - Script execution
- https://docs.dolphindb.cn/zh/javad4c/connect/upload.html - Data upload

### Java Implementation References
- Apache Commons IO for resource reading
- SLF4J for logging
- JUnit 4 for testing
- Mockito for mocking external dependencies