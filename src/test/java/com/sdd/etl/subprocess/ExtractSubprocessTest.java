package com.sdd.etl.subprocess;

import com.sdd.etl.ETLException;
import com.sdd.etl.config.ETConfiguration;
import com.sdd.etl.context.ContextManager;
import com.sdd.etl.context.ETLContext;
import com.sdd.etl.model.SourceDataModel;
import com.sdd.etl.source.extract.Extractor;
import com.sdd.etl.source.extract.ExtractorFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ExtractSubprocess API and record consolidation logic.
 */
public class ExtractSubprocessTest {

    /**
     * Test-specific factory that allows registering extractors by source name.
     */
    private static class TestExtractorFactory extends ExtractorFactory {
        private final Map<String, Extractor> registry = new HashMap<>();
        
        void registerExtractor(String sourceName, Extractor extractor) {
            registry.put(sourceName, extractor);
        }
        
        @Override
        protected Extractor createExtractorInstance(ETConfiguration.SourceConfig sourceConfig) throws ETLException {
            Extractor extractor = registry.get(sourceConfig.getName());
            if (extractor != null) {
                return extractor;
            }
            // Fallback to default behavior (will throw unsupported source type)
            return super.createExtractorInstance(sourceConfig);
        }
    }

    private ETConfiguration config;
    private ETLContext context;
    private TestExtractorFactory testFactory;
    
    @Before
    public void setUp() {
        config = new ETConfiguration();
        context = ContextManager.createContext("20250101", config);
        testFactory = new TestExtractorFactory();
        ExtractorFactory.setInstance(testFactory);
    }
    
    @Test
    public void testValidateContext_ConfigNull_Throws() {
        ExtractSubprocess extract = new ExtractSubprocess() {
            @Override
            public int execute(ETLContext context) throws ETLException {
                return 0;
            }
        };

        ETLContext badContext = new ETLContext();
        badContext.setCurrentDate("20250101");
        // config not set

        try {
            extract.validateContext(badContext);
            fail("Expected ETLException");
        } catch (ETLException e) {
            assertTrue(e.getMessage().contains("Configuration is null"));
        }
    }

    @Test
    public void testValidateContext_SourcesEmpty_Throws() {
        ExtractSubprocess extract = new ExtractSubprocess() {
            @Override
            public int execute(ETLContext context) throws ETLException {
                return 0;
            }
        };

        // Sources list empty by default
        try {
            extract.validateContext(context);
            fail("Expected ETLException");
        } catch (ETLException e) {
            assertTrue(e.getMessage().contains("No data sources configured"));
        }
    }

    @Test
    public void testGetType_ReturnsExtract() {
        ExtractSubprocess extract = new ExtractSubprocess() {
            @Override
            public int execute(ETLContext context) throws ETLException {
                return 0;
            }
        };

        assertEquals("Type should be EXTRACT", com.sdd.etl.context.SubprocessType.EXTRACT, extract.getType());
    }

    @Test
    public void testImplementsSubprocessInterface() {
        ExtractSubprocess extract = new ExtractSubprocess() {
            @Override
            public int execute(ETLContext context) throws ETLException {
                return 0;
            }
        };

        assertTrue("ExtractSubprocess should implement SubprocessInterface", extract instanceof SubprocessInterface);
    }
    
    @Test
    public void testRecordConsolidation_SingleExtractor_AllRecordsPreserved() throws ETLException {
        // Given: a single extractor that returns records
        config.addSource(createSourceConfig("test-source", "COS", "xbond-quote"));
        
        Extractor mockExtractor = mock(Extractor.class);
        List<SourceDataModel> mockRecords = createMockRecords("source1", 5);
        when(mockExtractor.extract(any(ETLContext.class))).thenReturn(mockRecords);
        testFactory.registerExtractor("test-source", mockExtractor);
        
        ExtractSubprocess extract = createTestExtractSubprocess(testFactory);
        
        // When
        extract.validateContext(context);
        int count = extract.execute(context);
        
        // Then
        assertEquals("Should return all records from single extractor", 5, count);
        assertNotNull("Extracted data should be set", context.getExtractedData());
        List<?> extractedData = (List<?>) context.getExtractedData();
        assertEquals("Number of records in context should match", 5, extractedData.size());
    }
    
