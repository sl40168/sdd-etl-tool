# Workflow API Contract

**Feature Branch**: `001-etl-core-workflow`
**Date**: 2026-01-08
**Status**: Phase 1 Design

## Overview

This document defines the Workflow API for orchestrating the ETL process. The workflow engine manages day-by-day execution and subprocess sequencing.

## API Overview

### Core Interfaces

**Package**: `com.sdd.etl.workflow`

**Primary Classes**:
- `DailyETLWorkflow` - Orchestrates a single day's ETL process
- `SubprocessExecutor` - Executes subprocesses in strict sequence
- `WorkflowEngine` - Manages multi-day date range iteration

---

## DailyETLWorkflow Class

### Purpose

Orchestrates all five subprocesses (extract, transform, load, validate, clean) for a single day's ETL process with strict sequencing and dependency enforcement.

### Class Definition

```java
package com.sdd.etl.workflow;

import com.sdd.etl.context.ETLContext;
import com.sdd.etl.context.SubprocessType;

/**
 * DailyETLWorkflow - Orchestrates a single day's ETL process.
 * Executes subprocesses in strict sequence: extract → transform → load → validate → clean.
 */
public class DailyETLWorkflow {

    private final SubprocessExecutor executor;
    private final ETLogger logger;

    /**
     * Constructor.
     * @param executor SubprocessExecutor instance
     * @param logger Logger instance for status logging
     */
    public DailyETLWorkflow(SubprocessExecutor executor, ETLogger logger);

    /**
     * Executes the ETL workflow for a single day.
     * @param context ETLContext for the day
     * @return DailyProcessResult containing subprocess results and final context state
     * @throws ETLException If any subprocess fails
     */
    public DailyProcessResult execute(ETLContext context) throws ETLException;

    /**
     * Validates that context is in correct state before execution.
     * @param context Context to validate
     * @return true if valid, false otherwise
     */
    private boolean validateInitialState(ETLContext context);

    /**
     * Logs workflow completion status.
     * @param result DailyProcessResult to log
     */
    private void logCompletion(DailyProcessResult result);
}
```

---

## SubprocessExecutor Class

### Purpose

Executes subprocesses in strict sequence, enforcing dependencies and validating context state between steps.

### Class Definition

```java
package com.sdd.etl.workflow;

import com.sdd.etl.context.ETLContext;
import com.sdd.etl.context.SubprocessType;
import com.sdd.etl.subprocess.*;

/**
 * SubprocessExecutor - Executes ETL subprocesses in strict sequence.
 * Enforces dependency order: extract → transform → load → validate → clean.
 */
public class SubprocessExecutor {

    private final Map<SubprocessType, SubprocessInterface> subprocesses;

    /**
     * Constructor.
     * @param subprocesses Map of subprocess implementations
     */
    public SubprocessExecutor(Map<SubprocessType, SubprocessInterface> subprocesses);

    /**
     * Executes all subprocesses in sequence for a single day.
     * @param context ETLContext for the day
     * @return List of SubprocessResult in execution order
     * @throws ETLException If any subprocess fails
     */
    public List<SubprocessResult> executeAll(ETLContext context) throws ETLException;

    /**
     * Executes a single subprocess with context validation.
     * @param context ETLContext for the day
     * @param subprocessType Type of subprocess to execute
     * @return SubprocessResult from execution
     * @throws ETLException If subprocess fails
     */
    public SubprocessResult execute(ETLContext context, SubprocessType subprocessType) throws ETLException;

    /**
     * Validates context state before subprocess execution.
     * @param context Context to validate
     * @param subprocessType Subprocess about to execute
     * @return true if context is valid, false otherwise
     */
    private boolean validateContextBeforeExecution(ETLContext context, SubprocessType subprocessType);

    /**
     * Validates that previous subprocess completed successfully.
     * @param context Context to check
     * @param subprocessType Subprocess about to execute
     * @return true if previous subprocess completed, false otherwise
     */
    private boolean validatePreviousSubprocessCompletion(ETLContext context, SubprocessType subprocessType);
}
```

---

## WorkflowEngine Class

### Purpose

Manages multi-day ETL processing by iterating through date ranges and orchestrating daily workflows.

### Class Definition

