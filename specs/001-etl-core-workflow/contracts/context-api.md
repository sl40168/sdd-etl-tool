# Context API Contract

**Feature**: ETL Core Workflow  
**Date**: January 8, 2026  
**Purpose**: Define the Context API for daily ETL process state management

## Overview

This document defines the Context API that holds runtime state for each day's ETL process. The Context is passed to each subprocess during execution and contains the current day's date, current subprocess status, data counts, and configuration.

**CRITICAL REQUIREMENT**: The Context is the **ONLY** mechanism for data transfer between subprocesses. All subprocesses (Extract, Transform, Load, Validate, Clean) must read from and write to the Context to exchange data. No subprocess should store data externally or access another subprocess's data directly.

## Context Entity

### DailyProcessContext

**Purpose**: Concrete implementation of context that holds runtime state for each day's ETL process.

**Package**: `com.sdd.etl.context`

**Design Pattern**: Immutable with Builder Pattern

---

## API Contract

### Class: DailyProcessContext

```java
package com.sdd.etl.context;

import com.sdd.etl.api.model.SourceDataModel;
import com.sdd.etl.api.model.TargetDataModel;
import com.sdd.etl.config.Configuration;
import com.sdd.etl.enums.SubprocessType;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable context holding runtime state for a daily ETL process.
 *
 * This context is created for each day's ETL process and passed to
 * each subprocess during execution. It contains:
 * - The date being processed
 * - Current subprocess status
 * - Counts of records extracted, transformed, and loaded
 * - Configuration for the ETL process
 * - Start and end times for the process
 * - Data storage for inter-subprocess communication (extractedData, transformedData, tempFiles)
 *
 * Thread-safety: This class is immutable and thus thread-safe.
 * New instances are created for each subprocess state transition.
 *
 * CRITICAL: This Context is the ONLY mechanism for data transfer between subprocesses:
 * - Extract: WRITES extractedData field
 * - Transform: READS extractedData, WRITES transformedData
 * - Load: READS transformedData, UPDATES recordsLoaded
 * - Validate: READS recordsLoaded
 * - Clean: READS tempFiles
 *
 * DO NOT use static variables, files, databases, or any external storage for
 * inter-subprocess data transfer. ALL data MUST flow through this Context object.
 */
public final class DailyProcessContext {
    
    // Required fields
    private final LocalDate date;
    private final SubprocessType currentSubprocess;
    private final Configuration configuration;
    private final LocalDateTime startTime;
    
    // Optional fields (with defaults)
    private final int recordsExtracted;
    private final int recordsTransformed;
    private final int recordsLoaded;
    private final LocalDateTime endTime;

    // Data storage for inter-subprocess communication
    private final List<SourceDataModel> extractedData;  // Populated by Extract, read by Transform
    private final List<TargetDataModel> transformedData; // Populated by Transform, read by Load
    private final List<Path> tempFiles;                  // Populated by Extract/Transform/Load, read by Clean
    
    /**
     * Private constructor - use Builder to create instances
     */
    private DailyProcessContext(Builder builder) {
        this.date = builder.date;
        this.currentSubprocess = builder.currentSubprocess;
        this.configuration = builder.configuration;
        this.startTime = builder.startTime;
        this.recordsExtracted = builder.recordsExtracted;
        this.recordsTransformed = builder.recordsTransformed;
        this.recordsLoaded = builder.recordsLoaded;
        this.endTime = builder.endTime;
        this.extractedData = builder.extractedData != null
            ? Collections.unmodifiableList(new ArrayList<>(builder.extractedData))
            : Collections.emptyList();
        this.transformedData = builder.transformedData != null
            ? Collections.unmodifiableList(new ArrayList<>(builder.transformedData))
            : Collections.emptyList();
        this.tempFiles = builder.tempFiles != null
            ? Collections.unmodifiableList(new ArrayList<>(builder.tempFiles))
            : Collections.emptyList();

        validate();
    }
    
    // ============ GETTERS ============
    
    /**
     * @return The date being processed
     */
    public LocalDate getDate() {
        return date;
    }
    
    /**
     * @return Current subprocess being executed
     */
    public SubprocessType getCurrentSubprocess() {
        return currentSubprocess;
    }
    
    /**
     * @return Configuration for the ETL process
     */
    public Configuration getConfiguration() {
        return configuration;
    }
    
    /**
     * @return Process start time
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    /**
     * @return Count of records extracted from sources
     */
    public int getRecordsExtracted() {
        return recordsExtracted;
    }
    
    /**
     * @return Count of records transformed
     */
    public int getRecordsTransformed() {
        return recordsTransformed;
    }
    
    /**
     * @return Count of records loaded to targets
     */
    public int getRecordsLoaded() {
        return recordsLoaded;
    }
    
    /**
     * @return Process end time, null if not completed
     */
    public LocalDateTime getEndTime() {
        return endTime;
    }

    /**
     * @return Data extracted by Extract subprocess (read by Transform)
     */
    public List<SourceDataModel> getExtractedData() {
        return extractedData;
    }

    /**
     * @return Data transformed by Transform subprocess (read by Load)
     */
    public List<TargetDataModel> getTransformedData() {
        return transformedData;
    }

    /**
     * @return Temporary files created during execution (read by Clean)
     */
    public List<Path> getTempFiles() {
        return tempFiles;
    }
    
    // ============ BUILDER ============
    
    /**
     * Creates a new builder for constructing DailyProcessContext instances.
     * 
     * @return New Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Creates a new builder initialized with values from an existing context.
     * Useful for updating context while preserving existing values.
     * 
     * @param context Existing context to copy values from
     * @return New Builder instance initialized with context values
     */
    public static Builder builder(DailyProcessContext context) {
        return new Builder(context);
    }
    
    /**
     * Builder for DailyProcessContext instances.
     * Follows fluent builder pattern for readable context construction.
     */
    public static final class Builder {
        private LocalDate date;
        private SubprocessType currentSubprocess = SubprocessType.NOT_STARTED;
        private Configuration configuration;
        private LocalDateTime startTime;
        private int recordsExtracted = 0;
        private int recordsTransformed = 0;
        private int recordsLoaded = 0;
        private LocalDateTime endTime;

        // Data storage for inter-subprocess communication
        private List<SourceDataModel> extractedData = new ArrayList<>();
        private List<TargetDataModel> transformedData = new ArrayList<>();
        private List<Path> tempFiles = new ArrayList<>();
        
        private Builder() {
            // Default constructor
        }
        
        private Builder(DailyProcessContext context) {
            this.date = context.date;
            this.currentSubprocess = context.currentSubprocess;
            this.configuration = context.configuration;
            this.startTime = context.startTime;
            this.recordsExtracted = context.recordsExtracted;
            this.recordsTransformed = context.recordsTransformed;
            this.recordsLoaded = context.recordsLoaded;
            this.endTime = context.endTime;
            this.extractedData = new ArrayList<>(context.extractedData);
            this.transformedData = new ArrayList<>(context.transformedData);
            this.tempFiles = new ArrayList<>(context.tempFiles);
        }
        
        /**
         * Sets the date being processed (required).
         * 
         * @param date Date being processed
         * @return This builder for method chaining
         */
        public Builder date(LocalDate date) {
            this.date = date;
            return this;
        }
        
        /**
         * Sets the current subprocess status.
         * Defaults to NOT_STARTED if not set.
         * 
         * @param currentSubprocess Current subprocess type
         * @return This builder for method chaining
         */
        public Builder currentSubprocess(SubprocessType currentSubprocess) {
            this.currentSubprocess = currentSubprocess;
            return this;
        }
        
        /**
         * Sets the configuration (required).
         * 
         * @param configuration ETL configuration
         * @return This builder for method chaining
         */
        public Builder configuration(Configuration configuration) {
            this.configuration = configuration;
            return this;
        }
        
        /**
         * Sets the process start time (required).
         * 
         * @param startTime Process start time
         * @return This builder for method chaining
         */
        public Builder startTime(LocalDateTime startTime) {
            this.startTime = startTime;
            return this;
        }
        
        /**
         * Sets the count of records extracted.
         * Defaults to 0 if not set.
         * 
         * @param recordsExtracted Number of records extracted
         * @return This builder for method chaining
         */
        public Builder recordsExtracted(int recordsExtracted) {
            this.recordsExtracted = recordsExtracted;
            return this;
        }
        
        /**
         * Sets the count of records transformed.
         * Defaults to 0 if not set.
         * 
         * @param recordsTransformed Number of records transformed
         * @return This builder for method chaining
         */
        public Builder recordsTransformed(int recordsTransformed) {
            this.recordsTransformed = recordsTransformed;
            return this;
        }
        
        /**
         * Sets the count of records loaded.
         * Defaults to 0 if not set.
         * 
         * @param recordsLoaded Number of records loaded
         * @return This builder for method chaining
         */
        public Builder recordsLoaded(int recordsLoaded) {
            this.recordsLoaded = recordsLoaded;
            return this;
        }
        
        /**
         * Sets the process end time.
         * Should only be set when process completes.
         *
         * @param endTime Process end time
         * @return This builder for method chaining
         */
        public Builder endTime(LocalDateTime endTime) {
            this.endTime = endTime;
            return this;
        }

        /**
         * Sets the extracted data (populated by Extract subprocess).
         * This data will be read by Transform subprocess.
         *
         * @param extractedData List of extracted source data models
         * @return This builder for method chaining
         */
        public Builder extractedData(List<SourceDataModel> extractedData) {
            this.extractedData = extractedData != null ? new ArrayList<>(extractedData) : null;
            return this;
        }

        /**
         * Sets the transformed data (populated by Transform subprocess).
         * This data will be read by Load subprocess.
         *
         * @param transformedData List of transformed target data models
         * @return This builder for method chaining
         */
        public Builder transformedData(List<TargetDataModel> transformedData) {
            this.transformedData = transformedData != null ? new ArrayList<>(transformedData) : null;
            return this;
        }

        /**
         * Sets the temporary files list.
         * These files will be cleaned up by Clean subprocess.
         *
         * @param tempFiles List of temporary file paths
         * @return This builder for method chaining
         */
        public Builder tempFiles(List<Path> tempFiles) {
            this.tempFiles = tempFiles != null ? new ArrayList<>(tempFiles) : null;
            return this;
        }
        
        /**
         * Builds a new DailyProcessContext instance with the configured values.
         * 
         * @return New DailyProcessContext instance
         * @throws IllegalStateException if required fields are not set
         */
        public DailyProcessContext build() {
            validate();
            return new DailyProcessContext(this);
        }
        
        /**
         * Validates builder state before building.
         * 
         * @throws IllegalStateException if validation fails
         */
        private void validate() {
            if (date == null) {
                throw new IllegalStateException("Date is required");
            }
            if (configuration == null) {
                throw new IllegalStateException("Configuration is required");
            }
            if (startTime == null) {
                throw new IllegalStateException("Start time is required");
            }
        }
    }
    
    /**
     * Validates context state.
     * 
     * @throws IllegalStateException if validation fails
     */
    private void validate() {
        if (recordsExtracted < 0) {
            throw new IllegalStateException("Records extracted cannot be negative");
        }
        if (recordsTransformed < 0) {
            throw new IllegalStateException("Records transformed cannot be negative");
        }
        if (recordsLoaded < 0) {
            throw new IllegalStateException("Records loaded cannot be negative");
        }
        if (recordsTransformed > recordsExtracted) {
            throw new IllegalStateException(
                "Records transformed cannot exceed records extracted");
        }
        if (recordsLoaded > recordsTransformed) {
            throw new IllegalStateException(
                "Records loaded cannot exceed records transformed");
        }
    }
    
    // ============ UTILITY METHODS ============
    
    /**
     * Creates a copy of this context with updated subprocess status.
     * 
     * @param subprocessType New subprocess status
     * @return New context with updated subprocess status
     */
    public DailyProcessContext withSubprocess(SubprocessType subprocessType) {
        return builder(this).currentSubprocess(subprocessType).build();
    }
    
    /**
     * Creates a copy of this context with updated records extracted count.
     * 
     * @param count New records extracted count
     * @return New context with updated count
     */
    public DailyProcessContext withRecordsExtracted(int count) {
        return builder(this).recordsExtracted(count).build();
    }
    
    /**
     * Creates a copy of this context with updated records transformed count.
     * 
     * @param count New records transformed count
     * @return New context with updated count
     */
    public DailyProcessContext withRecordsTransformed(int count) {
        return builder(this).recordsTransformed(count).build();
    }
    
/**
 * Creates a copy of this context with updated records loaded count.
 *
 * @param count New records loaded count
 * @return New context with updated count
 */

/**
 * Creates a copy of this context with extracted data.
 * Used by Extract subprocess to store data for Transform.
 *
 * @param data List of extracted source data models
 * @return New context with extracted data
 */
public DailyProcessContext withExtractedData(List<SourceDataModel> data) {
    return builder(this).extractedData(data).build();
}

/**
 * Creates a copy of this context with transformed data.
 * Used by Transform subprocess to store data for Load.
 *
 * @param data List of transformed target data models
 * @return New context with transformed data
 */
public DailyProcessContext withTransformedData(List<TargetDataModel> data) {
    return builder(this).transformedData(data).build();
}

/**
 * Creates a copy of this context with additional temporary files.
 * Used by Extract, Transform, and Load subprocesses to track temp files.
 *
 * @param files List of temporary file paths to add
 * @return New context with updated temp files list
 */
public DailyProcessContext withTempFiles(List<Path> files) {
    List<Path> updated = new ArrayList<>(this.tempFiles);
    if (files != null) {
        updated.addAll(files);
    }
    return builder(this).tempFiles(updated).build();
}
    public DailyProcessContext withRecordsLoaded(int count) {
        return builder(this).recordsLoaded(count).build();
    }
    
    /**
     * Creates a copy of this context marked as completed.
     * 
     * @return New context marked as completed
     */
    public DailyProcessContext markCompleted() {
        return builder(this)
            .currentSubprocess(SubprocessType.COMPLETED)
            .endTime(LocalDateTime.now())
            .build();
    }
    
    /**
     * Creates a copy of this context marked as failed.
     * 
     * @return New context marked as failed
     */
    public DailyProcessContext markFailed() {
        return builder(this)
            .currentSubprocess(SubprocessType.FAILED)
            .endTime(LocalDateTime.now())
            .build();
    }
    
    // ============ OBJECT METHODS ============
    
    @Override
    public boolean equals(Object obj) {
        // Implementation for equality comparison
        // Based on date, configuration, and start time
    }
    
    @Override
    public int hashCode() {
        // Implementation for hash code
        // Based on date, configuration, and start time
    }
    
    @Override
    public String toString() {
        return String.format(
            "DailyProcessContext{date=%s, subprocess=%s, extracted=%d, transformed=%d, loaded=%d}",
            date, currentSubprocess, recordsExtracted, recordsTransformed, recordsLoaded
        );
    }
}
```

