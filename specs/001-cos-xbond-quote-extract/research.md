# Research Findings: COS Xbond Quote Extraction

**Date**: 2026-01-09  
**Feature**: 001-cos-xbond-quote-extract  
**Branch**: 001-cos-xbond-quote-extract

## Unknowns from Technical Context

### 1. Tencent COS SDK Version and Integration Approach

**Decision**: Use Tencent Cloud COS SDK for Java, version 5.6.93 (latest as of 2026-01-09 that supports Java 8)

**Rationale**:
- Official SDK maintained by Tencent Cloud
- Provides comprehensive API for COS operations (list objects, download files, authentication)
- Actively maintained and documented
- Compatible with Java 8 (requires Java 1.8 or above)
- Apache 2.0 license compatible with project

**Alternatives considered**:
- AWS S3 SDK with COS compatibility layer: Rejected due to additional complexity and potential compatibility issues
- Raw HTTP client implementation: Rejected due to higher implementation and maintenance burden
- Other cloud storage SDKs: Rejected as they lack direct COS support

**Integration details**:
- Maven dependency: `com.qcloud:cos_api:5.6.93`
- Configuration parameters needed in INI config:
  - `cos.endpoint` (e.g., `cos.ap-shanghai.myqcloud.com`)
  - `cos.bucket` (e.g., `xbond-quote-data`)
  - `cos.secretId` (API key ID)
  - `cos.secretKey` (API secret key)
- Implementation pattern: Abstract `CosExtractor` class that handles COS client initialization and file operations

### 2. Performance Goals: Specific Time Target for Extraction

**Decision**: Target extraction completion within 30 minutes for typical daily workload

**Rationale**:
- Based on ETL operational SLAs where extraction is the first phase
- Allows buffer for transformation and load phases within 2-hour total ETL window
- Accounts for network latency, file download time, and processing overhead
- Conservative estimate that can be optimized later based on monitoring

**Alternatives considered**:
- 15-minute target: Too aggressive given variable file sizes and network conditions
- 60-minute target: Too lenient, may impact downstream processes
- No specific target: Rejected as it lacks measurability for success criteria

**Monitoring approach**:
- Log extraction start/end times and file processing durations
- Set operational alert if extraction exceeds 45 minutes
- Track metrics: files processed per minute, average file size, download speed

### 3. Scale/Scope: Expected File Count and Record Volume

**Decision**: Design for up to 100 files per day, each up to 100MB, with total daily records up to 10 million

**Rationale**:
- Conservative upper bound based on typical bond quote data volumes
- Allows processing within reasonable memory constraints using streaming approach
- Supports concurrent processing of multiple files
- Aligns with 30-minute performance target (approx. 5.5MB/s average throughput)

**Alternatives considered**:
- Unlimited scaling: Rejected as it requires distributed processing beyond current scope
- Very small scale (10 files): Rejected as not representative of production needs
- File-specific limits: Rejected for simplicity in initial implementation

**Memory management strategy**:
- Process files one at a time using streaming CSV parser
- Limit in-memory record batches to 10,000 records
- Use temporary disk storage for downloaded files, clean up after processing

## Additional Research Areas

### 4. CSV Parsing for Large Files in Java 8

**Decision**: Use OpenCSV library for CSV parsing with streaming support

**Rationale**:
- Lightweight, mature library with good performance
- Supports streaming parsing (CSVReader with iterator pattern)
- Handles CSV quirks (quotes, escapes, delimiters)
- Java 8 compatible
- BSD license compatible

**Implementation approach**:
- Maven dependency: `com.opencsv:opencsv:5.7.1`
- Process records in batches to control memory usage
- Map CSV columns to internal data structures using column name mapping

### 5. Concurrent Extractor Execution Pattern

**Decision**: Use Java 8's `ExecutorService` with fixed thread pool for concurrent extractor execution

**Rationale**:
- Built-in Java concurrency utilities, no additional dependencies
- Provides thread management and lifecycle control
- Supports graceful shutdown and error propagation
- Configurable thread pool size based on available resources

**Implementation pattern**:
- Extract subprocess creates executor service
- Each extractor runs in separate thread
- Results consolidated after all extractors complete
- Fail-fast: If any extractor fails, entire extraction fails

### 6. Error Handling Strategy

**Decision**: Use existing `ETLException` hierarchy with specific error types for extraction failures

**Rationale**:
- Consistent with existing error handling patterns
- Provides structured error information for logging and monitoring
- Supports retry logic and failure categorization
- Maintains separation between operational errors and configuration errors

**Error categories**:
- `CosConnectionException`: COS connectivity issues
- `FileDownloadException`: Individual file download failures  
- `FileParseException`: CSV parsing errors
- `DataConversionException`: Record mapping/conversion errors

### 7. Data Type Decisions for Quote Fields

**Decision**: Use `Double` for price and yield fields with `Double.NaN` as default value, not `BigDecimal` or `0.0`

**Rationale**:
- `Double` provides sufficient precision for bond quote data (prices typically have 2-4 decimal places)
- Using `Double.NaN` as default clearly indicates unassigned values vs. legitimate zero values
- `0.0` is ambiguous - could represent actual zero price/yield or unassigned field
- `BigDecimal` adds unnecessary complexity and memory overhead for this use case
- Downstream consumers can check `Double.isNaN()` to distinguish missing values

**Implementation approach**:
- Initialize all `Double` fields to `Double.NaN` in constructor
- Validation logic checks for `!Double.isNaN()` to determine if field has been assigned
- Serialization/deserialization handles `NaN` values appropriately

## Summary of Technology Choices

| Component | Choice | Version | Rationale |
|-----------|--------|---------|-----------|
| COS Client | Tencent COS SDK | 5.6.93 | Official, Java 8 compatible |
| CSV Parser | OpenCSV | 5.7.1 | Streaming support, mature |
| Concurrency | ExecutorService | Java 8 built-in | No dependencies, proven |
| Configuration | Apache Commons Configuration | 2.8.0 | Already in use, INI support |
| Testing | JUnit 4 + Mockito | 4.13.2 + 4.5.1 | Project standard |

## Implementation Implications

1. **Maven dependencies**: Add cos_api and opencsv to pom.xml
2. **INI config extension**: Add COS-specific configuration section
3. **Memory management**: Implement streaming processing for large files
4. **Error handling**: Extend ETLException hierarchy for extraction-specific errors
5. **Concurrency**: Use thread pool for multiple extractors, ensure thread safety

## Risks and Mitigations

| Risk | Mitigation |
|------|-----------|
| COS SDK compatibility with Java 8 | Test with actual Java 8 runtime before implementation |
| Large file memory consumption | Implement streaming CSV parsing, batch processing |
| Network timeouts during file download | Configure reasonable timeouts, implement retry logic |
| Concurrent extractor resource contention | Limit thread pool size, monitor system resources |

## Next Steps

1. Update pom.xml with required dependencies
2. Extend INI configuration parser for COS settings
3. Implement abstract CosExtractor base class
4. Create concrete XbondQuoteExtractor with CSV mapping logic
5. Add unit tests for all new components
6. Integrate with existing ExtractSubprocess framework