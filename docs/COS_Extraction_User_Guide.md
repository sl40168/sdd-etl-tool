# COS Extraction User Guide

## Overview

The COS (Cloud Object Storage) Extraction feature enables retrieval of Xbond Quote data from Tencent Cloud Object Storage. This guide covers installation, configuration, usage, and troubleshooting for the COS extraction functionality.

### Key Features
- **File Filtering**: Select files based on date, category, and prefix patterns
- **Concurrent Processing**: Multiple file downloads and parsing in parallel
- **Size Validation**: Configurable maximum file size (default: 100MB)
- **Structured Logging**: JSON-formatted operational logs
- **Security**: Placeholder credential detection and secure handling
- **Error Handling**: Graceful failure with structured error messages

## Prerequisites

### System Requirements
- Java 8 or higher
- Maven 3.6+ (or use included Maven wrapper)
- Network access to Tencent COS endpoints

### Tencent COS Requirements
- Valid Tencent Cloud account
- COS bucket with Xbond Quote CSV files
- SecretId and SecretKey with read permissions
- Bucket region and endpoint information

## Installation

### Building from Source

```bash
# Clone the repository
git clone <repository-url>
cd sdd-etl-tool

# Build the project
mvn clean package

# Or use the Maven wrapper (Windows)
./mvnw.cmd clean package

# Or use the Maven wrapper (Unix/Linux/Mac)
./mvnw clean package
```

### Verifying the Build

```bash
# Run all tests
mvn test

# Generate coverage report
mvn jacoco:report
# Report available at: target/site/jacoco/index.html
```

## Configuration

### Configuration File Structure

The ETL tool uses INI format configuration files. The main sections are:

```ini
[logging]           # Logging configuration
[sources]          # Data source definitions
[targets]          # Data target definitions
[transformations]  # Data transformation rules
[validation]       # Data validation rules
```

### COS Source Configuration

To configure a COS data source, add a `[sourceN]` section with type `COS`:

```ini
[sources]
count = 1

[source1]
name = xbond-quote-cos
type = COS
connectionString = cos://your-bucket-name
primaryKeyField = id

# COS-specific properties
cos.endpoint = https://cos.ap-beijing.myqcloud.com
cos.bucket = your-bucket-name
cos.secretId = your-secret-id-here
cos.secretKey = your-secret-key-here
cos.region = ap-beijing
cos.prefix = AllPriceDepth/
cos.maxFileSize = 104857600  # 100MB in bytes (optional)
```

### Configuration Properties Reference

| Property | Required | Description | Example |
|----------|----------|-------------|---------|
| `name` | Yes | Unique source identifier | `xbond-quote-cos` |
| `type` | Yes | Source type (must be `COS`) | `COS` |
| `connectionString` | Yes | URI-style connection string | `cos://bucket-name` |
| `primaryKeyField` | Yes | Primary key field name | `id` |
| `cos.endpoint` | Yes | COS service endpoint URL | `https://cos.ap-beijing.myqcloud.com` |
| `cos.bucket` | Yes | COS bucket name | `xbond-quote-data` |
| `cos.secretId` | Yes | Tencent Cloud SecretId | `AKIDexample123456789` |
| `cos.secretKey` | Yes | Tencent Cloud SecretKey | `secret-key-example` |
| `cos.region` | Yes | COS bucket region | `ap-beijing` |
| `cos.prefix` | No | Path prefix for file filtering | `AllPriceDepth/` |
| `cos.maxFileSize` | No | Maximum file size in bytes (default: 104857600) | `104857600` |

### Security Best Practices

1. **Never commit credentials**: Use environment variables or secure configuration management
2. **Placeholder detection**: The system detects common placeholder patterns (`changeme`, `placeholder`, `example`, `fake`, `dummy`, `your-*`)
3. **Secure logging**: Credentials are never included in logs or error messages

#### Using Environment Variables

```bash
# Set environment variables (Linux/Mac)
export COS_SECRET_ID="your-secret-id"
export COS_SECRET_KEY="your-secret-key"

# Set environment variables (Windows PowerShell)
$env:COS_SECRET_ID = "your-secret-id"
$env:COS_SECRET_KEY = "your-secret-key"
```

Then reference them in configuration:

```ini
cos.secretId = ${COS_SECRET_ID}
cos.secretKey = ${COS_SECRET_KEY}
```

## Usage

### Basic Extraction Command

```bash
java -jar target/sdd-etl-tool-*.jar \
  --config .etlconfig.ini \
  --date 20250101
```

### Command Line Options

| Option | Description | Required |
|--------|-------------|----------|
| `--config <file>` | Path to configuration file | Yes |
| `--date <YYYYMMDD>` | Business date for extraction | Yes |
| `--log-level <LEVEL>` | Logging level (TRACE, DEBUG, INFO, WARN, ERROR) | No |
| `--help` | Display help information | No |

### Running with Maven

```bash
# Direct execution (development)
mvn exec:java -Dexec.mainClass="com.sdd.etl.Main" \
  -Dexec.args="--config .etlconfig.ini --date 20250101"
```

