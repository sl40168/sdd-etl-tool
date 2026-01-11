# Implementation Plan: Xbond Trade Extraction from COS

**Branch**: `003-xbond-trade-extract` | **Date**: 2026-01-11 | **Spec**: `specs/003-xbond-trade-extract/spec.md`

## Summary

Extract Xbond Trade data from Tencent COS by selecting files matching configured rules, downloading them, parsing CSV content, and converting to standardized `SourceDataModel` records with trade-specific attributes. Implementation follows the established pattern of `XbondQuoteExtractor` (Phase II), reusing the `CosExtractor` base class, `CsvParser` utility, and integration with the existing ETL workflow. The extractor will handle trade-specific fields like trade price, yield, volume, counterparty, and trade ID while maintaining consistency with existing date handling and validation patterns.

## Technical Context

**Language/Version**: Java 8 (non-negotiable per Constitution)  
**Primary Dependencies**: 
- Apache Commons CLI, Apache Commons Configuration (INI parsing)
- JUnit 4, Mockito (testing)
- SLF4J + Logback (logging)
- Tencent COS SDK (file operations)
- OpenCSV 5.7.1 (streaming CSV parsing)

**Storage**: N/A (extraction outputs to memory/disk for downstream ETL steps)  
**Testing**: JUnit 4, Mockito (TDD with >60% coverage requirement)  
**Target Platform**: CLI tool running on server environments (Linux/Windows)  
**Project Type**: Single (CLI-based ETL tool)  

**Performance Goals**: Extract and process COS trade files within daily ETL operational time window, concurrent execution with quote extractor  
**Constraints**: 
- Must follow existing `Extractor` API contract
- Use established CSV parsing and date handling patterns (`YYYYMMDD`, `YYYY.MM.DD`, `yyyyMMdd-HH:mm:ss.SSS`)
- Implement trade-specific validation rules
- Maintain thread safety for concurrent execution

**Scale/Scope**: Daily ETL jobs processing Xbond Trade data from COS source files, alongside existing quote extraction

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

Refer to `.specify/memory/constitution.md` for current project principles. Gates include:

- ✅ Java 8 platform compliance
- ✅ Maven build tool usage
- ✅ CLI interface only (no GUI/Web)
- ✅ INI configuration format
- ✅ Component boundary clarity
- ✅ TDD with >60% coverage
- ✅ Bug fix version recording
- ✅ Third-party library usage (OpenCSV, Tencent COS SDK)
- ✅ Full build/test pass
- ✅ Protected files restriction
- ✅ Primitive number field initialization (use `Double.NaN` for unassigned values)

**Status**: All gates passed. Design complies with constitution principles.

## Project Structure

### Documentation (this feature)

```text
specs/003-xbond-trade-extract/
├── spec.md              # Existing feature specification
├── plan.md              # This implementation plan
├── data-model.md        # Trade data model specification
├── quickstart.md        # Configuration and usage guide
├── contracts/           # API contracts for testing
└── tasks.md             # Implementation tasks (to be generated)
```

### Source Code (repository root)

```text
sdd-etl-tool/
├── src/main/java/com/sdd/etl/
│   ├── source/extract/cos/
│   │   ├── XbondTradeExtractor.java           # NEW: Main trade extractor class
│   │   ├── model/
│   │   │   ├── RawTradeRecord.java            # NEW: Parsed trade record representation
│   │   │   └── (existing files)
│   │   └── (existing files)
│   ├── model/
│   │   ├── XbondTradeDataModel.java           # NEW: Trade-specific data model
│   │   └── (existing files)
│   └── (existing packages)
├── src/test/java/com/sdd/etl/
│   ├── source/extract/cos/
│   │   ├── XbondTradeExtractorTest.java       # NEW: Unit tests
│   │   ├── XbondTradeExtractorIntegrationTest.java # NEW: Integration tests
│   │   └── model/RawTradeRecordTest.java      # NEW: Unit tests
│   ├── model/XbondTradeDataModelTest.java     # NEW: Unit tests
│   └── (existing tests)
└── (existing structure)
```

