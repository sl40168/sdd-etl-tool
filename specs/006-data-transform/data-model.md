# Data Model: Data Transformation Pipeline

**Feature**: 006-data-transform
**Date**: 2026-01-11

## Overview

This document defines the data entities and their relationships for the data transformation pipeline. It includes source models (input), target models (output), and transformation metadata.

---

## 1. Source Data Models

Source data models are existing implementations from prior features. They represent data extracted from various sources (COS, database, API).

### 1.1 XbondQuoteDataModel (Source)

**Package**: `com.sdd.etl.model.XbondQuoteDataModel`

**Description**: Represents standardized bond quote records extracted from COS CSV files. Contains bid and offer price levels (0-5) with associated yields, volumes, and timestamps.

**Key Fields** (83 total):

| Field | Type | Description | Source |
|-------|------|-------------|--------|
| businessDate | String | Trading date in YYYYMMDD format | COS CSV |
| exchProductId | String | Exchange product identifier | COS CSV |
| productType | String | Product type classification | COS CSV |
| exchange | String | Exchange code | COS CSV |
| source | String | Data source identifier | COS CSV |
| settleSpeed | Integer | Settlement speed indicator | COS CSV |
| level | String | Quote level (0 = best, 1-5 = tradable) | COS CSV |
| status | String | Quote status | COS CSV |
| eventTime | LocalDateTime | Event timestamp | COS CSV |
| receiveTime | LocalDateTime | Receive timestamp | COS CSV |
| bid0Price ~ bid5Price | Double | Bid prices for levels 0-5 | COS CSV |
| offer0Price ~ offer5Price | Double | Offer prices for levels 0-5 | COS CSV |
| bid0Yield ~ bid5Yield | Double | Bid yields for levels 0-5 | COS CSV |
| offer0Yield ~ offer5Yield | Double | Offer yields for levels 0-5 | COS CSV |
| bid0YieldType ~ bid5YieldType | String | Bid yield types for levels 0-5 | COS CSV |
| offer0YieldType ~ offer5YieldType | String | Offer yield types for levels 0-5 | COS CSV |
| bid0Volume ~ bid5Volume | Long | Bid volumes for levels 0-5 | COS CSV |
| offer0Volume ~ offer5Volume | Long | Offer volumes for levels 0-5 | COS CSV |
| bid1TradableVolume ~ bid5TradableVolume | Long | Tradable bid volumes for levels 1-5 | COS CSV |
| offer1TradableVolume ~ offer5TradableVolume | Long | Tradable offer volumes for levels 1-5 | COS CSV |

**Validation Rules**:
- businessDate must match YYYYMMDD format
- At least one price field must be non-null
- eventTime must not be null
- Level 0 fields represent best quotes (may not be tradable)
- Levels 1-5 fields represent tradable quotes

**Key Methods**:
- `validate()`: Returns true if data is valid
- `getPrimaryKey()`: Returns composite key (businessDate + exchProductId + eventTime)
- `getSourceType()`: Returns "cos_csv"

---

### 1.2 XbondTradeDataModel (Source)

**Package**: `com.sdd.etl.model.XbondTradeDataModel`

**Description**: Represents standardized bond trade records extracted from COS CSV files.

**Key Fields** (15 total):

| Field | Type | Description | Source |
|-------|------|-------------|--------|
| businessDate | String | Trading date in YYYYMMDD format | COS CSV |
| exchProductId | String | Exchange product identifier | COS CSV |
| productType | String | Product type classification | COS CSV |
| exchange | String | Exchange code | COS CSV |
| source | String | Data source identifier | COS CSV |
| settleSpeed | Integer | Settlement speed indicator | COS CSV |
| level | String | Trade level | COS CSV |
| status | String | Trade status | COS CSV |
| eventTime | LocalDateTime | Trade timestamp | COS CSV |
| receiveTime | LocalDateTime | Receive timestamp | COS CSV |
| price | Double | Trade price | COS CSV |
| volume | Long | Trade volume | COS CSV |
| side | String | Trade side (buy/sell) | COS CSV |
| tradeId | String | Unique trade identifier | COS CSV |

