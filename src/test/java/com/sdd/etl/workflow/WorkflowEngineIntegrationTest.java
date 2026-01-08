package com.sdd.etl.workflow;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

import com.sdd.etl.config.ETConfiguration;
import com.sdd.etl.context.ContextManager;
import com.sdd.etl.context.ETLContext;
import com.sdd.etl.logging.StatusLogger;
import com.sdd.etl.model.DailyProcessResult;
import com.sdd.etl.model.SubprocessResult;
import com.sdd.etl.model.WorkflowResult;
import com.sdd.etl.subprocess.SubprocessInterface;
import com.sdd.etl.workflow.SubprocessExecutor;

import java.util.HashMap;
import java.util.Map;

/**
 * Integration tests for WorkflowEngine with real components.
 * Tests end-to-end workflow execution with mock subprocess implementations.
 */
public class WorkflowEngineIntegrationTest {

    private StatusLogger statusLogger;
    private SubprocessExecutor subprocessExecutor;
    private DailyETLWorkflow dailyWorkflow;
    private WorkflowEngine workflowEngine;

    @Before
    public void setUp() {
        statusLogger = new StatusLogger();
        subprocessExecutor = new SubprocessExecutor(statusLogger);
        dailyWorkflow = new DailyETLWorkflow(statusLogger, subprocessExecutor);
        workflowEngine = new WorkflowEngine(statusLogger, dailyWorkflow);
    }

    @After
    public void tearDown() {
        // Clean up any resources if needed
    }

    @Test
    public void testExecute_SingleDayWithMockSubprocesses() {
        // Setup
        String date = "20250101";
        ETConfiguration config = createTestConfiguration();

        // Create a mock daily workflow that returns a successful result
        DailyETLWorkflow mockWorkflow = new DailyETLWorkflow(statusLogger, subprocessExecutor) {
            @Override
            public DailyProcessResult execute(String dateStr, ETConfiguration configuration) {
                DailyProcessResult result = new DailyProcessResult(dateStr);

                // Create successful subprocess results using correct constructors
                SubprocessResult extractResult = new SubprocessResult(100);
                SubprocessResult transformResult = new SubprocessResult(100);
                SubprocessResult loadResult = new SubprocessResult(100);
                SubprocessResult validateResult = new SubprocessResult(0);
                SubprocessResult cleanResult = new SubprocessResult(0);

                result.addSubprocessResult("EXTRACT", extractResult);
                result.addSubprocessResult("TRANSFORM", transformResult);
                result.addSubprocessResult("LOAD", loadResult);
                result.addSubprocessResult("VALIDATE", validateResult);
                result.addSubprocessResult("CLEAN", cleanResult);
                result.setSuccess(true);

                return result;
            }
        };

        WorkflowEngine testEngine = new WorkflowEngine(statusLogger, mockWorkflow);

        // Execute
        WorkflowResult result = testEngine.execute(date, date, config);

        // Verify
        assertTrue("Workflow should be successful", result.isSuccess());
        assertEquals("Should process 1 day", 1, result.getProcessedDays());
        assertEquals("Should have 1 successful day", 1, result.getSuccessfulDays());
        assertEquals("Should have 0 failed days", 0, result.getFailedDays());
        assertNotNull("Should have daily results", result.getDailyResults());
        assertTrue("Should contain date 20250101", result.getDailyResults().containsKey("20250101"));

        DailyProcessResult dailyResult = result.getDailyResult("20250101");
        assertNotNull("Daily result should not be null", dailyResult);
        assertTrue("Day should be successful", dailyResult.isSuccess());
    }

