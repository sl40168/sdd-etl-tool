# Quick Start Guide: ETL Core Workflow

**Feature Branch**: `001-etl-core-workflow`
**Date**: 2026-01-08
**Status**: Phase 1 Design

## Overview

This guide provides a quick start for implementing and testing the ETL Core Workflow feature. It covers building the project, running tests, and executing the CLI tool.

---

## Prerequisites

- **Java 8** (JDK 1.8.0_xxx)
- **Maven 3.6+**
- **Git** (for version control)

---

## Project Setup

### 1. Clone Repository

```bash
git clone <repository-url>
cd sdd-etl-tool
git checkout 001-etl-core-workflow
```

---

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

---

### 3. Verify Build

```bash
# Check that JARs were created
# Windows (PowerShell)
dir target\etl-tool-1.0.0.jar
dir target\etl-tool-1.0.0-jar-with-dependencies.jar

# Linux/Mac
ls target/etl-tool-1.0.0.jar
ls target/etl-tool-1.0.0-jar-with-dependencies.jar
```

---

## Project Structure

```
sdd-etl-tool/
├── src/
│   ├── main/java/com/sdd/etl/
│   │   ├── cli/
│   │   │   ├── ETLCommandLine.java          # CLI entry point
│   │   │   └── CommandLineValidator.java    # Input validation
│   │   ├── context/
│   │   │   ├── ETLContext.java              # Context implementation
│   │   │   ├── ContextManager.java          # Context lifecycle
│   │   │   └── ContextConstants.java        # Context keys
│   │   ├── workflow/
│   │   │   ├── DailyETLWorkflow.java       # Daily orchestration
│   │   │   ├── SubprocessExecutor.java      # Subprocess sequencing
│   │   │   └── WorkflowEngine.java          # Multi-day coordinator
│   │   ├── subprocess/
│   │   │   ├── ExtractSubprocess.java       # API only (no impl)
│   │   │   ├── TransformSubprocess.java     # API only (no impl)
│   │   │   ├── LoadSubprocess.java          # API only (no impl)
│   │   │   ├── ValidateSubprocess.java      # API only (no impl)
│   │   │   └── CleanSubprocess.java         # API only (no impl)
│   │   ├── model/
│   │   │   ├── SourceDataModel.java         # API only (no impl)
│   │   │   └── TargetDataModel.java         # API only (no impl)
│   │   ├── config/
│   │   │   ├── ConfigurationLoader.java     # INI loader
│   │   │   └── ETConfiguration.java        # Config POJO
│   │   └── logging/
│   │       ├── ETLogger.java                # Logging facade
│   │       └── StatusLogger.java            # Status logging
│   ├── main/resources/
│   │   └── logback.xml                     # Logging config
│   └── test/java/com/sdd/etl/              # Unit tests
│       ├── cli/
│       ├── context/
│       ├── workflow/
│       └── config/
├── specs/001-etl-core-workflow/             # Feature specs
│   ├── plan.md
│   ├── research.md
│   ├── data-model.md
│   ├── quickstart.md
│   └── contracts/
│       ├── cli-api.md
│       ├── context-api.md
│       ├── workflow-api.md
│       ├── subprocess-api.md
│       └── datamodel-api.md
├── pom.xml                                 # Maven configuration
├── README.md                               # Project documentation
└── .etlconfig.ini.example                 # Example config
```

---

## Running Tests

### Run All Tests

```bash
mvn test
```

### Run Specific Test Class

```bash
mvn test -Dtest=ETLCommandLineTest
```

### Run Specific Test Method

```bash
mvn test -Dtest=ETLCommandLineTest#testValidateArguments
```

### View Test Coverage Report

```bash
mvn clean test jacoco:report
# Open target/site/jacoco/index.html in browser
```

**Expected Coverage**: >60% (per constitution requirement)

---

## Running the ETL Tool

### 1. Create Configuration File