**Validation Rules**:
- businessDate must match YYYYMMDD format
- price must not be null
- volume must be positive (> 0)
- eventTime must not be null
- tradeId must be unique within businessDate

**Key Methods**:
- `validate()`: Returns true if data is valid
- `getPrimaryKey()`: Returns tradeId
- `getSourceType()`: Returns "cos_csv"

---

### 1.3 BondFutureQuoteDataModel (Source)

**Package**: `com.sdd.etl.source.extract.db.quote.BondFutureQuoteDataModel`

**Description**: Represents bond future quote records extracted from database sources.

**Key Fields** (96 total):

**Common Fields** (same structure as XbondQuoteDataModel):
| Field | Type | Description | Source |
|-------|------|-------------|--------|
| businessDate | String | Trading date in YYYYMMDD format | Database |
| exchProductId | String | Exchange product identifier | Database |
| productType | String | Product type classification | Database |
| exchange | String | Exchange code | Database |
| source | String | Data source identifier | Database |
| settleSpeed | Integer | Settlement speed indicator | Database |
| level | String | Quote level | Database |
| status | String | Quote status | Database |
| eventTime | LocalDateTime | Quote timestamp | Database |
| receiveTime | LocalDateTime | Receive timestamp | Database |

**Additional Bond Future-Specific Fields**:
- Market data fields: preClosePrice, preSettlePrice, openPrice, highPrice, lowPrice, closePrice, settlePrice
- Limit fields: upperLimit, lowerLimit
- Aggregate fields: totalVolume, totalTurnover, openInterest
- Quote levels: bid0-5, offer0-5 (price, yield, yieldType, volume, tradableVolume)

**Validation Rules**:
- businessDate must match YYYYMMDD format
- At least one price field must be non-null
- eventTime must not be null

**Key Methods**:
- `validate()`: Returns true if data is valid
- `getPrimaryKey()`: Returns composite key (businessDate + exchProductId + eventTime)
- `getSourceType()`: Returns "database"

---

## 2. Target Data Models

Target data models are optimized for DolphinDB storage with @ColumnOrder annotations.

### 2.1 XbondQuoteDataModel (Target)

**Package**: `com.sdd.etl.loader.model.XbondQuoteDataModel`

**Description**: Target model for Xbond Quote data destined for DolphinDB `xbond_quote_stream_temp` table.

**Key Fields** (83 total with @ColumnOrder):

| Order | Field | Type | Default | Description |
|-------|-------|------|---------|-------------|
| 1 | businessDate | LocalDate | null | Trading date |
| 2 | exchProductId | String | null | Exchange product identifier |
| 3 | productType | String | null | Product type classification |
| 4 | exchange | String | null | Exchange code |
| 5 | source | String | null | Data source identifier |
| 6 | settleSpeed | int | -1 | Settlement speed (-1 = not set) |
| 7 | level | String | null | Quote level |
| 8 | status | String | null | Quote status |
| 9-21 | Market data | double | NaN | preClosePrice, preSettlePrice, preInterest, openPrice, highPrice, lowPrice, closePrice, settlePrice, upperLimit, lowerLimit, totalVolume, totalTurnover, openInterest |
| 22-83 | Quote levels | mixed | NaN/-1/null | bid0-5*, offer0-5* (price, yield, yieldType, volume, tradableVolume) |

**Field Mapping Notes**:
- settleSpeed: int with default -1 (Constitution Principle 11 compliance)
- double fields: default Double.NaN (Constitution Principle 11 compliance)
- String fields: default null (optional fields)
- @ColumnOrder ensures DolphinDB column order matches table schema

**Key Methods**:
- `validate()`: Returns true if data is valid for target system
- `toTargetFormat()`: Returns DolphinDB-compatible format
- `getTargetType()`: Returns "dolphindb"
- `getDataType()`: Returns "XBOND_QUOTE"
- `getOrderedFieldNames()`: Returns field names in @ColumnOrder sequence

---

### 2.2 XbondTradeDataModel (Target)

**Package**: `com.sdd.etl.loader.model.XbondTradeDataModel`

**Description**: Target model for Xbond Trade data destined for DolphinDB.

**Key Fields** (15 total):

