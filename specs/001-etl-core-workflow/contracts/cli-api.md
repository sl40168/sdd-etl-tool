# CLI API Contract

**Feature**: ETL Core Workflow  
**Date**: January 8, 2026  
**Purpose**: Define the command-line interface API contracts

## Overview

This document defines the CLI interface API for the ETL tool. The tool exposes a single command with multiple parameters for executing ETL processes.

## Command Specification

### Main Command: `etl`

The main entry point for the ETL tool.

**Usage**: `etl --from <date> --to <date> --config <path> [options]`

### Required Parameters

| Parameter | Type | Format | Description |
|-----------|------|--------|-------------|
| `--from` | String | YYYYMMDD | Inclusive start date for ETL process |
| `--to` | String | YYYYMMDD | Inclusive end date for ETL process |
| `--config` | Path | File path | Path to INI configuration file |

### Optional Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `--help` | Flag | N/A | Display help message and exit |
| `--version` | Flag | N/A | Display version information and exit |

## Parameter Validation Rules

### Date Validation (`--from`, `--to`)

**Format**: YYYYMMDD (e.g., 20250101 for January 1, 2025)

**Validation Steps**:
1. Check format is exactly 8 digits
2. Parse as valid date using `DateTimeFormatter.ofPattern("yyyyMMdd")`
3. Validate month is between 01-12
4. Validate day is valid for the given month/year (handles leap years)

**Error Conditions**:
| Condition | Error Message | Example |
|-----------|---------------|----------|
| Not 8 characters | `Error: Date must be in YYYYMMDD format (8 digits)` | `--from 2025-01-01` |
| Invalid month | `Error: Invalid date - month must be between 01 and 12` | `--from 20251301` |
| Invalid day | `Error: Invalid date - day is out of range for month` | `--from 20250132` |
| From date after to date | `Error: From date must be before or equal to to date` | `--from 20250105 --to 20250101` |

**Valid Examples**:
- `--from 20250101 --to 20250131` (January 2025)
- `--from 20240229 --to 20240229` (Leap day, single day)
- `--from 20250101 --to 20250101` (Single day)

### Configuration File Validation (`--config`)

**Validation Steps**:
1. Check file path is provided
2. Check file exists at specified path
3. Check file has `.ini` extension (case-insensitive)
4. Check file is readable

**Error Conditions**:
| Condition | Error Message | Example |
|-----------|---------------|----------|
| Not provided | `Error: Configuration file path is required` | Missing `--config` |
| File not found | `Error: Configuration file not found: /path/to/config.ini` | Non-existent file |
| Wrong extension | `Error: Configuration file must be in INI format (.ini extension)` | `--config config.json` |
| File not readable | `Error: Configuration file is not readable: /path/to/config.ini` | Permission denied |

**Valid Examples**:
- `--config /home/etl/etl-config.ini`
- `--config ./config/production.ini`
- `--config config.ini`

## Help Command

### `--help` Flag

Displays usage information and parameter descriptions.

**Output Format**:
```text
ETL Tool - Extract, Transform, Load Data Pipeline

Usage:
  etl --from YYYYMMDD --to YYYYMMDD --config <path> [options]

Required Parameters:
  --from <date>    Inclusive start date in YYYYMMDD format (e.g., 20250101)
  --to <date>      Inclusive end date in YYYYMMDD format (e.g., 20250131)
  --config <path>  Path to INI configuration file

Optional Parameters:
  --help           Display this help message and exit
  --version        Display version information and exit

Examples:
  etl --from 20250101 --to 20250131 --config config.ini
  etl --from 20250201 --to 20250201 --config /path/to/config.ini

For more information, visit: https://github.com/example/sdd-etl-tool
```

## Exit Codes

| Code | Meaning | Description |
|------|---------|-------------|
| 0 | Success | ETL process completed successfully |
| 1 | Parameter Error | Invalid parameters or configuration file |
| 2 | Execution Error | ETL process failed during execution |
| 3 | Concurrent Execution | Another ETL process is already running |

## Behavior Specifications

### Normal Execution Flow

1. **Parse Command Line Arguments**
   - Parse `--from`, `--to`, `--config` parameters
   - If `--help` or `--version` provided, display information and exit with code 0

2. **Validate Parameters**
   - Validate date formats
   - Validate date range (from <= to)
   - Validate configuration file exists and is readable
   - If validation fails, display error message and exit with code 1

3. **Load Configuration**
   - Parse INI configuration file
   - Validate configuration structure
   - If configuration invalid, display error message and exit with code 1

4. **Detect Concurrent Execution**
   - Attempt to acquire file lock
   - If lock acquisition fails, display error and exit with code 3

5. **Execute ETL Process**
   - Iterate through date range from `--from` to `--to`
   - For each date, execute daily ETL workflow
   - If any day's process fails, stop execution and exit with code 2

6. **Cleanup and Exit**
   - Release file lock
   - Log completion status
   - Exit with code 0 if all days completed successfully

### Error Handling

All errors should display:
1. Clear error message
2. Suggested resolution (when applicable)
3. Exit code

**Error Message Format**:
```text
Error: [Description of the error]

Suggestion: [What the user can do to fix it]

Example:
Error: From date must be before or equal to to date (20250105 > 20250101)

Suggestion: Ensure the from date is earlier than or equal to the to date

Usage: etl --from <date> --to <date> --config <path>
  etl --help for more information
```

## API Contract Summary

### Public Methods

The CLI implementation must provide these public methods:

```java
package com.sdd.etl.cli;

/**
 * Main CLI entry point
 */
public class ETLCliCommand {
    
    /**
     * Executes the ETL command
     * @param args Command line arguments
     * @return Exit code
     */
    public int execute(String[] args);
    
    /**
     * Validates command line parameters
     * @param parameters Parsed parameters
     * @throws ParameterValidationException if validation fails
     */
    public void validateParameters(CLIParameters parameters);
    
    /**
     * Displays help message
     */
    public void displayHelp();
    
    /**
     * Displays version information
     */
    public void displayVersion();
}

/**
 * Parsed CLI parameters
 */
public class CLIParameters {
    private LocalDate fromDate;
    private LocalDate toDate;
    private Path configPath;
    private boolean help;
    private boolean version;
    
    // Getters and setters
}
```

## Testing Requirements

### Unit Tests

- **ParameterValidatorTest**: Validate all parameter validation rules
- **HelpCommandTest**: Verify help message format and content
- **ETLCliCommandTest**: Test command execution with valid and invalid inputs

### Integration Tests

- **CLIIntegrationTest**: Test full CLI execution flow with sample configuration

### Edge Cases to Test

- Invalid date formats (wrong length, invalid characters)
- Invalid date ranges (from > to)
- Missing or non-existent configuration file
- Configuration file with wrong extension
- Concurrent execution attempts
- Leap day dates
- Month boundaries
- Year boundaries
