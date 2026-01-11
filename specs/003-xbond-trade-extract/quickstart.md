# Quick Start Guide: Xbond Trade Extraction

## Overview

This guide provides step-by-step instructions for configuring and using the Xbond Trade extraction feature. The trade extractor follows the same patterns as the existing quote extractor, making integration straightforward for users familiar with the ETL system.

## Prerequisites

- Java 8 JDK installed
- Maven 3.6+ installed
- Tencent COS account with appropriate credentials
- Existing sdd-etl-tool installation

## Configuration Steps

### 1. Add COS Configuration

Edit your `.etlconfig.ini` file to add a new COS source configuration for trade data:

```ini
# ====== SOURCES ======

[sources.xbond_trade]
type = cos
category = TradeData
cos.endpoint = https://cos.ap-beijing.myqcloud.com
cos.bucket = xbond-data-prod
cos.region = ap-beijing
cos.prefix = trade/
cos.secretId = ${COS_SECRET_ID}      # Use environment variable for security
cos.secretKey = ${COS_SECRET_KEY}    # Use environment variable for security
cos.maxFileSize = 104857600          # 100MB in bytes (optional, defaults to 100MB)

# Optional: Set environment variables (recommended for production)
# export COS_SECRET_ID="your-secret-id"
# export COS_SECRET_KEY="your-secret-key"
```

### 2. Set Environment Variables (Recommended)

For security, set COS credentials as environment variables:

```bash
# Linux/Mac
export COS_SECRET_ID="your-secret-id"
export COS_SECRET_KEY="your-secret-key"

# Windows PowerShell
$env:COS_SECRET_ID = "your-secret-id"
$env:COS_SECRET_KEY = "your-secret-key"
```

### 3. Verify Configuration

Run the configuration validation command:

```bash
java -jar target/sdd-etl-tool.jar validate --config .etlconfig.ini
```

Expected output includes validation of the new `xbond_trade` source.

## Running Extraction

### 1. Single-Day Extraction

Extract trade data for a specific date:

```bash
java -jar target/sdd-etl-tool.jar extract \
  --config .etlconfig.ini \
  --date 2025-01-01 \
  --source xbond_trade \
  --output ./trade_data_20250101.json
```

### 2. Multi-Day Extraction

Extract trade data for a date range:

```bash
java -jar target/sdd-etl-tool.jar extract \
  --config .etlconfig.ini \
  --start-date 2025-01-01 \
  --end-date 2025-01-07 \
  --source xbond_trade \
  --output-dir ./trade_data_week1/
```

### 3. Concurrent Extraction with Quotes

Run both trade and quote extraction simultaneously:

```bash
java -jar target/sdd-etl-tools.jar extract-all \
  --config .etlconfig.ini \
  --date 2025-01-01 \
  --output-dir ./extract_20250101/ \
  --parallel
```

## Expected CSV Format

The trade extractor expects CSV files with the following column order:

### Required Columns

1. `id` - Unique record identifier (optional)
2. `underlying_security_id` - Bond security identifier (required)
3. `underlying_settlement_type` - Settlement type: 0 (T+0) or 1 (T+1) (required)
4. `trade_price` - Executed trade price, decimal > 0 (required)
5. `trade_yield` - Trade yield, decimal (optional)
6. `trade_yield_type` - Yield calculation type string (optional)
7. `trade_volume` - Trade volume, integer > 0 (required)
8. `counterparty` - Counterparty identifier (optional)
9. `trade_id` - Unique trade identifier (required)
10. `transact_time` - Trade execution timestamp, format: `yyyyMMdd-HH:mm:ss.SSS` (required)
11. `mq_offset` - Message queue offset, integer ≥ 0 (required)
12. `recv_time` - System receive timestamp, format: `yyyyMMDD-HH:mm:ss.SSS` (required)

### Example CSV Row

```csv
id,underlying_security_id,underlying_settlement_type,trade_price,trade_yield,trade_yield_type,trade_volume,counterparty,trade_id,transact_time,mq_offset,recv_time
12345,1021001,1,100.5,2.5,YTM,1000,C001,T20250101-001,20250101-10:30:00.000,500,20250101-10:30:05.000
```

## Output Format

Extracted trade data is output in JSON format. Each record includes:

### Common Fields

```json
{
  "businessDate": "2025.01.01",
  "exchProductId": "1021001.IB",
  "productType": "BOND",
  "exchange": "CFETS",
  "source": "XBOND",
  "settleSpeed": 1,
  "level": "TRADE",
  "status": "Normal",
  "metadata": {
    "sourceType": "xbond_trade_cos",
    "extractionTimestamp": "2025-01-01T10:30:00.000Z",
    "fileCount": 3,
    "recordCount": 1500
  }
}
```

### Trade-Specific Fields