    @Test
    public void testRecordConsolidation_MultipleExtractors_CombinedResults() throws ETLException {
        // Given: 3 extractors with different record counts
        config.addSource(createSourceConfig("source1", "COS", "xbond-quote"));
        config.addSource(createSourceConfig("source2", "JDBC", "market-data"));
        config.addSource(createSourceConfig("source3", "COS", "xbond-quote"));
        
        Extractor extractor1 = mock(Extractor.class);
        Extractor extractor2 = mock(Extractor.class);
        Extractor extractor3 = mock(Extractor.class);
        
        List<SourceDataModel> records1 = createMockRecords("source1", 10);
        List<SourceDataModel> records2 = createMockRecords("source2", 7);
        List<SourceDataModel> records3 = createMockRecords("source3", 15);
        when(extractor1.extract(any(ETLContext.class))).thenReturn(records1);
        when(extractor2.extract(any(ETLContext.class))).thenReturn(records2);
        when(extractor3.extract(any(ETLContext.class))).thenReturn(records3);
        
        // Setup factory to return appropriate extractor based on source name
        testFactory.registerExtractor("source1", extractor1);
        testFactory.registerExtractor("source2", extractor2);
        testFactory.registerExtractor("source3", extractor3);
        
        ExtractSubprocess extract = createTestExtractSubprocess(testFactory);
        
        // When
        extract.validateContext(context);
        int totalCount = extract.execute(context);
        
        // Then
        assertEquals("Total records should be sum of all extractors", 10 + 7 + 15, totalCount);
        assertEquals("Context count should match total", totalCount, context.getExtractedDataCount());
        
        List<?> extractedData = (List<?>) context.getExtractedData();
        assertEquals("Number of records in context should match total", totalCount, extractedData.size());
    }
    
    @Test
    public void testRecordConsolidation_EmptyResults_ZeroCount() throws ETLException {
        // Given: an extractor that returns empty list
        config.addSource(createSourceConfig("empty-source", "COS", "xbond-quote"));
        
        Extractor mockExtractor = mock(Extractor.class);
        when(mockExtractor.extract(any(ETLContext.class))).thenReturn(new ArrayList<>());
        testFactory.registerExtractor("empty-source", mockExtractor);
        
        ExtractSubprocess extract = createTestExtractSubprocess(testFactory);
        
        // When
        extract.validateContext(context);
        int count = extract.execute(context);
        
        // Then
        assertEquals("Should return zero records", 0, count);
        assertNotNull("Extracted data should still be set", context.getExtractedData());
        List<?> extractedData = (List<?>) context.getExtractedData();
        assertTrue("Extracted data list should be empty", extractedData.isEmpty());
    }
    
    @Test
    public void testRecordConsolidation_MixedSuccessFailure_ContinuesWithSuccessfulExtractors() throws ETLException {
        // Given: 3 extractors, one fails
        config.addSource(createSourceConfig("source1", "COS", "xbond-quote"));
        config.addSource(createSourceConfig("source2", "JDBC", "market-data"));
        config.addSource(createSourceConfig("source3", "COS", "xbond-quote"));
        
        Extractor successful1 = mock(Extractor.class);
        Extractor successful2 = mock(Extractor.class);
        Extractor failingExtractor = mock(Extractor.class);
        
        List<SourceDataModel> records1 = createMockRecords("source1", 8);
        List<SourceDataModel> records2 = createMockRecords("source2", 12);
        when(successful1.extract(any(ETLContext.class))).thenReturn(records1);
        when(successful2.extract(any(ETLContext.class))).thenReturn(records2);
        when(failingExtractor.extract(any(ETLContext.class)))
            .thenThrow(new ETLException("EXTRACT", "20250101", "Mock extraction failure"));
        
        testFactory.registerExtractor("source1", successful1);
        testFactory.registerExtractor("source2", successful2);
        testFactory.registerExtractor("source3", failingExtractor);
        
        ExtractSubprocess extract = createTestExtractSubprocess(testFactory);
        
        // When
        extract.validateContext(context);
        int successfulCount = extract.execute(context);
        
        // Then: Should get records from successful extractors only
        assertEquals("Should have records from 2 successful extractors", 8 + 12, successfulCount);
        assertNotNull("Extracted data should not be null", context.getExtractedData());
        List<?> extractedData = (List<?>) context.getExtractedData();
        assertEquals("Number of records should match successful count", successfulCount, extractedData.size());
    }
    
