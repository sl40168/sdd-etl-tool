# Quick Start: Implementing COS Xbond Quote Extraction

**Feature**: 001-cos-xbond-quote-extract  
**Target**: Java 8, Maven, CLI ETL Tool  
**Prerequisites**: Existing ETL tool codebase structure

## Overview

This guide walks through implementing a COS-based extractor for Xbond Quote data. The solution includes:
1. Defining a common Extractor API
2. Creating an abstract COS extractor base class  
3. Implementing concrete Xbond Quote extractor
4. Integrating with existing ExtractSubprocess

## Step 1: Update Dependencies

Add required libraries to `pom.xml`:

```xml
<!-- Tencent COS SDK -->
<dependency>
    <groupId>com.qcloud</groupId>
    <artifactId>cos_api</artifactId>
    <version>5.6.93</version>
</dependency>

<!-- OpenCSV for CSV parsing -->
<dependency>
    <groupId>com.opencsv</groupId>
    <artifactId>opencsv</artifactId>
    <version>5.7.1</version>
</dependency>
```

## Step 2: Create Extractor Interface

Create `src/main/java/com/sdd/etl/source/extract/Extractor.java`:

```java
package com.sdd.etl.source.extract;

import com.sdd.etl.ETLException;
import com.sdd.etl.context.ETLContext;
import com.sdd.etl.model.SourceDataModel;
import java.util.List;

public interface Extractor {
    String getCategory();
    void setup(ETLContext context) throws ETLException;
    List<SourceDataModel> extract(ETLContext context) throws ETLException;
    void cleanup() throws ETLException;
    String getName();
    void validate(ETLContext context) throws ETLException;
}
```

## Step 3: Create COS Configuration Model

Create `src/main/java/com/sdd/etl/source/extract/cos/config/CosSourceConfig.java`:

```java
package com.sdd.etl.source.extract.cos.config;

public class CosSourceConfig {
    private String endpoint;
    private String bucket;
    private String secretId;
    private String secretKey;
    private String region;
    private String prefix;
    
    // Getters, setters, validation
}
```

## Step 4: Create Raw Data Model

Create `src/main/java/com/sdd/etl/source/extract/cos/model/RawQuoteRecord.java`:

```java
package com.sdd.etl.source.extract.cos.model;

import java.time.LocalDateTime;

public class RawQuoteRecord {
    private Long id;
    private String underlyingSecurityId;
    private Integer underlyingSettlementType;
    private Integer underlyingMdEntryType;
    private Double underlyingMdEntryPx;
    private Integer underlyingMdPriceLevel;
    private Long underlyingMdEntrySize;
    private String underlyingYieldType;
    private Double underlyingYield;
    private LocalDateTime transactTime;
    private Long mqOffset;
    private LocalDateTime recvTime;
    
    // Getters, setters
}
```

## Step 5: Create Abstract COS Extractor

Create `src/main/java/com/sdd/etl/source/extract/cos/CosExtractor.java`:

```java
package com.sdd.etl.source.extract.cos;

import com.sdd.etl.source.extract.Extractor;
import com.sdd.etl.source.extract.cos.config.CosSourceConfig;
import com.sdd.etl.source.extract.cos.model.CosFileMetadata;
import com.sdd.etl.source.extract.cos.model.RawQuoteRecord;

public abstract class CosExtractor implements Extractor {
    protected CosClient cosClient;
    protected CosSourceConfig sourceConfig;
    protected File tempDirectory;
    protected List<CosFileMetadata> selectedFiles;
    
    // Implement Extractor interface methods
    // Provide abstract methods for concrete implementations
    protected abstract List<SourceDataModel> convertRawRecords(List<RawQuoteRecord> rawRecords);
    protected abstract String getBusinessDateFormat();
}
```

## Step 6: Create Xbond Quote Data Model

Create `src/main/java/com/sdd/etl/model/XbondQuoteDataModel.java`:

```java
package com.sdd.etl.model;

public class XbondQuoteDataModel extends SourceDataModel {
    private String businessDate;
    private String exchProductId;
    private String productType;
    private String exchange;
    private String source;
    private Integer settleSpeed;
    private String level;
    private String status;
    private LocalDateTime eventTime;
    private LocalDateTime receiveTime;
    
    // Bid/offer fields for levels 0-5
    private Double bid0Price;
    private Double bid0Yield;
    private String bid0YieldType;
    private Long bid0Volume;
    
    // ... additional fields ...
    
    // Getters, setters, validation
}
```

