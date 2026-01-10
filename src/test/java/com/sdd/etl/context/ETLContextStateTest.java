package com.sdd.etl.context;

import com.sdd.etl.config.ETConfiguration;
import org.junit.Test;
import org.junit.Before;
import com.sdd.etl.util.DateUtils;
import static org.junit.Assert.*;

/**
 * Unit tests for ETL context state transitions.
 * Focuses on verifying that context state changes correctly as subprocesses
 * execute and update their respective data and counters.
 */
public class ETLContextStateTest {

    private ETLContext context;
    private ETConfiguration config;

    @Before
    public void setUp() {
        config = new ETConfiguration();
        context = new ETLContext();
        context.setCurrentDate(DateUtils.parseDate("20250101"));
        context.setConfig(config);
    }

    /**
     * Tests that context transitions from initial state to EXTRACT state.
     * Verifies that extracted data and count are properly set.
     */
    @Test
    public void testStateTransition_InitialToExtract() {
        // When
        context.setCurrentSubprocess(SubprocessType.EXTRACT);
        context.setExtractedDataCount(100);
        context.setExtractedData(new Object());

        // Then
        assertEquals("Current subprocess should be EXTRACT", SubprocessType.EXTRACT, context.getCurrentSubprocess());
        assertEquals("Extracted data count should be 100", 100, context.getExtractedDataCount());
        assertNotNull("Extracted data should not be null", context.getExtractedData());
    }

    /**
     * Tests that context transitions from EXTRACT state to TRANSFORM state.
     * Verifies that transformed data and count are properly set while
     * extracted data remains unchanged.
     */
    @Test
    public void testStateTransition_ExtractToTransform() {
        // Given
        Object extractedData = new Object();
        context.setCurrentSubprocess(SubprocessType.EXTRACT);
        context.setExtractedDataCount(100);
        context.setExtractedData(extractedData);

        // When
        context.setCurrentSubprocess(SubprocessType.TRANSFORM);
        context.setTransformedDataCount(95);
        context.setTransformedData(new Object());

        // Then
        assertEquals("Current subprocess should be TRANSFORM", SubprocessType.TRANSFORM, context.getCurrentSubprocess());
        assertEquals("Extracted data count should remain 100", 100, context.getExtractedDataCount());
        assertSame("Extracted data should remain same", extractedData, context.getExtractedData());
        assertEquals("Transformed data count should be 95", 95, context.getTransformedDataCount());
        assertNotNull("Transformed data should not be null", context.getTransformedData());
    }

    /**
     * Tests that context transitions from TRANSFORM state to LOAD state.
     * Verifies that loaded data count is properly set while transformed data
     * remains unchanged.
     */
    @Test
    public void testStateTransition_TransformToLoad() {
        // Given
        Object transformedData = new Object();
        context.setCurrentSubprocess(SubprocessType.TRANSFORM);
        context.setTransformedDataCount(95);
        context.setTransformedData(transformedData);

        // When
        context.setCurrentSubprocess(SubprocessType.LOAD);
        context.setLoadedDataCount(95);

        // Then
        assertEquals("Current subprocess should be LOAD", SubprocessType.LOAD, context.getCurrentSubprocess());
        assertEquals("Transformed data count should remain 95", 95, context.getTransformedDataCount());
        assertSame("Transformed data should remain same", transformedData, context.getTransformedData());
        assertEquals("Loaded data count should be 95", 95, context.getLoadedDataCount());
    }

    /**
     * Tests that context transitions from LOAD state to VALIDATE state.
     * Verifies that validation flag is properly set while loaded data count
     * remains unchanged.
     */
    @Test
    public void testStateTransition_LoadToValidate() {
        // Given
        context.setCurrentSubprocess(SubprocessType.LOAD);
        context.setLoadedDataCount(95);

        // When
        context.setCurrentSubprocess(SubprocessType.VALIDATE);
        context.setValidationPassed(true);

        // Then
        assertEquals("Current subprocess should be VALIDATE", SubprocessType.VALIDATE, context.getCurrentSubprocess());
        assertEquals("Loaded data count should remain 95", 95, context.getLoadedDataCount());
        assertTrue("Validation should have passed", context.isValidationPassed());
    }