    @Test
    public void testExecute_MultiDaySequentialExecution() {
        // Setup
        String fromDate = "20250101";
        String toDate = "20250103";
        ETConfiguration config = createTestConfiguration();

        // Create a mock daily workflow that returns successful results for all days
        DailyETLWorkflow mockWorkflow = new DailyETLWorkflow(statusLogger, subprocessExecutor) {
            @Override
            public DailyProcessResult execute(String dateStr, ETConfiguration configuration) {
                DailyProcessResult result = new DailyProcessResult(dateStr);

                // Simulate different data counts for each day
                int dataCount = Integer.parseInt(dateStr.substring(6)) * 10;

                SubprocessResult extractResult = new SubprocessResult(dataCount);
                SubprocessResult transformResult = new SubprocessResult(dataCount);
                SubprocessResult loadResult = new SubprocessResult(dataCount);
                SubprocessResult validateResult = new SubprocessResult(0);
                SubprocessResult cleanResult = new SubprocessResult(0);

                result.addSubprocessResult("EXTRACT", extractResult);
                result.addSubprocessResult("TRANSFORM", transformResult);
                result.addSubprocessResult("LOAD", loadResult);
                result.addSubprocessResult("VALIDATE", validateResult);
                result.addSubprocessResult("CLEAN", cleanResult);
                result.setSuccess(true);

                return result;
            }
        };

        WorkflowEngine testEngine = new WorkflowEngine(statusLogger, mockWorkflow);

        // Execute
        WorkflowResult result = testEngine.execute(fromDate, toDate, config);

        // Verify
        assertTrue("Workflow should be successful", result.isSuccess());
        assertEquals("Should process 3 days", 3, result.getProcessedDays());
        assertEquals("Should have 3 successful days", 3, result.getSuccessfulDays());
        assertEquals("Should have 0 failed days", 0, result.getFailedDays());
        assertEquals("Start date should match", fromDate, result.getStartDate());
        assertEquals("End date should match", toDate, result.getEndDate());

        // Verify each day's result
        assertTrue("Should contain 20250101", result.getDailyResults().containsKey("20250101"));
        assertTrue("Should contain 20250102", result.getDailyResults().containsKey("20250102"));
        assertTrue("Should contain 20250103", result.getDailyResults().containsKey("20250103"));

        DailyProcessResult day1 = result.getDailyResult("20250101");
        assertEquals("Day 1 should have 10 records", 10, day1.getSubprocessResult("EXTRACT").getDataCount());

        DailyProcessResult day2 = result.getDailyResult("20250102");
        assertEquals("Day 2 should have 20 records", 20, day2.getSubprocessResult("EXTRACT").getDataCount());

        DailyProcessResult day3 = result.getDailyResult("20250103");
        assertEquals("Day 3 should have 30 records", 30, day3.getSubprocessResult("EXTRACT").getDataCount());
    }

    @Test
    public void testExecute_DayFailure_StopsProcessing() {
        // Setup
        String fromDate = "20250101";
        String toDate = "20250105";
        ETConfiguration config = createTestConfiguration();

        // Create a mock daily workflow that fails on day 2
        DailyETLWorkflow mockWorkflow = new DailyETLWorkflow(statusLogger, subprocessExecutor) {
            private int dayCount = 0;

            @Override
            public DailyProcessResult execute(String dateStr, ETConfiguration configuration) {
                dayCount++;
                DailyProcessResult result = new DailyProcessResult(dateStr);

                if (dayCount == 2) {
                    // Fail on day 2
                    SubprocessResult extractResult = new SubprocessResult("Extraction failed");
                    result.addSubprocessResult("EXTRACT", extractResult);
                    result.setSuccess(false);
                } else {
                    // Succeed on other days
                    SubprocessResult extractResult = new SubprocessResult(100);
                    SubprocessResult transformResult = new SubprocessResult(100);
                    SubprocessResult loadResult = new SubprocessResult(100);
                    SubprocessResult validateResult = new SubprocessResult(0);
                    SubprocessResult cleanResult = new SubprocessResult(0);

                    result.addSubprocessResult("EXTRACT", extractResult);
                    result.addSubprocessResult("TRANSFORM", transformResult);
                    result.addSubprocessResult("LOAD", loadResult);
                    result.addSubprocessResult("VALIDATE", validateResult);
                    result.addSubprocessResult("CLEAN", cleanResult);
                    result.setSuccess(true);
                }

                return result;
            }
        };

        WorkflowEngine testEngine = new WorkflowEngine(statusLogger, mockWorkflow);

        // Execute
        WorkflowResult result = testEngine.execute(fromDate, toDate, config);

        // Verify
        assertFalse("Workflow should fail due to day 2 failure", result.isSuccess());
        assertEquals("Should process only 2 days before failure", 2, result.getProcessedDays());
        assertEquals("Should have 1 successful day", 1, result.getSuccessfulDays());
        assertEquals("Should have 1 failed day", 1, result.getFailedDays());

        // Verify that only first 2 days have results
        assertTrue("Should contain 20250101", result.getDailyResults().containsKey("20250101"));
        assertTrue("Should contain 20250102", result.getDailyResults().containsKey("20250102"));
        assertFalse("Should NOT contain 20250103", result.getDailyResults().containsKey("20250103"));
        assertFalse("Should NOT contain 20250104", result.getDailyResults().containsKey("20250104"));
        assertFalse("Should NOT contain 20250105", result.getDailyResults().containsKey("20250105"));

        // Verify day 2 failed
        DailyProcessResult day2 = result.getDailyResult("20250102");
        assertFalse("Day 2 should be marked as failed", day2.isSuccess());
    }

