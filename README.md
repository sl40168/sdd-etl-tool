# SDD ETL Tool

A Java-based ETL (Extract, Transform, Load) command-line tool for processing financial data across multiple days and data sources. Built with Java 8 and Maven, this tool provides a flexible framework for data extraction from various sources including Tencent Cloud Object Storage (COS), with support for concurrent processing and structured logging.

## Features

- **Multi-source Extraction**: Concurrent extraction from multiple data sources (COS, databases, APIs)
- **COS Integration**: Built-in support for Tencent Cloud Object Storage (COS) with file filtering and validation
- **Structured Logging**: JSON-formatted operational logs for monitoring and debugging
- **Performance Monitoring**: Built-in timing hooks for performance measurement
- **Security Hardening**: Placeholder credential detection and secure configuration handling
- **Configuration-Driven**: INI-based configuration system for flexible deployment
- **Extensible Architecture**: Plugin-based extractor API for adding new data sources

## Quick Start

### Prerequisites
- Java 8 or higher
- Maven 3.6+ (or use included Maven wrapper)
- Tencent COS credentials (for COS extraction)

### Installation
1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd sdd-etl-tool
   ```

2. Build the project:
   ```bash
   mvn clean package
   ```
   or using the Maven wrapper:
   ```bash
   ./mvnw clean package
   ```

3. Create configuration file:
   ```bash
   cp .etlconfig.ini.example .etlconfig.ini
   ```
   Edit `.etlconfig.ini` with your data source configurations.

### Configuration Example (COS Source)

Add the following to your `.etlconfig.ini` file:

```ini
[sources]
count = 1

[source1]
name = xbond-quote-cos
type = COS
connectionString = cos://example-bucket
primaryKeyField = id

# COS-specific properties
cos.endpoint = https://cos.ap-beijing.myqcloud.com
cos.bucket = example-bucket
cos.secretId = your-secret-id-here
cos.secretKey = your-secret-key-here
cos.region = ap-beijing
cos.prefix = AllPriceDepth/
cos.maxFileSize = 104857600  # 100MB in bytes
```

### Running the ETL Process

```bash
java -jar target/sdd-etl-tool-*.jar --config .etlconfig.ini --date 20250101
```

## DolphinDB Loader Feature

The DolphinDB loader enables efficient data loading into DolphinDB time-series database with automatic sorting and table management. Key capabilities:

### Supported Data Types
- **Xbond Quote Data**: 83 fields → `xbond_quote_stream_temp` table
- **Xbond Trade Data**: 15 fields → `xbond_trade_stream_temp` table  
- **Bond Future Quote Data**: 96 fields → `fut_market_price_stream_temp` table

### Core Features
- **Automatic Sorting**: External merge sort for memory-efficient processing of large datasets
- **Connection Management**: Shared connection pool with automatic reconnection
- **Error Handling**: Comprehensive exception handling with forensic table preservation
- **Performance Optimization**: Configurable memory limits and batch sizes

### Configuration Example
Add the following to your `.etlconfig.ini` file:

```ini
[loader]
type = DOLPHINDB
host = localhost
port = 8848
username = admin
password = 123456

# Sorting configuration
sort.memory.limit = 512MB
sort.buffer.size = 10000

# Table configuration
table.xbond.quote = xbond_quote_stream_temp
table.xbond.trade = xbond_trade_stream_temp
table.bond.future.quote = fut_market_price_stream_temp
```

### Usage
The loader automatically handles:
1. Data sorting by primary key/date fields
2. Temporary table creation for data loading
3. Data validation and type conversion
4. Error recovery and logging

## COS Extraction Feature

The COS extraction feature enables retrieval of Xbond Quote data from Tencent Cloud Object Storage. Key capabilities:

### File Selection
- Configurable file matching rules based on context (date, category)
- Prefix-based filtering with wildcard support
- Date-based record filtering within mixed-date files

### Validation & Security
- File size validation (configurable threshold, default 100MB)
- Placeholder credential detection
- Secure credential handling (never logged)
- Structured error reporting

### Performance Features
- Concurrent file download and processing
- Performance timing hooks
- Memory-efficient streaming CSV parsing
- Configurable timeouts

### Structured Logging
Example log entry:
```json
{
  "timestamp": "2026-01-10T10:30:15.123Z",
  "level": "INFO",
  "category": "EXTRACT",
  "selectedFiles": 5,
  "extractedRecords": 1250,
  "durationMs": 4520,
  "status": "SUCCESS"
}
```

## Architecture

### Core Components
- **Extractor Interface**: Common API for all data sources
- **CosExtractor**: COS-specific implementation with file filtering
- **ExtractSubprocess**: Orchestrates concurrent extraction
- **CosClient**: Tencent COS SDK wrapper
- **ConfigurationLoader**: INI configuration parser

### Data Flow
1. **Configuration Parsing**: INI → `ETConfiguration` → `CosSourceConfig`
2. **File Selection**: COS listing → rule evaluation → filtered file list
3. **Download & Parsing**: Concurrent download → streaming CSV parsing → raw records
4. **Conversion**: Raw records → `SourceDataModel` records
5. **Consolidation**: Multi-source results → unified output set

## Development

### Building from Source
```bash
# Clean build
mvn clean compile

# Run tests
mvn test

# Generate coverage report
mvn jacoco:report
```

### Code Style
- Java 8 language features
- Follow standard Java conventions
- Unit test coverage must exceed 60%
- Use SLF4J for logging
- Structured JSON logging for operational events

### Adding New Extractors
1. Implement the `Extractor` interface
2. Register with `ExtractorFactory`
3. Add configuration parsing in `ConfigurationLoader`
4. Write comprehensive unit tests

## Testing

The project includes:
- **Unit Tests**: JUnit 4 with Mockito
- **Integration Tests**: End-to-end extraction scenarios
- **Coverage Reporting**: JaCoCo integration (>60% coverage)
- **Performance Tests**: Timing and concurrency validation

Run all tests:
```bash
mvn test
```

## Documentation

- **Constitution**: Core principles and development guidelines (`docs/Constitution.md`)
- **Specifications**: Feature specifications (`docs/v*/Specification.md`)
- **Quick Start**: This README
- **API Documentation**: Javadoc (generate with `mvn javadoc:javadoc`)

## License

Proprietary - All rights reserved.

## Support

For issues and feature requests, please contact the development team.
