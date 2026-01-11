# Data Model: DolphinDB Data Loader

## Overview
This document defines the data entities and relationships for the DolphinDB data loader implementation. The model supports loading transformed data to DolphinDB with temporary table management and extensible loader architecture.

## Core Entities

### ColumnOrder Annotation
**Purpose**: Annotation to define column order for DolphinDB table insertion.

```java
package com.sdd.etl.loader.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ColumnOrder {
    int value();
}
```

### TargetDataModel (Abstract Base)
**Purpose**: Base class for all transformed data ready for loading to DolphinDB.

**Attributes**:
- `dataType` (String): Identifier for the specific data model type (e.g., "XbondQuote", "XbondTrade", "BondFutureQuote")
- `fieldOrder` (Map<String, Integer>): Mapping of field names to column order in DolphinDB tables
- `validationErrors` (List<String>): Accumulated validation errors during transformation

**Constraints**:
- All concrete implementations must define field order using `@ColumnOrder` annotation
- Field names must match DolphinDB column names
- Data types must be compatible with DolphinDB types

### Concrete Data Model Classes

Three concrete classes extend `TargetDataModel`, each corresponding to a specific data type and target table configuration.

**XbondQuoteDataModel**
- **Data Type**: `"XbondQuote"`
- **Target Table**: `xbond_quote_stream_temp` (configurable via `LoaderConfiguration.targetTableMappings`)
- **Field Count**: 83 fields

