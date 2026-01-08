package com.sdd.etl.model;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.util.Map;

/**
 * Unit tests for WorkflowResult.
 */
public class WorkflowResultTest {

    @Test
    public void testConstructor_InitializesFields() {
        WorkflowResult result = new WorkflowResult();

        assertFalse("Success should be false initially", result.isSuccess());
        assertEquals("Processed days should be 0", 0, result.getProcessedDays());
        assertEquals("Successful days should be 0", 0, result.getSuccessfulDays());
        assertEquals("Failed days should be 0", 0, result.getFailedDays());
        assertNotNull("Daily results map should be initialized", result.getDailyResults());
        assertTrue("Daily results map should be empty", result.getDailyResults().isEmpty());
        assertNull("Start date should be null initially", result.getStartDate());
        assertNull("End date should be null initially", result.getEndDate());
    }

    @Test
    public void testSetSuccess_ChangesSuccessStatus() {
        WorkflowResult result = new WorkflowResult();

        result.setSuccess(true);
        assertTrue("Should be successful after setting true", result.isSuccess());

        result.setSuccess(false);
        assertFalse("Should be failed after setting false", result.isSuccess());
    }

    @Test
    public void testSetProcessedDays_ChangesProcessedDays() {
        WorkflowResult result = new WorkflowResult();

        result.setProcessedDays(5);
        assertEquals("Processed days should change to 5", 5, result.getProcessedDays());
    }

    @Test
    public void testSetSuccessfulDays_ChangesSuccessfulDays() {
        WorkflowResult result = new WorkflowResult();

        result.setSuccessfulDays(3);
        assertEquals("Successful days should change to 3", 3, result.getSuccessfulDays());
    }

    @Test
    public void testSetFailedDays_ChangesFailedDays() {
        WorkflowResult result = new WorkflowResult();

        result.setFailedDays(2);
        assertEquals("Failed days should change to 2", 2, result.getFailedDays());
    }

    @Test
    public void testAddDailyResult_AddsToMap() {
        WorkflowResult result = new WorkflowResult();

        DailyProcessResult dailyResult = new DailyProcessResult("20250101");
        result.addDailyResult("20250101", dailyResult);

        Map<String, DailyProcessResult> map = result.getDailyResults();
        assertEquals("Map should have one entry", 1, map.size());
        assertEquals("Added daily result should match", dailyResult, map.get("20250101"));
    }

    @Test
    public void testGetDailyResult_ReturnsAddedResult() {
        WorkflowResult result = new WorkflowResult();

        DailyProcessResult dailyResult = new DailyProcessResult("20250102");
        result.addDailyResult("20250102", dailyResult);

        DailyProcessResult retrieved = result.getDailyResult("20250102");
        assertEquals("Retrieved daily result should match", dailyResult, retrieved);
    }

    @Test
    public void testGetDailyResult_ReturnsNullForMissingDate() {
        WorkflowResult result = new WorkflowResult();

        DailyProcessResult retrieved = result.getDailyResult("20250103");
        assertNull("Should return null for missing date", retrieved);
    }

    @Test
    public void testSetStartDate_SetsStartDate() {
        WorkflowResult result = new WorkflowResult();

        String startDate = "20250101";
        result.setStartDate(startDate);

        assertEquals("Start date should be set", startDate, result.getStartDate());
    }

    @Test
    public void testSetEndDate_SetsEndDate() {
        WorkflowResult result = new WorkflowResult();

        String endDate = "20250110";
        result.setEndDate(endDate);

        assertEquals("End date should be set", endDate, result.getEndDate());
    }
}