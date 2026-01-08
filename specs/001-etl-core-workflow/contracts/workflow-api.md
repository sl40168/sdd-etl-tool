# Workflow API Contract

**Feature**: ETL Core Workflow  
**Date**: January 8, 2026  
**Purpose**: Define the Workflow API for daily ETL process execution and orchestration

## Overview

This document defines the Workflow API that orchestrates the day-by-day execution of ETL processes. The workflow API includes:
- DailyETLWorkflow: Concrete implementation of daily ETL process orchestration
- ProcessExecutionOrchestrator: Orchestrates day-by-day sequential execution
- ETLDailyProcess API: Interface for daily ETL process (API definition only)

## Workflow Components

### 1. DailyETLWorkflow

**Purpose**: Concrete implementation of daily ETL workflow that executes subprocesses in strict sequence.

**Package**: `com.sdd.etl.workflow`

**Design Pattern**: Strategy Pattern for subprocess execution with pipeline chaining

---

### 2. ProcessExecutionOrchestrator

**Purpose**: Orchestrates day-by-day sequential execution of ETL processes.

**Package**: `com.sdd.etl.workflow`

**Design Pattern**: Iterator Pattern for date range traversal

---

### 3. ETLDailyProcess (API Definition Only)

**Purpose**: Interface for daily ETL process execution. Concrete implementation is provided by DailyETLWorkflow.

**Package**: `com.sdd.etl.api.workflow`

**Note**: This is an API definition only. No concrete implementation required in this phase.

---

## API Contract

### Interface: ETLDailyProcess

```java
package com.sdd.etl.api.workflow;

/**
 * Interface for executing a daily ETL process.
 * 
 * A daily ETL process consists of five subprocesses executed in strict sequence:
 * 1. Extract - Extract data from all configured sources
 * 2. Transform - Transform source data to target data formats
 * 3. Load - Load transformed data to all configured targets
 * 4. Validate - Validate loaded data
 * 5. Clean - Clean up temporary resources
 * 
 * Each subprocess must complete successfully before the next subprocess starts.
 * If any subprocess fails, the entire daily process fails and no further
 * subprocesses are executed.
 * 
 * This interface is implemented by DailyETLWorkflow.
 */
public interface ETLDailyProcess {
    
    /**
     * Executes the daily ETL process for the specified date.
     * 
     * The process executes subprocesses in strict sequence:
     * - Creates context with date and configuration
     * - Executes extract subprocess
     * - Executes transform subprocess
     * - Executes load subprocess
     * - Executes validate subprocess
     * - Executes clean subprocess
     * - Marks context as completed or failed
     * 
     * @param date The date to process
     * @param configuration The ETL configuration
     * @throws SubprocessException if any subprocess fails
     * @throws ETLException if workflow execution fails
     */
    void execute(LocalDate date, Configuration configuration) 
            throws SubprocessException, ETLException;
    
    /**
     * Checks if the daily process completed successfully.
     * 
     * @return true if the process completed successfully, false otherwise
     */
    boolean isCompleted();
    
    /**
     * Gets the context for the last executed daily process.
     * 
     * @return The context, or null if no process has been executed
     */
    DailyProcessContext getContext();
}
```

---

### Class: DailyETLWorkflow