```java
package com.sdd.etl.loader.model;

import com.sdd.etl.loader.annotation.ColumnOrder;
import com.sdd.etl.model.TargetDataModel;
import java.time.LocalDate;
import java.time.Instant;

public class XbondQuoteDataModel extends TargetDataModel {

    @ColumnOrder(1)
    private LocalDate businessDate;

    @ColumnOrder(2)
    private String exchProductId;

    @ColumnOrder(3)
    private String productType;

    @ColumnOrder(4)
    private String exchange;

    @ColumnOrder(5)
    private String source;

    @ColumnOrder(6)
    private int settleSpeed;

    @ColumnOrder(7)
    private String level;

    @ColumnOrder(8)
    private String status;

    @ColumnOrder(9)
    private double preClosePrice;

    @ColumnOrder(10)
    private double preSettlePrice;

    @ColumnOrder(11)
    private double preInterest;

    @ColumnOrder(12)
    private double openPrice;

    @ColumnOrder(13)
    private double highPrice;

    @ColumnOrder(14)
    private double lowPrice;

    @ColumnOrder(15)
    private double closePrice;

    @ColumnOrder(16)
    private double settlePrice;

    @ColumnOrder(17)
    private double upperLimit;

    @ColumnOrder(18)
    private double lowerLimit;

    @ColumnOrder(19)
    private double totalVolume;

    @ColumnOrder(20)
    private double totalTurnover;

    @ColumnOrder(21)
    private double openInterest;

    @ColumnOrder(22)
    private double bid0Price;

    @ColumnOrder(23)
    private double bid0Yield;

    @ColumnOrder(24)
    private String bid0YieldType;

    @ColumnOrder(25)
    private double bid0TradableVolume;

    @ColumnOrder(26)
    private double bid0Volume;

    @ColumnOrder(27)
    private double offer0Price;

    @ColumnOrder(28)
    private double offer0Yield;

    @ColumnOrder(29)
    private String offer0YieldType;

    @ColumnOrder(30)
    private double offer0TradableVolume;

    @ColumnOrder(31)
    private double offer0Volume;

    @ColumnOrder(32)
    private double bid1Price;

    @ColumnOrder(33)
    private double bid1Yield;

    @ColumnOrder(34)
    private String bid1YieldType;

    @ColumnOrder(35)
    private double bid1TradableVolume;

    @ColumnOrder(36)
    private double bid1Volume;

    @ColumnOrder(37)
    private double offer1Price;

    @ColumnOrder(38)
    private double offer1Yield;

    @ColumnOrder(39)
    private String offer1YieldType;

    @ColumnOrder(40)
    private double offer1TradableVolume;

    @ColumnOrder(41)
    private double offer1Volume;

    @ColumnOrder(42)
    private double bid2Price;

    @ColumnOrder(43)
    private double bid2Yield;

    @ColumnOrder(44)
    private String bid2YieldType;

    @ColumnOrder(45)
    private double bid2TradableVolume;

    @ColumnOrder(46)
    private double bid2Volume;

    @ColumnOrder(47)
    private double offer2Price;

    @ColumnOrder(48)
    private double offer2Yield;

    @ColumnOrder(49)
    private String offer2YieldType;

    @ColumnOrder(50)
    private double offer2TradableVolume;

    @ColumnOrder(51)
    private double offer2Volume;

    @ColumnOrder(52)
    private double bid3Price;

    @ColumnOrder(53)
    private double bid3Yield;

    @ColumnOrder(54)
    private String bid3YieldType;

    @ColumnOrder(55)
    private double bid3TradableVolume;

    @ColumnOrder(56)
    private double bid3Volume;

    @ColumnOrder(57)
    private double offer3Price;

    @ColumnOrder(58)
    private double offer3Yield;

    @ColumnOrder(59)
    private String offer3YieldType;

    @ColumnOrder(60)
    private double offer3TradableVolume;

    @ColumnOrder(61)
    private double offer3Volume;

    @ColumnOrder(62)
    private double bid4Price;

    @ColumnOrder(63)
    private double bid4Yield;

    @ColumnOrder(64)
    private String bid4YieldType;

    @ColumnOrder(65)
    private double bid4TradableVolume;

    @ColumnOrder(66)
    private double bid4Volume;

    @ColumnOrder(67)
    private double offer4Price;

    @ColumnOrder(68)
    private double offer4Yield;

    @ColumnOrder(69)
    private String offer4YieldType;

    @ColumnOrder(70)
    private double offer4TradableVolume;

    @ColumnOrder(71)
    private double offer4Volume;

    @ColumnOrder(72)
    private double bid5Price;

    @ColumnOrder(73)
    private double bid5Yield;

    @ColumnOrder(74)
    private String bid5YieldType;

    @ColumnOrder(75)
    private double bid5TradableVolume;

    @ColumnOrder(76)
    private double bid5Volume;

    @ColumnOrder(77)
    private double offer5Price;

    @ColumnOrder(78)
    private double offer5Yield;

    @ColumnOrder(79)
    private String offer5YieldType;

    @ColumnOrder(80)
    private double offer5TradableVolume;

    @ColumnOrder(81)
    private double offer5Volume;

    @ColumnOrder(82)
    private Instant eventTime;

    @ColumnOrder(83)
    private Instant receiveTime;

    @Override
    public String getDataType() {
        return "XbondQuote";
    }

    @Override
    public boolean validate() {
        // Implementation per business rules
        return true;
    }

    @Override
    public Object toTargetFormat() {
        // Conversion to DolphinDB format
        return null;
    }

    @Override
    public String getTargetType() {
        return "DolphinDB";
    }

    // Getters and setters...
}
```

**XbondTradeDataModel**
- **Data Type**: `"XbondTrade"`
- **Target Table**: `xbond_trade_stream_temp` (configurable via `LoaderConfiguration.targetTableMappings`)
- **Field Count**: 15 fields

```java
package com.sdd.etl.loader.model;

import com.sdd.etl.loader.annotation.ColumnOrder;
import com.sdd.etl.model.TargetDataModel;
import java.time.LocalDate;
import java.time.Instant;

public class XbondTradeDataModel extends TargetDataModel {

    @ColumnOrder(1)
    private LocalDate businessDate;

    @ColumnOrder(2)
    private String exchProductId;

    @ColumnOrder(3)
    private String productType;

    @ColumnOrder(4)
    private String exchange;

    @ColumnOrder(5)
    private String source;

    @ColumnOrder(6)
    private int settleSpeed;

    @ColumnOrder(7)
    private double lastTradePrice;

    @ColumnOrder(8)
    private double lastTradeYield;

    @ColumnOrder(9)
    private String lastTradeYieldType;

    @ColumnOrder(10)
    private double lastTradeVolume;

    @ColumnOrder(11)
    private double lastTradeTurnover;

    @ColumnOrder(12)
    private double lastTradeInterest;

    @ColumnOrder(13)
    private String lastTradeSide;

    @ColumnOrder(14)
    private Instant eventTime;

    @ColumnOrder(15)
    private Instant receiveTime;

    @Override
    public String getDataType() {
        return "XbondTrade";
    }

    @Override
    public boolean validate() {
        // Implementation per business rules
        return true;
    }

    @Override
    public Object toTargetFormat() {
        // Conversion to DolphinDB format
        return null;
    }

    @Override
    public String getTargetType() {
        return "DolphinDB";
    }

    // Getters and setters...
}
```

