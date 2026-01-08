package com.sdd.etl.context;

import org.junit.Test;
import org.junit.Before;

import com.sdd.etl.config.ETConfiguration;
import com.sdd.etl.model.SubprocessResult;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit tests for ETLContext.
 */
public class ETLContextTest {

    private ETLContext context;

    @Before
    public void setUp() {
        context = new ETLContext();
    }

    @Test
    public void testGetCurrentDate_InitiallyNull() {
        assertNull("Current date should be null initially", context.getCurrentDate());
    }

    @Test
    public void testSetCurrentDate_SetsValue() {
        String date = "20250101";
        context.setCurrentDate(date);

        assertEquals("Current date should be set", date, context.getCurrentDate());
    }

    @Test
    public void testGetCurrentSubprocess_InitiallyNull() {
        assertNull("Current subprocess should be null initially", context.getCurrentSubprocess());
    }

    @Test
    public void testSetCurrentSubprocess_SetsValue() {
        SubprocessType subprocess = SubprocessType.EXTRACT;
        context.setCurrentSubprocess(subprocess);

        assertEquals("Current subprocess should be set", subprocess, context.getCurrentSubprocess());
    }

    @Test
    public void testGetConfig_InitiallyNull() {
        assertNull("Config should be null initially", context.getConfig());
    }

    @Test
    public void testSetConfig_SetsValue() {
        ETConfiguration config = new ETConfiguration();
        context.setConfig(config);

        assertEquals("Config should be set", config, context.getConfig());
    }

    @Test
    public void testGetExtractedDataCount_InitiallyZero() {
        assertEquals("Extracted data count should be 0 initially", 0, context.getExtractedDataCount());
    }

    @Test
    public void testSetExtractedDataCount_SetsValue() {
        int count = 100;
        context.setExtractedDataCount(count);

        assertEquals("Extracted data count should be set", count, context.getExtractedDataCount());
    }

    @Test
    public void testGetExtractedData_InitiallyNull() {
        assertNull("Extracted data should be null initially", context.getExtractedData());
    }

    @Test
    public void testSetExtractedData_SetsValue() {
        Object data = "test data";
        context.setExtractedData(data);

        assertEquals("Extracted data should be set", data, context.getExtractedData());
    }

    @Test
    public void testGetTransformedDataCount_InitiallyZero() {
        assertEquals("Transformed data count should be 0 initially", 0, context.getTransformedDataCount());
    }

    @Test
    public void testSetTransformedDataCount_SetsValue() {
        int count = 100;
        context.setTransformedDataCount(count);

        assertEquals("Transformed data count should be set", count, context.getTransformedDataCount());
    }

    @Test
    public void testGetTransformedData_InitiallyNull() {
        assertNull("Transformed data should be null initially", context.getTransformedData());
    }

    @Test
    public void testSetTransformedData_SetsValue() {
        Object data = "transformed data";
        context.setTransformedData(data);

        assertEquals("Transformed data should be set", data, context.getTransformedData());
    }

    @Test
    public void testGetLoadedDataCount_InitiallyZero() {
        assertEquals("Loaded data count should be 0 initially", 0, context.getLoadedDataCount());
    }

    @Test
    public void testSetLoadedDataCount_SetsValue() {
        int count = 100;
        context.setLoadedDataCount(count);

        assertEquals("Loaded data count should be set", count, context.getLoadedDataCount());
    }

    @Test
    public void testIsValidationPassed_InitiallyFalse() {
        assertFalse("Validation passed should be false initially", context.isValidationPassed());
    }

    @Test
    public void testSetValidationPassed_SetsValue() {
        context.setValidationPassed(true);

        assertTrue("Validation passed should be true after setting", context.isValidationPassed());
    }

    @Test
    public void testGetValidationErrors_InitiallyEmptyList() {
        java.util.List<String> errors = context.getValidationErrors();
        assertNotNull("Validation errors should not be null", errors);
        assertTrue("Validation errors should be empty initially", errors.isEmpty());
    }

    @Test
    public void testSetValidationErrors_SetsValue() {
        java.util.List<String> errors = new java.util.ArrayList<>();
        errors.add("Error 1");
        errors.add("Error 2");
        context.setValidationErrors(errors);

        java.util.List<String> result = context.getValidationErrors();
        assertEquals("Validation errors should match", errors, result);
    }

    @Test
    public void testIsCleanupPerformed_InitiallyFalse() {
        assertFalse("Cleanup performed should be false initially", context.isCleanupPerformed());
    }

    @Test
    public void testSetCleanupPerformed_SetsValue() {
        context.setCleanupPerformed(true);

        assertTrue("Cleanup performed should be true after setting", context.isCleanupPerformed());
    }

    @Test
    public void testGet_GenericType_ReturnsValue() {
        String key = "testKey";
        String value = "testValue";

        context.set(key, value);

        String result = context.get(key);
        assertEquals("Generic get should return value", value, result);
    }

    @Test
    public void testSet_GenericType_SetsValue() {
        String key = "testKey";
        String value = "testValue";

        context.set(key, value);

        String result = context.get(key);
        assertEquals("Generic set should store value", value, result);
    }

    @Test
    public void testGetAll_ReturnsCopyOfData() {
        context.setCurrentDate("20250101");
        context.setCurrentSubprocess(SubprocessType.EXTRACT);
        context.setExtractedDataCount(100);

        Map<String, Object> result = context.getAll();

        assertNotNull("Result should not be null", result);
        assertEquals("Should contain current date", "20250101", result.get(ContextConstants.CURRENT_DATE));
        assertEquals("Should contain current subprocess", SubprocessType.EXTRACT, result.get(ContextConstants.CURRENT_SUBPROCESS));
        assertEquals("Should contain extracted count", 100, result.get(ContextConstants.EXTRACTED_DATA_COUNT));
    }

    @Test
    public void testClear_ClearsAllData() {
        context.setCurrentDate("20250101");
        context.setExtractedDataCount(100);

        context.clear();

        assertNull("Current date should be null after clear", context.getCurrentDate());
        assertEquals("Extracted count should be 0 after clear", 0, context.getExtractedDataCount());
    }
}