```java
package com.sdd.etl.workflow;

import com.sdd.etl.api.workflow.ETLDailyProcess;
import com.sdd.etl.api.subprocess.*;
import com.sdd.etl.context.DailyProcessContext;
import com.sdd.etl.config.Configuration;
import com.sdd.etl.enums.SubprocessType;
import com.sdd.etl.exception.ETLException;
import com.sdd.etl.exception.SubprocessException;
import com.sdd.etl.logging.ProcessStatusLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Concrete implementation of daily ETL workflow.
 * 
 * This class orchestrates the execution of all subprocesses in strict sequence.
 * It creates a context for the daily process and passes it to each subprocess.
 * 
 * Process Flow:
 * 1. Create context with date and configuration
 * 2. Execute extract subprocess → update context
 * 3. Execute transform subprocess → update context
 * 4. Execute load subprocess → update context
 * 5. Execute validate subprocess → update context
 * 6. Execute clean subprocess → update context
 * 7. Mark context as completed
 * 
 * If any subprocess fails, the process stops immediately and the context
 * is marked as failed. No subsequent subprocesses are executed.
 */
public class DailyETLWorkflow implements ETLDailyProcess {
    
    private static final Logger logger = LoggerFactory.getLogger(DailyETLWorkflow.class);
    
    // Subprocesses in execution order
    private final List<Subprocess> subprocesses;
    
    // Logger for status updates
    private final ProcessStatusLogger statusLogger;
    
    // Context from last execution
    private DailyProcessContext context;
    
    /**
     * Constructs a DailyETLWorkflow with the specified subprocesses.
     * 
     * @param subprocesses List of subprocesses in execution order
     * @param statusLogger Logger for status updates
     * @throws IllegalArgumentException if subprocesses list is null or empty
     */
    public DailyETLWorkflow(List<Subprocess> subprocesses, 
                           ProcessStatusLogger statusLogger) {
        if (subprocesses == null || subprocesses.isEmpty()) {
            throw new IllegalArgumentException(
                "Subprocesses list cannot be null or empty");
        }
        if (statusLogger == null) {
            throw new IllegalArgumentException("Status logger cannot be null");
        }
        
        this.subprocesses = Collections.unmodifiableList(subprocesses);
        this.statusLogger = statusLogger;
        
        logger.info("DailyETLWorkflow initialized with {} subprocesses", 
                   subprocesses.size());
    }
    
    /**
     * Executes the daily ETL process for the specified date.
     * 
     * Implementation of ETLDailyProcess interface.
     * 
     * @param date The date to process
     * @param configuration The ETL configuration
     * @throws SubprocessException if any subprocess fails
     * @throws ETLException if workflow execution fails
     */
    @Override
    public void execute(LocalDate date, Configuration configuration) 
            throws SubprocessException, ETLException {
        logger.info("Starting daily ETL process for date: {}", date);
        
        try {
            // Step 1: Create initial context
            context = createInitialContext(date, configuration);
            statusLogger.logProcessStart(date);
            
            // Step 2-6: Execute subprocesses in strict sequence
            for (Subprocess subprocess : subprocesses) {
                executeSubprocess(subprocess, context);
            }
            
            // Step 7: Mark context as completed
            context = context.markCompleted();
            statusLogger.logProcessComplete(date, context);
            
            logger.info("Daily ETL process completed successfully for date: {}", date);
            
        } catch (SubprocessException e) {
            // Subprocess failed - mark context as failed and rethrow
            context = context.markFailed();
            statusLogger.logProcessFailed(date, context, e);
            logger.error("Daily ETL process failed for date: {}", date, e);
            throw e;
            
        } catch (Exception e) {
            // Unexpected error - wrap in ETLException
            context = context.markFailed();
            statusLogger.logProcessFailed(date, context, e);
            logger.error("Unexpected error during daily ETL process for date: {}", date, e);
            throw new ETLException("Workflow execution failed", e);
        }
    }
    
    /**
     * Creates initial context for the daily process.
     * 
     * @param date The date to process
     * @param configuration The ETL configuration
     * @return Initial context
     */
    private DailyProcessContext createInitialContext(LocalDate date, 
                                                    Configuration configuration) {
        return DailyProcessContext.builder()
            .date(date)
            .configuration(configuration)
            .startTime(LocalDateTime.now())
            .build();
    }
    
    /**
     * Executes a single subprocess and updates the context.
     * 
     * @param subprocess The subprocess to execute
     * @param context The current context
     * @throws SubprocessException if subprocess execution fails
     */
    private void executeSubprocess(Subprocess subprocess, 
                                  DailyProcessContext context) 
            throws SubprocessException {
        
        SubprocessType subprocessType = subprocess.getType();
        logger.debug("Executing subprocess: {}", subprocessType);
        
        // Log subprocess start
        statusLogger.logSubprocessStart(context, subprocessType);
        
        // Update context with current subprocess
        context = context.withSubprocess(subprocessType);
        
        try {
            // Execute subprocess
            DailyProcessContext updatedContext = subprocess.execute(context);
            
            // Update stored context
            this.context = updatedContext;
            
            // Log subprocess completion
            statusLogger.logSubprocessComplete(context, subprocessType);
            logger.debug("Subprocess completed: {}", subprocessType);
            
        } catch (SubprocessException e) {
            // Subprocess execution failed
            logger.error("Subprocess failed: {}", subprocessType, e);
            throw e;
        }
    }
    
    /**
     * Checks if the daily process completed successfully.
     * 
     * @return true if the process completed successfully, false otherwise
     */
    @Override
    public boolean isCompleted() {
        return context != null && 
               context.getCurrentSubprocess() == SubprocessType.COMPLETED;
    }
    
    /**
     * Gets the context for the last executed daily process.
     * 
     * @return The context, or null if no process has been executed
     */
    @Override
    public DailyProcessContext getContext() {
        return context;
    }
}
```

---

### Class: ProcessExecutionOrchestrator