## Step 7: Implement Xbond Quote Extractor

Create `src/main/java/com/sdd/etl/source/extract/cos/XbondQuoteExtractor.java`:

```java
package com.sdd.etl.source.extract.cos;

public class XbondQuoteExtractor extends CosExtractor {
    private static final String CATEGORY = "AllPriceDepth";
    
    @Override
    public String getCategory() { return CATEGORY; }
    
    @Override
    protected List<SourceDataModel> convertRawRecords(List<RawQuoteRecord> rawRecords) {
        // Group by mq_offset
        // Convert each group to XbondQuoteDataModel
        // Implementation details:
        // 1. Map raw fields based on price level (1-6) and entry type (0=bid, 1=offer)
        // 2. Add ".IB" suffix to underlying_security_id if missing
        // 3. Map settlement type: 1→0 (T+0), 2→1 (T+1)
        // 4. Set constant fields: product_type="BOND", exchange="CFETS", source="XBOND"
        // 5. Set timestamps from transact_time and recv_time
    }
}
```

## Step 8: Update INI Configuration

Extend INI configuration parsing in `ConfigurationLoader.java`:

```java
// Add method to parse COS-specific configuration
void parseCosSourceConfig(INIConfiguration iniConfig, ETConfiguration config) {
    // Parse COS-specific parameters:
    // cos.endpoint, cos.bucket, cos.secretId, cos.secretKey, cos.region, cos.prefix
}
```

Example INI configuration:

```ini
[source1]
name=xbond_quote_cos
type=cos
connectionString=cos://xbond-quote-data/AllPriceDepth/
cos.endpoint=cos.ap-shanghai.myqcloud.com
cos.bucket=xbond-quote-data
cos.secretId=${COS_SECRET_ID}
cos.secretKey=${COS_SECRET_KEY}
cos.region=ap-shanghai
dateField=transact_time
```

## Step 9: Update Extract Subprocess

Modify `src/main/java/com/sdd/etl/subprocess/ExtractSubprocess.java`:

```java
package com.sdd.etl.subprocess;

import com.sdd.etl.source.extract.Extractor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ExtractSubprocess extends SubprocessInterface {
    
    @Override
    public int execute(ETLContext context) throws ETLException {
        List<Extractor> extractors = createExtractors(context);
        
        // Execute extractors concurrently
        ExecutorService executor = Executors.newFixedThreadPool(extractors.size());
        List<Future<List<SourceDataModel>>> futures = new ArrayList<>();
        
        for (Extractor extractor : extractors) {
            futures.add(executor.submit(() -> extractor.extract(context)));
        }
        
        // Collect results and handle failures
        List<SourceDataModel> allRecords = new ArrayList<>();
        for (int i = 0; i < futures.size(); i++) {
            try {
                List<SourceDataModel> records = futures.get(i).get();
                allRecords.addAll(records);
            } catch (Exception e) {
                // If any extractor fails, fail the entire extraction
                throw new ETLException("EXTRACT", context.getCurrentDate(),
                        "Extractor failed: " + extractors.get(i).getName(), e);
            }
        }
        
        // Shutdown executor
        executor.shutdown();
        
        // Store extracted records in context for downstream processing
        context.setExtractedRecords(allRecords);
        
        return allRecords.size();
    }
    
    private List<Extractor> createExtractors(ETLContext context) {
        // Factory method to create extractors based on configuration
        List<Extractor> extractors = new ArrayList<>();
        
        for (ETConfiguration.SourceConfig sourceConfig : context.getConfig().getSources()) {
            if ("cos".equals(sourceConfig.getType())) {
                Extractor extractor = createCosExtractor(sourceConfig);
                extractors.add(extractor);
            }
        }
        
        return extractors;
    }
    
    private Extractor createCosExtractor(ETConfiguration.SourceConfig sourceConfig) {
        // Determine which concrete COS extractor to create based on category
        String category = parseCategoryFromConfig(sourceConfig);
        
        switch (category) {
            case "AllPriceDepth":
                return new XbondQuoteExtractor();
            // Add other COS extractor types as needed
            default:
                throw new IllegalArgumentException("Unsupported COS extractor category: " + category);
        }
    }
}
```

## Step 10: Implement CSV Parsing

