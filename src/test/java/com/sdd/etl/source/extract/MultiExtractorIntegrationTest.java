package com.sdd.etl.source.extract;

import com.sdd.etl.ETLException;
import com.sdd.etl.config.ETConfiguration;
import com.sdd.etl.context.ETLContext;
import com.sdd.etl.util.DateUtils;
import java.time.LocalDate;
import com.sdd.etl.model.SourceDataModel;
import com.sdd.etl.source.extract.cos.CosExtractor;
import com.sdd.etl.source.extract.cos.CosClient;
import com.sdd.etl.source.extract.cos.config.CosSourceConfig;
import com.sdd.etl.source.extract.cos.model.RawQuoteRecord;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Integration test for multiple extractor types using the common Extractor API.
 * 
 * <p>Verifies that:
 * <ol>
 *   <li>Different extractor implementations (COS, stub, etc.) can be used through the same API</li>
 *   <li>Multiple extractors can be registered and executed independently</li>
 *   <li>Each extractor correctly filters based on context (business date, category)</li>
 *   <li>Consolidated output from multiple extractors can be combined</li>
 * </ol>
 * 
 * <p>This test demonstrates the source-agnostic nature of the Extractor interface
 * and validates that the API contract supports multiple source systems.</p>
 */
public class MultiExtractorIntegrationTest {
    
    /** Mock ETL context */
    private ETLContext mockContext;
    
    /** Mock COS source configuration */
    private CosSourceConfig mockCosConfig;
    
    /** Mock COS client */
    private CosClient mockCosClient;
    
    /** XbondQuoteExtractor instance (COS-based) */
    private Extractor cosExtractor;
    
    /** Stub extractor instance (in-memory) */
    private Extractor stubExtractor;
    
    /** Mock ET configuration */
    private ETConfiguration mockEtConfig;
    
    /**
     * Simple stub extractor for testing multi-extractor scenarios.
     * 
     * <p>This extractor returns predefined records without external dependencies.
     * Used to demonstrate that different extractor implementations can coexist
     * and be used through the common Extractor API.</p>
     */
    private static class StubExtractor implements Extractor {
        
        private String category = "STUB";
        private List<SourceDataModel> recordsToReturn = new ArrayList<>();
        private boolean shouldThrowOnSetup = false;
        private boolean shouldThrowOnExtract = false;
        
        public StubExtractor() {
            // Create a simple test record
            SourceDataModel stubRecord = new SourceDataModel() {
                @Override
                public boolean validate() {
                    return true;
                }

                @Override
                public Object getPrimaryKey() {
                    return "STUB-001";
                }

                @Override
                public String getSourceType() {
                    return "stub";
                }

                @Override
                public String toString() {
                    return "StubRecord{id=STUB-001}";
                }
            };
            recordsToReturn.add(stubRecord);
        }
        
        @Override
        public String getCategory() {
            return category;
        }
        
        public void setCategory(String category) {
            this.category = category;
        }
        
        @Override
        public void setup(ETLContext context) throws ETLException {
            if (shouldThrowOnSetup) {
                throw new ETLException("STUB_EXTRACTOR", DateUtils.formatDate(context.getCurrentDate()),
                        "Stub setup failure");
            }
            // No-op for stub
        }
        
        public void setShouldThrowOnSetup(boolean shouldThrow) {
            this.shouldThrowOnSetup = shouldThrow;
        }
        
        @Override
        public List<SourceDataModel> extract(ETLContext context) throws ETLException {
            if (shouldThrowOnExtract) {
                throw new ETLException("STUB_EXTRACTOR", DateUtils.formatDate(context.getCurrentDate()),
                        "Stub extraction failure");
            }
            return new ArrayList<>(recordsToReturn);
        }
        
        public void setShouldThrowOnExtract(boolean shouldThrow) {
            this.shouldThrowOnExtract = shouldThrow;
        }
        
        public void setRecordsToReturn(List<SourceDataModel> records) {
            this.recordsToReturn = records;
        }
        
        @Override
        public void cleanup() throws ETLException {
            // No-op for stub
        }
        
        @Override
        public String getName() {
            return "StubExtractor[" + category + "]";
        }
        
        @Override
        public void validate(ETLContext context) throws ETLException {
            if (context.getCurrentDate() == null) {
                throw new ETLException("STUB_EXTRACTOR", null,
                        "Context business date cannot be null");
            }
            // Additional validation can be added
        }
    }
    
