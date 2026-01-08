package com.sdd.etl.context;

import com.sdd.etl.ETLException;
import com.sdd.etl.config.ETConfiguration;
import com.sdd.etl.subprocess.SubprocessInterface;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Integration tests for context-based data flow between subprocesses.
 * Verifies that extract, transform, and load subprocesses correctly read from
 * and write to the shared ETL context.
 */
public class ContextDataFlowIntegrationTest {

    private ETLContext context;
    private ETConfiguration config;

    @Before
    public void setUp() {
        config = new ETConfiguration();
        // Minimal configuration for testing
        context = ContextManager.createContext("20250101", config);
    }

    /**
     * Integration test for extract writing to context.
     * Verifies that extract subprocess writes extracted data and count to context.
     */
    @Test
    public void testExtractWritesToContext() throws ETLException {
        // Given
        SubprocessInterface extractSubprocess = new SubprocessInterface() {
            @Override
            public SubprocessType getType() {
                return SubprocessType.EXTRACT;
            }

            @Override
            public int execute(ETLContext ctx) throws ETLException {
                // Simulate extraction of 100 records
                ctx.setExtractedDataCount(100);
                ctx.setExtractedData(new Object());
                return 100;
            }

            @Override
            public void validateContext(ETLContext ctx) throws ETLException {
                // Basic validation
                if (ctx.getConfig() == null) {
                    throw new ETLException("EXTRACT", ctx.getCurrentDate(),
                            "Configuration is null");
                }
            }
        };

        // When
        extractSubprocess.validateContext(context);
        int result = extractSubprocess.execute(context);

        // Then
        assertEquals("Extract should return 100 records", 100, result);
        assertEquals("Context extracted data count should be 100", 100, context.getExtractedDataCount());
        assertNotNull("Context extracted data should not be null", context.getExtractedData());
        assertEquals("Current subprocess should be EXTRACT", SubprocessType.EXTRACT, context.getCurrentSubprocess());
    }

    /**
     * Integration test for transform reading from and writing to context.
     * Verifies that transform subprocess reads extracted data and writes
     * transformed data and count to context.
     */
    @Test
    public void testTransformReadsFromAndWritesToContext() throws ETLException {
        // Given
        // First, setup extract data in context
        Object extractedData = new Object();
        context.setExtractedDataCount(100);
        context.setExtractedData(extractedData);
        context.setCurrentSubprocess(SubprocessType.TRANSFORM);

        SubprocessInterface transformSubprocess = new SubprocessInterface() {
            @Override
            public SubprocessType getType() {
                return SubprocessType.TRANSFORM;
            }

            @Override
            public int execute(ETLContext ctx) throws ETLException {
                // Verify extract data is present
                assertNotNull("Extracted data should be present", ctx.getExtractedData());
                assertEquals("Extracted data count should be 100", 100, ctx.getExtractedDataCount());

                // Simulate transformation of 95 records (5 rejected)
                Object transformedData = new Object();
                ctx.setTransformedDataCount(95);
                ctx.setTransformedData(transformedData);
                return 95;
            }

            @Override
            public void validateContext(ETLContext ctx) throws ETLException {
                // Ensure extracted data exists
                if (ctx.getExtractedData() == null) {
                    throw new ETLException("TRANSFORM", ctx.getCurrentDate(),
                            "No extracted data found");
                }
            }
        };

        // When
        transformSubprocess.validateContext(context);
        int result = transformSubprocess.execute(context);

        // Then
        assertEquals("Transform should return 95 records", 95, result);
        assertEquals("Context transformed data count should be 95", 95, context.getTransformedDataCount());
        assertNotNull("Context transformed data should not be null", context.getTransformedData());
        assertEquals("Current subprocess should be TRANSFORM", SubprocessType.TRANSFORM, context.getCurrentSubprocess());
    }

    /**
     * Integration test for load reading from and writing to context.
     * Verifies that load subprocess reads transformed data and writes
     * loaded data count to context.
     */
    @Test
    public void testLoadReadsFromAndWritesToContext() throws ETLException {
        // Given
        // First, setup transform data in context
        Object transformedData = new Object();
        context.setTransformedDataCount(95);
        context.setTransformedData(transformedData);
        context.setCurrentSubprocess(SubprocessType.LOAD);

        SubprocessInterface loadSubprocess = new SubprocessInterface() {
            @Override
            public SubprocessType getType() {
                return SubprocessType.LOAD;
            }

            @Override
            public int execute(ETLContext ctx) throws ETLException {
                // Verify transformed data is present
                assertNotNull("Transformed data should be present", ctx.getTransformedData());
                assertEquals("Transformed data count should be 95", 95, ctx.getTransformedDataCount());

                // Simulate loading of 95 records
                ctx.setLoadedDataCount(95);
                return 95;
            }

            @Override
            public void validateContext(ETLContext ctx) throws ETLException {
                // Ensure transformed data exists
                if (ctx.getTransformedData() == null) {
                    throw new ETLException("LOAD", ctx.getCurrentDate(),
                            "No transformed data found");
                }
            }
        };

        // When
        loadSubprocess.validateContext(context);
        int result = loadSubprocess.execute(context);

        // Then
        assertEquals("Load should return 95 records", 95, result);
        assertEquals("Context loaded data count should be 95", 95, context.getLoadedDataCount());
        assertEquals("Current subprocess should be LOAD", SubprocessType.LOAD, context.getCurrentSubprocess());
    }
}