Create `src/main/java/com/sdd/etl/source/extract/cos/CsvParser.java`:

```java
package com.sdd.etl.source.extract.cos;

import com.opencsv.CSVReader;
import com.sdd.etl.ETLException;
import com.sdd.etl.source.extract.cos.model.RawQuoteRecord;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CsvParser {
    
    private static final DateTimeFormatter TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss.SSS");
    
    public List<RawQuoteRecord> parse(File csvFile) throws ETLException {
        List<RawQuoteRecord> records = new ArrayList<>();
        
        try (CSVReader reader = new CSVReader(new FileReader(csvFile))) {
            String[] headers = reader.readNext();
            
            String[] line;
            while ((line = reader.readNext()) != null) {
                RawQuoteRecord record = createRecord(headers, line);
                records.add(record);
            }
            
        } catch (Exception e) {
            throw new ETLException("CSV_PARSE", "FILE",
                    "Failed to parse CSV file: " + csvFile.getName(), e);
        }
        
        return records;
    }
    
    private RawQuoteRecord createRecord(String[] headers, String[] values) {
        RawQuoteRecord record = new RawQuoteRecord();
        
        for (int i = 0; i < headers.length; i++) {
            if (i < values.length) {
                setField(record, headers[i].trim(), values[i].trim());
            }
        }
        
        return record;
    }
    
    private void setField(RawQuoteRecord record, String fieldName, String value) {
        if (value == null || value.isEmpty()) {
            return;
        }
        
        switch (fieldName) {
            case "id":
                record.setId(Long.parseLong(value));
                break;
            case "underlying_security_id":
                record.setUnderlyingSecurityId(value);
                break;
            case "underlying_settlement_type":
                record.setUnderlyingSettlementType(Integer.parseInt(value));
                break;
            case "underlying_md_entry_type":
                record.setUnderlyingMdEntryType(Integer.parseInt(value));
                break;
            case "underlying_md_entry_px":
                record.setUnderlyingMdEntryPx(Double.parseDouble(value));
                break;
            case "underlying_md_price_level":
                record.setUnderlyingMdPriceLevel(Integer.parseInt(value));
                break;
            case "underlying_md_entry_size":
                record.setUnderlyingMdEntrySize(Long.parseLong(value));
                break;
            case "underlying_yield_type":
                record.setUnderlyingYieldType(value);
                break;
            case "underlying_yield":
                record.setUnderlyingYield(Double.parseDouble(value));
                break;
            case "transact_time":
                record.setTransactTime(LocalDateTime.parse(value, TIME_FORMATTER));
                break;
            case "mq_offset":
                record.setMqOffset(Long.parseLong(value));
                break;
            case "recv_time":
                record.setRecvTime(LocalDateTime.parse(value, TIME_FORMATTER));
                break;
            // Ignore other fields
        }
    }
}
```

## Step 11: Create Unit Tests

Create test classes for each component:

1. `XbondQuoteExtractorTest.java` - Test conversion logic
2. `CsvParserTest.java` - Test CSV parsing with sample data
3. `CosExtractorIntegrationTest.java` - Integration tests with mocked COS
4. `XbondQuoteDataModelTest.java` - Test validation rules

Example test structure:

```java
package com.sdd.etl.source.extract.cos;

import org.junit.Test;
import static org.junit.Assert.*;

public class XbondQuoteExtractorTest {
    
    @Test
    public void testConvertRawRecords_ValidInput() {
        // Arrange: Create raw records with known mq_offset groups
        
        // Act: Call convertRawRecords
        
        // Assert: Verify converted records match expected mapping
        // - Fields mapped correctly based on price level and entry type
        // - ".IB" suffix added to security_id if missing

        // - Settlement type mapped correctly (1→0, 2→1)

        // - Constant fields set correctly
    }
    
    @Test(expected = ETLException.class)
    public void testConvertRawRecords_InvalidSettlementType() {
        // Test with invalid settlement type (e.g., 3)

    }
    
    @Test
    public void testGroupingByMqOffset() {
        // Verify records are grouped correctly by mq_offset

        // Each group produces one output record

    }
}
```

## Step 12: Integration with Workflow

Ensure the extractor integrates with existing ETL workflow:

1. **Configuration**: Update `ETConfiguration` to support COS source type
2. **Factory**: Create `ExtractorFactory` to instantiate extractors based on config
3. **Error Handling**: Implement proper error propagation and logging
4. **Performance**: Add metrics for extraction performance monitoring