Create a file named `.etlconfig.ini` (copy from `.etlconfig.ini.example`):

```ini
[sources]
# Data source configurations
count=2

[source1]
name=database_source
type=database
connectionString=jdbc:mysql://localhost:3306/etl_db?user=root&password=secret
primaryKeyField=id
extractQuery=SELECT * FROM customers WHERE date = '{date}'

[source2]
name=api_source
type=api
connectionString=https://api.example.com/data?date={date}
primaryKeyField=customer_id

[targets]
# Data target configurations
count=2

[target1]
name=database_target
type=database
connectionString=jdbc:postgresql://localhost:5432/etl_db?user=etl&password=secret
batchSize=1000

[target2]
name=api_target
type=api
connectionString=https://target-api.example.com/load
batchSize=500

[transformations]
# Transformation rules
count=1

[transformation1]
name=map_fields
sourceField=customer_name
targetField=name
transformType=map

[validation]
# Validation rules
count=2

[rule1]
name=not_null
field=id
ruleType=not_null

[rule2]
name=email_format
field=email
ruleType=pattern
ruleValue=^[^@]+@[^@]+$

[logging]
logFilePath=./etl.log
logLevel=INFO
```

---

### 2. Execute ETL Process

#### Single-Day Execution

```bash
java -jar target/etl-tool-1.0.0-jar-with-dependencies.jar --from 20250101 --to 20250101 --config .etlconfig.ini
```

**Expected Output**:
```
ETL Tool v1.0.0
Starting ETL process...
  From: 20250101
  To:  20250101
  Config: .etlconfig.ini

Processing date: 20250101
  [EXTRACT]   Success (1000 records)
  [TRANSFORM] Success (1000 records)
  [LOAD]      Success (1000 records to 2 targets)
  [VALIDATE]  Success (all rules passed)
  [CLEAN]     Success
  Result: Success

ETL Process Completed
  Total Days: 1
  Successful: 1
  Failed: 0
  Duration: 00:00:05
```

#### Multi-Day Execution

```bash
java -jar target/etl-tool-1.0.0-jar-with-dependencies.jar --from 20250101 --to 20250107 --config .etlconfig.ini
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
  [LOAD]      Success (1000 records to 2 targets)
  [VALIDATE]  Success (all rules passed)
  [CLEAN]     Success
  Result: Success

Processing date: 20250102
  [EXTRACT]   Success (1050 records)
  [TRANSFORM] Success (1050 records)
  [LOAD]      Success (1050 records to 2 targets)
  [VALIDATE]  Success (all rules passed)
  [CLEAN]     Success
  Result: Success

... (days 20250103 through 20250107) ...

ETL Process Completed
  Total Days: 7
  Successful: 7
  Failed: 0
  Duration: 00:05:23
```

---

### 3. View Help

```bash
java -jar target/etl-tool-1.0.0-jar-with-dependencies.jar --help
```

**Expected Output**:
```
ETL Tool - Extract, Transform, Load data across multiple dates

Usage:
  java -jar etl-tool-1.0.0-jar-with-dependencies.jar --from <YYYYMMDD> --to <YYYYMMDD> --config <path>

Required Parameters:
  --from <YYYYMMDD>    Inclusive start date (format: YYYYMMDD)
  --to <YYYYMMDD>      Inclusive end date (format: YYYYMMDD)
  --config <path>      Absolute path to INI configuration file

Optional Parameters:
  --help               Display this help message

Examples:
  java -jar etl-tool-1.0.0-jar-with-dependencies.jar --from 20250101 --to 20250107 --config /path/to/config.ini
  java -jar etl-tool-1.0.0-jar-with-dependencies.jar --help

Exit Codes:
  0 - Success
  1 - Input validation error
  2 - Concurrent execution detected
  3 - ETL process error
  4 - Configuration error
  5 - Unexpected error
```

---

## Error Scenarios

### 1. Invalid Date Format

