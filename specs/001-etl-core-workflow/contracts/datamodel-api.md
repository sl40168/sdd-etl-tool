# DataModel API Contract

**Feature Branch**: `001-etl-core-workflow`
**Date**: 2026-01-08
**Status**: Phase 1 Design

## Overview

This document defines the DataModel API for Source and Target data models. **NOTE**: This phase defines API contracts only; concrete implementations are NOT required.

## API Overview

### Data Models

1. **SourceDataModel** - Abstract base for source-specific data models
2. **TargetDataModel** - Abstract base for target-specific data models

**Scope**: API definitions only; concrete implementations will be provided in future phases.

---

## SourceDataModel (API Definition)

### Purpose

Abstract base class representing the structure of data from various sources. Specific source implementations (database, API, file) will extend this model.

### Class Definition

```java
package com.sdd.etl.model;

import java.util.List;
import java.util.Map;

/**
 * SourceDataModel - Abstract base class for source-specific data models.
 * Represents the structure of data extracted from various sources.
 *
 * Concrete implementations (future phases):
 * - DatabaseSourceData - Database-specific data model
 * - APISourceData - API-specific data model
 * - FileSourceData - File-specific data model
 *
 * NOTE: This phase defines the API only; concrete implementations are not required.
 */
public abstract class SourceDataModel {

    // Protected fields accessible by subclasses

    /**
     * Metadata about data fields and types.
     * Keys: field names
     * Values: field metadata (type, constraints, etc.)
     */
    protected Map<String, Object> metadata;

    /**
     * Data records with field-value pairs.
     * Each record is a Map<String, Object> where:
     * - Keys: field names
     * - Values: field values
     */
    protected List<Map<String, Object>> records;

    /**
     * Constructor - Initializes empty data model.
     */
    protected SourceDataModel();

    /**
     * Constructor - Initializes with metadata and records.
     * @param metadata Field metadata
     * @param records Data records
     */
    protected SourceDataModel(Map<String, Object> metadata, List<Map<String, Object>> records);

    // Abstract methods (must be implemented by subclasses)

    /**
     * Validates data integrity and completeness.
     * Checks:
     * - Required fields are present
     * - Field values match metadata types
     * - Primary key is present and unique
     *
     * @return true if data is valid, false otherwise
     */
    public abstract boolean validate();

    /**
     * Gets the primary key value for this record.
     * Primary key field name is configurable via metadata.
     *
     * @param recordIndex Index of record to get primary key for
     * @return Primary key value
     * @throws IndexOutOfBoundsException If recordIndex is invalid
     * @throws IllegalStateException If primary key field not found
     */
    public abstract Object getPrimaryKey(int recordIndex);

    /**
     * Gets the source type for this data model.
     * Examples: "database", "api", "file"
     *
     * @return Source type identifier
     */
    public abstract String getSourceType();

    // Concrete methods (shared by all subclasses)

    /**
     * Gets the metadata for this data model.
     * @return Metadata map
     */
    public Map<String, Object> getMetadata();

    /**
     * Sets the metadata for this data model.
     * @param metadata Metadata to set
     */
    public void setMetadata(Map<String, Object> metadata);

    /**
     * Gets all records in this data model.
     * @return List of records
     */
    public List<Map<String, Object>> getRecords();

    /**
     * Sets all records for this data model.
     * @param records Records to set
     */
    public void setRecords(List<Map<String, Object>> records);

    /**
     * Gets the number of records in this data model.
     * @return Record count
     */
    public int getRecordCount();

    /**
     * Adds a single record to this data model.
     * @param record Record to add
     */
    public void addRecord(Map<String, Object> record);

    /**
     * Gets a specific record by index.
     * @param index Record index
     * @return Record data
     * @throws IndexOutOfBoundsException If index is invalid
     */
    public Map<String, Object> getRecord(int index);

    /**
     * Clears all records from this data model.
     */
    public void clearRecords();
}
```

---

## TargetDataModel (API Definition)

### Purpose

Abstract base class representing the structure of data in target systems. Specific target implementations (database, API, file) will extend this model.

### Class Definition