```java
package com.sdd.etl.workflow;

import com.sdd.etl.context.ETLContext;
import com.sdd.etl.cli.CommandLineArguments;
import com.sdd.etl.config.ETConfiguration;

/**
 * WorkflowEngine - Manages multi-day ETL processing.
 * Iterates through date ranges and orchestrates daily workflows.
 */
public class WorkflowEngine {

    private final DailyETLWorkflow dailyWorkflow;
    private final ContextManager contextManager;
    private final ETLogger logger;

    /**
     * Constructor.
     * @param dailyWorkflow DailyETLWorkflow instance
     * @param contextManager ContextManager instance
     * @param logger Logger instance
     */
    public WorkflowEngine(DailyETLWorkflow dailyWorkflow, ContextManager contextManager, ETLogger logger);

    /**
     * Executes the ETL workflow for all dates in the range.
     * @param args Command-line arguments
     * @param config Loaded ETL configuration
     * @return WorkflowResult containing daily results and summary
     * @throws ETLException If any day fails
     */
    public WorkflowResult execute(CommandLineArguments args, ETConfiguration config) throws ETLException;

    /**
     * Generates a list of dates from the date range.
     * @param fromDate Start date (YYYYMMDD)
     * @param toDate End date (YYYYMMDD)
     * @return List of dates in YYYYMMDD format
     */
    private List<String> generateDateRange(String fromDate, String toDate);

    /**
     * Executes workflow for a single day.
     * @param date Processing date (YYYYMMDD)
     * @param config ETL configuration
     * @return DailyProcessResult for the day
     */
    private DailyProcessResult executeDay(String date, ETConfiguration config);

    /**
     * Aggregates daily results into final workflow result.
     * @param dailyResults Map of date to DailyProcessResult
     * @param args Original command-line arguments
     * @return WorkflowResult with summary
     */
    private WorkflowResult aggregateResults(Map<String, DailyProcessResult> dailyResults, CommandLineArguments args);

    /**
     * Logs final workflow summary.
     * @param result WorkflowResult to log
     */
    private void logSummary(WorkflowResult result);
}
```

---

## SubprocessInterface (API Definition Only)

### Purpose

Defines the contract that all subprocess implementations must follow. This is an API definition only; concrete implementations are not required in this phase.

### Interface Definition

```java
package com.sdd.etl.subprocess;

import com.sdd.etl.context.ETLContext;

/**
 * SubprocessInterface - Contract for all ETL subprocesses.
 * Concrete implementations: ExtractSubprocess, TransformSubprocess, LoadSubprocess, ValidateSubprocess, CleanSubprocess
 * NOTE: This phase defines the API only; concrete implementations are not required.
 */
public interface SubprocessInterface {

    /**
     * Executes the subprocess with the given context.
     * Subprocess MUST:
     * 1. Read required data from context (per FR-027)
     * 2. Perform subprocess logic
     * 3. Write results to context (per FR-027)
     * 4. Return SubprocessResult
     *
     * @param context ETLContext containing current state
     * @return SubprocessResult with execution status and data count
     * @throws ETLException If subprocess fails
     */
    SubprocessResult execute(ETLContext context) throws ETLException;

    /**
     * Validates that context is in correct state before execution.
     * @param context Context to validate
     * @return true if context is valid, false otherwise
     */
    boolean validateContext(ETLContext context);

    /**
     * Gets the subprocess type.
     * @return SubprocessType for this subprocess
     */
    SubprocessType getType();
}
```

---

## Execution Flow

### 1. Multi-Day Workflow Execution

```
WorkflowEngine.execute()
  │
  ├─> Validate command-line arguments
  ├─> Load configuration from INI file
  ├─> Detect concurrent execution (file lock)
  │
  └─> For each date in range (from → to):
        │
        ├─> Create ETLContext for the date
        ├─> DailyETLWorkflow.execute(context)
        │     │
        │     └─> SubprocessExecutor.executeAll(context)
        │           │
        │           ├─> ExtractSubprocess.execute(context)
        │           │     ├─> Read config from context
        │           │     ├─> Extract data from sources
        │           │     ├─> Write extractedDataCount to context
        │           │     └─> Return success or fail
        │           │
        │           ├─> [Only if Extract success] TransformSubprocess.execute(context)
        │           │     ├─> Read extractedDataCount from context
        │           │     ├─> Transform data
        │           │     ├─> Write transformedDataCount to context
        │           │     └─> Return success or fail
        │           │
        │           ├─> [Only if Transform success] LoadSubprocess.execute(context)
        │           │     ├─> Read transformedDataCount from context
        │           │     ├─> Load data to targets
        │           │     ├─> Write loadedDataCount to context
        │           │     └─> Return success or fail
        │           │
        │           ├─> [Only if Load success] ValidateSubprocess.execute(context)
        │           │     ├─> Read loadedDataCount from context
        │           │     ├─> Validate data
        │           │     ├─> Write validationPassed and errors to context
        │           │     └─> Return success or fail
        │           │
        │           └─> [Only if Validate success] CleanSubprocess.execute(context)
        │                 ├─> Read validationPassed from context
        │                 ├─> Clean up resources
        │                 ├─> Write cleanupPerformed to context
        │                 └─> Return success or fail
        │
        └─> Log day result
              └─> If day failed, stop processing (do not proceed to next day)

  └─> Aggregate all daily results into WorkflowResult
        └─> Log final summary
              └─> Exit with appropriate code
```