| Order | Field | Type | Default | Description |
|-------|-------|------|---------|-------------|
| 1 | businessDate | LocalDate | null | Trading date |
| 2 | exchProductId | String | null | Exchange product identifier |
| 3 | productType | String | null | Product type classification |
| 4 | exchange | String | null | Exchange code |
| 5 | source | String | null | Data source identifier |
| 6 | settleSpeed | int | -1 | Settlement speed |
| 7 | level | String | null | Trade level |
| 8 | status | String | null | Trade status |
| 9 | eventTime | Instant | null | Trade timestamp |
| 10 | receiveTime | Instant | null | Receive timestamp |
| 11 | price | double | NaN | Trade price |
| 12 | volume | double | NaN | Trade volume |
| 13 | side | String | null | Trade side |
| 14 | tradeId | String | null | Unique trade identifier |

**Field Mapping Notes**:
- LocalDateTime → Instant conversion required (target uses Instant)
- settleSpeed: int with default -1
- double fields: default Double.NaN

**Key Methods**:
- `validate()`: Returns true if data is valid
- `toTargetFormat()`: Returns DolphinDB-compatible format
- `getTargetType()`: Returns "dolphindb"
- `getDataType()`: Returns "XBOND_TRADE"
- `getOrderedFieldNames()`: Returns field names in @ColumnOrder sequence

---

### 2.3 BondFutureQuoteDataModel (Target)

**Package**: `com.sdd.etl.loader.model.BondFutureQuoteDataModel`

**Description**: Target model for Bond Future Quote data destined for DolphinDB `fut_market_price_stream_temp` table.

**Key Fields** (96 total):

Structure similar to target XbondQuoteDataModel with additional bond future-specific fields.

**Field Mapping Notes**:
- Same field name mapping strategy as XbondQuoteDataModel
- All numeric fields use sentinel values (-1 or NaN)

**Key Methods**:
- `validate()`: Returns true if data is valid
- `toTargetFormat()`: Returns DolphinDB-compatible format
- `getTargetType()`: Returns "dolphindb"
- `getDataType()`: Returns "BOND_FUTURE_QUOTE"
- `getOrderedFieldNames()`: Returns field names in @ColumnOrder sequence

---

## 3. Transformation Entities

### 3.1 Transformer (Interface)

**Package**: `com.sdd.etl.loader.transformer.Transformer`

**Description**: Common API for transforming lists of source data models to target data models.

**Interface Definition**:
```java
public interface Transformer<S extends SourceDataModel, T extends TargetDataModel> {

    /**
     * Transforms a list of source records to target records.
     * Records are transformed one by one.
     *
     * @param sourceRecords List of source records (same type)
     * @return List of transformed target records (same type)
     * @throws TransformationException if transformation fails
     */
    List<T> transform(List<S> sourceRecords) throws TransformationException;

    /**
     * Gets the source data model type this transformer supports.
     *
     * @return Source data model class
     */
    Class<S> getSourceType();

    /**
     * Gets the target data model type this transformer produces.
     *
     * @return Target data model class
     */
    Class<T> getTargetType();
}
```

**Key Behaviors**:
- One-to-one transformation: Each source record produces one target record
- Field mapping: Based on field name matching (case-sensitive)
- Null handling: Missing source fields result in unassigned target fields
- Type conversion: Automatic conversion between compatible types
- Error handling: Throws TransformationException on failure

---

### 3.2 TransformerFactory

**Package**: `com.sdd.etl.loader.transformer.TransformerFactory`

**Description**: Factory for selecting appropriate transformer based on source data type.

**Factory Method**:
```java
public class TransformerFactory {

    private static final Map<Class<?>, Transformer<?, ?>> TRANSFORMERS = new HashMap<>();

    static {
        register(new XbondQuoteTransformer());
        register(new XbondTradeTransformer());
        register(new BondFutureQuoteTransformer());
    }

    public static Transformer<?, ?> getTransformer(Class<?> sourceType)
            throws TransformationException {

        Transformer<?, ?> transformer = TRANSFORMERS.get(sourceType);
        if (transformer == null) {
            throw new TransformationException(sourceType.getSimpleName(),
                    0, "No transformer found for source type: " + sourceType.getName());
        }
        return transformer;
    }

    private static void register(Transformer<?, ?> transformer) {
        TRANSFORMERS.put(transformer.getSourceType(), transformer);
    }
}
```