```json
{
  "tradePrice": 100.5,
  "tradeYield": 2.5,
  "tradeYieldType": "YTM",
  "tradeVolume": 1000,
  "counterparty": "C001",
  "tradeId": "T20250101-001",
  "eventTime": "2025-01-01T10:30:00",
  "receiveTime": "2025-01-01T10:30:05"
}
```

## File Selection Rules

The extractor selects files based on:

1. **Category Match**: Files under `/TradeData/` directory in COS
2. **Date Filter**: Files within `/{businessDate}/` subdirectory
3. **Extension**: `.csv` files only

### Example COS File Paths

```
xbond-data-prod/trade/TradeData/20250101/
├── xbond_trade_20250101_001.csv
├── xbond_trade_20250101_002.csv
└── xbond_trade_20250101_003.csv
```

## Troubleshooting

### Common Issues

#### 1. File Not Found

**Symptoms**: Log shows "No files selected" or empty output
**Solution**: Verify COS configuration and file paths
- Check `cos.bucket` and `cos.prefix` settings
- Ensure files exist in `/{category}/{businessDate}/` structure
- Confirm date format matches `YYYYMMDD`

#### 2. Authentication Failure

**Symptoms**: "Failed to download file" or "Access denied"
**Solution**: Verify credentials
- Check `cos.secretId` and `cos.secretKey`
- Ensure environment variables are set correctly
- Verify COS account permissions

#### 3. CSV Parsing Errors

**Symptoms**: "Failed to parse CSV row" warnings
**Solution**: Validate CSV format
- Confirm column order matches expected format
- Check timestamp format: `yyyyMMdd-HH:mm:ss.SSS`
- Ensure numeric fields contain valid numbers

#### 4. File Size Exceeded

**Symptoms**: "File size exceeds maximum allowed size"
**Solution**: Adjust configuration or file size
- Increase `cos.maxFileSize` in configuration
- Split large files into smaller chunks

### Logging

Enable debug logging for detailed troubleshooting:

```bash
java -jar target/sdd-etl-tool.jar extract \
  --config .etlconfig.ini \
  --date 2025-01-01 \
  --source xbond_trade \
  --log-level DEBUG \
  --log-file ./trade_extract_20250101.log
```

### Expected Log Output

```json
{"timestamp":"2025-01-01T10:30:00Z","level":"INFO","category":"TradeData","event":"extraction_started"}
{"timestamp":"2025-01-01T10:30:05Z","level":"INFO","category":"TradeData","event":"files_selected","fileCount":3}
{"timestamp":"2025-01-01T10:30:10Z","level":"INFO","category":"TradeData","event":"files_downloaded","fileCount":3}
{"timestamp":"2025-01-01T10:30:15Z","level":"INFO","category":"TradeData","event":"records_converted","recordCount":1500}
{"timestamp":"2025-01-01T10:30:20Z","level":"INFO","category":"TradeData","event":"extraction_completed"}
```

## Performance Considerations

### File Size Limits

- Default maximum file size: 100MB
- Configurable via `cos.maxFileSize` property
- Files exceeding limit cause extraction failure

### Concurrent Execution

- Trade extractor can run concurrently with quote extractor
- Each extractor uses separate thread pool
- Resource limits configurable via ETL context

### Memory Usage

- CSV parsing uses streaming approach
- Raw records processed in batches
- Output models aggregated before serialization

## Security Best Practices

### Credential Management

1. **Never hardcode credentials** in configuration files
2. **Use environment variables** for sensitive data
3. **Rotate credentials regularly** for production systems
4. **Apply principle of least privilege** for COS account permissions

### Logging Security

- **Never log** `cos.secretId` or `cos.secretKey`
- **Sanitize error messages** to prevent information leakage
- **Use structured logging** with controlled output

## Testing Configuration

### 1. Validate Configuration

```bash
# Test configuration without executing extraction
java -jar target/sdd-etl-tool.jar validate \
  --config .etlconfig.ini \
  --source xbond_trade
```

### 2. Dry Run Extraction

```bash
# Simulate extraction without writing output
java -jar target/sdd-etl-tool.jar extract \
  --config .etlconfig.ini \
  --date 2025-01-01 \
  --source xbond_trade \
  --dry-run
```

### 3. Sample Data Test

Create a test CSV file to verify parsing:

```bash
# Create test.csv
echo "id,underlying_security_id,underlying_settlement_type,trade_price,trade_yield,trade_yield_type,trade_volume,counterparty,trade_id,transact_time,mq_offset,recv_time
1,1021001,1,100.5,2.5,YTM,1000,C001,T20250101-001,20250101-10:30:00.000,500,20250101-10:30:05.000" > test.csv
```

## Next Steps

After successful extraction:

1. **Review output data** for completeness and accuracy
2. **Monitor performance** for large file processing
3. **Integrate with downstream ETL steps** (transform, load)
4. **Schedule regular extraction** using cron or workflow scheduler

For advanced configuration and customization, refer to the detailed specification and data model documentation.