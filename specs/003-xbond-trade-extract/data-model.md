# Data Model Design: Xbond Trade Extraction

## Overview

This document specifies the data models for the Xbond Trade extraction feature. The design follows the established patterns from the existing `XbondQuoteDataModel` and `RawQuoteRecord` models, ensuring consistency with the ETL framework while accommodating trade-specific attributes.

## Model Relationships

```
┌─────────────────────────────────────────────────────────────┐
│                     Extractor Workflow                       │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  CSV Files → RawTradeRecord → XbondTradeDataModel           │
│                                                             │
│  • CSV Parsing → RawTradeRecord (intermediate)              │
│  • Business Rules → XbondTradeDataModel (standardized)      │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

## 1. RawTradeRecord (Intermediate Model)

### Purpose
Intermediate representation of parsed CSV trade data. Acts as a bridge between raw CSV content and the standardized `SourceDataModel` extension.

### Package
`com.sdd.etl.source.extract.cos.model`

### Fields

| Field Name | Type | Required | Description | Validation |
|------------|------|----------|-------------|------------|
| `id` | `Long` | No | Unique record identifier | Optional |
| `underlyingSecurityId` | `String` | **Yes** | Bond security identifier | Non-empty string |
| `underlyingSettlementType` | `Integer` | **Yes** | Settlement type | 0 (T+0) or 1 (T+1) |
| `tradePrice` | `Double` | **Yes** | Executed trade price | > 0.0 |
| `tradeYield` | `Double` | No | Trade yield (if applicable) | Optional, ≥ 0.0 |
| `tradeYieldType` | `String` | No | Yield calculation type | Optional |
| `tradeVolume` | `Long` | **Yes** | Trade volume/quantity | > 0 |
| `counterparty` | `String` | No | Counterparty identifier | Optional |
| `tradeId` | `String` | **Yes** | Unique trade identifier | Non-empty string |
| `transactTime` | `LocalDateTime` | **Yes** | Trade execution timestamp | Valid timestamp |
| `mqOffset` | `Long` | **Yes** | Message queue offset (primary grouping key) | ≥ 0 |
| `recvTime` | `LocalDateTime` | **Yes** | System receive timestamp | Valid timestamp |

### Methods

#### `isValid() : boolean`
Validates the record according to business rules:
1. `underlyingSecurityId` must be non-null and non-empty
2. `underlyingSettlementType` must be 0 or 1
3. `tradePrice` must be > 0.0
4. `tradeVolume` must be > 0
5. `tradeId` must be non-null and non-empty
6. `transactTime` must be non-null
7. `mqOffset` must be non-null and ≥ 0
8. `recvTime` must be non-null

#### Standard Methods
- Default constructor
- Full-parameter constructor (for testing convenience)
- Getters and setters for all fields
- `equals(Object o)` and `hashCode()` using `Objects.equals()` and `Objects.hash()`
- `toString()` in format: `RawTradeRecord{id=..., underlyingSecurityId='...', ...}`

## 2. XbondTradeDataModel (Standardized Output)

### Purpose
Standardized trade data model extending `SourceDataModel`. Represents processed trade records ready for downstream ETL steps.

### Package
`com.sdd.etl.model`

### Inheritance
`extends SourceDataModel`

### Fields

#### Common Fields (Following Quote Pattern)

| Field Name | Type | Default Value | Required | Description |
|------------|------|--------------|----------|-------------|
| `businessDate` | `String` | `null` | **Yes** | Business date in `YYYY.MM.DD` format |
| `exchProductId` | `String` | `null` | **Yes** | Exchange product ID, ends with ".IB" |
| `productType` | `String` | `"BOND"` | Yes | Always "BOND" |
| `exchange` | `String` | `"CFETS"` | Yes | Always "CFETS" |
| `source` | `String` | `"XBOND"` | Yes | Always "XBOND" |
| `settleSpeed` | `Integer` | `null` | **Yes** | Settlement speed: 0 (T+0) or 1 (T+1) |
| `level` | `String` | `"TRADE"` | Yes | Always "TRADE" |
| `status` | `String` | `"Normal"` | Yes | Default "Normal" |

#### Trade-Specific Fields

| Field Name | Type | Initial Value | Required | Description |
|------------|------|---------------|----------|-------------|
| `tradePrice` | `Double` | `Double.NaN` | **Yes** | Executed trade price |
| `tradeYield` | `Double` | `Double.NaN` | No | Trade yield (if applicable) |
| `tradeYieldType` | `String` | `null` | No | Yield calculation type |
| `tradeVolume` | `Long` | `null` | **Yes** | Trade volume/quantity |
| `counterparty` | `String` | `null` | No | Counterparty identifier |
| `tradeId` | `String` | `null` | **Yes** | Unique trade identifier |
| `eventTime` | `LocalDateTime` | `null` | **Yes** | Trade execution timestamp |
| `receiveTime` | `LocalDateTime` | `null` | **Yes** | System receive timestamp |

### Methods

#### `validate() : boolean` (Implements `SourceDataModel.validate()`)

Validates the data model according to business rules:

1. **Required Field Validation**:
   - `businessDate` must be non-null and match pattern `\d{4}\.\d{2}\.\d{2}`
   - `exchProductId` must be non-null and end with ".IB"
   - `settleSpeed` must be 0 or 1
   - `eventTime` and `receiveTime` must be non-null
   - `tradeId` must be non-null and non-empty

2. **Business Rule Validation**:
   - At least one of `tradePrice`, `tradeYield`, or `tradeVolume` must be populated
   - If `tradePrice` is provided and not NaN, must be > 0.0
   - If `tradeVolume` is provided, must be ≥ 0

3. **Consistency Validation**:
   - `eventTime` should be ≤ `receiveTime` (optional, can be warning)

#### `getPrimaryKey() : Object` (Implements `SourceDataModel.getPrimaryKey()`)

Returns a composite primary key string:
```
{businessDate}:{exchProductId}:{tradeId}
```

Example: `"2025.01.01:1021001.IB:T123456789"`

#### `getSourceType() : String` (Implements `SourceDataModel.getSourceType()`)

Returns the source type identifier:
```
"xbond_trade_cos"
```

#### Constructor

```java
public XbondTradeDataModel() {
    super();
    this.productType = "BOND";
    this.exchange = "CFETS";
    this.source = "XBOND";
    this.level = "TRADE";
    this.status = "Normal";
    
    // Initialize Double fields to NaN to indicate unassigned values
    this.tradePrice = Double.NaN;
    this.tradeYield = Double.NaN;
}
```

#### Standard Methods

- Getters and setters for all fields
- `toString()` method for debugging
- Optional helper methods for common calculations (e.g., `getTotalValue()`)

## Field Mapping from CSV to Output

### CSV Column to RawTradeRecord Mapping

| CSV Column | RawTradeRecord Field | Transformation | Notes |
|------------|----------------------|----------------|-------|
| `id` | `id` | `Long.parseLong()` | Optional, may be null |
| `underlying_security_id` | `underlyingSecurityId` | `String.trim()` | Required |
| `underlying_settlement_type` | `underlyingSettlementType` | `Integer.parseInt()` | Must be 0 or 1 |
| `trade_price` | `tradePrice` | `Double.parseDouble()` | Required, > 0 |
| `trade_yield` | `tradeYield` | `Double.parseDouble()` | Optional |
| `trade_yield_type` | `tradeYieldType` | `String.trim()` | Optional |
| `trade_volume` | `tradeVolume` | `Long.parseLong()` | Required, > 0 |
| `counterparty` | `counterparty` | `String.trim()` | Optional |
| `trade_id` | `tradeId` | `String.trim()` | Required |
| `transact_time` | `transactTime` | `LocalDateTime.parse("yyyyMMdd-HH:mm:ss.SSS")` | Required |
| `mq_offset` | `mqOffset` | `Long.parseLong()` | Required, ≥ 0 |
| `recv_time` | `recvTime` | `LocalDateTime.parse("yyyyMMdd-HH:mm:ss.SSS")` | Required |

### RawTradeRecord to XbondTradeDataModel Mapping

| RawTradeRecord Field | XbondTradeDataModel Field | Business Rule | Example |
|----------------------|---------------------------|---------------|---------|
| `underlyingSecurityId` | `exchProductId` | Add ".IB" suffix if not present | "1021001" → "1021001.IB" |
| `underlyingSettlementType` | `settleSpeed` | 0 → 0 (T+0), 1 → 1 (T+1) | 1 → 1 |
| `tradePrice` | `tradePrice` | Direct mapping | 100.5 → 100.5 |
| `tradeYield` | `tradeYield` | Direct mapping | 2.5 → 2.5 |
| `tradeYieldType` | `tradeYieldType` | Direct mapping | "YTM" → "YTM" |
| `tradeVolume` | `tradeVolume` | Direct mapping | 1000 → 1000 |
| `counterparty` | `counterparty` | Direct mapping | "C001" → "C001" |
| `tradeId` | `tradeId` | Direct mapping | "T123" → "T123" |
| `transactTime` | `eventTime` | Direct mapping | 2025-01-01T10:30:00 |
| `recvTime` | `receiveTime` | Direct mapping | 2025-01-01T10:30:05 |
| (Context Date) | `businessDate` | Format `YYYYMMDD` → `YYYY.MM.DD` | "20250101" → "2025.01.01" |

## Validation Rules Summary

### RawTradeRecord Validation

1. **Basic Validation**:
   - `underlyingSecurityId` ≠ null, ≠ empty
   - `underlyingSettlementType` ∈ {0, 1}
   - `tradePrice` > 0.0
   - `tradeVolume` > 0
   - `tradeId` ≠ null, ≠ empty
   - `transactTime` ≠ null
   - `mqOffset` ≥ 0
   - `recvTime` ≠ null

2. **Optional Fields**:
   - `id`, `tradeYield`, `tradeYieldType`, `counterparty` may be null

### XbondTradeDataModel Validation

1. **Format Validation**:
   - `businessDate` matches `\d{4}\.\d{2}\.\d{2}`
   - `exchProductId` ends with ".IB"
   - `settleSpeed` ∈ {0, 1}

2. **Required Field Validation**:
   - `businessDate`, `exchProductId`, `settleSpeed`, `eventTime`, `receiveTime`, `tradeId` ≠ null

3. **Business Rule Validation**:
   - At least one of `tradePrice`, `tradeYield`, `tradeVolume` must be populated (non-null and not NaN)
   - `tradePrice` > 0.0 (if provided and not NaN)
   - `tradeVolume` ≥ 0 (if provided)

4. **Consistency Validation** (Optional):
   - `eventTime` ≤ `receiveTime`
   - `businessDate` matches date portion of `eventTime`

## Example Instances

### RawTradeRecord Example

```java
RawTradeRecord record = new RawTradeRecord(
    12345L,                          // id
    "1021001",                       // underlyingSecurityId
    1,                               // underlyingSettlementType
    100.5,                           // tradePrice
    2.5,                             // tradeYield
    "YTM",                           // tradeYieldType
    1000L,                           // tradeVolume
    "C001",                          // counterparty
    "T20250101-001",                 // tradeId
    LocalDateTime.parse("2025-01-01T10:30:00"), // transactTime
    500L,                            // mqOffset
    LocalDateTime.parse("2025-01-01T10:30:05")  // recvTime
);
```

### XbondTradeDataModel Example

```java
XbondTradeDataModel model = new XbondTradeDataModel();
model.setBusinessDate("2025.01.01");
model.setExchProductId("1021001.IB");
model.setSettleSpeed(1);
model.setTradePrice(100.5);
model.setTradeYield(2.5);
model.setTradeYieldType("YTM");
model.setTradeVolume(1000L);
model.setCounterparty("C001");
model.setTradeId("T20250101-001");
model.setEventTime(LocalDateTime.parse("2025-01-01T10:30:00"));
model.setReceiveTime(LocalDateTime.parse("2025-01-01T10:30:05"));
```

## Integration Notes

### CSV Parsing

- Use existing `CsvParser` utility with trade-specific field mapping
- CSV files expected to have header row with exact column names
- UTF-8 encoding, comma-separated values
- Timestamps in `yyyyMMdd-HH:mm:ss.SSS` format

### Data Transformation

1. **Grouping**: Group `RawTradeRecord` instances by `mqOffset`
2. **Mapping**: Apply business rules to transform raw records to standardized model
3. **Validation**: Validate each `XbondTradeDataModel` instance before output

### Error Handling

- Invalid CSV rows logged as warnings, processing continues
- Validation failures for output models logged, failed models excluded from output
- File download failures cause day extraction failure with structured error