**BondFutureQuoteDataModel**
- **Data Type**: `"BondFutureQuote"`
- **Target Table**: `fut_market_price_stream_temp` (configurable via `LoaderConfiguration.targetTableMappings`)
- **Field Count**: 96 fields

```java
package com.sdd.etl.loader.model;

import com.sdd.etl.loader.annotation.ColumnOrder;
import com.sdd.etl.model.TargetDataModel;
import java.time.LocalDate;
import java.time.Instant;

public class BondFutureQuoteDataModel extends TargetDataModel {

    @ColumnOrder(1)
    private LocalDate businessDate;

    @ColumnOrder(2)
    private String exchProductId;

    @ColumnOrder(3)
    private String productType;

    @ColumnOrder(4)
    private String exchange;

    @ColumnOrder(5)
    private String source;

    @ColumnOrder(6)
    private int settleSpeed;

    @ColumnOrder(7)
    private double lastTradePrice;

    @ColumnOrder(8)
    private double lastTradeYield;

    @ColumnOrder(9)
    private String lastTradeYieldType;

    @ColumnOrder(10)
    private double lastTradeVolume;

    @ColumnOrder(11)
    private double lastTradeTurnover;

    @ColumnOrder(12)
    private double lastTradeInterest;

    @ColumnOrder(13)
    private String lastTradeSide;

    @ColumnOrder(14)
    private String level;

    @ColumnOrder(15)
    private String status;

    @ColumnOrder(16)
    private double preClosePrice;

    @ColumnOrder(17)
    private double preSettlePrice;

    @ColumnOrder(18)
    private double preInterest;

    @ColumnOrder(19)
    private double openPrice;

    @ColumnOrder(20)
    private double highPrice;

    @ColumnOrder(21)
    private double lowPrice;

    @ColumnOrder(22)
    private double closePrice;

    @ColumnOrder(23)
    private double settlePrice;

    @ColumnOrder(24)
    private double upperLimit;

    @ColumnOrder(25)
    private double lowerLimit;

    @ColumnOrder(26)
    private double totalVolume;

    @ColumnOrder(27)
    private double totalTurnover;

    @ColumnOrder(28)
    private double openInterest;

    @ColumnOrder(29)
    private double bid0Price;

    @ColumnOrder(30)
    private double bid0Yield;

    @ColumnOrder(31)
    private String bid0YieldType;

    @ColumnOrder(32)
    private double bid0TradableVolume;

    @ColumnOrder(33)
    private double bid0Volume;

    @ColumnOrder(34)
    private double offer0Price;

    @ColumnOrder(35)
    private double offer0Yield;

    @ColumnOrder(36)
    private String offer0YieldType;

    @ColumnOrder(37)
    private double offer0TradableVolume;

    @ColumnOrder(38)
    private double offer0Volume;

    @ColumnOrder(39)
    private double bid1Price;

    @ColumnOrder(40)
    private double bid1Yield;

    @ColumnOrder(41)
    private String bid1YieldType;

    @ColumnOrder(42)
    private double bid1TradableVolume;

    @ColumnOrder(43)
    private double bid1Volume;

    @ColumnOrder(44)
    private double offer1Price;

    @ColumnOrder(45)
    private double offer1Yield;

    @ColumnOrder(46)
    private String offer1YieldType;

    @ColumnOrder(47)
    private double offer1TradableVolume;

    @ColumnOrder(48)
    private double offer1Volume;

    @ColumnOrder(49)
    private double bid2Price;

    @ColumnOrder(50)
    private double bid2Yield;

    @ColumnOrder(51)
    private String bid2YieldType;

    @ColumnOrder(52)
    private double bid2TradableVolume;

    @ColumnOrder(53)
    private double bid2Volume;

    @ColumnOrder(54)
    private double offer2Price;

    @ColumnOrder(55)
    private double offer2Yield;

    @ColumnOrder(56)
    private String offer2YieldType;

    @ColumnOrder(57)
    private double offer2TradableVolume;

    @ColumnOrder(58)
    private double offer2Volume;

    @ColumnOrder(59)
    private double bid3Price;

    @ColumnOrder(60)
    private double bid3Yield;

    @ColumnOrder(61)
    private String bid3YieldType;

    @ColumnOrder(62)
    private double bid3TradableVolume;

    @ColumnOrder(63)
    private double bid3Volume;

    @ColumnOrder(64)
    private double offer3Price;

    @ColumnOrder(65)
    private double offer3Yield;

    @ColumnOrder(66)
    private String offer3YieldType;

    @ColumnOrder(67)
    private double offer3TradableVolume;

    @ColumnOrder(68)
    private double offer3Volume;

    @ColumnOrder(69)
    private double bid4Price;

    @ColumnOrder(70)
    private double bid4Yield;

    @ColumnOrder(71)
    private String bid4YieldType;

    @ColumnOrder(72)
    private double bid4TradableVolume;

    @ColumnOrder(73)
    private double bid4Volume;

    @ColumnOrder(74)
    private double offer4Price;

    @ColumnOrder(75)
    private double offer4Yield;

    @ColumnOrder(76)
    private String offer4YieldType;

    @ColumnOrder(77)
    private double offer4TradableVolume;

    @ColumnOrder(78)
    private double offer4Volume;

    @ColumnOrder(79)
    private double bid5Price;

    @ColumnOrder(80)
    private double bid5Yield;

    @ColumnOrder(81)
    private String bid5YieldType;

    @ColumnOrder(82)
    private double bid5TradableVolume;

    @ColumnOrder(83)
    private double bid5Volume;

    @ColumnOrder(84)
    private double offer5Price;

    @ColumnOrder(85)
    private double offer5Yield;

    @ColumnOrder(86)
    private String offer5YieldType;

    @ColumnOrder(87)
    private double offer5TradableVolume;

    @ColumnOrder(88)
    private double offer5Volume;

    @ColumnOrder(89)
    private Instant eventTimeTrade;

    @ColumnOrder(90)
    private Instant receiveTimeTrade;

    @ColumnOrder(91)
    private Instant createTimeTrade;

    @ColumnOrder(92)
    private Instant eventTimeQuote;

    @ColumnOrder(93)
    private Instant receiveTimeQuote;

    @ColumnOrder(94)
    private Instant createTimeQuote;

    @ColumnOrder(95)
    private String tickType;

    @ColumnOrder(96)
    private Instant receiveTime;

    @Override
    public String getDataType() {
        return "BondFutureQuote";
    }

    @Override
    public boolean validate() {
        // Implementation per business rules
        return true;
    }

    @Override
    public Object toTargetFormat() {
        // Conversion to DolphinDB format
        return null;
    }

    @Override
    public String getTargetType() {
        return "DolphinDB";
    }

    // Getters and setters...
}
```

