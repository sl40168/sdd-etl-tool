package com.sdd.etl.model;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import com.sdd.etl.context.ETLContext;
import java.util.Map;

/**
 * Unit tests for DailyProcessResult.
 */
public class DailyProcessResultTest {

    @Test
    public void testConstructor_WithDate_InitializesFields() {
        String date = "20250101";
        DailyProcessResult result = new DailyProcessResult(date);

        assertEquals("Date should match", date, result.getDate());
        assertFalse("Success should be false initially", result.isSuccess());
        assertNotNull("Subprocess results map should be initialized", result.getSubprocessResults());
        assertTrue("Subprocess results map should be empty", result.getSubprocessResults().isEmpty());
        assertNull("Context should be null initially", result.getContext());
    }

    @Test
    public void testSetSuccess_ChangesSuccessStatus() {
        DailyProcessResult result = new DailyProcessResult("20250101");

        result.setSuccess(true);
        assertTrue("Should be successful after setting true", result.isSuccess());

        result.setSuccess(false);
        assertFalse("Should be failed after setting false", result.isSuccess());
    }

    @Test
    public void testSetDate_ChangesDate() {
        DailyProcessResult result = new DailyProcessResult("20250101");

        String newDate = "20250102";
        result.setDate(newDate);

        assertEquals("Date should change to new date", newDate, result.getDate());
    }

    @Test
    public void testAddSubprocessResult_AddsToMap() {
        DailyProcessResult result = new DailyProcessResult("20250101");

        SubprocessResult subResult = new SubprocessResult(100);
        result.addSubprocessResult("EXTRACT", subResult);

        Map<String, SubprocessResult> map = result.getSubprocessResults();
        assertEquals("Map should have one entry", 1, map.size());
        assertEquals("Added subprocess result should match", subResult, map.get("EXTRACT"));
    }

    @Test
    public void testGetSubprocessResult_ReturnsAddedResult() {
        DailyProcessResult result = new DailyProcessResult("20250101");

        SubprocessResult subResult = new SubprocessResult(200);
        result.addSubprocessResult("TRANSFORM", subResult);

        SubprocessResult retrieved = result.getSubprocessResult("TRANSFORM");
        assertEquals("Retrieved subprocess result should match", subResult, retrieved);
    }

    @Test
    public void testGetSubprocessResult_ReturnsNullForMissingKey() {
        DailyProcessResult result = new DailyProcessResult("20250101");

        SubprocessResult retrieved = result.getSubprocessResult("LOAD");
        assertNull("Should return null for missing key", retrieved);
    }

    @Test
    public void testSetContext_SetsContext() {
        DailyProcessResult result = new DailyProcessResult("20250101");

        ETLContext context = new ETLContext();
        result.setContext(context);

        assertEquals("Context should be set", context, result.getContext());
    }

    @Test
    public void testSetContext_NullAllowed() {
        DailyProcessResult result = new DailyProcessResult("20250101");

        result.setContext(null);
        assertNull("Context should be null after setting null", result.getContext());
    }
}