```java
package com.sdd.etl.workflow;

import com.sdd.etl.api.workflow.ETLDailyProcess;
import com.sdd.etl.config.Configuration;
import com.sdd.etl.exception.ConcurrentExecutionException;
import com.sdd.etl.exception.ETLException;
import com.sdd.etl.logging.ConcurrentExecutionDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.LocalDate;

/**
 * Orchestrates day-by-day sequential execution of ETL processes.
 * 
 * This class:
 * - Validates date range (from <= to)
 * - Detects concurrent execution (prevents multiple ETL processes from running)
 * - Iterates through date range from 'from' date to 'to' date (inclusive)
 * - Executes daily ETL process for each date
 * - Stops execution if any day's process fails
 * 
 * The orchestrator ensures that each day's process completes successfully
 * before the next day's process starts. This maintains data integrity
 * and provides traceability.
 */
public class ProcessExecutionOrchestrator {
    
    private static final Logger logger = LoggerFactory.getLogger(
        ProcessExecutionOrchestrator.class);
    
    // Daily ETL process executor
    private final ETLDailyProcess dailyProcess;
    
    // Concurrent execution detector
    private final ConcurrentExecutionDetector executionDetector;
    
    /**
     * Constructs a ProcessExecutionOrchestrator.
     * 
     * @param dailyProcess The daily ETL process to execute
     * @param executionDetector Detector for concurrent execution
     * @throws IllegalArgumentException if any parameter is null
     */
    public ProcessExecutionOrchestrator(ETLDailyProcess dailyProcess,
                                      ConcurrentExecutionDetector executionDetector) {
        if (dailyProcess == null) {
            throw new IllegalArgumentException("Daily process cannot be null");
        }
        if (executionDetector == null) {
            throw new IllegalArgumentException("Execution detector cannot be null");
        }
        
        this.dailyProcess = dailyProcess;
        this.executionDetector = executionDetector;
        
        logger.info("ProcessExecutionOrchestrator initialized");
    }
    
    /**
     * Executes the ETL process for the specified date range.
     * 
     * Execution Flow:
     * 1. Validate date range (from <= to)
     * 2. Detect concurrent execution (fail if another process is running)
     * 3. Acquire execution lock
     * 4. Iterate through date range
     *    - Execute daily process for each date
     *    - Stop if any day fails
     * 5. Release execution lock
     * 
     * @param fromDate Inclusive start date
     * @param toDate Inclusive end date
     * @param configuration The ETL configuration
     * @param configPath Path to configuration file (for lock file naming)
     * @throws ConcurrentExecutionException if another ETL process is running
     * @throws ETLException if date range is invalid or execution fails
     */
    public void execute(LocalDate fromDate, LocalDate toDate, 
                     Configuration configuration, Path configPath) 
            throws ConcurrentExecutionException, ETLException {
        
        logger.info("Starting ETL process execution: {} to {}", fromDate, toDate);
        
        // Step 1: Validate date range
        validateDateRange(fromDate, toDate);
        
        // Step 2: Detect and prevent concurrent execution
        executionDetector.detectAndPreventConcurrentExecution();
        
        // Step 3: Acquire execution lock (managed by detector)
        AutoCloseable lock = null;
        try {
            lock = executionDetector.acquireExecutionLock();
            
            // Step 4: Execute daily processes in sequence
            executeDateRange(fromDate, toDate, configuration);
            
            logger.info("ETL process execution completed successfully");
            
        } catch (ConcurrentExecutionException e) {
            // Concurrent execution detected
            logger.error("Concurrent execution detected: {}", e.getMessage());
            throw e;
            
        } catch (ETLException e) {
            // ETL execution failed
            logger.error("ETL process execution failed: {}", e.getMessage(), e);
            throw e;
            
        } finally {
            // Step 5: Release execution lock
            if (lock != null) {
                try {
                    lock.close();
                    logger.debug("Execution lock released");
                } catch (Exception e) {
                    logger.warn("Failed to release execution lock", e);
                }
            }
        }
    }
    
    /**
     * Validates the date range.
     * 
     * @param fromDate Inclusive start date
     * @param toDate Inclusive end date
     * @throws ETLException if date range is invalid
     */
    private void validateDateRange(LocalDate fromDate, LocalDate toDate) 
            throws ETLException {
        if (fromDate == null || toDate == null) {
            throw new ETLException("From and to dates are required");
        }
        
        if (fromDate.isAfter(toDate)) {
            throw new ETLException(
                String.format("From date (%s) must be before or equal to to date (%s)",
                            fromDate, toDate));
        }
        
        logger.debug("Date range validated: {} to {}", fromDate, toDate);
    }
    
    /**
     * Executes the ETL process for each date in the date range.
     * 
     * @param fromDate Inclusive start date
     * @param toDate Inclusive end date
     * @param configuration The ETL configuration
     * @throws ETLException if any day's process fails
     */
    private void executeDateRange(LocalDate fromDate, LocalDate toDate, 
                                 Configuration configuration) 
            throws ETLException {
        
        LocalDate currentDate = fromDate;
        int dayCount = 0;
        int successCount = 0;
        
        while (!currentDate.isAfter(toDate)) {
            dayCount++;
            logger.info("Processing day {}/{}: {}", dayCount, 
                       dayCount + (int) toDate.until(fromDate).getDays(), currentDate);
            
            try {
                // Execute daily process
                dailyProcess.execute(currentDate, configuration);
                successCount++;
                
                logger.info("Day {} processed successfully", currentDate);
                
            } catch (Exception e) {
                // Daily process failed - stop execution
                logger.error("Day {} failed - stopping execution: {}", 
                            currentDate, e.getMessage(), e);
                throw new ETLException(
                    String.format("Failed to process date %s (day %d of %d)",
                                currentDate, dayCount, dayCount + 
                                (int) toDate.until(fromDate).getDays()), 
                    e);
            }
            
            // Move to next day
            currentDate = currentDate.plusDays(1);
        }
        
        logger.info("All days processed successfully: {}/{} days", 
                   successCount, dayCount);
    }
}
```

