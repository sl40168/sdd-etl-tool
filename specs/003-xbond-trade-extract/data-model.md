# Data Model Design: Xbond Trade Extraction

## Overview

This document specifies the data models for the Xbond Trade extraction feature. The design follows the established patterns from the existing `XbondQuoteDataModel` and `RawQuoteRecord` models, ensuring consistency with the ETL framework while accommodating trade-specific attributes.

## Model Relationships

```
┌─────────────────────────────────────────────────────────────┐
│                     Extractor Workflow                       │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  CSV Row (1:1) → RawTradeRecord (1:1) → XbondTradeDataModel  │
│                                                             │
│  • Each CSV row creates ONE RawTradeRecord                  │
│  • Each RawTradeRecord converts to ONE XbondTradeDataModel  │
│  • Simple one-to-one mapping throughout                    │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

## 1. RawTradeRecord (Intermediate Model)

### Purpose
Intermediate representation of parsed CSV trade data. Acts as a bridge between raw CSV content and the standardized `SourceDataModel` extension.

### Package
`com.sdd.etl.source.extract.cos.model`

### Fields

**Note**: The actual CSV contains more columns than stored in RawTradeRecord. Only fields needed for output mapping are stored.

| Field Name | Type | Required | CSV Column | Description | Validation |
|------------|------|----------|------------|-------------|------------|
| `id` | `Long` | No | `id` | Unique record identifier (no business meaning) | Optional |
| `underlyingSecurityId` | `String` | **Yes** | `bond_key` | Bond security identifier (e.g., "250210.IB") | Non-empty string, ends with ".IB" |
| `underlyingSettlementType` | `Integer` | **Yes** | `set_days` | Settlement type (mapped from "T+0"/"T+1") | 0 (T+0) or 1 (T+1) |
| `tradePrice` | `Double` | No | `net_price` | Clean price of the deal | Optional, > 0.0 if present |
| `tradeYield` | `Double` | No | `yield` | Trade yield | Optional, ≥ 0.0 if present |
| `tradeYieldType` | `String` | No | `yield_type` | Yield calculation type | Optional |
| `tradeVolume` | `Long` | No | `deal_size` | Trade volume/quantity | Optional, > 0 if present |
| `tradeSide` | `String` | No | `side` | Trade direction/side (X=Taken, Y=Given, Z=Trade, D=Done) | Optional, raw values from CSV |
| `tradeId` | `String` | No | `hlid` | Unique trade identifier | Optional |
| `transactTime` | `LocalDateTime` | **Yes** | `deal_time` | Trade execution timestamp | Valid timestamp, format: "yyyy-MM-dd HH:mm:ss.SSS" |
| `recvTime` | `LocalDateTime` | No | `recv_time` | System receive timestamp | Optional (added later), format: "yyyy-MM-dd HH:mm:ss.SSS" |

**Ignored CSV Columns** (not stored in RawTradeRecord):
- `bond_code`: Redundant (bond_key already has full ID)
- `symbol`: Short name, not needed
- `act_dt`, `act_tm`: Redundant (deal_time contains full timestamp)
- `pre_market`: Always 0, no business value
- `trade_method`: Not needed for output

### Methods

#### `isValid() : boolean`
Validates the record according to business rules:
1. `underlyingSecurityId` must be non-null and non-empty
2. `underlyingSettlementType` must be 0 or 1
3. `transactTime` must be non-null

**Note**: The actual implementation has minimal validation. Fields like `tradePrice`, `tradeVolume`, `tradeId`, `mqOffset`, and `recvTime` are optional in RawTradeRecord and validated later in XbondTradeDataModel.

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
| `tradePrice` | `Double` | `Double.NaN` | No | Executed trade price (net_price from CSV) |
| `tradeYield` | `Double` | `Double.NaN` | No | Trade yield |
| `tradeYieldType` | `String` | `null` | No | Yield calculation type |
| `tradeVolume` | `Long` | `null` | No | Trade volume/quantity (deal_size from CSV) |
| `tradeSide` | `String` | `null` | No | Trade direction/side (mapped from CSV: X→TKN, Y→GVN, Z→TRD, D→DONE) |
| `tradeId` | `String` | `null` | **Yes** | Unique trade identifier (hlid from CSV) |
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
| `id` | `id` | `Long.parseLong()` | Optional, no business meaning |
| `bond_key` | `underlyingSecurityId` | `String.trim()` | Required, already ends with ".IB" |
| `bond_code` | *(ignored)* | - | Redundant |
| `symbol` | *(ignored)* | - | Not needed |
| `deal_time` | `transactTime` | `LocalDateTime.parse("yyyy-MM-dd HH:mm:ss.SSS")` | Required |
| `act_dt` | *(ignored)* | - | Redundant |
| `act_tm` | *(ignored)* | - | Redundant |
| `pre_market` | *(ignored)* | - | Always 0 |
| `trade_method` | *(ignored)* | - | Not needed |
| `side` | `counterparty` | `String.trim()` | Optional, X/Y/Z/D |
| `net_price` | `tradePrice` | `Double.parseDouble()` | Optional, clean price |
| `set_days` | `underlyingSettlementType` | Map "T+0"→0, "T+1"→1 | Required |
| `yield` | `tradeYield` | `Double.parseDouble()` | Optional |
| `yield_type` | `tradeYieldType` | `String.trim()` | Optional |
| `deal_size` | `tradeVolume` | `Long.parseLong()` | Optional |
| `recv_time` | `recvTime` | `LocalDateTime.parse("yyyy-MM-dd HH:mm:ss.SSS")` | Optional (added later) |
| `hlid` | `tradeId` | `String.trim()` | Optional, unique deal ID |

### RawTradeRecord to XbondTradeDataModel Mapping

| RawTradeRecord Field | XbondTradeDataModel Field | Business Rule | Example |
|----------------------|---------------------------|---------------|---------|
| `underlyingSecurityId` | `exchProductId` | Direct mapping (already has ".IB") | "250210.IB" → "250210.IB" |
| `underlyingSettlementType` | `settleSpeed` | Direct mapping | 0 → 0 (T+0), 1 → 1 (T+1) |
| `tradePrice` | `tradePrice` | Direct mapping | 98.4289 → 98.4289 |
| `tradeYield` | `tradeYield` | Direct mapping | 1.9875 → 1.9875 |
| `tradeYieldType` | `tradeYieldType` | Direct mapping | "1" → "1" |
| `tradeVolume` | `tradeVolume` | Direct mapping | 5000 → 5000 |
| `tradeSide` | `tradeSide` | Map: X→TKN, Y→GVN, Z→TRD, D→DONE | "Y" → "GVN" |
| `tradeId` | `tradeId` | Direct mapping | "4455380029616468" → "4455380029616468" |
| `transactTime` | `eventTime` | Direct mapping | 2026-01-05T10:07:45.068 |
| `recvTime` | `receiveTime` | Direct mapping, or copy from eventTime if null | 2026-01-05T10:07:45.102 |
| (Context Date) | `businessDate` | Format `YYYYMMDD` → `YYYY.MM.DD` | "20260105" → "2026.01.05" |

**Note**: The trade direction mapping (X→TKN/Taken, Y→GVN/Given, Z→TRD/Trade, D→DONE) is applied in the extractor during conversion from RawTradeRecord to XbondTradeDataModel.

## Validation Rules Summary

### RawTradeRecord Validation

1. **Basic Validation** (from `isValid()` method):
   - `underlyingSecurityId` ≠ null, ≠ empty
   - `underlyingSettlementType` ∈ {0, 1}
   - `transactTime` ≠ null

2. **Optional Fields**:
   - `id`, `tradePrice`, `tradeYield`, `tradeYieldType`, `tradeVolume`, `tradeSide`, `tradeId`, `recvTime` may be null
   - These fields are validated later in XbondTradeDataModel

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
    11568725L,                       // id
    "250210.IB",                     // underlyingSecurityId (bond_key)
    1,                               // underlyingSettlementType (T+1)
    98.4289,                         // tradePrice (net_price)
    1.9875,                          // tradeYield
    "1",                             // tradeYieldType
    5000L,                           // tradeVolume (deal_size)
    "Y",                             // counterparty (side)
    "4455380029616468",              // tradeId (hlid)
    LocalDateTime.parse("2026-01-05T10:07:45.068"), // transactTime (deal_time)
    null,                            // mqOffset (internal)
    LocalDateTime.parse("2026-01-05T10:07:45.102")  // recvTime
);
```