```java
package com.sdd.etl.model;

import java.util.List;
import java.util.Map;

/**
 * TargetDataModel - Abstract base class for target-specific data models.
 * Represents the structure of data formatted for target systems.
 *
 * Concrete implementations (future phases):
 * - DatabaseTargetData - Database-specific data model
 * - APITargetData - API-specific data model
 * - FileTargetData - File-specific data model
 *
 * NOTE: This phase defines the API only; concrete implementations are not required.
 */
public abstract class TargetDataModel {

    // Protected fields accessible by subclasses

    /**
     * Metadata about data fields and types for target system.
     * Keys: field names
     * Values: field metadata (type, constraints, etc.)
     */
    protected Map<String, Object> metadata;

    /**
     * Data records formatted for target system.
     * Each record is a Map<String, Object> where:
     * - Keys: field names
     * - Values: field values formatted for target
     */
    protected List<Map<String, Object>> records;

    /**
     * Constructor - Initializes empty data model.
     */
    protected TargetDataModel();

    /**
     * Constructor - Initializes with metadata and records.
     * @param metadata Field metadata for target system
     * @param records Data records formatted for target
     */
    protected TargetDataModel(Map<String, Object> metadata, List<Map<String, Object>> records);

    // Abstract methods (must be implemented by subclasses)

    /**
     * Validates data integrity for target system.
     * Checks:
     * - Required fields are present
     * - Field values match target metadata types
     * - Field constraints are satisfied
     *
     * @return true if data is valid for target, false otherwise
     */
    public abstract boolean validate();

    /**
     * Converts records to target-specific format.
     * Examples:
     * - Database: SQL INSERT statements or batch operations
     * - API: JSON payload or multipart form data
     * - File: CSV, JSON, or other format
     *
     * @return Object in target-specific format
     */
    public abstract Object toTargetFormat();

    /**
     * Gets the target type for this data model.
     * Examples: "database", "api", "file"
     *
     * @return Target type identifier
     */
    public abstract String getTargetType();

    // Concrete methods (shared by all subclasses)

    /**
     * Gets the metadata for this data model.
     * @return Metadata map
     */
    public Map<String, Object> getMetadata();

    /**
     * Sets the metadata for this data model.
     * @param metadata Metadata to set
     */
    public void setMetadata(Map<String, Object> metadata);

    /**
     * Gets all records in this data model.
     * @return List of records
     */
    public List<Map<String, Object>> getRecords();

    /**
     * Sets all records for this data model.
     * @param records Records to set
     */
    public void setRecords(List<Map<String, Object>> records);

    /**
     * Gets the number of records in this data model.
     * @return Record count
     */
    public int getRecordCount();

    /**
     * Adds a single record to this data model.
     * @param record Record to add
     */
    public void addRecord(Map<String, Object> record);

    /**
     * Gets a specific record by index.
     * @param index Record index
     * @return Record data
     * @throws IndexOutOfBoundsException If index is invalid
     */
    public Map<String, Object> getRecord(int index);

    /**
     * Clears all records from this data model.
     */
    public void clearRecords();
}
```

---

## Metadata Structure

### Metadata Field Properties

Metadata maps should contain the following properties for each field:

```java
Map<String, Object> fieldMetadata = new HashMap<>();

// Required properties
fieldMetadata.put("type", "STRING|INTEGER|DATE|BOOLEAN|...");  // Field data type
fieldMetadata.put("required", true|false);                     // Is field required?

// Optional properties
fieldMetadata.put("primaryKey", true|false);                    // Is this the primary key?
fieldMetadata.put("maxLength", 100);                           // Maximum length for strings
fieldMetadata.put("minValue", 0);                              // Minimum value for numbers
fieldMetadata.put("maxValue", 1000);                           // Maximum value for numbers
fieldMetadata.put("pattern", "^[A-Za-z0-9]+$");               // Regex pattern for validation
```

### Example Metadata

```java
// Example: Database source metadata
Map<String, Object> customerMetadata = new HashMap<>();
customerMetadata.put("id", Map.of(
    "type", "INTEGER",
    "required", true,
    "primaryKey", true
));
customerMetadata.put("name", Map.of(
    "type", "STRING",
    "required", true,
    "maxLength", 100
));
customerMetadata.put("email", Map.of(
    "type", "STRING",
    "required", true,
    "pattern", "^[^@]+@[^@]+$"
));
customerMetadata.put("createdDate", Map.of(
    "type", "DATE",
    "required", true
));
```