    @Test
    public void testExecute_DailyResultAggregation() {
        // Setup
        String fromDate = "20250101";
        String toDate = "20250104";
        ETConfiguration config = createTestConfiguration();

        DailyETLWorkflow mockWorkflow = new DailyETLWorkflow(statusLogger, subprocessExecutor) {
            @Override
            public DailyProcessResult execute(String dateStr, ETConfiguration configuration) {
                DailyProcessResult result = new DailyProcessResult(dateStr);

                // Alternating success and failure
                boolean isSuccess = Integer.parseInt(dateStr.substring(6)) % 2 != 0;

                if (isSuccess) {
                    SubprocessResult extractResult = new SubprocessResult(100);
                    SubprocessResult transformResult = new SubprocessResult(100);
                    SubprocessResult loadResult = new SubprocessResult(100);
                    SubprocessResult validateResult = new SubprocessResult(0);
                    SubprocessResult cleanResult = new SubprocessResult(0);

                    result.addSubprocessResult("EXTRACT", extractResult);
                    result.addSubprocessResult("TRANSFORM", transformResult);
                    result.addSubprocessResult("LOAD", loadResult);
                    result.addSubprocessResult("VALIDATE", validateResult);
                    result.addSubprocessResult("CLEAN", cleanResult);
                    result.setSuccess(true);
                } else {
                    SubprocessResult extractResult = new SubprocessResult("Failed");
                    result.addSubprocessResult("EXTRACT", extractResult);
                    result.setSuccess(false);
                }

                return result;
            }
        };

        WorkflowEngine testEngine = new WorkflowEngine(statusLogger, mockWorkflow);

        // Execute
        WorkflowResult result = testEngine.execute(fromDate, toDate, config);

        // Verify
        assertFalse("Workflow should fail due to day 2 failure", result.isSuccess());
        assertEquals("Should process 2 days before failure", 2, result.getProcessedDays());
        assertEquals("Should have 1 successful day", 1, result.getSuccessfulDays());
        assertEquals("Should have 1 failed day", 1, result.getFailedDays());

        // Verify day 1 succeeded
        DailyProcessResult day1 = result.getDailyResult("20250101");
        assertTrue("Day 1 should be successful", day1.isSuccess());
        assertNotNull("Day 1 extract result should not be null", day1.getSubprocessResult("EXTRACT"));
        assertTrue("Day 1 extract should be successful", day1.getSubprocessResult("EXTRACT").isSuccess());

        // Verify day 2 failed
        DailyProcessResult day2 = result.getDailyResult("20250102");
        assertFalse("Day 2 should be failed", day2.isSuccess());
        assertFalse("Day 2 extract should be failed", day2.getSubprocessResult("EXTRACT").isSuccess());
    }

    /**
     * Creates a test ETConfiguration object.
     *
     * @return test configuration
     */
    private ETConfiguration createTestConfiguration() {
        ETConfiguration config = new ETConfiguration();
        // Add minimal configuration for testing
        // Configuration details can be expanded as needed
        return config;
    }
}
