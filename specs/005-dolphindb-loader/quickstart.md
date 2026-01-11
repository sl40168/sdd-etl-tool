# Quick Start Guide: DolphinDB Loader

**Feature Branch**: `005-dolphindb-loader`
**Date**: 2026-01-11
**Status**: Phase 1 Design

## Overview

This guide provides a quick start for implementing and testing the DolphinDB Loader feature. The feature adds a common Loader API and a DolphinDB‑specific implementation that loads transformed data into DolphinDB using its Java API, following a daily ETL process with temporary tables, external sorting, and sequential loading by data type.

## Prerequisites

- **Java 8** (JDK 1.8.0_xxx) – non‑negotiable per project constitution
- **Maven 3.6+**
- **Git** (for version control)
- **DolphinDB server** (version 2.00 or later) running and accessible
- **DolphinDB Java API** JAR (will be added as a Maven dependency)

## Project Setup

### 1. Clone Repository

```bash
git clone <repository-url>
cd sdd-etl-tool
git checkout 005-dolphindb-loader
```

### 2. Build Project

Using system Maven:

```bash
mvn clean install
```

> Note: This repository does not include a Maven wrapper by default.

**Expected Output**:
```
[INFO] BUILD SUCCESS
[INFO] Total time: XX s
```

### 3. Verify Build

```bash
# Windows (PowerShell)
dir target\etl-tool-1.0.0.jar
dir target\etl-tool-1.0.0-jar-with-dependencies.jar

# Linux/Mac
ls target/etl-tool-1.0.0.jar
ls target/etl-tool-1.0.0-jar-with-dependencies.jar
```

## Project Structure

```
sdd-etl-tool/
├── src/main/java/com/sdd/etl/
│   ├── loader/                   # New loader module
│   │   ├── api/
│   │   │   ├── Loader.java                 # Common interface
│   │   │   ├── LoaderConfiguration.java    # Configuration POJO
│   │   │   ├── TargetTable.java            # Target table descriptor
│   │   │   └── exceptions/                 # Loader‑specific exceptions
│   │   └── dolphin/               # DolphinDB implementation
│   │       ├── DolphinDBLoader.java       # Main implementation
│   │       ├── DolphinDBConnection.java   # Connection wrapper
│   │       ├── DolphinDBScriptExecutor.java  # Script execution
│   │       └── sort/               # External sorting utilities
│   └── ... existing packages (cli, context, workflow, subprocess, etc.)
├── src/test/java/com/sdd/etl/loader/      # Unit tests
├── specs/005-dolphindb-loader/
│   ├── spec.md              # Detailed specification
│   ├── plan.md              # Implementation plan
│   ├── research.md          # DolphinDB API research
│   ├── data-model.md        # Data model definitions
│   ├── quickstart.md        # This file
│   └── contracts/
│       ├── loader-api.md    # Common Loader API contract
│       └── dolphindb-loader-api.md  # DolphinDB‑specific contract
└── pom.xml                  # Maven configuration (updated with DolphinDB dependency)
```

## Running Tests

### Run All Tests

```bash
mvn test
```

### Run Loader‑Specific Tests

```bash
mvn test -Dtest=*Loader*Test
```

### View Test Coverage Report

```bash
mvn clean test jacoco:report
# Open target/site/jacoco/index.html in browser
```

**Expected Coverage**: >60% (per constitution requirement)

## Configuration

### INI File Section

Add a `[loader]` section to your `.etlconfig.ini`:

```ini
[loader]
connection.url = localhost:8848
connection.username = admin
connection.password = 123456
sort.fields = date,symbol
max.memory.mb = 256
temporary.table.prefix = temp_
```

- **connection.url**: DolphinDB server host:port (default 8848)
- **connection.username**, **connection.password**: Authentication credentials
- **sort.fields**: Comma‑separated field names to sort by before loading (e.g., `date,symbol`). Records with null values in any sort field are skipped.
- **max.memory.mb**: Maximum memory (MB) for in‑memory sorting; if exceeded, external (disk‑based) sorting is used.
- **temporary.table.prefix**: Prefix for temporary table names (immutable; generated once per ETL run).

### Target Configuration