---

### 2. Subprocess Dependency Enforcement

**Rule**: Each subprocess must complete successfully before the next subprocess starts.

**Implementation**:
```java
public List<SubprocessResult> executeAll(ETLContext context) throws ETLException {
    List<SubprocessResult> results = new ArrayList<>();

    // Execute in strict order
    SubprocessResult extractResult = execute(context, SubprocessType.EXTRACT);
    results.add(extractResult);

    if (!extractResult.isSuccess()) {
        throw new ETLException("Extract failed", extractResult);
    }

    SubprocessResult transformResult = execute(context, SubprocessType.TRANSFORM);
    results.add(transformResult);

    if (!transformResult.isSuccess()) {
        throw new ETLException("Transform failed", transformResult);
    }

    SubprocessResult loadResult = execute(context, SubprocessType.LOAD);
    results.add(loadResult);

    if (!loadResult.isSuccess()) {
        throw new ETLException("Load failed", loadResult);
    }

    SubprocessResult validateResult = execute(context, SubprocessType.VALIDATE);
    results.add(validateResult);

    if (!validateResult.isSuccess()) {
        throw new ETLException("Validate failed", validateResult);
    }

    SubprocessResult cleanResult = execute(context, SubprocessType.CLEAN);
    results.add(cleanResult);

    if (!cleanResult.isSuccess()) {
        throw new ETLException("Clean failed", cleanResult);
    }

    return results;
}
```

---

### 3. Day Failure Stop Behavior

**Rule**: If a day's process fails, do not proceed to subsequent days.

**Implementation**:
```java
public WorkflowResult execute(CommandLineArguments args, ETConfiguration config) throws ETLException {
    List<String> dates = generateDateRange(args.getFromDate(), args.getToDate());
    Map<String, DailyProcessResult> dailyResults = new LinkedHashMap<>();

    try {
        for (String date : dates) {
            DailyProcessResult result = executeDay(date, config);
            dailyResults.put(date, result);

            if (!result.isSuccess()) {
                // Stop processing, do not proceed to next day
                logger.error("Day " + date + " failed. Stopping workflow.");
                break;
            }
        }
    } catch (ETLException e) {
        // Log error and stop processing
        logger.error("Workflow error: " + e.getMessage());
        throw e;
    }

    return aggregateResults(dailyResults, args);
}
```

---

## Error Handling

### ETLException

**Purpose**: Custom exception for ETL-specific errors.

**Fields**:
- `subprocessType` - Subprocess that failed (if applicable)
- `date` - Processing date (if applicable)
- `rootCause` - Original exception

**Usage**:
```java
throw new ETLException("Extract failed: Connection timeout", SubprocessType.EXTRACT);
```

---

### Failure Propagation

**Behavior**:
1. Subprocess failure → `ETLException` thrown
2. Daily workflow catches exception, logs error, marks day as failed
3. Workflow engine catches exception, stops multi-day processing
4. CLI catches exception, displays error message, exits with code 3

**Recovery**:
- User must manually fix the issue
- User restarts ETL process manually (no automatic retry)
- System does not provide resume capability (out of scope per clarification)

---

## Feature Requirements Mapping

| FR # | Requirement | API Coverage |
|------|-------------|--------------|
| FR-004 | Day-by-day execution | ✅ WorkflowEngine.generateDateRange(), executeDay() |
| FR-005 | Subprocess sequence: extract → transform → load → validate → clean | ✅ SubprocessExecutor.executeAll() |
| FR-006 | Next subprocess only triggered when current completes successfully | ✅ SubprocessExecutor dependency checks |
| FR-007 | Day marked complete only when all subprocesses complete | ✅ DailyETLWorkflow result aggregation |
| FR-022 | Provide component API for each subprocess | ✅ SubprocessInterface (API definition) |
| FR-023 | Provide ETL API for each day's process | ✅ DailyETLWorkflow.execute() |

---

## Source

- Feature Specification: FR-004 through FR-007, FR-022, FR-023
- User Stories: US2 (Daily Process Orchestration), US3 (Subprocess Sequential Execution)
- Data Model: DailyProcessResult, WorkflowResult, SubprocessResult
- Research: Orchestrator Pattern, Context Pattern