```bash
java -jar target/etl-tool-1.0.0-jar-with-dependencies.jar --from 2025-01-01 --to 20250107 --config .etlconfig.ini
```

**Expected Output**:
```
Error: Invalid date format for --from/--to parameter.
Expected format: YYYYMMDD (e.g., 20250101)
Provided value: 2025-01-01
```

**Exit Code**: 1

---

### 2. Invalid Date Range

```bash
java -jar target/etl-tool-1.0.0-jar-with-dependencies.jar --from 20250107 --to 20250101 --config .etlconfig.ini
```

**Expected Output**:
```
Error: Invalid date range.
From date (20250107) must be before or equal to To date (20250101).
```

**Exit Code**: 1

---

### 3. Missing Configuration File

```bash
java -jar target/etl-tool-1.0.0-jar-with-dependencies.jar --from 20250101 --to 20250107 --config /nonexistent/config.ini
```

**Expected Output**:
```
Error: Configuration file not found or not readable.
Path: /nonexistent/config.ini
```

**Exit Code**: 1

---

### 4. Concurrent Execution Detection

```bash
# Terminal 1
java -jar target/etl-tool-1.0.0-jar-with-dependencies.jar --from 20250101 --to 20250107 --config .etlconfig.ini

# Terminal 2 (while Terminal 1 is still running)
java -jar target/etl-tool-1.0.0-jar-with-dependencies.jar --from 20250108 --to 20250114 --config .etlconfig.ini
```

**Expected Output (Terminal 2)**:
```
Error: Another ETL process is already running.
Please wait for it to complete before starting a new process.
Lock file: <path>/.etl.lock
```

**Exit Code**: 2

---

### 5. ETL Process Failure

```bash
# Configuration has unreachable source
java -jar target/etl-tool-1.0.0-jar-with-dependencies.jar --from 20250101 --to 20250107 --config .etlconfig.ini
```

**Expected Output**:
```
Processing date: 20250101
  [EXTRACT]   Failed: Connection timeout
  Result: Failed
Error: Day 20250101 failed during Extract subprocess.
Details: Unable to connect to source: database_source

Suggested Action: Check network connectivity and source credentials.
Process stopped. No further dates processed.
```

**Exit Code**: 3

---

## Log Files

### Console Log

All status messages are printed to console in real-time.

### File Log

Detailed logs are written to the file specified in configuration (default: `./etl.log`).

**Example Log Content**:
```
2025-01-08 10:00:00.000 INFO  ETLCommandLine - ETL Tool v1.0.0 starting
2025-01-08 10:00:00.100 INFO  ETLCommandLine - From: 20250101, To: 20250107, Config: .etlconfig.ini
2025-01-08 10:00:00.200 INFO  WorkflowEngine - Processing 7 days
2025-01-08 10:00:00.300 INFO  DailyETLWorkflow - Starting day: 20250101
2025-01-08 10:00:01.000 INFO  StatusLogger - [20250101] EXTRACT Success (1000 records)
2025-01-08 10:00:02.000 INFO  StatusLogger - [20250101] TRANSFORM Success (1000 records)
2025-01-08 10:00:03.000 INFO  StatusLogger - [20250101] LOAD Success (1000 records to 2 targets)
2025-01-08 10:00:04.000 INFO  StatusLogger - [20250101] VALIDATE Success (all rules passed)
2025-01-08 10:00:04.500 INFO  StatusLogger - [20250101] CLEAN Success
...
2025-01-08 10:05:23.000 INFO  WorkflowEngine - All days processed: 7 successful, 0 failed
2025-01-08 10:05:23.100 INFO  ETLCommandLine - ETL Process Completed in 00:05:23
```

---

## Development Workflow

### 1. Implement a Feature

1. Read the feature specification: `specs/001-etl-core-workflow/spec.md`
2. Read the implementation plan: `specs/001-etl-core-workflow/plan.md`
3. Read the relevant API contracts in `specs/001-etl-core-workflow/contracts/`
4. Implement the feature following TDD approach
5. Write unit tests before writing implementation code
6. Run tests to ensure they pass
7. Run full build: `mvn clean install`