Target tables are defined in the existing `[targets]` section. Each target must have a `dataType` property (e.g., `quote`, `trade`) that determines loading order.

Example:
```ini
[targets]
count=2

[target1]
name=dolphindb_quote
type=dolphindb
dataType=quote
connectionString=localhost:8848
schemaTable=quote_schema

[target2]
name=dolphindb_trade
type=dolphindb
dataType=trade
connectionString=localhost:8848
schemaTable=trade_schema
```

## Execution

### 1. Prepare DolphinDB Environment

Ensure DolphinDB server is running and the target tables (or their schemas) exist. The loader will create temporary tables automatically.

### 2. Run ETL Process with DolphinDB Loader

```bash
java -jar target/etl-tool-1.0.0-jar-with-dependencies.jar \
  --from 20250101 \
  --to 20250107 \
  --config .etlconfig.ini
```

**Expected Output**:
```
ETL Tool v1.0.0
Starting ETL process...
  From: 20250101
  To:  20250107
  Config: .etlconfig.ini

Processing date: 20250101
  [EXTRACT]   Success (1000 records)
  [TRANSFORM] Success (1000 records)
  [LOAD]      Creating temporary tables...
  [LOAD]      Sorting data by [date, symbol]...
  [LOAD]      Loading quote data...
  [LOAD]      Loading trade data...
  [LOAD]      Validating load...
  [LOAD]      Cleaning up temporary tables...
  [LOAD]      Success (1000 records to 2 targets)
  [VALIDATE]  Success (all rules passed)
  [CLEAN]     Success
  Result: Success

... (remaining days) ...

ETL Process Completed
  Total Days: 7
  Successful: 7
  Failed: 0
  Duration: 00:05:23
```

### 3. Verify Data in DolphinDB

Connect to DolphinDB and query the loaded tables:

```python
# DolphinDB script
select count(*) from quote where date between 2025.01.01 : 2025.01.07;
select count(*) from trade where date between 2025.01.01 : 2025.01.07;
```

## Error Scenarios

### 1. DolphinDB Server Unavailable

**Symptoms**: Connection timeout during `init()`.
**Error Message**:
```
Error: Unable to connect to DolphinDB server.
URL: localhost:8848
Details: Connection refused (Connection refused)
Suggested Action: Verify DolphinDB server is running and reachable.
```

**Exit Code**: 3 (ETL Process Error)

### 2. Invalid Sort Field

**Symptoms**: Sort field not present in TargetDataModel schema.
**Error Message**:
```
Error: Invalid sort field 'symbol'.
Available fields: [date, price, volume]
Suggested Action: Update sort.fields in configuration.
```

**Exit Code**: 3

### 3. Memory Limit Exceeded (External Sorting)

**Behavior**: Loader automatically switches to disk‑based sorting and logs a warning.
**Log Message**:
```
WARN  ExternalSorter - Data size (300 MB) exceeds memory limit (256 MB). Switching to external sort.
INFO  ExternalSorter - Using temporary directory: /tmp/etl_sort_12345
```

Process continues normally.

### 4. Partial Loading Failure

**Scenario**: Quote data loads successfully, trade data fails (e.g., duplicate key).
**Behavior**: Loader stops loading further data, leaves temporary tables intact, throws `LoadingException`.
**Error Message**:
```
Error: Failed to load data into table 'trade'.
Target table: trade
Temporary table: temp_trade_20250101_123456
Details: Duplicate primary key value: 12345
Suggested Action: Investigate temporary table data, fix duplicates, restart ETL.
```

**Exit Code**: 3

### 5. Null Values in Sort Fields

**Behavior**: Records with null values in any configured sort field are skipped (logged as warning).
**Log Message**:
```
WARN  DolphinDBLoader - Skipped 5 records with null sort field(s). Records discarded.
```

## Development Workflow

### 1. Implement the Common Loader API

1. Read the API contract: `specs/005-dolphindb-loader/contracts/loader-api.md`
2. Create interface `com.sdd.etl.loader.api.Loader` and supporting classes.
3. Write unit tests for the interface (mock implementations).
4. Ensure the API is extensible for future target systems.

### 2. Implement DolphinDB Loader