**Key Behaviors**:
- Thread-safe initialization (static initializer)
- Returns specific transformer for source type
- Throws TransformationException if no transformer registered

---

### 3.3 Concrete Transformers

#### 3.3.1 XbondQuoteTransformer

**Package**: `com.sdd.etl.loader.transformer.XbondQuoteTransformer`

**Description**: Transforms `com.sdd.etl.model.XbondQuoteDataModel` (source) to `com.sdd.etl.loader.model.XbondQuoteDataModel` (target).

**Field Mapping**: 83 fields with name-based matching

**Special Conversions**:
- String businessDate → LocalDate businessDate
- LocalDateTime → LocalDateTime (direct copy)
- Wrapper types → primitives with sentinel values

#### 3.3.2 XbondTradeTransformer

**Package**: `com.sdd.etl.loader.transformer.XbondTradeTransformer`

**Description**: Transforms `com.sdd.etl.model.XbondTradeDataModel` (source) to `com.sdd.etl.loader.model.XbondTradeDataModel` (target).

**Field Mapping**: 15 fields with name-based matching

**Special Conversions**:
- String businessDate → LocalDate businessDate
- LocalDateTime → Instant (time zone conversion required)
- Wrapper types → primitives with sentinel values

#### 3.3.3 BondFutureQuoteTransformer

**Package**: `com.sdd.etl.loader.transformer.BondFutureQuoteTransformer`

**Description**: Transforms `com.sdd.etl.source.extract.db.quote.BondFutureQuoteDataModel` (source) to `com.sdd.etl.loader.model.BondFutureQuoteDataModel` (target).

**Field Mapping**: 96 fields with name-based matching

**Special Conversions**:
- Same as XbondQuoteTransformer for common fields

---

## 4. Orchestration Entities

### 4.1 TransformSubprocess

**Package**: `com.sdd.etl.subprocess.TransformSubprocess`

**Description**: Orchestrates concurrent transformation of multiple data types.

**Key Responsibilities**:
1. Retrieve all extracted data from ETLContext
2. Group extracted data by source type
3. Select appropriate transformer for each group
4. Execute transformations concurrently using ExecutorService
5. Consolidate all transformed data into single list
6. Transfer transformed data to ETLContext
7. Halt immediately on any exception

**Process Flow**:
```
ETLContext.getExtractedData()
        → Group by source type
        → Create transformer tasks (one per type)
        → Execute concurrently (ExecutorService)
        → Wait for all tasks to complete
        → Consolidate results
        → ETLContext.setTransformedData()
```

**Concurrency Model**:
- Fixed thread pool (size = number of data types)
- Each transformation runs in separate thread
- First exception cancels all pending tasks
- Immediate halt on failure (FR-014)

---

### 4.2 Extracted Data Group

**Description**: A collection of source data model records of the same type.

**Structure**:
```java
Map<Class<? extends SourceDataModel>, List<? extends SourceDataModel>> dataGroups
```

**Example**:
```java
{
    com.sdd.etl.model.XbondQuoteDataModel.class: [quote1, quote2, ...],
    com.sdd.etl.model.XbondTradeDataModel.class: [trade1, trade2, ...],
    com.sdd.etl.source.extract.db.quote.BondFutureQuoteDataModel.class: [future1, future2, ...]
}
```

---

## 5. ETL Context Integration

### 5.1 Context Keys

**Constants** (from `com.sdd.etl.context.ContextConstants`):

| Key | Type | Description |
|-----|------|-------------|
| EXTRACTED_DATA | List<SourceDataModel> | All extracted records |
| TRANSFORMED_DATA | List<TargetDataModel> | All transformed records |
| TRANSFORMED_DATA_COUNT | Integer | Count of transformed records |

### 5.2 Data Flow

