package com.sdd.etl.workflow;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.sdd.etl.logging.StatusLogger;
import com.sdd.etl.model.DailyProcessResult;
import com.sdd.etl.model.SubprocessResult;
import com.sdd.etl.config.ETConfiguration;

import java.util.List;

/**
 * Unit tests for WorkflowEngine.
 */
public class WorkflowEngineTest {

    private StatusLogger mockStatusLogger;
    private DailyETLWorkflow mockDailyWorkflow;
    private WorkflowEngine workflowEngine;

    @Before
    public void setUp() {
        mockStatusLogger = mock(StatusLogger.class);
        mockDailyWorkflow = mock(DailyETLWorkflow.class);
        workflowEngine = new WorkflowEngine(mockStatusLogger, mockDailyWorkflow);
    }

    @Test
    public void testGenerateDateRange_SameDay() {
        // This test will fail until generateDateRange method is implemented
        List<String> dates = workflowEngine.generateDateRange("20250101", "20250101");
        assertEquals("Single day should return one date", 1, dates.size());
        assertEquals("Date should match input", "20250101", dates.get(0));
    }

    @Test
    public void testGenerateDateRange_MultipleDays() {
        List<String> dates = workflowEngine.generateDateRange("20250101", "20250103");
        assertEquals("Three days should return three dates", 3, dates.size());
        assertEquals("First date should be start date", "20250101", dates.get(0));
        assertEquals("Second date should be next day", "20250102", dates.get(1));
        assertEquals("Third date should be end date", "20250103", dates.get(2));
    }

    @Test
    public void testExecuteDay_DelegatesToDailyWorkflow() {
        String date = "20250101";
        ETConfiguration config = new ETConfiguration();
        DailyProcessResult expectedResult = new DailyProcessResult(date);

        when(mockDailyWorkflow.execute(date, config)).thenReturn(expectedResult);

        DailyProcessResult result = workflowEngine.executeDay(date, config);

        assertSame("Should return result from daily workflow", expectedResult, result);
        verify(mockDailyWorkflow).execute(date, config);
    }

    @Test
    public void testExecute_MultiDayRange_AllSuccessful() {
        // Setup
        String fromDate = "20250101";
        String toDate = "20250103";
        ETConfiguration config = new ETConfiguration();

        DailyProcessResult day1Result = new DailyProcessResult("20250101");
        day1Result.setSuccess(true);

        DailyProcessResult day2Result = new DailyProcessResult("20250102");
        day2Result.setSuccess(true);

        DailyProcessResult day3Result = new DailyProcessResult("20250103");
        day3Result.setSuccess(true);

        when(mockDailyWorkflow.execute("20250101", config)).thenReturn(day1Result);
        when(mockDailyWorkflow.execute("20250102", config)).thenReturn(day2Result);
        when(mockDailyWorkflow.execute("20250103", config)).thenReturn(day3Result);

        // Execute
        com.sdd.etl.model.WorkflowResult result = workflowEngine.execute(fromDate, toDate, config);

        // Verify
        assertTrue("Overall workflow should be successful", result.isSuccess());
        assertEquals("Should process 3 days", 3, result.getProcessedDays());
        assertEquals("Should have 3 successful days", 3, result.getSuccessfulDays());
        assertEquals("Should have 0 failed days", 0, result.getFailedDays());
        assertEquals("Start date should match", fromDate, result.getStartDate());
        assertEquals("End date should match", toDate, result.getEndDate());
        assertEquals("Should have 3 daily results", 3, result.getDailyResults().size());

        verify(mockDailyWorkflow, times(3)).execute(anyString(), eq(config));
    }

    @Test
    public void testExecute_DayFailure_StopsSubsequentDays() {
        // Setup
        String fromDate = "20250101";
        String toDate = "20250105";
        ETConfiguration config = new ETConfiguration();

        // Day 1 succeeds
        DailyProcessResult day1Result = new DailyProcessResult("20250101");
        day1Result.setSuccess(true);

        // Day 2 fails
        DailyProcessResult day2Result = new DailyProcessResult("20250102");
        day2Result.setSuccess(false);
        SubprocessResult day2ExtractResult = new SubprocessResult("Extraction failed");
        day2Result.addSubprocessResult("EXTRACT", day2ExtractResult);

        when(mockDailyWorkflow.execute("20250101", config)).thenReturn(day1Result);
        when(mockDailyWorkflow.execute("20250102", config)).thenReturn(day2Result);

        // Execute
        com.sdd.etl.model.WorkflowResult result = workflowEngine.execute(fromDate, toDate, config);

        // Verify
        assertFalse("Overall workflow should be unsuccessful", result.isSuccess());
        assertEquals("Should process 2 days before failure", 2, result.getProcessedDays());
        assertEquals("Should have 1 successful day", 1, result.getSuccessfulDays());
        assertEquals("Should have 1 failed day", 1, result.getFailedDays());
        assertEquals("Should have 2 daily results (no processing after failure)", 2, result.getDailyResults().size());

        // Verify that only first 2 days were processed
        verify(mockDailyWorkflow).execute("20250101", config);
        verify(mockDailyWorkflow).execute("20250102", config);
        verify(mockDailyWorkflow, never()).execute("20250103", config);
        verify(mockDailyWorkflow, never()).execute("20250104", config);
        verify(mockDailyWorkflow, never()).execute("20250105", config);

        // Verify summary was logged
        verify(mockStatusLogger).logSummary(eq(2), eq(1), eq(1), anyLong());
    }

    @Test
    public void testExecute_SingleDay_Successful() {
        // Setup
        String date = "20250101";
        ETConfiguration config = new ETConfiguration();

        DailyProcessResult dayResult = new DailyProcessResult(date);
        dayResult.setSuccess(true);

        when(mockDailyWorkflow.execute(date, config)).thenReturn(dayResult);

        // Execute
        com.sdd.etl.model.WorkflowResult result = workflowEngine.execute(date, date, config);

        // Verify
        assertTrue("Overall workflow should be successful", result.isSuccess());
        assertEquals("Should process 1 day", 1, result.getProcessedDays());
        assertEquals("Should have 1 successful day", 1, result.getSuccessfulDays());
        assertEquals("Should have 0 failed days", 0, result.getFailedDays());
        assertEquals("Start and end date should be the same", date, result.getStartDate());
        assertEquals("Start and end date should be the same", date, result.getEndDate());

        verify(mockDailyWorkflow).execute(date, config);
        verify(mockStatusLogger).logSummary(eq(1), eq(1), eq(0), anyLong());
    }

    @Test
    public void testExecute_DayFailure_LogsSummary() {
        // Setup
        String fromDate = "20250101";
        String toDate = "20250103";
        ETConfiguration config = new ETConfiguration();

        DailyProcessResult day1Result = new DailyProcessResult("20250101");
        day1Result.setSuccess(true);

        DailyProcessResult day2Result = new DailyProcessResult("20250102");
        day2Result.setSuccess(false);

        when(mockDailyWorkflow.execute("20250101", config)).thenReturn(day1Result);
        when(mockDailyWorkflow.execute("20250102", config)).thenReturn(day2Result);

        // Execute
        workflowEngine.execute(fromDate, toDate, config);

        // Verify summary was logged (should be called once when day fails)
        verify(mockStatusLogger, times(1)).logSummary(eq(2), eq(1), eq(1), anyLong());
    }
}