---

## Record Structure

### Example Record

```java
// Example: Customer record
Map<String, Object> customerRecord = new HashMap<>();
customerRecord.put("id", 12345);
customerRecord.put("name", "John Doe");
customerRecord.put("email", "john.doe@example.com");
customerRecord.put("createdDate", "2025-01-01");
```

---

## Subclass Examples (Future Implementation)

### DatabaseSourceData (API Sketch)

```java
public class DatabaseSourceData extends SourceDataModel {

    private String tableName;
    private String connectionString;

    @Override
    public boolean validate() {
        // Validate against database schema
        // Check primary key existence and uniqueness
        // Validate field types match database columns
        return true;
    }

    @Override
    public Object getPrimaryKey(int recordIndex) {
        // Get primary key field name from metadata
        String pkField = (String) metadata.get("primaryKeyField");
        // Return primary key value from record
        return records.get(recordIndex).get(pkField);
    }

    @Override
    public String getSourceType() {
        return "database";
    }
}
```

### APISourceData (API Sketch)

```java
public class APISourceData extends SourceDataModel {

    private String endpointUrl;
    private Map<String, String> headers;

    @Override
    public boolean validate() {
        // Validate against API schema
        // Check required fields
        // Validate JSON structure
        return true;
    }

    @Override
    public Object getPrimaryKey(int recordIndex) {
        // Get primary key field name from metadata
        String pkField = (String) metadata.get("primaryKeyField");
        // Return primary key value from record
        return records.get(recordIndex).get(pkField);
    }

    @Override
    public String getSourceType() {
        return "api";
    }
}
```

### DatabaseTargetData (API Sketch)

```java
public class DatabaseTargetData extends TargetDataModel {

    private String tableName;
    private String connectionString;

    @Override
    public boolean validate() {
        // Validate against database table schema
        // Check field constraints
        // Validate data types
        return true;
    }

    @Override
    public Object toTargetFormat() {
        // Convert to SQL INSERT statements or batch operations
        List<String> insertStatements = new ArrayList<>();
        for (Map<String, Object> record : records) {
            insertStatements.add(buildInsertStatement(record));
        }
        return insertStatements;
    }

    @Override
    public String getTargetType() {
        return "database";
    }
}
```

---

## Data Flow in ETL Pipeline

### SourceDataModel Usage

```
ExtractSubprocess
  ├─> Create SourceDataModel instance (DatabaseSourceData, APISourceData, etc.)
  ├─> Extract data from source
  ├─> Populate SourceDataModel.records
  ├─> Set SourceDataModel.metadata
  ├─> Call SourceDataModel.validate()
  └─> Pass SourceDataModel (or record count) to Context
```

### TargetDataModel Usage

```
TransformSubprocess
  ├─> Read SourceDataModel from Context (or source data count)
  ├─> Transform data for target format
  ├─> Create TargetDataModel instance (DatabaseTargetData, APITargetData, etc.)
  ├─> Populate TargetDataModel.records with transformed data
  ├─> Set TargetDataModel.metadata
  ├─> Call TargetDataModel.validate()
  └─> Pass TargetDataModel (or transformed count) to Context
```

---

## Feature Requirements Mapping

| FR # | Requirement | API Coverage |
|------|-------------|--------------|
| FR-018 | Define Source Data Model with metadata | ✅ SourceDataModel.metadata field |
| FR-019 | Define Target Data Model with metadata | ✅ TargetDataModel.metadata field |
| FR-020 | Source implementations extend Source Data Model | ✅ Abstract class with concrete methods |
| FR-021 | Target implementations extend Target Data Model | ✅ Abstract class with concrete methods |

---

## Source

- Feature Specification: FR-018 through FR-021
- Key Entities: Source Data, Target Data
- Scope: docs/v1/Plan.md - "NO Concrete implementation is required for Source and Target Data Model, ONLY DEFINE API"
