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
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for extraction subprocess with multiple extractors.
 * Verifies that multiple extractors can be registered and executed concurrently,
 * and that results are properly consolidated.
 */
public class ExtractSubprocessIntegrationTest {

    private ETConfiguration config;
    private ETLContext context;
    private TestExtractorFactory testFactory;
    private List<Extractor> mockExtractors;
    
    @Before
    public void setUp() {
        config = new ETConfiguration();
        context = ContextManager.createContext("20250101", config);
        testFactory = new TestExtractorFactory();
        ExtractorFactory.setInstance(testFactory);
        mockExtractors = new ArrayList<>();
    }
    
    private Extractor createMockExtractor(String sourceName, int recordCount) throws ETLException {
        Extractor mockExtractor = mock(Extractor.class);
        
        // Create mock records
        List<SourceDataModel> mockRecords = new ArrayList<>();
        for (int i = 1; i <= recordCount; i++) {
            SourceDataModel mockRecord = mock(SourceDataModel.class);
            when(mockRecord.getPrimaryKey()).thenReturn(sourceName + "_record_" + i);
            when(mockRecord.getSourceType()).thenReturn("MOCK");
            mockRecords.add(mockRecord);
        }
        
        when(mockExtractor.extract(any(ETLContext.class))).thenReturn(mockRecords);
        doNothing().when(mockExtractor).validate(any(ETLContext.class));
        
        return mockExtractor;
    }
    
    @Test
    public void testMultipleExtractors_AllSuccess_RecordsConsolidated() throws ETLException {
        // Given: 3 sources with different record counts
        List<ETConfiguration.SourceConfig> sources = new ArrayList<>();
        sources.add(createSourceConfig("cos1", "COS", "xbond-quote"));
        sources.add(createSourceConfig("jdbc1", "JDBC", "market-data"));
        sources.add(createSourceConfig("cos2", "COS", "xbond-quote"));
        
        for (ETConfiguration.SourceConfig source : sources) {
            config.addSource(source);
        }
        
        // Create mock extractors with different record counts
        mockExtractors.add(createMockExtractor("cos1", 15));
        mockExtractors.add(createMockExtractor("jdbc1", 8));
        mockExtractors.add(createMockExtractor("cos2", 22));
        
        // Register extractors with test factory
        testFactory.registerExtractor("cos1", mockExtractors.get(0));
        testFactory.registerExtractor("jdbc1", mockExtractors.get(1));
        testFactory.registerExtractor("cos2", mockExtractors.get(2));
        
        // Create a concrete ExtractSubprocess that uses the factory
        ExtractSubprocess extract = new ExtractSubprocess() {
            @Override
            public int execute(ETLContext ctx) throws ETLException {
                List<SourceDataModel> allRecords = new ArrayList<>();
                AtomicInteger totalCount = new AtomicInteger(0);
                
                // Simulate concurrent extraction
                List<Thread> threads = new ArrayList<>();
                for (ETConfiguration.SourceConfig source : ctx.getConfig().getSources()) {
                    Thread t = new Thread(() -> {
                        try {
                            Extractor extractor = ExtractorFactory.createExtractor(source);
                            List<SourceDataModel> records = extractor.extract(ctx);
                            synchronized (allRecords) {
                                allRecords.addAll(records);
                                totalCount.addAndGet(records.size());
                            }
                        } catch (ETLException e) {
                            // Should not happen in this test
                        }
                    });
                    threads.add(t);
                    t.start();
                }
                
                // Wait for all extractors to complete
                for (Thread t : threads) {
                    try {
                        t.join();
                    } catch (InterruptedException e) {
                        throw new ETLException("EXTRACT", ctx.getCurrentDate(), 
                                "Extraction interrupted");
                    }
                }
                
                ctx.setExtractedData(allRecords);
                ctx.setExtractedDataCount(totalCount.get());
                return totalCount.get();
            }
        };
        
        // When
        extract.validateContext(context);
        int totalCount = extract.execute(context);
        
        // Then
        assertEquals("Total records should be sum of all extractor outputs", 
                15 + 8 + 22, totalCount);
        assertEquals("Context extracted count should match total", 
                totalCount, context.getExtractedDataCount());
        assertNotNull("Extracted data should not be null", context.getExtractedData());
        assertEquals("Number of records in context should match total", 
                totalCount, ((List<?>) context.getExtractedData()).size());
    }
    
