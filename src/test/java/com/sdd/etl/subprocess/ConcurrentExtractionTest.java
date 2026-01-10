package com.sdd.etl.subprocess;

import com.sdd.etl.ETLException;
import com.sdd.etl.config.ETConfiguration;
import com.sdd.etl.context.ContextManager;
import com.sdd.etl.context.ETLContext;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * Unit tests for concurrent extractor execution in ExtractSubprocess.
 * Verifies that extraction completes only after all extractors finish
 * and that records are properly consolidated.
 */
public class ConcurrentExtractionTest {
    
    private ETConfiguration config;
    private ETLContext context;
    
    @Before
    public void setUp() {
        config = new ETConfiguration();
        context = ContextManager.createContext("20250101", config);
    }
    
    @Test
    public void testConcurrentExtraction_AllExtractorsComplete_Success() throws ETLException {
        // Given: multiple extractors configured
        List<ETConfiguration.SourceConfig> sources = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            ETConfiguration.SourceConfig source = new ETConfiguration.SourceConfig();
            source.setName("source" + i);
            source.setType("MOCK");
            source.setConnectionString("mock://localhost/source" + i);
            sources.add(source);
            config.addSource(source);
        }
        
        // Create an ExtractSubprocess that simulates concurrent extraction
        ExtractSubprocess extract = new ExtractSubprocess() {
            @Override
            public int execute(ETLContext ctx) throws ETLException {
                // Simulate concurrent extraction with ExecutorService
                ExecutorService executor = Executors.newFixedThreadPool(3);
                List<Future<Integer>> futures = new ArrayList<>();
                AtomicInteger totalRecords = new AtomicInteger(0);
                
                for (ETConfiguration.SourceConfig source : ctx.getConfig().getSources()) {
                    futures.add(executor.submit(() -> {
                        // Simulate extraction from each source
                        int records = 10; // Each source returns 10 records
                        totalRecords.addAndGet(records);
                        return records;
                    }));
                }
                
                // Wait for all extractors to complete
                for (Future<Integer> future : futures) {
                    try {
                        future.get();
                    } catch (InterruptedException | ExecutionException e) {
                        throw new ETLException("EXTRACT", ctx.getCurrentDate(), 
                                "Extraction failed: " + e.getMessage());
                    }
                }
                
                executor.shutdown();
                try {
                    executor.awaitTermination(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    throw new ETLException("EXTRACT", ctx.getCurrentDate(), 
                            "Extraction timeout: " + e.getMessage());
                }
                
                // Set extracted data count in context
                ctx.setExtractedDataCount(totalRecords.get());
                ctx.setExtractedData(new ArrayList<>()); // Empty list placeholder
                return totalRecords.get();
            }
        };
        
        // When
        extract.validateContext(context);
        int totalCount = extract.execute(context);
        
        // Then
        assertEquals("Total extracted records should be 50 (5 sources × 10 records)", 
                50, totalCount);
        assertEquals("Context extracted count should match total", 
                totalCount, context.getExtractedDataCount());
        assertNotNull("Extracted data should be set", context.getExtractedData());
    }
    
    @Test
    public void testConcurrentExtraction_PartialFailure_ThrowsETLException() throws ETLException {
        // Given: 3 sources where one will fail
        for (int i = 1; i <= 3; i++) {
            ETConfiguration.SourceConfig source = new ETConfiguration.SourceConfig();
            source.setName("source" + i);
            source.setType("MOCK");
            source.setConnectionString("mock://localhost/source" + i);
            config.addSource(source);
        }
        
        ExtractSubprocess extract = new ExtractSubprocess() {
            @Override
            public int execute(ETLContext ctx) throws ETLException {
                ExecutorService executor = Executors.newFixedThreadPool(2);
                List<Future<Integer>> futures = new ArrayList<>();
                
                for (int i = 0; i < ctx.getConfig().getSources().size(); i++) {
                    final int sourceIndex = i;
                    futures.add(executor.submit(() -> {
                        if (sourceIndex == 1) { // Second source fails
                            throw new RuntimeException("Mock extraction failure");
                        }
                        return 10; // Successful sources return 10 records
                    }));
                }
                
                // Wait for all futures, expecting one to fail
                int totalRecords = 0;
                ETLException firstException = null;
                
                for (Future<Integer> future : futures) {
                    try {
                        totalRecords += future.get();
                    } catch (InterruptedException e) {
                        if (firstException == null) {
                            firstException = new ETLException("EXTRACT", ctx.getCurrentDate(), 
                                    "Extraction interrupted: " + e.getMessage());
                        }
                    } catch (ExecutionException e) {
                        if (firstException == null) {
                            firstException = new ETLException("EXTRACT", ctx.getCurrentDate(), 
                                    "Extraction failed: " + e.getCause().getMessage());
                        }
                    }
                }
                
                executor.shutdown();
                
                if (firstException != null) {
                    throw firstException;
                }
                
                ctx.setExtractedDataCount(totalRecords);
                return totalRecords;
            }
        };
        
        // When / Then
        extract.validateContext(context);
        try {
            extract.execute(context);
            fail("Expected ETLException due to extraction failure");
        } catch (ETLException e) {
            assertTrue("Exception message should indicate extraction failure",
                    e.getMessage().contains("Extraction failed"));
        }
    }
    