**Implementation Notes**:
- The single `DolphinDBLoader` accepts a list containing mixed instances of these three classes.
- Each record is loaded into the corresponding target table based on its `dataType` field as per configuration mapping.
- Column order mapping (`fieldOrder`) must be defined for each concrete class according to the Plan.md tables.
- **Primitive Number Field Initialization**: All primitive numeric fields (int, double, long, etc.) in concrete TargetDataModel classes MUST be explicitly initialized in constructors, avoiding default zero values. This ensures data integrity and complies with constitution principle PR‑11.

### TemporaryTable
**Purpose**: Intermediate table in DolphinDB used during the loading process.

**Attributes**:
- `tableName` (String): Unique name for the temporary table (immutable after creation)
- `schemaDefinition` (String): SQL definition of table columns and types
- `creationTimestamp` (Instant): Time when table was created
- `rowCount` (int): Number of rows loaded into the table

**Constraints**:
- Table names must be unique to avoid conflicts
- Must be created before data loading begins
- Must be deleted after data validation completes

### LoaderConfiguration
**Purpose**: Configuration settings for the DolphinDB data loader.

**Attributes**:
- `dolphinDBHost` (String): DolphinDB server hostname or IP
- `dolphinDBPort` (int): DolphinDB server port
- `username` (String): Authentication username
- `password` (String): Authentication password
- `sortFields` (List<String>): Fields to sort by before loading (e.g., ["receive_time"])
- `tableMappings` (Map<String, String>): Mapping from data model types to target table names
- `memoryLimitMB` (int): Maximum memory for sorting operations (default: 512MB)
- `temporaryTablePrefix` (String): Prefix for temporary table names (default: "etl_temp_")
- `batchSize` (int): Number of records to process in a batch (default: 1000)