    /**
     * Tests that context transitions from VALIDATE state to CLEAN state.
     * Verifies that cleanup flag is properly set while validation flag
     * remains unchanged.
     */
    @Test
    public void testStateTransition_ValidateToClean() {
        // Given
        context.setCurrentSubprocess(SubprocessType.VALIDATE);
        context.setValidationPassed(true);

        // When
        context.setCurrentSubprocess(SubprocessType.CLEAN);
        context.setCleanupPerformed(true);

        // Then
        assertEquals("Current subprocess should be CLEAN", SubprocessType.CLEAN, context.getCurrentSubprocess());
        assertTrue("Validation should still have passed", context.isValidationPassed());
        assertTrue("Cleanup should have been performed", context.isCleanupPerformed());
    }

    /**
     * Tests that context can be cleared completely.
     * Note: clear() removes ALL data including configuration and date.
     */
    @Test
    public void testClearRemovesAllData() {
        // Given
        context.setCurrentSubprocess(SubprocessType.EXTRACT);
        context.setExtractedDataCount(100);
        context.setExtractedData(new Object());
        context.setTransformedDataCount(95);
        context.setTransformedData(new Object());
        context.setLoadedDataCount(95);
        context.setValidationPassed(true);
        context.setCleanupPerformed(true);

        // When
        context.clear();

        // Then
        assertNull("Current date should be null after clear", context.getCurrentDate());
        assertNull("Configuration should be null after clear", context.getConfig());
        assertEquals("Extracted data count should be 0", 0, context.getExtractedDataCount());
        assertNull("Extracted data should be null", context.getExtractedData());
        assertEquals("Transformed data count should be 0", 0, context.getTransformedDataCount());
        assertNull("Transformed data should be null", context.getTransformedData());
        assertEquals("Loaded data count should be 0", 0, context.getLoadedDataCount());
        assertFalse("Validation should be false", context.isValidationPassed());
        assertFalse("Cleanup should be false", context.isCleanupPerformed());
    }

    /**
     * Tests that state transitions maintain data integrity when
     * multiple subprocesses update the context.
     */
    @Test
    public void testStateTransitions_MaintainDataIntegrity() {
        // Simulate complete ETL workflow state transitions
        Object extractedData = new Object();
        Object transformedData = new Object();

        // EXTRACT phase
        context.setCurrentSubprocess(SubprocessType.EXTRACT);
        context.setExtractedDataCount(100);
        context.setExtractedData(extractedData);
        assertEquals("Extract count should be 100", 100, context.getExtractedDataCount());
        assertSame("Extracted data should match", extractedData, context.getExtractedData());

        // TRANSFORM phase
        context.setCurrentSubprocess(SubprocessType.TRANSFORM);
        context.setTransformedDataCount(95);
        context.setTransformedData(transformedData);
        assertEquals("Transform count should be 95", 95, context.getTransformedDataCount());
        assertSame("Transformed data should match", transformedData, context.getTransformedData());
        // Verify extract data unchanged
        assertEquals("Extract count should still be 100", 100, context.getExtractedDataCount());
        assertSame("Extracted data should still match", extractedData, context.getExtractedData());

        // LOAD phase
        context.setCurrentSubprocess(SubprocessType.LOAD);
        context.setLoadedDataCount(95);
        assertEquals("Load count should be 95", 95, context.getLoadedDataCount());
        // Verify previous data unchanged
        assertEquals("Transform count should still be 95", 95, context.getTransformedDataCount());
        assertSame("Transformed data should still match", transformedData, context.getTransformedData());

        // VALIDATE phase
        context.setCurrentSubprocess(SubprocessType.VALIDATE);
        context.setValidationPassed(true);
        assertTrue("Validation should pass", context.isValidationPassed());

        // CLEAN phase
        context.setCurrentSubprocess(SubprocessType.CLEAN);
        context.setCleanupPerformed(true);
        assertTrue("Cleanup should be performed", context.isCleanupPerformed());
    }
}