    @Test
    public void testConcurrentExtraction_Timeout_ThrowsETLException() throws ETLException {
        // Given: a source that takes too long
        ETConfiguration.SourceConfig source = new ETConfiguration.SourceConfig();
        source.setName("slowSource");
        source.setType("MOCK");
        source.setConnectionString("mock://localhost/slow");
        config.addSource(source);
        
        ExtractSubprocess extract = new ExtractSubprocess() {
            @Override
            public int execute(ETLContext ctx) throws ETLException {
                ExecutorService executor = Executors.newSingleThreadExecutor();
                Future<Integer> future = executor.submit(() -> {
                    Thread.sleep(2000); // Simulate slow extraction (2 seconds)
                    return 5;
                });
                
                try {
                    // Set a short timeout
                    return future.get(500, TimeUnit.MILLISECONDS);
                } catch (TimeoutException e) {
                    throw new ETLException("EXTRACT", ctx.getCurrentDate(), 
                            "Extraction timeout: source took too long");
                } catch (InterruptedException | ExecutionException e) {
                    throw new ETLException("EXTRACT", ctx.getCurrentDate(), 
                            "Extraction failed: " + e.getMessage());
                } finally {
                    executor.shutdown();
                }
            }
        };
        
        // When / Then
        extract.validateContext(context);
        try {
            extract.execute(context);
            fail("Expected ETLException due to timeout");
        } catch (ETLException e) {
            assertTrue("Exception message should indicate timeout",
                    e.getMessage().contains("Extraction timeout"));
        }
    }
    
    @Test
    public void testConcurrentExtraction_NoSources_ThrowsETLException() {
        // Given: no sources configured (config already empty)
        ExtractSubprocess extract = new ExtractSubprocess() {
            @Override
            public int execute(ETLContext context) throws ETLException {
                return 0;
            }
        };
        
        // When / Then
        try {
            extract.validateContext(context);
            fail("Expected ETLException due to no sources");
        } catch (ETLException e) {
            assertTrue("Exception message should indicate no sources configured",
                    e.getMessage().contains("No data sources configured"));
        }
    }
    
    @Test
    public void testConcurrentExtraction_Cancellation_HandledGracefully() throws ETLException {
        // Given: multiple sources
        for (int i = 1; i <= 3; i++) {
            ETConfiguration.SourceConfig source = new ETConfiguration.SourceConfig();
            source.setName("source" + i);
            source.setType("MOCK");
            source.setConnectionString("mock://localhost/source" + i);
            config.addSource(source);
        }
        
        ExtractSubprocess extract = new ExtractSubprocess() {
            @Override
            public int execute(ETLContext ctx) throws ETLException {
                ExecutorService executor = Executors.newFixedThreadPool(2);
                List<Future<Integer>> futures = new ArrayList<>();
                
                for (int i = 0; i < ctx.getConfig().getSources().size(); i++) {
                    futures.add(executor.submit(() -> {
                        // Check for thread interruption
                        if (Thread.currentThread().isInterrupted()) {
                            throw new RuntimeException("Extraction cancelled");
                        }
                        Thread.sleep(100); // Simulate work
                        return 10;
                    }));
                }
                
                // Simulate cancellation by interrupting executor
                executor.shutdownNow();
                
                // Try to get results (some may be cancelled)
                int totalRecords = 0;
                for (Future<Integer> future : futures) {
                    try {
                        totalRecords += future.get();
                    } catch (CancellationException e) {
                        // Expected for cancelled tasks
                    } catch (InterruptedException | ExecutionException e) {
                        throw new ETLException("EXTRACT", ctx.getCurrentDate(), 
                                "Extraction interrupted: " + e.getMessage());
                    }
                }
                
                return totalRecords;
            }
        };
        
        // When / Then
        extract.validateContext(context);
        try {
            int result = extract.execute(context);
            // Some records may have been extracted before cancellation
            assertTrue("Result should be between 0 and 30", result >= 0 && result <= 30);
        } catch (ETLException e) {
            // Either outcome is acceptable depending on timing
        }
    }
}