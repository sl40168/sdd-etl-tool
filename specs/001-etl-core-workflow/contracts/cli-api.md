# CLI API Contract

**Feature Branch**: `001-etl-core-workflow`
**Date**: 2026-01-08
**Status**: Phase 1 Design

## Overview

This document defines the CLI interface contract for the ETL tool. All CLI functionality is exposed through command-line arguments with no GUI or web interface.

## Command Structure

### Main Entry Point

**Command**: `java -jar etl-tool.jar [options]`

**Required Parameters**:
- `--from <YYYYMMDD>` - Inclusive start date for ETL processing
- `--to <YYYYMMDD>` - Inclusive end date for ETL processing
- `--config <path>` - Absolute path to INI configuration file

**Optional Parameters**:
- `--help` - Display usage information and exit

---

## Exit Codes

| Code | Meaning | Condition |
|------|---------|-----------|
| 0 | Success | ETL process completed successfully for all days |
| 1 | Input Validation Error | Invalid parameters (format, missing file, date range) |
| 2 | Concurrent Execution | Another ETL process is already running |
| 3 | ETL Process Error | Any subprocess or day failed during execution |
| 4 | Configuration Error | Invalid or corrupted INI configuration file |
| 5 | Unexpected Error | Unhandled exception or system error |

---

## Usage Examples

### 1. Standard Execution

```bash
java -jar etl-tool.jar --from 20250101 --to 20250107 --config /path/to/config.ini
```

**Behavior**:
- Validates all parameters
- Loads configuration from `/path/to/config.ini`
- Processes dates 2025-01-01 through 2025-01-07 (7 days)
- For each day: extract → transform → load → validate → clean
- Logs status to console and log file
- Exits with code 0 if all days succeed

---

### 2. Single-Day Execution

```bash
java -jar etl-tool.jar --from 20250101 --to 20250101 --config /path/to/config.ini
```

**Behavior**:
- Processes only 2025-01-01
- Same subprocess sequence applies
- Logs completion status

---

### 3. Help Command

```bash
java -jar etl-tool.jar --help
```

**Output**:
```
ETL Tool - Extract, Transform, Load data across multiple dates

Usage:
  java -jar etl-tool.jar --from <YYYYMMDD> --to <YYYYMMDD> --config <path>

Required Parameters:
  --from <YYYYMMDD>    Inclusive start date (format: YYYYMMDD)
  --to <YYYYMMDD>      Inclusive end date (format: YYYYMMDD)
  --config <path>      Absolute path to INI configuration file

Optional Parameters:
  --help               Display this help message

Examples:
  java -jar etl-tool.jar --from 20250101 --to 20250107 --config /path/to/config.ini
  java -jar etl-tool.jar --help

Exit Codes:
  0 - Success
  1 - Input validation error
  2 - Concurrent execution detected
  3 - ETL process error
  4 - Configuration error
  5 - Unexpected error
```

---

## Input Validation Rules

### Date Format Validation

**Rule**: Dates must be in YYYYMMDD format.

**Valid Examples**:
- `20250101` - January 1, 2025
- `20251231` - December 31, 2025

**Invalid Examples**:
- `2025-01-01` - Wrong format (dashes)
- `01/01/2025` - Wrong format (slashes)
- `20251301` - Invalid month (13)
- `20250132` - Invalid day (32)

**Error Message**:
```
Error: Invalid date format for --from/--to parameter.
Expected format: YYYYMMDD (e.g., 20250101)
Provided value: <invalid_value>
```

---

### Date Range Validation

**Rule**: `--from` date must be ≤ `--to` date.

**Invalid Example**:
```bash
java -jar etl-tool.jar --from 20250107 --to 20250101 --config /path/to/config.ini
```

**Error Message**:
```
Error: Invalid date range.
From date (20250107) must be before or equal to To date (20250101).
```

---

### Configuration File Validation

**Rule**: Configuration file must exist and be readable.

**Invalid Example**:
```bash
java -jar etl-tool.jar --from 20250101 --to 20250107 --config /nonexistent/path/config.ini
```

**Error Message**:
```
Error: Configuration file not found or not readable.
Path: /nonexistent/path/config.ini
```

---

### Concurrent Execution Detection

**Rule**: Only one ETL process can run at a time.

**Error Message**:
```
Error: Another ETL process is already running.
Please wait for it to complete before starting a new process.
Lock file: <lock_file_path>
```

---

## Console Output Format

### 1. Startup Banner

```
ETL Tool v1.0.0
Starting ETL process...
  From: 20250101
  To:  20250107
  Config: /path/to/config.ini
```

---

### 2. Per-Day Progress

```
Processing date: 20250101
  [EXTRACT]   Success (1000 records)
  [TRANSFORM] Success (1000 records)
  [LOAD]      Success (1000 records to 2 targets)
  [VALIDATE]  Success (all rules passed)
  [CLEAN]     Success
  Result: Success

Processing date: 20250102
  [EXTRACT]   Failed: Connection timeout
  Result: Failed
Error: Day 20250102 failed during Extract subprocess.
Process stopped. No further dates processed.
```

---

### 3. Completion Summary

```
ETL Process Completed
  Total Days: 7
  Successful: 7
  Failed: 0
  Duration: 00:05:23
```

---

### 4. Error Output

```
Error: <error_description>
  Date: <YYYYMMDD>
  Subprocess: <EXTRACT|TRANSFORM|LOAD|VALIDATE|CLEAN>
  Details: <detailed_error_message>

Suggested Action: <action_for_user>
```

---

## Concurrent Execution Locking

**Mechanism**: File lock using `java.nio.channels.FileLock`

**Lock File Location**: `<application_dir>/.etl.lock`

**Behavior**:
1. On startup, attempt to acquire exclusive lock on `.etl.lock`
2. If lock acquired, proceed with ETL process
3. If lock not acquired, display error and exit with code 2
4. Lock automatically released on JVM exit (normal or crash)
5. Manual intervention required if lock file exists after crash

**Recovery After Crash**:
```bash
# User manually removes lock file if stale
rm .etl.lock
# Then retry ETL process
java -jar etl-tool.jar --from 20250101 --to 20250107 --config /path/to/config.ini
```

---

## Logging

**Console Logging**: Always enabled with INFO level by default.

**File Logging**: Enabled by default, path specified in configuration.

**Log Levels**:
- `INFO` - Normal progress updates (subprocess completion, day completion)
- `WARN` - Non-fatal issues (no data extracted, retry not applicable)
- `ERROR` - Process failures (subprocess failure, day failure, system error)

---

## Feature Requirements Mapping

| FR # | Requirement | Contract Coverage |
|------|-------------|-------------------|
| FR-001 | Required CLI parameters | ✅ --from, --to, --config |
| FR-002 | Input validation | ✅ Date format, file existence, date range |
| FR-003 | --help command | ✅ Usage display |
| FR-024 | Concurrent execution detection | ✅ File lock mechanism |

---

## Source

- Feature Specification: FR-001 through FR-003, FR-024, FR-025
- User Stories: US1 (Command Line Execution)
- Edge Cases: Concurrent execution, Invalid date range, Missing/corrupted config
- Clarifications: No retry, manual restart required
