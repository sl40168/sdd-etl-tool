package com.sdd.etl.model;

import org.junit.Test;
import org.junit.Before;

import static org.junit.Assert.*;

/**
 * Unit tests for SubprocessResult.
 */
public class SubprocessResultTest {

    @Test
    public void testConstructor_WithDataCount_CreatesSuccessfulResult() {
        int dataCount = 100;
        SubprocessResult result = new SubprocessResult(dataCount);

        assertTrue("Should be successful", result.isSuccess());
        assertEquals("Data count should match", dataCount, result.getDataCount());
        assertNull("Error message should be null", result.getErrorMessage());
        assertNotNull("Timestamp should be set", result.getTimestamp());
    }

    @Test
    public void testConstructor_WithErrorMessage_CreatesFailedResult() {
        String errorMessage = "Connection failed";
        SubprocessResult result = new SubprocessResult(errorMessage);

        assertFalse("Should be failed", result.isSuccess());
        assertEquals("Error message should match", errorMessage, result.getErrorMessage());
        assertEquals("Data count should be 0", 0, result.getDataCount());
        assertNotNull("Timestamp should be set", result.getTimestamp());
    }

    @Test
    public void testSetSuccess_ChangesSuccessStatus() {
        SubprocessResult result = new SubprocessResult(100);

        result.setSuccess(false);
        assertFalse("Should be failed after setting false", result.isSuccess());

        result.setSuccess(true);
        assertTrue("Should be successful after setting true", result.isSuccess());
    }

    @Test
    public void testSetDataCount_ChangesDataCount() {
        SubprocessResult result = new SubprocessResult(100);

        result.setDataCount(200);
        assertEquals("Data count should change to 200", 200, result.getDataCount());
    }

    @Test
    public void testSetErrorMessage_ChangesErrorMessage() {
        SubprocessResult result = new SubprocessResult(100);

        String errorMessage = "New error message";
        result.setErrorMessage(errorMessage);

        assertEquals("Error message should match", errorMessage, result.getErrorMessage());
        assertFalse("Should become failed when error message is set", result.isSuccess());
    }

    @Test
    public void testSetTimestamp_ChangesTimestamp() {
        SubprocessResult result = new SubprocessResult(100);

        long newTimestamp = System.currentTimeMillis();
        result.setTimestamp(newTimestamp);

        assertEquals("Timestamp should change", newTimestamp, result.getTimestamp());
    }

    @Test
    public void testGetAllValues_ReturnsCorrectValues() {
        int dataCount = 150;
        SubprocessResult result = new SubprocessResult(dataCount);

        assertEquals("Success should be true", true, result.isSuccess());
        assertEquals("Data count should match", dataCount, result.getDataCount());
        assertNull("Error message should be null", result.getErrorMessage());
        assertNotNull("Timestamp should not be null", result.getTimestamp());
    }
}