### Output Locations

- **Extracted Data**: Stored in `ETLContext` for downstream processing
- **Log Files**: Default: `./etl.log` (configurable via `[logging].logFilePath`)
- **Temporary Files**: Downloaded to `./work/<date>/<category>/` during processing

## File Selection Rules

### Default File Pattern

The system uses the following pattern to select files:
```
/<prefix>/<category>/<business_date>/*.csv
```

Where:
- `prefix`: Configured via `cos.prefix` (default: empty)
- `category`: Extractor-specific (for Xbond Quote: `AllPriceDepth`)
- `business_date`: Provided via `--date` parameter (format: `YYYYMMDD`)

### Example File Paths

For date `20250101` with `cos.prefix = AllPriceDepth/`:
- ✅ `AllPriceDepth/AllPriceDepth/20250101/file1.csv` (selected)
- ✅ `AllPriceDepth/AllPriceDepth/20250101/file2.csv` (selected)
- ❌ `AllPriceDepth/AllPriceDepth/20250102/file3.csv` (wrong date)
- ❌ `AllPriceDepth/OtherCategory/20250101/file4.csv` (wrong category)

### Mixed-Date File Handling

If a CSV file contains records for multiple dates:
- All records are parsed
- Records filtered to the target `business_date`
- Non-matching records are discarded

## Performance Monitoring

### Built-in Metrics

The system automatically tracks:
- **File selection time**: Duration to list and filter COS files
- **Download time**: Time to download each file
- **Parsing time**: CSV parsing duration
- **Conversion time**: Raw record to SourceDataModel conversion
- **Total extraction time**: End-to-end extraction duration

### Structured Logging Example

```json
{
  "timestamp": "2026-01-10T10:30:15.123Z",
  "level": "INFO",
  "category": "EXTRACT",
  "selectedFiles": 5,
  "extractedRecords": 1250,
  "downloadDurationMs": 3200,
  "parseDurationMs": 820,
  "convertDurationMs": 400,
  "totalDurationMs": 4520,
  "status": "SUCCESS",
  "source": "xbond-quote-cos"
}
```

### Performance Tuning

#### Configuring Timeouts

```ini
# In your configuration (if supported by extractor)
extraction.timeout = 1800  # 30 minutes in seconds
```

#### Concurrent Processing

The system automatically uses thread pool based on:
- Number of configured sources
- Available processors (`Runtime.getRuntime().availableProcessors()`)
- Maximum of `sources.size()` or `processors * 2`

## Error Handling

### Common Errors

| Error | Cause | Solution |
|-------|-------|----------|
| `Invalid COS credentials` | Invalid SecretId/SecretKey | Verify credentials and permissions |
| `File too large` | File exceeds `cos.maxFileSize` | Increase limit or split large files |
| `No matching files` | No files match selection rules | Verify date, prefix, and file patterns |
| `Download failed` | Network or permission issue | Check network connectivity and bucket permissions |
| `Parse error` | Invalid CSV format | Verify CSV file structure |

### Structured Error Messages

Errors include:
- Error code and category
- Timestamp
- Detailed message
- File name (if applicable)
- Suggested actions

Example error log:
```json
{
  "timestamp": "2026-01-10T10:35:22.456Z",
  "level": "ERROR",
  "category": "EXTRACT",
  "errorCode": "COS_DOWNLOAD_FAILED",
  "message": "Failed to download file: AllPriceDepth/20250101/file1.csv",
  "detail": "Connection timeout after 30 seconds",
  "file": "AllPriceDepth/20250101/file1.csv",
  "suggestedAction": "Check network connectivity and bucket permissions"
}
```

### Recovery Procedures

1. **Transient failures**: System retries with exponential backoff
2. **Credential issues**: Update configuration and restart
3. **File size limits**: Adjust `cos.maxFileSize` or process files separately
4. **Network issues**: Verify connectivity and proxy settings

## Troubleshooting

### Diagnostic Commands

```bash
# Verify configuration parsing
mvn test -Dtest=ConfigurationLoaderTest

# Test COS connectivity (requires valid credentials)
mvn test -Dtest=CosClientTest

# Run integration tests
mvn test -Dtest=XbondQuoteExtractorIntegrationTest
```

### Common Issues

#### Issue: "No data sources configured"
**Solution**: 
1. Verify `[sources].count` matches number of `[sourceN]` sections
2. Ensure each source has required fields (`name`, `type`, `connectionString`, `primaryKeyField`)

#### Issue: "Invalid endpoint URL"
**Solution**:
1. Verify `cos.endpoint` format: `https://cos.<region>.myqcloud.com`
2. Check region matches bucket region

#### Issue: "Placeholder credentials detected"
**Solution**:
1. Replace placeholder values with actual credentials
2. Avoid using: `changeme`, `placeholder`, `example`, `fake`, `dummy`, `your-*`

#### Issue: "Extraction timeout"
**Solution**:
1. Check network connectivity to COS endpoints
2. Verify file sizes are within limits
3. Consider increasing timeout in extractor configuration