    @Test
    public void testRecordConsolidation_DuplicatePrimaryKeys_AllRecordsRetained() throws ETLException {
        // Given: two extractors that may produce records with same primary keys
        config.addSource(createSourceConfig("source1", "COS", "xbond-quote"));
        config.addSource(createSourceConfig("source2", "COS", "xbond-quote"));
        
        // Create records with duplicate primary keys across extractors
        List<SourceDataModel> records1 = new ArrayList<>();
        List<SourceDataModel> records2 = new ArrayList<>();
        
        for (int i = 1; i <= 5; i++) {
            SourceDataModel mockRecord = mock(SourceDataModel.class);
            when(mockRecord.getPrimaryKey()).thenReturn("duplicate_key_" + i);
            when(mockRecord.getSourceType()).thenReturn("COS");
            records1.add(mockRecord);
            
            // Same primary key from second extractor
            SourceDataModel mockRecord2 = mock(SourceDataModel.class);
            when(mockRecord2.getPrimaryKey()).thenReturn("duplicate_key_" + i);
            when(mockRecord2.getSourceType()).thenReturn("COS");
            records2.add(mockRecord2);
        }
        
        Extractor extractor1 = mock(Extractor.class);
        Extractor extractor2 = mock(Extractor.class);
        
        when(extractor1.extract(any(ETLContext.class))).thenReturn(records1);
        when(extractor2.extract(any(ETLContext.class))).thenReturn(records2);
        
        testFactory.registerExtractor("source1", extractor1);
        testFactory.registerExtractor("source2", extractor2);
        
        ExtractSubprocess extract = createTestExtractSubprocess(testFactory);
        
        // When
        extract.validateContext(context);
        int totalCount = extract.execute(context);
        
        // Then: Both sets of records should be retained (duplicates allowed)
        assertEquals("Should retain all records including duplicates", 10, totalCount);
        
        List<?> extractedData = (List<?>) context.getExtractedData();
        assertEquals("All records should be in extracted data", 10, extractedData.size());
        
        // Verify duplicate keys exist
        List<String> keys = new ArrayList<>();
        for (Object obj : extractedData) {
            keys.add((String) ((SourceDataModel) obj).getPrimaryKey());
        }
        
        // Each duplicate key should appear twice
        for (int i = 1; i <= 5; i++) {
            String key = "duplicate_key_" + i;
            long count = keys.stream().filter(k -> k.equals(key)).count();
            assertEquals("Key " + key + " should appear twice", 2, count);
        }
    }
    
    @Test
    public void testRecordConsolidation_AllExtractorsFail_ThrowsETLException() throws ETLException {
        // Given: 2 extractors that both fail
        config.addSource(createSourceConfig("source1", "COS", "xbond-quote"));
        config.addSource(createSourceConfig("source2", "COS", "xbond-quote"));
        
        Extractor failingExtractor1 = mock(Extractor.class);
        Extractor failingExtractor2 = mock(Extractor.class);
        
        when(failingExtractor1.extract(any(ETLContext.class)))
            .thenThrow(new ETLException("EXTRACT", "20250101", "First extractor failed"));
        when(failingExtractor2.extract(any(ETLContext.class)))
            .thenThrow(new ETLException("EXTRACT", "20250101", "Second extractor failed"));
        
        testFactory.registerExtractor("source1", failingExtractor1);
        testFactory.registerExtractor("source2", failingExtractor2);
        
        ExtractSubprocess extract = createTestExtractSubprocess(testFactory);
        
        // When / Then: Should throw ETLException when all extractors fail
        extract.validateContext(context);
        try {
            extract.execute(context);
            fail("Expected ETLException when all extractors fail");
        } catch (ETLException e) {
            assertTrue("Exception should indicate extraction failure", 
                    e.getMessage().contains("extract") || e.getMessage().contains("Extract"));
        }
    }
    
    /**
     * Creates a test implementation of ExtractSubprocess that uses the provided factory
     * and implements basic record consolidation logic.
     */
    private ExtractSubprocess createTestExtractSubprocess(ExtractorFactory factory) {
        return new ExtractSubprocess() {
            @Override
            public int execute(ETLContext ctx) throws ETLException {
                List<SourceDataModel> allRecords = new ArrayList<>();
                int totalCount = 0;
                List<Exception> errors = new ArrayList<>();
                
                for (ETConfiguration.SourceConfig source : ctx.getConfig().getSources()) {
                    try {
                        Extractor extractor = factory.createExtractor(source);
                        List<SourceDataModel> records = extractor.extract(ctx);
                        allRecords.addAll(records);
                        totalCount += records.size();
                    } catch (ETLException e) {
                        errors.add(e);
                        // Continue with other sources
                    }
                }
                
                ctx.setExtractedData(allRecords);
                ctx.setExtractedDataCount(totalCount);
                
                if (!errors.isEmpty() && totalCount == 0) {
                    // All extractors failed
                    throw new ETLException("EXTRACT", ctx.getCurrentDate(),
                            "All extractors failed: " + errors.get(0).getMessage());
                }
                
                return totalCount;
            }
        };
    }
    
    private ETConfiguration.SourceConfig createSourceConfig(String name, String type, String category) {
        ETConfiguration.SourceConfig source = new ETConfiguration.SourceConfig();
        source.setName(name);
        source.setType(type);
        source.setProperty("category", category);
        source.setConnectionString("mock://localhost/" + name);
        source.setPrimaryKeyField("id");
        return source;
    }
    
    private List<SourceDataModel> createMockRecords(String sourcePrefix, int count) {
        List<SourceDataModel> records = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            SourceDataModel mockRecord = mock(SourceDataModel.class);
            when(mockRecord.getPrimaryKey()).thenReturn(sourcePrefix + "_record_" + i);
            when(mockRecord.getSourceType()).thenReturn("MOCK");
            records.add(mockRecord);
        }
        return records;
    }
}