## Step 13: Build and Test

Run the complete build and test cycle:

```bash
# Clean and build
./mvnw clean compile

# Run unit tests
./mvnw test

# Generate test coverage report
./mvnw jacoco:report

# Create executable JAR
./mvnw package
```

## Step 14: Multi-Source Concurrent Extraction

The enhanced `ExtractSubprocess` now supports concurrent extraction from multiple data sources. The implementation includes:

1. **MultiSourceExtractSubprocess** - Inner class that manages concurrent execution
2. **ExecutorService-based parallel extraction** - Uses thread pool for performance
3. **Result consolidation and error handling** - Graceful handling of partial failures

### Concurrent Extraction Implementation

The `MultiSourceExtractSubprocess` inner class in `ExtractSubprocess.java` provides concurrent execution:

```java
// MultiSourceExtractSubprocess inner class in ExtractSubprocess.java
public static class MultiSourceExtractSubprocess extends ExtractSubprocess {
    @Override
    public int execute(ETLContext context) throws ETLException {
        List<Extractor> extractors = createExtractors(context);
        
        // Create thread pool sized to number of extractors
        ExecutorService executor = Executors.newFixedThreadPool(extractors.size());
        List<Future<List<SourceDataModel>>> futures = new ArrayList<>();
        
        // Submit all extraction tasks
        for (Extractor extractor : extractors) {
            futures.add(executor.submit(() -> extractor.extract(context)));
        }
        
        // Collect results and handle errors
        List<SourceDataModel> allRecords = new ArrayList<>();
        int successfulExtractors = 0;
        List<Exception> errors = new ArrayList<>();
        
        for (int i = 0; i < futures.size(); i++) {
            try {
                List<SourceDataModel> records = futures.get(i).get();
                allRecords.addAll(records);
                successfulExtractors++;
            } catch (ExecutionException e) {
                errors.add(e);
                // Continue processing other extractors
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ETLException("EXTRACT", context.getCurrentDate(),
                        "Extraction interrupted", e);
            }
        }
        
        executor.shutdown();
        context.setExtractedData(allRecords);
        context.setExtractedDataCount(allRecords.size());
        
        // If all extractors failed, throw exception
        if (successfulExtractors == 0 && !errors.isEmpty()) {
            throw new ETLException("EXTRACT", context.getCurrentDate(),
                    "All extractors failed: " + errors.get(0).getMessage());
        }
        
        return allRecords.size();
    }
}
```

### ExtractorFactory Singleton Pattern

The `ExtractorFactory` uses a singleton pattern with dependency injection for testing:

```java
// ExtractorFactory.java - Singleton with test support
public class ExtractorFactory {
    private static ExtractorFactory INSTANCE = new ExtractorFactory();
    
    protected ExtractorFactory() {}
    
    public static void setInstance(ExtractorFactory factory) {
        INSTANCE = factory;
    }
    
    public static Extractor createExtractor(ETConfiguration.SourceConfig sourceConfig) throws ETLException {
        return INSTANCE.createExtractorInstance(sourceConfig);
    }
    
    protected Extractor createExtractorInstance(ETConfiguration.SourceConfig sourceConfig) throws ETLException {
        // Factory logic here...
    }
}
```

### Integration with DailyETLWorkflow

Update `DailyETLWorkflow.java` to use the enhanced extractor:

```java
// DailyETLWorkflow.java - Updated to use MultiSourceExtractSubprocess
public class DailyETLWorkflow extends AbstractWorkflow {
    @Override
    protected SubprocessInterface createExtractSubprocess() {
        return new ExtractSubprocess.MultiSourceExtractSubprocess();
    }
}
```

## Troubleshooting

| Issue | Solution |
|-------|----------|
| COS SDK compatibility errors | Ensure using Java 8 compatible version (5.6.93) |
| CSV parsing memory issues | Use streaming parser (OpenCSV) and process in batches |
| Concurrent extractor failures | Ensure thread-safe implementation, use separate resources |
| INI configuration parsing errors | Validate all required COS parameters are present |

## Next Steps

After successful implementation:

1. **Monitoring**: Add extraction metrics and logging
2. **Optimization**: Profile and optimize performance bottlenecks  
3. **Testing**: Create integration tests with actual COS test bucket
4. **Documentation**: Update user documentation for COS extraction feature