---

## Usage Examples

### Creating and Executing Daily ETL Workflow

```java
// Create subprocesses (API definitions, not implementations in this phase)
List<Subprocess> subprocesses = Arrays.asList(
    extractProcess,    // API only
    transformProcess,  // API only
    loadProcess,       // API only
    validateProcess,   // API only
    cleanProcess       // API only
);

// Create status logger
ProcessStatusLogger statusLogger = new ProcessStatusLogger(config);

// Create daily workflow
DailyETLWorkflow workflow = new DailyETLWorkflow(subprocesses, statusLogger);

// Execute for a single date
try {
    workflow.execute(LocalDate.of(2025, 1, 1), configuration);
    if (workflow.isCompleted()) {
        logger.info("Process completed successfully");
        DailyProcessContext context = workflow.getContext();
        logger.info("Records: extracted={}, transformed={}, loaded={}",
                   context.getRecordsExtracted(),
                   context.getRecordsTransformed(),
                   context.getRecordsLoaded());
    }
} catch (SubprocessException e) {
    logger.error("Subprocess failed: {}", e.getMessage());
} catch (ETLException e) {
    logger.error("Workflow failed: {}", e.getMessage());
}
```

### Orchestrating Multi-Day Execution

```java
// Create daily workflow
DailyETLWorkflow dailyProcess = new DailyETLWorkflow(subprocesses, statusLogger);

// Create concurrent execution detector
ConcurrentExecutionDetector detector = new ConcurrentExecutionDetector();

// Create orchestrator
ProcessExecutionOrchestrator orchestrator = new ProcessExecutionOrchestrator(
    dailyProcess, detector);

// Execute for date range
try {
    orchestrator.execute(
        LocalDate.of(2025, 1, 1),  // from
        LocalDate.of(2025, 1, 31), // to
        configuration,
        Paths.get("/path/to/config.ini")
    );
    logger.info("All days processed successfully");
} catch (ConcurrentExecutionException e) {
    logger.error("Another ETL process is already running");
} catch (ETLException e) {
    logger.error("ETL process failed: {}", e.getMessage());
}
```

---

## Testing Requirements

### Unit Tests

- **DailyETLWorkflowTest**: Test subprocess execution in sequence
- **ProcessExecutionOrchestratorTest**: Test date range iteration and execution
- **ProcessExecutionOrchestratorTest**: Test concurrent execution detection

### Integration Tests

- **ETLWorkflowIntegrationTest**: Test full workflow execution with subprocesses

### Edge Cases to Test

- Invalid date ranges (from > to)
- Single-day date range (from == to)
- Multi-month date ranges (crossing month boundaries)
- Multi-year date ranges (crossing year boundaries)
- Leap day dates (February 29)
- Subprocess failures at different stages
- Concurrent execution attempts
- Empty subprocesses list
- Null configuration
- Interrupted execution (exception in middle of date range)

---

## State Transition Diagram

```
DailyProcessContext State Transitions:

NOT_STARTED
    ↓
EXTRACT (extract subprocess completes)
    ↓
TRANSFORM (transform subprocess completes)
    ↓
LOAD (load subprocess completes)
    ↓
VALIDATE (validate subprocess completes)
    ↓
CLEAN (clean subprocess completes)
    ↓
COMPLETED (all subprocesses completed)

FAILED (any subprocess fails at any stage)
```

## Execution Flow Diagram

```
ProcessExecutionOrchestrator
    ↓
Validate Date Range
    ↓
Detect Concurrent Execution
    ↓
Acquire Lock
    ↓
For Each Date in Range:
    ↓
    DailyETLWorkflow
        ↓
        Create Context
        ↓
        For Each Subprocess:
            ↓
            Execute Subprocess
            ↓
            Update Context
            ↓
            Log Status
        ↓
        Mark Context Completed
    ↓
    If Failed: Stop Execution
    ↓
Release Lock
```