---

## SubprocessType Enum

```java
package com.sdd.etl.enums;

/**
 * Enumeration of subprocess types in the ETL workflow.
 * 
 * The ETL workflow executes subprocesses in this order:
 * NOT_STARTED -> EXTRACT -> TRANSFORM -> LOAD -> VALIDATE -> CLEAN -> COMPLETED
 * 
 * If any subprocess fails, the state transitions to FAILED.
 */
public enum SubprocessType {
    /**
     * Process has not started
     */
    NOT_STARTED,
    
    /**
     * Extract subprocess: extract data from all configured sources
     */
    EXTRACT,
    
    /**
     * Transform subprocess: transform source data to target data formats
     */
    TRANSFORM,
    
    /**
     * Load subprocess: load transformed data to all configured targets
     */
    LOAD,
    
    /**
     * Validate subprocess: validate loaded data
     */
    VALIDATE,
    
    /**
     * Clean subprocess: clean up temporary resources
     */
    CLEAN,
    
    /**
     * All subprocesses completed successfully
     */
    COMPLETED,
    
    /**
     * Process failed
     */
    FAILED
}
```

---

## Usage Examples

### Creating Initial Context

```java
LocalDate date = LocalDate.of(2025, 1, 1);
Configuration config = configurationParser.parse("/path/to/config.ini");

DailyProcessContext context = DailyProcessContext.builder()
    .date(date)
    .configuration(config)
    .startTime(LocalDateTime.now())
    .build();
```