### XbondTradeDataModel Example

```java
XbondTradeDataModel model = new XbondTradeDataModel();
model.setBusinessDate("2026.01.05");
model.setExchProductId("250210.IB");
model.setSettleSpeed(1);  // T+1
model.setTradePrice(98.4289);
model.setTradeYield(1.9875);
model.setTradeYieldType("1");
model.setTradeVolume(5000L);
model.setCounterparty("Y");  // Raw value, not mapped
model.setTradeId("4455380029616468");
model.setEventTime(LocalDateTime.parse("2026-01-05T10:07:45.068"));
model.setReceiveTime(LocalDateTime.parse("2026-01-05T10:07:45.102"));
```

## Integration Notes

### CSV Parsing

- Use existing `TradeCsvParser` utility with trade-specific field mapping
- CSV files expected to have header row with exact column names: `id`, `bond_key`, `bond_code`, `symbol`, `deal_time`, `act_dt`, `act_tm`, `pre_market`, `trade_method`, `side`, `net_price`, `set_days`, `yield`, `yield_type`, `deal_size`, `recv_time`, `hlid`
- UTF-8 encoding, pipe-separated values (`|`)
- Timestamps in `yyyy-MM-dd HH:mm:ss.SSS` format (e.g., "2026-01-05 10:07:45.068")
- Settlement speed as string: "T+0" or "T+1" (mapped to 0 or 1)

### Data Transformation

1. **One-to-One Mapping**: Each `RawTradeRecord` is converted to exactly one `XbondTradeDataModel`
2. **Field Mapping**: Apply business rules to transform raw fields to standardized model fields
3. **Validation**: Validate each `XbondTradeDataModel` instance before output
4. **Trade Side Mapping**: The trade direction (X/Y/Z/D) is mapped to standardized values (TKN/GVN/TRD/DONE)

### Error Handling

- Invalid CSV rows logged as warnings, processing continues
- Validation failures for output models logged, failed models excluded from output
- File download failures cause day extraction failure with structured error