**Structure Decision**: Single project structure aligns with existing ETL CLI tool architecture. The repository follows standard Maven layout with trade extractor implementation placed alongside the existing quote extractor. This maintains consistency with existing components and minimizes architectural complexity.

## Detailed Implementation Steps

### Phase 1: Data Model Design (Week 1)

#### 1.1 Create RawTradeRecord Model
- **File**: `src/main/java/com/sdd/etl/source/extract/cos/model/RawTradeRecord.java`
- **Purpose**: Intermediate representation of parsed CSV trade data
- **Fields**:
  - `Long id` - Unique record identifier
  - `String underlyingSecurityId` - Bond security identifier
  - `Integer underlyingSettlementType` - Settlement type (0=T+0, 1=T+1)
  - `Double tradePrice` - Executed trade price
  - `Double tradeYield` - Trade yield (if applicable)
  - `String tradeYieldType` - Yield calculation type
  - `Long tradeVolume` - Trade volume/quantity
  - `String counterparty` - Counterparty identifier
  - `String tradeId` - Unique trade identifier
  - `LocalDateTime transactTime` - Trade execution timestamp
  - `Long mqOffset` - Message queue offset (primary grouping key)
  - `LocalDateTime recvTime` - System receive timestamp
- **Methods**:
  - `isValid()`: Validates required fields (securityId, tradePrice, tradeVolume, transactTime)
  - Standard getters/setters, equals(), hashCode(), toString()

#### 1.2 Create XbondTradeDataModel
- **File**: `src/main/java/com/sdd/etl/model/XbondTradeDataModel.java`
- **Purpose**: Standardized output model extending `SourceDataModel`
- **Common Fields** (following quote pattern):
  - `String businessDate` - `YYYY.MM.DD` format
  - `String exchProductId` - Ends with ".IB"
  - `String productType = "BOND"`
  - `String exchange = "CFETS"`
  - `String source = "XBOND"`
  - `Integer settleSpeed` - 0 or 1
  - `String level = "TRADE"`
  - `String status = "Normal"`
- **Trade-Specific Fields**:
  - `Double tradePrice` - Initialized to `Double.NaN`
  - `Double tradeYield` - Initialized to `Double.NaN`
  - `String tradeYieldType`
  - `Long tradeVolume`
  - `String counterparty`
  - `String tradeId`
  - `LocalDateTime eventTime` - Mapped from `transactTime`
  - `LocalDateTime receiveTime` - Mapped from `recvTime`
- **Methods**:
  - `validate()`: Implements trade-specific validation rules
  - `getPrimaryKey()`: Returns composite key `businessDate:exchProductId:tradeId`
  - `getSourceType()`: Returns `"xbond_trade_cos"`
  - Standard getters/setters

### Phase 2: Core Extractor Implementation (Week 2)

#### 2.1 Create XbondTradeExtractor Class
- **File**: `src/main/java/com/sdd/etl/source/extract/cos/XbondTradeExtractor.java`
- **Extends**: `CosExtractor`
- **Key Methods**:
  - `getCategory()`: Returns `"TradeData"` (configurable)
  - `parseCsvFile(File csvFile)`: Uses `CsvParser` with trade-specific field mapping
  - `convertRawRecords(List<RawTradeRecord> rawRecords)`:
    - Groups records by `mqOffset`
    - Maps CSV fields to `XbondTradeDataModel` attributes
    - Adds ".IB" suffix to security IDs
    - Maps settlement types to settleSpeed (0=T+0, 1=T+1)
    - Sets business date from context (converted to `YYYY.MM.DD`)
  - `createCosClient(CosSourceConfig config)`: Uses Tencent COS SDK

#### 2.2 Update ExtractorFactory
- **File**: `src/main/java/com/sdd/etl/source/extract/ExtractorFactory.java`
- **Modify**: `createCosExtractor()` method (lines 85-99)
- **Add**: Case for `"TradeData"` category
- **Create**: `XbondTradeExtractor` instance
- **Verify**: Category match between configuration and extractor