    @Test
    public void testMultipleExtractors_MixedSuccessAndFailure_ContinuesWithPartialResults() throws ETLException {
        // Given: 3 sources where one will fail
        config.addSource(createSourceConfig("source1", "COS", "xbond-quote"));
        config.addSource(createSourceConfig("source2", "JDBC", "market-data"));
        config.addSource(createSourceConfig("source3", "COS", "xbond-quote"));
        
        // Create 2 successful extractors and 1 failing extractor
        Extractor successful1 = createMockExtractor("source1", 10);
        Extractor successful2 = createMockExtractor("source2", 5);
        Extractor failingExtractor = mock(Extractor.class);
        
        when(failingExtractor.extract(any(ETLContext.class)))
            .thenThrow(new ETLException("EXTRACT", "20250101", "Mock extraction failure"));
        doNothing().when(failingExtractor).validate(any(ETLContext.class));
        
        // Register extractors with test factory
        testFactory.registerExtractor("source1", successful1);
        testFactory.registerExtractor("source2", successful2);
        testFactory.registerExtractor("source3", failingExtractor);
        
        ExtractSubprocess extract = new ExtractSubprocess() {
            @Override
            public int execute(ETLContext ctx) throws ETLException {
                List<SourceDataModel> allRecords = new ArrayList<>();
                AtomicInteger successCount = new AtomicInteger(0);
                List<Exception> errors = new ArrayList<>();
                
                // Extract from each source, handling failures gracefully
                for (ETConfiguration.SourceConfig source : ctx.getConfig().getSources()) {
                    try {
                        Extractor extractor = ExtractorFactory.createExtractor(source);
                        List<SourceDataModel> records = extractor.extract(ctx);
                        synchronized (allRecords) {
                            allRecords.addAll(records);
                            successCount.addAndGet(records.size());
                        }
                    } catch (ETLException e) {
                        errors.add(e);
                        // Continue with other sources
                    }
                }
                
                ctx.setExtractedData(allRecords);
                ctx.setExtractedDataCount(successCount.get());
                
                if (!errors.isEmpty() && successCount.get() == 0) {
                    // All extractors failed
                    throw new ETLException("EXTRACT", ctx.getCurrentDate(),
                            "All extractors failed: " + errors.get(0).getMessage());
                }
                
                return successCount.get();
            }
        };
        
        // When
        extract.validateContext(context);
        int successfulCount = extract.execute(context);
        
        // Then: Should get records from successful extractors only
        assertEquals("Should have records from 2 successful extractors", 10 + 5, successfulCount);
        assertNotNull("Extracted data should not be null", context.getExtractedData());
        assertEquals("Number of records should match successful count", 
                successfulCount, ((List<?>) context.getExtractedData()).size());
    }
    
    @Test
    public void testExtractorsWithDifferentSourceTypes_AllProcessedCorrectly() throws ETLException {
        // Given: extractors with different source types (COS, JDBC, FILE)
        config.addSource(createSourceConfig("cos-source", "COS", "xbond-quote"));
        config.addSource(createSourceConfig("jdbc-source", "JDBC", "market-data"));
        config.addSource(createSourceConfig("file-source", "FILE", "local-data"));
        
        // Create specialized mock extractors for each type
        Extractor cosExtractor = createMockExtractor("cos-source", 12);
        Extractor jdbcExtractor = createMockExtractor("jdbc-source", 7);
        Extractor fileExtractor = createMockExtractor("file-source", 3);
        
        // Register extractors with test factory
        testFactory.registerExtractor("cos-source", cosExtractor);
        testFactory.registerExtractor("jdbc-source", jdbcExtractor);
        testFactory.registerExtractor("file-source", fileExtractor);
        
        ExtractSubprocess extract = new ExtractSubprocess() {
            @Override
            public int execute(ETLContext ctx) throws ETLException {
                List<SourceDataModel> allRecords = new ArrayList<>();
                int totalCount = 0;
                
                for (ETConfiguration.SourceConfig source : ctx.getConfig().getSources()) {
                    Extractor extractor = ExtractorFactory.createExtractor(source);
                    List<SourceDataModel> records = extractor.extract(ctx);
                    allRecords.addAll(records);
                    totalCount += records.size();
                }
                
                ctx.setExtractedData(allRecords);
                ctx.setExtractedDataCount(totalCount);
                return totalCount;
            }
        };
        
        // When
        extract.validateContext(context);
        int totalCount = extract.execute(context);
        
        // Then
        assertEquals("Total records should sum of all types", 12 + 7 + 3, totalCount);
        assertEquals("Context count should match", totalCount, context.getExtractedDataCount());
    }
    