1. Read the DolphinDB‑specific contract: `specs/005-dolphindb-loader/contracts/dolphindb-loader-api.md`
2. Study the research document: `specs/005-dolphindb-loader/research.md`
3. Implement `DolphinDBLoader` following the lifecycle:
   - `init()` – establish connection using DolphinDB Java API.
   - `createTemporaryTables()` – execute DolphinDB script to create temporary tables with same schema as target tables.
   - `sortData()` – sort records by configured fields (external sort if needed).
   - `loadData()` – for each data type, insert sorted data from temporary tables into target tables using `tableInsert`.
   - `validateLoad()` – compare row counts between temporary and target tables.
   - `cleanupTemporaryTables()` – drop temporary tables.
   - `shutdown()` – close connection.
4. Write integration tests that require a running DolphinDB instance (use testcontainers or a dedicated test server).

### 3. Integrate with Existing Subprocesses

1. Update `LoadSubprocess` to instantiate `DolphinDBLoader` (via a factory) and call its methods in sequence.
2. Update `CleanSubprocess` to call `cleanupTemporaryTables()` and `shutdown()`.
3. Ensure exceptions propagate correctly (break the ETL process).

### 4. Update Maven Configuration

Add DolphinDB Java API dependency to `pom.xml`:

```xml
<dependency>
    <groupId>com.dolphindb</groupId>
    <artifactId>dolphindb-java-api</artifactId>
    <version>2.00.0</version>
    <scope>system</scope>
    <systemPath>${basedir}/lib/dolphindb-java-api.jar</systemPath>
</dependency>
```

Place the JAR file in the `lib/` directory (create if missing).

## Manual Testing Checklist

- [ ] CLI starts ETL process with loader configuration
- [ ] Loader initializes successfully with valid DolphinDB connection
- [ ] Temporary tables are created with correct schema
- [ ] Data is sorted by configured fields (in‑memory)
- [ ] Data is sorted externally when memory limit exceeded
- [ ] Records with null sort fields are skipped
- [ ] Data loads sequentially by data type (quote before trade)
- [ ] Row‑count validation passes after successful load
- [ ] Temporary tables are dropped after validation
- [ ] Loader shuts down and releases connection
- [ ] Loading exception breaks ETL process (no further dates processed)
- [ ] Temporary tables remain on failure for forensic analysis
- [ ] Error messages are descriptive and suggest actionable steps

## Troubleshooting

### Build Fails Due to Missing DolphinDB JAR

**Solution**: Download `dolphindb-java-api.jar` from DolphinDB website and place it in `lib/` directory.

### Connection Refused

**Solution**: Verify DolphinDB server is running and the port (default 8848) is accessible.

### Temporary Table Already Exists

**Solution**: The loader uses immutable temporary table names per run. Ensure no stale temporary tables exist (drop them manually in DolphinDB).

### Sorting Performance Poor

**Solution**: Adjust `max.memory.mb` to allow more in‑memory sorting, or optimize disk I/O (use SSD, increase buffer size).

### Validation Fails (Row Count Mismatch)

**Solution**: Check for duplicate keys or data corruption in temporary tables. Examine DolphinDB logs for constraint violations.

## Next Steps

After implementing the DolphinDB loader:

1. **Add more target systems** (MySQL, PostgreSQL) by implementing additional `Loader` implementations.
2. **Enhance sorting** with multi‑threaded external sort.
3. **Add incremental loading** support (update existing records).
4. **Improve monitoring** with metrics on loading speed, memory usage, and error rates.

## Support

For issues or questions:

1. Check log file (`etl.log`) for detailed error messages.
2. Review feature specification: `specs/005-dolphindb-loader/spec.md`.
3. Review API contracts: `specs/005-dolphindb-loader/contracts/`.
4. Contact development team.

## Source

- Feature Specification: `specs/005-dolphindb-loader/spec.md`
- Implementation Plan: `specs/005-dolphindb-loader/plan.md`
- Research: `specs/005-dolphindb-loader/research.md`
- Data Model: `specs/005-dolphindb-loader/data-model.md`
- API Contracts: `specs/005-dolphindb-loader/contracts/*`
- Scope Definition: `docs/v6/Plan.md`