### Phase 3: CSV Parsing Configuration (Week 3)

#### 3.1 Extend CsvParser for Trade Data
- **Option A**: Create `TradeCsvParser` extending `CsvParser`
- **Option B**: Enhance `CsvParser` with configurable field mapping
- **Expected CSV Format**:
  - Header row with field names
  - Fixed column order (similar to quote CSV with trade-specific fields)
  - UTF-8 encoding, comma-separated
  - Timestamps in `yyyyMMdd-HH:mm:ss.SSS` format

#### 3.2 Date Handling Implementation
- **Business Date**: Convert context date from `YYYYMMDD` to `YYYY.MM.DD`
- **Record Filtering**: Filter records by target date when files contain mixed dates
- **Timestamps**: Parse `transactTime` and `recvTime` using existing patterns

### Phase 4: Validation Rules Implementation (Week 4)

#### 4.1 XbondTradeDataModel Validation
- **Required Fields**: businessDate, exchProductId, settleSpeed, eventTime, receiveTime, tradeId
- **Format Validation**:
  - businessDate matches `YYYY.MM.DD` pattern
  - exchProductId ends with ".IB"
  - settleSpeed is 0 or 1
- **Business Validation**:
  - tradePrice > 0 (if provided and not NaN)
  - tradeVolume ≥ 0 (if provided)
  - At least one of tradePrice, tradeYield, or tradeVolume must be populated

#### 4.2 RawTradeRecord Validation
- **Required Fields**: underlyingSecurityId, tradePrice, tradeVolume, transactTime, tradeId
- **Value Ranges**:
  - tradePrice > 0
  - tradeVolume > 0
  - mqOffset ≥ 0

### Phase 5: Integration and Testing (Week 5)

#### 5.1 Integration with ETL Workflow
- **Test**: Concurrent execution with `XbondQuoteExtractor` in `MultiSourceExtractSubprocess`
- **Verify**: Structured JSON logging follows existing patterns
- **Validate**: Performance metrics collection and reporting

#### 5.2 Comprehensive Testing Suite
- **Unit Tests**: >60% coverage for all new classes
- **Integration Tests**: Mock COS client with sample trade CSV files
- **Contract Tests**: `Extractor` API compliance verification
- **Performance Tests**: Large file handling (up to 100MB threshold)

## Data Model Design Details

### RawTradeRecord Field Mapping (CSV Columns)

| CSV Column Name | Java Field | Type | Required | Validation |
|----------------|------------|------|----------|------------|
| `id` | `id` | `Long` | No | Optional unique identifier |
| `underlying_security_id` | `underlyingSecurityId` | `String` | Yes | Non-empty string |
| `underlying_settlement_type` | `underlyingSettlementType` | `Integer` | Yes | 0 or 1 |
| `trade_price` | `tradePrice` | `Double` | Yes | > 0 |
| `trade_yield` | `tradeYield` | `Double` | No | Optional |
| `trade_yield_type` | `tradeYieldType` | `String` | No | Optional |
| `trade_volume` | `tradeVolume` | `Long` | Yes | > 0 |
| `counterparty` | `counterparty` | `String` | No | Optional |
| `trade_id` | `tradeId` | `String` | Yes | Non-empty string |
| `transact_time` | `transactTime` | `LocalDateTime` | Yes | Valid timestamp |
| `mq_offset` | `mqOffset` | `Long` | Yes | ≥ 0 |
| `recv_time` | `recvTime` | `LocalDateTime` | Yes | Valid timestamp |

### XbondTradeDataModel Business Rules

1. **Security ID Transformation**:
   - Input: `underlyingSecurityId` (e.g., "1021001")
   - Output: `exchProductId` (e.g., "1021001.IB")
   - Rule: Always add ".IB" suffix if not present

2. **Settlement Type Mapping**:
   - `underlyingSettlementType` = 0 → `settleSpeed` = 0 (T+0)
   - `underlyingSettlementType` = 1 → `settleSpeed` = 1 (T+1)

