package com.sdd.etl.context;

import org.junit.Test;
import org.junit.Before;

import com.sdd.etl.config.ETConfiguration;

import static org.junit.Assert.*;

/**
 * Unit tests for ContextManager.
 */
public class ContextManagerTest {

    private ETConfiguration config;

    @Before
    public void setUp() {
        config = new ETConfiguration();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateContext_NullDate_ThrowsException() {
        ContextManager.createContext(null, config);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateContext_NullConfig_ThrowsException() {
        ContextManager.createContext("20250101", null);
    }

    @Test
    public void testCreateContext_ValidParameters_CreatesContext() {
        String date = "20250101";
        ETLContext context = ContextManager.createContext(date, config);

        assertNotNull("Context should not be null", context);
        assertEquals("Current date should be set", date, context.getCurrentDate());
        assertEquals("Config should be set", config, context.getConfig());
        assertEquals("Current subprocess should be EXTRACT", SubprocessType.EXTRACT, context.getCurrentSubprocess());
    }

    @Test
    public void testCreateContext_InitializesCountersToZero() {
        ETLContext context = ContextManager.createContext("20250101", config);

        assertEquals("Extracted count should be 0", 0, context.getExtractedDataCount());
        assertEquals("Transformed count should be 0", 0, context.getTransformedDataCount());
        assertEquals("Loaded count should be 0", 0, context.getLoadedDataCount());
    }

    @Test
    public void testCreateContext_InitializesFlagsToFalse() {
        ETLContext context = ContextManager.createContext("20250101", config);

        assertFalse("Validation passed should be false", context.isValidationPassed());
        assertFalse("Cleanup performed should be false", context.isCleanupPerformed());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateContext_NullContext_ThrowsException() {
        ContextManager.validateContext(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateContext_NullDate_ThrowsException() {
        ETLContext context = new ETLContext();
        context.setConfig(config);
        ContextManager.validateContext(context);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateContext_NullConfig_ThrowsException() {
        ETLContext context = new ETLContext();
        context.setCurrentDate("20250101");
        ContextManager.validateContext(context);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateContext_NullSubprocess_ThrowsException() {
        ETLContext context = new ETLContext();
        context.setCurrentDate("20250101");
        context.setConfig(config);
        ContextManager.validateContext(context);
    }

    @Test
    public void testValidateContext_NegativeCounts_ThrowsException() {
        ETLContext context = ContextManager.createContext("20250101", config);
        context.setExtractedDataCount(-1);

        try {
            ContextManager.validateContext(context);
            fail("Should throw exception for negative count");
        } catch (IllegalArgumentException e) {
            assertTrue("Exception message should mention count", e.getMessage().contains("count"));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSnapshot_NullContext_ThrowsException() {
        ContextManager.snapshot(null);
    }

    @Test
    public void testSnapshot_ValidContext_ReturnsCopy() {
        ETLContext context = ContextManager.createContext("20250101", config);
        context.setExtractedDataCount(100);

        java.util.Map<String, Object> snapshot = ContextManager.snapshot(context);

        assertNotNull("Snapshot should not be null", snapshot);
        assertEquals("Snapshot should contain date", "20250101", snapshot.get(ContextConstants.CURRENT_DATE));
        assertEquals("Snapshot should contain count", 100, snapshot.get(ContextConstants.EXTRACTED_DATA_COUNT));
    }

    @Test
    public void testSnapshot_ModifyingOriginalDoesNotAffectSnapshot() {
        ETLContext context = ContextManager.createContext("20250101", config);
        context.setExtractedDataCount(100);

        java.util.Map<String, Object> snapshot = ContextManager.snapshot(context);

        // Modify original context
        context.setExtractedDataCount(200);

        // Snapshot should still have original value
        assertEquals("Snapshot should not change", 100, snapshot.get(ContextConstants.EXTRACTED_DATA_COUNT));
    }

    @Test
    public void testLogContextState_NullContext_LogsError() {
        // This test verifies that null context doesn't throw NPE
        ContextManager.logContextState(null);
        // No exception should be thrown
    }

    @Test
    public void testLogContextState_ValidContext_LogsAllFields() {
        ETLContext context = ContextManager.createContext("20250101", config);
        context.setExtractedDataCount(100);

        // This test verifies logging doesn't throw exception
        ContextManager.logContextState(context);
        // No exception should be thrown
    }
}