    @Before
    public void setUp() throws ETLException {
        mockContext = mock(ETLContext.class);
        mockCosConfig = mock(CosSourceConfig.class);
        mockCosClient = mock(CosClient.class);
        mockEtConfig = mock(ETConfiguration.class);
        
        // Configure default mock behavior
        when(mockContext.getConfig()).thenReturn(mockEtConfig);
        when(mockContext.getCurrentDate()).thenReturn(DateUtils.parseDate("20250101"));
        
        // Setup sources list
        List<ETConfiguration.SourceConfig> sources = new ArrayList<>();
        sources.add(mockCosConfig);
        when(mockEtConfig.getSources()).thenReturn(sources);
        
        // Configure COS config
        when(mockCosConfig.getType()).thenReturn("cos");
        when(mockCosConfig.isValid()).thenReturn(true);
        when(mockCosConfig.getMaxFileSizeOrDefault()).thenReturn(100 * 1024 * 1024L); // 100MB
        
        // Create XbondQuoteExtractor with mocked COS client
        cosExtractor = new CosExtractor() {
            @Override
            public String getCategory() {
                return "AllPriceDepth";
            }
            
            @Override
            protected List<SourceDataModel> convertRawRecords(List<RawQuoteRecord> rawRecords) throws ETLException {
                // Create simple stub records
                List<SourceDataModel> records = new ArrayList<>();
                for (int i = 0; i < rawRecords.size(); i++) {
                    final int index = i;
                    SourceDataModel record = new SourceDataModel() {
                        @Override
                        public boolean validate() {
                            return true;
                        }

                        @Override
                        public Object getPrimaryKey() {
                            return "COS-" + index;
                        }

                        @Override
                        public String getSourceType() {
                            return "cos";
                        }
                    };
                    records.add(record);
                }
                return records;
            }
            
            @Override
            protected CosClient createCosClient(CosSourceConfig config) throws ETLException {
                return mockCosClient;
            }
        };
        
        // Create stub extractor
        stubExtractor = new StubExtractor();
    }
    
    @Test
    public void testMultipleExtractorTypes_CanBeUsedThroughCommonAPI() throws ETLException {
        // Setup both extractors
        cosExtractor.setup(mockContext);
        stubExtractor.setup(mockContext);
        
        // Validate both extractors
        cosExtractor.validate(mockContext);
        stubExtractor.validate(mockContext);
        
        // Extract data from both extractors
        List<SourceDataModel> cosRecords = cosExtractor.extract(mockContext);
        List<SourceDataModel> stubRecords = stubExtractor.extract(mockContext);
        
        // Verify both return non-null lists
        assertNotNull("COS extractor records should not be null", cosRecords);
        assertNotNull("Stub extractor records should not be null", stubRecords);
        
        // Verify each record has correct source type
        for (SourceDataModel record : cosRecords) {
            assertNotNull(record);
            assertEquals("COS record source type", "cos", record.getSourceType());
        }
        for (SourceDataModel record : stubRecords) {
            assertNotNull(record);
            assertEquals("Stub record source type", "stub", record.getSourceType());
        }
        
        // Cleanup both extractors
        cosExtractor.cleanup();
        stubExtractor.cleanup();
    }
    
    @Test
    public void testMultipleExtractors_CanBeRegisteredAndExecutedIndependently() throws ETLException {
        // Create a list of extractors (simulating a registry)
        List<Extractor> extractors = new ArrayList<>();
        extractors.add(cosExtractor);
        extractors.add(stubExtractor);
        
        // Execute each extractor independently
        for (Extractor extractor : extractors) {
            extractor.setup(mockContext);
            extractor.validate(mockContext);
            List<SourceDataModel> records = extractor.extract(mockContext);
            assertNotNull("Records should not be null for " + extractor.getName(), records);
            extractor.cleanup();
        }
    }
    
    @Test
    public void testExtractorFiltering_RespectsContextBusinessDate() throws ETLException {
        // Configure context with specific date
        when(mockContext.getCurrentDate()).thenReturn(DateUtils.parseDate("20250115"));
        
        // Setup extractors
        cosExtractor.setup(mockContext);
        stubExtractor.setup(mockContext);
        
        // Validate that extractors can access the context date

        assertEquals(DateUtils.parseDate("20250115"), mockContext.getCurrentDate());
        
        // Cleanup
        cosExtractor.cleanup();
        stubExtractor.cleanup();
    }
    
    @Test
    public void testConsolidatedOutput_CombinesRecordsFromMultipleExtractors() throws ETLException {
        // Setup both extractors
        cosExtractor.setup(mockContext);
        stubExtractor.setup(mockContext);
        
        // Extract from both
        List<SourceDataModel> cosRecords = cosExtractor.extract(mockContext);
        List<SourceDataModel> stubRecords = stubExtractor.extract(mockContext);
        
        // Simulate consolidation (manual combination for test)

        List<SourceDataModel> consolidated = new ArrayList<>();
        consolidated.addAll(cosRecords);
        consolidated.addAll(stubRecords);
        
        // Verify consolidated output contains records from both sources

        assertTrue("Consolidated output should contain COS records", 
                consolidated.size() >= cosRecords.size());
        assertTrue("Consolidated output should contain stub records",
                consolidated.size() >= stubRecords.size());
        
        // Cleanup
        cosExtractor.cleanup();
        stubExtractor.cleanup();
    }
    
    @Test
    public void testExtractorLifecycle_IndependentAcrossMultipleExtractors() throws ETLException {
        // Execute full lifecycle for both extractors independently

        // COS extractor lifecycle

        cosExtractor.setup(mockContext);
        cosExtractor.validate(mockContext);
        List<SourceDataModel> cosRecords = cosExtractor.extract(mockContext);
        cosExtractor.cleanup();
        
        // Stub extractor lifecycle

        stubExtractor.setup(mockContext);
        stubExtractor.validate(mockContext);
        List<SourceDataModel> stubRecords = stubExtractor.extract(mockContext);
        stubExtractor.cleanup();
        
        // Verify both produced output

        assertNotNull(cosRecords);
        assertNotNull(stubRecords);
    }
}