3. **Timestamp Mapping**:
   - `transactTime` → `eventTime` (trade execution time)
   - `recvTime` → `receiveTime` (system receive time)

4. **Business Date Format**:
   - Input: Context date in `YYYYMMDD` (e.g., "20250101")
   - Output: `businessDate` in `YYYY.MM.DD` (e.g., "2025.01.01")

## Integration Points

### 1. ETL Workflow Integration
- **WorkflowEngine**: Generates date ranges, processes each day sequentially
- **DailyETLWorkflow**: Orchestrates subprocesses for single day
- **ExtractSubprocess**: `MultiSourceExtractSubprocess` executes trade extractor concurrently with other extractors

### 2. Configuration Integration
- **ConfigurationLoader**: Loads INI configuration for trade extractor
- **CosSourceConfig**: Extends with trade-specific properties (category, filters, thresholds)
- **Context integration**: ETL context provides business date and filter criteria

### 3. Logging and Monitoring
- **Structured JSON logging**: Follows existing pattern with trade-specific metrics
- **Performance tracking**: Extraction duration, file count, record count, success/failure status
- **Error handling**: Structured error messages with file details, failure reasons, timestamps

## Testing Strategy

### Unit Tests (>60% coverage requirement)

1. **`XbondTradeExtractorTest`**
   - File selection based on category and date
   - CSV parsing and raw record conversion
   - Business rule application (security ID suffix, settlement mapping)
   - Error handling for download failures, parsing errors

2. **`RawTradeRecordTest`**
   - Field validation in `isValid()` method
   - Edge cases for null/empty values
   - Boundary testing for numeric fields

3. **`XbondTradeDataModelTest`**
   - Validation logic for all business rules
   - Primary key generation
   - Source type identification

### Integration Tests

1. **COS client integration**
   - Mock COS client for file listing and download
   - Test with sample trade CSV files
   - Verify concurrent execution with existing extractors

2. **ETL workflow integration**
   - End-to-end extraction in `MultiSourceExtractSubprocess`
   - Structured logging verification
   - Performance metrics collection

### Contract Tests

1. **`Extractor` API compliance**
   - Verify all interface methods implemented correctly
   - Thread safety validation for concurrent execution
   - Error handling contract adherence

### Performance Tests

1. **Large file handling**
   - Files up to 100MB (configurable threshold)
   - Memory usage monitoring
   - Graceful failure on size limit exceeded

## Configuration Examples

### INI Configuration

```ini
[sources.xbond_trade]
type = cos
category = TradeData
cos.endpoint = https://cos.ap-beijing.myqcloud.com
cos.bucket = xbond-data-prod
cos.region = ap-beijing
cos.prefix = trade/
cos.secretId = ${COS_SECRET_ID}
cos.secretKey = ${COS_SECRET_KEY}
cos.maxFileSize = 104857600  # 100MB in bytes
```

### Context Filter Criteria

```java
// Example context setup for trade extraction
ETLContext context = new ETLContext();
context.setCurrentDate(LocalDate.of(2025, 1, 1));  // YYYY-MM-DD
context.setCategoryFilter("TradeData");
context.setSourceType("cos");
```

## Success Criteria

### Measurable Outcomes

1. **Extraction Completeness**: For a given run date, extract all matching COS trade files and produce consolidated output
2. **Graceful Empty Results**: When no matching trade files exist, complete extraction successfully with empty output
3. **Error Handling**: Unrecoverable errors mark day's extraction failed with clear error messages
4. **Performance**: Trade extraction completes within 30 minutes for typical daily workload
5. **Concurrent Execution**: Trade extractor runs concurrently with quote extractor without conflicts

### Validation Metrics

- **File Selection Accuracy**: 100% match between configured rules and selected files
- **Record Conversion Fidelity**: All trade-specific attributes preserved in output model
- **Error Reporting Clarity**: Structured error messages with actionable details
- **Performance Compliance**: Extraction within operational time window

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

No violations identified. Design follows established patterns from existing `XbondQuoteExtractor` and complies with all constitution principles.