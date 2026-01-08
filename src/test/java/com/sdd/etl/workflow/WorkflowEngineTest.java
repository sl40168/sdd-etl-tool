package com.sdd.etl.workflow;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.sdd.etl.logging.StatusLogger;
import com.sdd.etl.model.DailyProcessResult;
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
}