### Updating Context During Subprocess Execution

```java
// After extract subprocess
List<SourceDataModel> extracted = extractFromSources();
context = context.withSubprocess(SubprocessType.EXTRACT)
               .withRecordsExtracted(extracted.size())
               .withExtractedData(extracted);  // Store data for Transform

// After transform subprocess
List<TargetDataModel> transformed = transformData(context.getExtractedData());
context = context.withSubprocess(SubprocessType.TRANSFORM)
               .withRecordsTransformed(transformed.size())
               .withTransformedData(transformed);  // Store data for Load

// After load subprocess
context = context.withSubprocess(SubprocessType.LOAD)
               .withRecordsLoaded(transformed.size());

// Clean subprocess reads temp files for cleanup
List<Path> tempFiles = context.getTempFiles();
cleanUpFiles(tempFiles);
```

### Marking Process as Completed

```java
context = context.markCompleted();
// context.getCurrentSubprocess() == SubprocessType.COMPLETED
// context.getEndTime() != null
```

### Marking Process as Failed

```java
context = context.markFailed();
// context.getCurrentSubprocess() == SubprocessType.FAILED
// context.getEndTime() != null
```

---

## Testing Requirements

### Unit Tests

- **DailyProcessContextBuilderTest**: Test builder pattern with all field combinations
- **DailyProcessContextValidationTest**: Test validation rules
- **DailyProcessContextImmutabilityTest**: Ensure immutability
- **DailyProcessContextCopyTest**: Test context copying and updates

### Edge Cases to Test

- Null values for required fields
- Negative record counts
- Record counts violating business rules (transformed > extracted)
- Multiple context updates in sequence
- Thread-safety of immutable instances
- Builder copy from existing context