### Log Analysis

#### Enabling Debug Logging

```ini
[logging]
logLevel = DEBUG
logFilePath = ./etl-debug.log
```

#### Key Log Categories

- `EXTRACT`: Extraction process events
- `COS_CLIENT`: COS SDK interactions
- `CONFIG`: Configuration loading and validation
- `PERFORMANCE`: Timing and performance metrics

## API Reference

### Extractor Interface

```java
public interface Extractor {
    String getCategory();
    void setup(ETLContext context) throws ETLException;
    List<SourceDataModel> extract(ETLContext context) throws ETLException;
    void cleanup(ETLContext context) throws ETLException;
    String getName();
    boolean validate(ETLContext context) throws ETLException;
}
```

### CosExtractor Base Class

Key methods:
- `selectFiles(ETLContext context)`: Selects COS files based on rules
- `downloadFiles(List<CosFileMetadata> files)`: Downloads selected files
- `parseAllFiles(List<File> localFiles)`: Parses CSV files to raw records
- `convertRawRecords(List<RawQuoteRecord> rawRecords)`: Converts to SourceDataModel

### CosClient Interface

```java
public interface CosClient {
    List<CosFileMetadata> listObjects(String prefix) throws ETLException;
    InputStream downloadObject(String key) throws ETLException;
    void close() throws ETLException;
}
```

## Advanced Topics

### Custom Extractors

To create a custom extractor:

1. Implement `Extractor` interface
2. Register with `ExtractorFactory`
3. Add configuration parsing in `ConfigurationLoader`
4. Write unit and integration tests

### Integration with Other Systems

#### Database Extractors
- Implement JDBC-based extraction
- Use connection pooling for performance
- Support parameterized queries with date parameters

#### API Extractors
- Implement REST/GraphQL clients
- Handle pagination and rate limiting
- Support authentication mechanisms

### Monitoring and Alerting

#### Integration Points
- Structured logs can be ingested by monitoring systems (ELK, Splunk, Datadog)
- Metrics can be exported to Prometheus or similar systems
- Alert rules can be defined based on error patterns and performance thresholds

## Support

### Getting Help

1. **Configuration issues**: Check error logs for validation failures
2. **Performance issues**: Review performance metrics in structured logs
3. **Integration issues**: Verify API compatibility and network connectivity

### Reporting Issues

When reporting issues, include:
- Configuration file (with credentials redacted)
- Error logs (full JSON entries)
- Environment details (Java version, OS, network setup)
- Steps to reproduce

### Further Resources

- [Tencent COS Documentation](https://doc.fincloud.tencent.cn/tcloud/Storage/COS)
- [Java 8 Documentation](https://docs.oracle.com/javase/8/docs/)
- [Apache Maven Documentation](https://maven.apache.org/guides/)

## Appendix

### Configuration File Example

Complete example `.etlconfig.ini`:

```ini
# ETL Tool Configuration File
# Copy to: .etlconfig.ini and modify as needed

[logging]
logFilePath = ./etl.log
logLevel = INFO

[sources]
count = 1

[source1]
name = xbond-quote-cos
type = COS
connectionString = cos://xbond-quote-data
primaryKeyField = id
cos.endpoint = https://cos.ap-beijing.myqcloud.com
cos.bucket = xbond-quote-data
cos.secretId = ${COS_SECRET_ID}
cos.secretKey = ${COS_SECRET_KEY}
cos.region = ap-beijing
cos.prefix = AllPriceDepth/
cos.maxFileSize = 104857600

[targets]
count = 1

[target1]
name = target-database
type = database
connectionString = jdbc:mysql://localhost:3306/etl_target
batchSize = 1000

[transformations]
count = 0

[validation]
count = 0
```

### CSV File Format

Xbond Quote CSV columns:
```csv
id,underlying_symbol,underlying_security_id,underlying_settlement_type,
underlying_md_entry_type,underlying_trade_volume,underlying_md_entry_px,
underlying_md_price_level,underlying_md_entry_size,underlying_un_match_qty,
underlying_yield_type,underlying_yield,transact_time,mq_partition,mq_offset,recv_time
```

### Data Conversion Mapping

| Source Column | Target Field | Transformation |
|---------------|--------------|----------------|
| `underlying_security_id` | `exch_product_id` | Append `.IB` if missing |
| `underlying_settlement_type` | `settle_speed` | `1→0`, `2→1` |
| `underlying_md_entry_type` | Bid/Offer fields | `0→bid`, `1→offer` |
| `underlying_md_price_level` | Level fields | `1→0`, `2→1`, `3→2`, etc. |
| `underlying_md_entry_px` | `bid_N_price`/`offer_N_price` | Direct mapping |
| `underlying_yield` | `bid_N_yield`/`offer_N_yield` | Direct mapping |
| `underlying_yield_type` | `bid_N_yield_type`/`offer_N_yield_type` | Direct mapping |
| `underlying_md_entry_size` | Volume fields | Level-specific mapping |

---

*Last updated: 2026-01-10*  
*Version: 2.0*