```
ExtractSubprocess → ETLContext.setExtractedData()
                                ↓
                        TransformSubprocess
                        (groups by type)
                                ↓
                    Concurrent Transformers
                                ↓
                        TransformSubprocess
                        (consolidates results)
                                ↓
                    ETLContext.setTransformedData()
                                ↓
                        LoadSubprocess
```

---

## 6. Exception Model

### 6.1 TransformationException

**Package**: `com.sdd.etl.loader.transformer.exceptions.TransformationException`

**Description**: Custom exception for transformation failures.

**Attributes**:
- `sourceDataType`: String - Name of source data type that failed
- `recordCount`: int - Number of records processed before failure
- `message`: String - Detailed error message
- `cause`: Throwable - Root cause exception

**Usage Scenarios**:
- Missing transformer for source type
- Field mapping error
- Type conversion error
- Data validation error
- Concurrency error

---

## 7. Validation Rules

### 7.1 Source Data Validation

- Validate in ExtractSubprocess (before transformation)
- Source model must pass `validate()` method
- Missing optional fields are acceptable

### 7.2 Transformation Validation

- Validate field name matching
- Validate type conversion compatibility
- Validate target model integrity after transformation

### 7.3 Target Data Validation

- Target model must pass `validate()` method
- Sentinel values (-1, NaN) used for unassigned fields
- All required fields must have valid values

---

## 8. Performance Considerations

### 8.1 Memory

- Each record: ~1-2 KB (depends on number of fields)
- 10,000 records: ~10-20 MB
- Concurrent processing: Multiply by number of data types (max 3x)
- Total memory: ~30-60 MB for 10K records per type

### 8.2 CPU

- Field reflection access: ~0.001ms per field
- 83 fields per record: ~0.083ms per record
- 10,000 records: ~830ms (~0.83s) per transformer
- Expected overhead: ~20-30% for ExecutorService and type conversion
- Expected time: ~1-2s per 10K records (well under 30s requirement)

### 8.3 I/O

- No disk I/O during transformation (in-memory only)
- Network I/O: None (transformation is CPU-bound)

---

## 9. Index of Entities

| Entity | Package | Role | Status |
|--------|---------|------|--------|
| XbondQuoteDataModel (source) | com.sdd.etl.model | Input | ✅ Existing |
| XbondTradeDataModel (source) | com.sdd.etl.model | Input | ✅ Existing |
| BondFutureQuoteDataModel (source) | com.sdd.etl.source.extract.db.quote | Input | ✅ Existing |
| XbondQuoteDataModel (target) | com.sdd.etl.loader.model | Output | ✅ Existing |
| XbondTradeDataModel (target) | com.sdd.etl.loader.model | Output | ✅ Existing |
| BondFutureQuoteDataModel (target) | com.sdd.etl.loader.model | Output | ✅ Existing |
| Transformer | com.sdd.etl.loader.transformer | API | 🆕 New |
| TransformerFactory | com.sdd.etl.loader.transformer | Factory | 🆕 New |
| XbondQuoteTransformer | com.sdd.etl.loader.transformer | Transformer | 🆕 New |
| XbondTradeTransformer | com.sdd.etl.loader.transformer | Transformer | 🆕 New |
| BondFutureQuoteTransformer | com.sdd.etl.loader.transformer | Transformer | 🆕 New |
| TransformationException | com.sdd.etl.loader.transformer.exceptions | Exception | 🆕 New |
| TransformSubprocess | com.sdd.etl.subprocess | Orchestrator | 🔄 Update existing stub |

**Legend**: ✅ Existing, 🆕 New, 🔄 Update

---

## 10. References

- Feature Specification: [spec.md](./spec.md)
- Research Findings: [research.md](./research.md)
- Implementation Plan: [plan.md](./plan.md)
- Source Code:
  - `src/main/java/com/sdd/etl/model/XbondQuoteDataModel.java`
  - `src/main/java/com/sdd/etl/model/XbondTradeDataModel.java`
  - `src/main/java/com/sdd/etl/loader/model/XbondQuoteDataModel.java`
  - `src/main/java/com/sdd/etl/loader/model/XbondTradeDataModel.java`
  - `src/main/java/com/sdd/etl/loader/model/BondFutureQuoteDataModel.java`