---

### 2. Debugging

1. Enable DEBUG logging in `logback.xml`
2. Run with verbose output: `java -jar etl-tool-1.0.0-jar-with-dependencies.jar --from 20250101 --to 20250101 --config .etlconfig.ini`
3. Check log file: `etl.log`
4. Use context snapshots for debugging (logged on error)

---

### 3. Manual Testing Checklist

- [ ] CLI accepts valid parameters and starts ETL process
- [ ] CLI rejects invalid date formats with clear error message
- [ ] CLI rejects invalid date ranges (from > to)
- [ ] CLI rejects missing configuration files with clear error message
- [ ] Help command displays usage information
- [ ] Multi-day execution processes days in sequence
- [ ] Single-day execution completes successfully
- [ ] Each subprocess executes in strict sequence
- [ ] Subprocess failure stops the day's process
- [ ] Day failure stops multi-day execution
- [ ] Concurrent execution is detected and rejected
- [ ] Status logs appear on console and file
- [ ] Context is created and passed to subprocesses
- [ ] Data flows through context between subprocesses

---

## Scope Limitations (This Phase)

**Concrete Implementations** (Required):
- ✅ CLI interface (argument parsing, validation, entry point)
- ✅ Context implementation (state management, data transfer)
- ✅ Daily ETL Workflow (orchestration, sequencing)

**API Definitions Only** (No Implementation):
- ❌ ExtractSubprocess - API defined, no concrete implementation
- ❌ TransformSubprocess - API defined, no concrete implementation
- ❌ LoadSubprocess - API defined, no concrete implementation
- ❌ ValidateSubprocess - API defined, no concrete implementation
- ❌ CleanSubprocess - API defined, no concrete implementation
- ❌ SourceDataModel - API defined, no concrete implementation
- ❌ TargetDataModel - API defined, no concrete implementation

**Note**: This phase focuses on establishing the framework (CLI, Context, Workflow). Subprocess and DataModel concrete implementations will be added in future phases.

---

## Troubleshooting

### Build Fails with "Compilation Error"

**Solution**: Ensure Java 8 is installed and configured:
```bash
java -version  # Should show 1.8.0_xxx
mvn -version  # Should show Java 8
```

---

### Tests Fail with "NoClassDefFoundError"

**Solution**: Clean and rebuild:
```bash
mvn clean install
```

---

### Concurrent Execution Lock Not Released

**Solution**: Manually remove lock file:
```bash
rm .etl.lock  # Linux/Mac
del .etl.lock  # Windows
```

---

### Configuration File Not Found

**Solution**: Use absolute path to configuration file:
```bash
java -jar target/etl-tool-1.0.0-jar-with-dependencies.jar --from 20250101 --to 20250107 --config C:\path\to\.etlconfig.ini
```

---

## Next Steps

1. **Implement Subprocesses**: Concrete implementations for Extract, Transform, Load, Validate, Clean
2. **Implement Data Models**: Concrete implementations for Database, API, File sources and targets
3. **Add More Features**: Retry mechanism, incremental updates, distributed processing
4. **Performance Optimization**: Batch processing, parallel extraction, caching

---

## Support

For issues or questions:
1. Check log file (`etl.log`) for detailed error messages
2. Review feature specification: `specs/001-etl-core-workflow/spec.md`
3. Review API contracts: `specs/001-etl-core-workflow/contracts/`
4. Contact development team

---

## Source

- Feature Specification: specs/001-etl-core-workflow/spec.md
- Implementation Plan: specs/001-etl-core-workflow/plan.md
- Data Model: specs/001-etl-core-workflow/data-model.md
- Research: specs/001-etl-core-workflow/research.md
- API Contracts: specs/001-etl-core-workflow/contracts/*
- Scope Definition: docs/v1/Plan.md