**Constraints**:
- Authentication credentials must be provided for secure connections
- Sort fields must exist in the TargetDataModel being processed
- Memory limit must be positive integer

## Data Type Mapping

### DolphinDB → Java Mapping
| DolphinDB Type | Java Class | Java Type | Conversion Notes |
|----------------|------------|-----------|------------------|
| DATE           | `BasicDate` | `LocalDate` | Use `LocalDate.parse()` for string conversion |
| SYMBOL         | `BasicString` | `String` | Direct string assignment |
| INT            | `BasicInt` | `int` | Use Java primitive int |
| DOUBLE         | `BasicDouble` | `double` | Use Java primitive double |
| TIMESTAMP      | `BasicTimestamp` | `Instant` | Requires nanosecond precision |

### Implementation Notes
- All DolphinDB data type classes reside in `com.xxdb.data` package
- For missing `receive_time` fields, records are skipped with warning
- Null values in sort fields cause record exclusion during sorting
- Data volume exceeding memory limit triggers external (disk-based) sorting

## Data Conversion for Column-Based Insertion

DolphinDB is a column-based database where data insertion typically requires converting record-oriented data into column-oriented arrays. Each field's values across all records must be transformed into a single array per column.

### Conversion Process

1. **Input**: List of `TargetDataModel` instances sorted by configured fields.
2. **Column Extraction**: For each field in the data model schema, extract values across all records into a Java array.
3. **Type Mapping**: Convert Java primitives/wrappers to DolphinDB API scalar types (`BasicDate`, `BasicString`, `BasicInt`, `BasicDouble`, `BasicTimestamp`).
4. **Output**: Column‑wise arrays ready for `tableInsert` or appender operations.

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

## Relationships

### TargetDataModel → Target Table (via LoaderConfiguration)
- Each concrete TargetDataModel maps to exactly one target table name
- Mapping defined in LoaderConfiguration.targetTableMappings (dataType → tableName)
- Table schemas are managed by DolphinDB, not duplicated in Java

### LoaderConfiguration → Data Loader (1:1)
- Each loader instance uses a single configuration
- Configuration provides connection parameters and runtime settings



## Validation Rules

### Data Integrity
1. All fields in TargetDataModel must have corresponding columns in target tables (managed by DolphinDB schemas)
2. Field types must be compatible between Java and DolphinDB
3. Required fields (e.g., `receive_time`) must not be null

### Operational Constraints
1. Temporary tables must be uniquely named to prevent collisions
2. Sorting memory usage must not exceed configured limit
3. Failed loads must stop the ETL process for manual intervention

## Performance Considerations

### Sorting Strategy
- **In-memory sort**: For datasets under configured memory limit
- **External sort**: For datasets exceeding memory limit (disk-based)
- **Configurable memory limit**: Allows tuning for different data volumes

### Connection Management
- Connection pooling for efficient reuse
- Configurable timeouts for network operations
- Automatic reconnection handling for transient failures

## Extensibility Patterns

### Adding New Data Model Types
1. Extend `TargetDataModel` abstract class
2. Define field order mapping
3. Add table mapping to LoaderConfiguration
4. Implement data conversion logic

### Supporting New Target Systems
1. Implement common Loader API interfaces
2. Extend LoaderConfiguration for system-specific settings
3. Create system-specific data conversion utilities