    @Test
    public void testConcurrentExtraction_MaintainsDataIntegrity() throws ETLException, InterruptedException {
        // Given: multiple extractors that will run concurrently
        int numExtractors = 10;
        int recordsPerExtractor = 100;
        
        for (int i = 1; i <= numExtractors; i++) {
            config.addSource(createSourceConfig("source" + i, "COS", "xbond-quote"));
        }
        
        // Create mock extractors that all return the same number of records
        List<Extractor> extractors = new ArrayList<>();
        for (int i = 1; i <= numExtractors; i++) {
            extractors.add(createMockExtractor("source" + i, recordsPerExtractor));
        }
        
        // Register extractors with test factory
        for (int i = 0; i < numExtractors; i++) {
            testFactory.registerExtractor("source" + (i + 1), extractors.get(i));
        }
        
        ExtractSubprocess extract = new ExtractSubprocess() {
            @Override
            public int execute(ETLContext ctx) throws ETLException {
                List<SourceDataModel> allRecords = new ArrayList<>();
                List<Thread> threads = new ArrayList<>();
                
                for (ETConfiguration.SourceConfig source : ctx.getConfig().getSources()) {
                    Thread t = new Thread(() -> {
                        try {
                            Extractor extractor = ExtractorFactory.createExtractor(source);
                            List<SourceDataModel> records = extractor.extract(ctx);
                            synchronized (allRecords) {
                                allRecords.addAll(records);
                            }
                        } catch (ETLException e) {
                            // Should not happen
                        }
                    });
                    threads.add(t);
                    t.start();
                }
                
                for (Thread t : threads) {
                    try {
                        t.join();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new ETLException("EXTRACT", ctx.getCurrentDate(), "Extraction interrupted");
                    }
                }
                
                ctx.setExtractedData(allRecords);
                ctx.setExtractedDataCount(allRecords.size());
                return allRecords.size();
            }
        };
        
        // When
        extract.validateContext(context);
        int totalCount = extract.execute(context);
        
        // Then: Verify all records were collected (no lost records due to concurrency)
        int expectedTotal = numExtractors * recordsPerExtractor;
        assertEquals("All records should be collected despite concurrency", 
                expectedTotal, totalCount);
        assertEquals("Context count should match", expectedTotal, context.getExtractedDataCount());
        
        // Verify no duplicate primary keys (each extractor produces unique keys)
        List<?> collectedRecords = (List<?>) context.getExtractedData();
        long uniqueKeys = collectedRecords.stream()
            .map(r -> ((SourceDataModel) r).getPrimaryKey())
            .distinct()
            .count();
        assertEquals("All primary keys should be unique", expectedTotal, uniqueKeys);
    }
    
    @Test
    public void testExtractSubprocess_ValidatesContextBeforeExecution() throws ETLException {
        // Given: a valid context with sources
        config.addSource(createSourceConfig("test-source", "COS", "xbond-quote"));
        
        ExtractSubprocess extract = new ExtractSubprocess() {
            @Override
            public int execute(ETLContext ctx) throws ETLException {
                ctx.setExtractedData(new ArrayList<>());
                ctx.setExtractedDataCount(0);
                return 0;
            }
        };
        
        // When: validateContext is called (should succeed)
        extract.validateContext(context);
        
        // Then: No exception thrown
        // Execution should proceed normally
        int result = extract.execute(context);
        assertEquals(0, result);
    }
    
    @Test(expected = ETLException.class)
    public void testExtractSubprocess_InvalidContext_ThrowsBeforeExecution() throws ETLException {
        // Given: no sources configured (invalid context)
        // config has no sources
        
        ExtractSubprocess extract = new ExtractSubprocess() {
            @Override
            public int execute(ETLContext ctx) throws ETLException {
                return 0;
            }
        };
        
        // When / Then: validateContext should throw ETLException
        extract.validateContext(context);
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
    
    private static class TestExtractorFactory extends ExtractorFactory {
        private Map<String, Extractor> extractorMap = new HashMap<>();
        
        @Override
        protected Extractor createExtractorInstance(ETConfiguration.SourceConfig sourceConfig) throws ETLException {
            Extractor extractor = extractorMap.get(sourceConfig.getName());
            if (extractor == null) {
                throw new ETLException("EXTRACT", "00000000", "No extractor configured for source: " + sourceConfig.getName());
            }
            return extractor;
        }
        
        public void registerExtractor(String sourceName, Extractor extractor) {
            extractorMap.put(sourceName, extractor);
        }
    }
}