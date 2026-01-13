package com.sdd.etl.workflow;

import com.sdd.etl.ETLException;
import com.sdd.etl.config.ETConfiguration;
import com.sdd.etl.context.ETLContext;
import com.sdd.etl.util.DateUtils;
import com.sdd.etl.context.SubprocessType;
import com.sdd.etl.logging.StatusLogger;
import com.sdd.etl.model.DailyProcessResult;
import com.sdd.etl.model.SubprocessResult;
import com.sdd.etl.subprocess.SubprocessInterface;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.*;

/**
 * Integration tests for DailyETLWorkflow with complete subprocess sequence.
 * Tests end-to-end daily workflow execution with mock subprocess implementations.
 */
public class DailyETLWorkflowIntegrationTest {

    private StatusLogger statusLogger;
    private SubprocessExecutor subprocessExecutor;
    private DailyETLWorkflow dailyWorkflow;
    
    @Before
    public void setUp() {
        statusLogger = new StatusLogger();
        subprocessExecutor = new SubprocessExecutor(statusLogger);
        dailyWorkflow = new DailyETLWorkflow(statusLogger, subprocessExecutor);
    }
    
    @After
    public void tearDown() {
        // Clean up any resources if needed
    }
    
    @Test
    public void testCompleteSubprocessSequence_Success() throws ETLException {
        // Given
        String date = "20250101";
        ETConfiguration config = createTestConfiguration();
        
        // Create an extended DailyETLWorkflow with mock subprocesses
        DailyETLWorkflow testWorkflow = new DailyETLWorkflow(statusLogger, subprocessExecutor) {
            @Override
            protected List<SubprocessInterface> createSubprocesses() {
                // Create mock subprocesses for each type
                SubprocessInterface extract = new SubprocessInterface() {
                    @Override
                    public SubprocessType getType() {
                        return SubprocessType.EXTRACT;
                    }
                    
                    @Override
                    public int execute(ETLContext context) throws ETLException {
                        // Simulate extraction of 100 records
                        context.setExtractedDataCount(100);
                        context.setExtractedData(Collections.emptyList());
                        return 100;
                    }
                    
                    @Override
                    public void validateContext(ETLContext context) throws ETLException {
                        // Basic validation - context should be in initial state
                        if (context.getExtractedDataCount() != 0) {
                            throw new ETLException("EXTRACT", DateUtils.formatDate(context.getCurrentDate()),
                                "Context already has extracted data");
                        }
                    }
                };
                
                SubprocessInterface transform = new SubprocessInterface() {
                    @Override
                    public SubprocessType getType() {
                        return SubprocessType.TRANSFORM;
                    }
                    
                    @Override
                    public int execute(ETLContext context) throws ETLException {
                        // Simulate transformation of 95 records (5 rejected)
                        context.setTransformedDataCount(95);
                        context.setTransformedData(new Object());
                        return 95;
                    }
                    
                    @Override
                    public void validateContext(ETLContext context) throws ETLException {
                        // Validate that extract completed
                        if (context.getExtractedDataCount() == 0) {
                            throw new ETLException("TRANSFORM", DateUtils.formatDate(context.getCurrentDate()),
                                "No data extracted to transform");
                        }
                    }
                };
                
                SubprocessInterface load = new SubprocessInterface() {
                    @Override
                    public SubprocessType getType() {
                        return SubprocessType.LOAD;
                    }
                    
                    @Override
                    public int execute(ETLContext context) throws ETLException {
                        // Simulate loading of 95 records
                        context.setLoadedDataCount(95);
                        return 95;
                    }
                    
                    @Override
                    public void validateContext(ETLContext context) throws ETLException {
                        // Validate that transform completed
                        if (context.getTransformedDataCount() == 0) {
                            throw new ETLException("LOAD", DateUtils.formatDate(context.getCurrentDate()),
                                "No data transformed to load");
                        }
                    }
                };
                
                SubprocessInterface validate = new SubprocessInterface() {
                    @Override
                    public SubprocessType getType() {
                        return SubprocessType.VALIDATE;
                    }
                    
                    @Override
                    public int execute(ETLContext context) throws ETLException {
                        // Simulate validation passing
                        context.setValidationPassed(true);
                        return 95; // Validated records
                    }
                    
                    @Override
                    public void validateContext(ETLContext context) throws ETLException {
                        // Validate that load completed
                        if (context.getLoadedDataCount() == 0) {
                            throw new ETLException("VALIDATE", DateUtils.formatDate(context.getCurrentDate()),
                                "No data loaded to validate");
                        }
                    }
                };
                
                SubprocessInterface clean = new SubprocessInterface() {
                    @Override
                    public SubprocessType getType() {
                        return SubprocessType.CLEAN;
                    }
                    
                    @Override
                    public int execute(ETLContext context) throws ETLException {
                        // Simulate cleanup performed
                        context.setCleanupPerformed(true);
                        return 0; // No data processed in cleanup
                    }
                    
                    @Override
                    public void validateContext(ETLContext context) throws ETLException {
                        // Validate that validation completed
                        if (!context.isValidationPassed()) {
                            throw new ETLException("CLEAN", DateUtils.formatDate(context.getCurrentDate()),
                                "Validation did not pass - cannot clean");
                        }
                    }
                };
                
                return Arrays.asList(extract, transform, load, validate, clean);
            }
        };
        
        // When
        DailyProcessResult result = testWorkflow.execute(date, config);
        
        // Then
        assertTrue("Day should be successful", result.isSuccess());
        assertEquals("Date should match", date, result.getDate());
        
        Map<String, SubprocessResult> subprocessResults = result.getSubprocessResults();
        assertEquals("Should have 5 subprocess results", 5, subprocessResults.size());
        
        // Verify each subprocess result
        assertTrue("Extract should be successful", subprocessResults.get("EXTRACT").isSuccess());
        assertEquals("Extract should have 100 records", 100, subprocessResults.get("EXTRACT").getDataCount());
        
        assertTrue("Transform should be successful", subprocessResults.get("TRANSFORM").isSuccess());
        assertEquals("Transform should have 95 records", 95, subprocessResults.get("TRANSFORM").getDataCount());
        
        assertTrue("Load should be successful", subprocessResults.get("LOAD").isSuccess());
        assertEquals("Load should have 95 records", 95, subprocessResults.get("LOAD").getDataCount());
        
        assertTrue("Validate should be successful", subprocessResults.get("VALIDATE").isSuccess());
        assertEquals("Validate should have 95 records", 95, subprocessResults.get("VALIDATE").getDataCount());
        
        assertTrue("Clean should be successful", subprocessResults.get("CLEAN").isSuccess());
        assertEquals("Clean should have 0 records", 0, subprocessResults.get("CLEAN").getDataCount());
        
        // Verify context state after execution
        ETLContext context = result.getContext();
        assertNotNull("Context should not be null", context);
        assertEquals("Extracted data count should be 100", 100, context.getExtractedDataCount());
        assertEquals("Transformed data count should be 95", 95, context.getTransformedDataCount());
        assertEquals("Loaded data count should be 95", 95, context.getLoadedDataCount());
        assertTrue("Validation should have passed", context.isValidationPassed());
        assertTrue("Cleanup should have been performed", context.isCleanupPerformed());
    }
    
    @Test
    public void testCompleteSubprocessSequence_ExtractFailure() throws ETLException {
        // Given
        String date = "20250101";
        ETConfiguration config = createTestConfiguration();
        
        // Create an extended DailyETLWorkflow where extract fails
        DailyETLWorkflow testWorkflow = new DailyETLWorkflow(statusLogger, subprocessExecutor) {
            @Override
            protected List<SubprocessInterface> createSubprocesses() {
                // Create failing extract subprocess
                SubprocessInterface extract = new SubprocessInterface() {
                    @Override
                    public SubprocessType getType() {
                        return SubprocessType.EXTRACT;
                    }
                    
                    @Override
                    public int execute(ETLContext context) throws ETLException {
                        throw new ETLException("EXTRACT", date, "Database connection failed");
                    }
                    
                    @Override
                    public void validateContext(ETLContext context) throws ETLException {
                        // Validation passes but execution will fail
                    }
                };
                
                // Create other subprocesses (should not be executed)
                SubprocessInterface transform = new SubprocessInterface() {
                    @Override
                    public SubprocessType getType() {
                        return SubprocessType.TRANSFORM;
                    }
                    
                    @Override
                    public int execute(ETLContext context) throws ETLException {
                        fail("Transform should not be executed after extract failure");
                        return 0;
                    }
                    
                    @Override
                    public void validateContext(ETLContext context) throws ETLException {
                        // Should not be called
                    }
                };
                
                SubprocessInterface load = new SubprocessInterface() {
                    @Override
                    public SubprocessType getType() {
                        return SubprocessType.LOAD;
                    }
                    
                    @Override
                    public int execute(ETLContext context) throws ETLException {
                        fail("Load should not be executed after extract failure");
                        return 0;
                    }
                    
                    @Override
                    public void validateContext(ETLContext context) throws ETLException {
                        // Should not be called
                    }
                };
                
                SubprocessInterface validate = new SubprocessInterface() {
                    @Override
                    public SubprocessType getType() {
                        return SubprocessType.VALIDATE;
                    }
                    
                    @Override
                    public int execute(ETLContext context) throws ETLException {
                        fail("Validate should not be executed after extract failure");
                        return 0;
                    }
                    
                    @Override
                    public void validateContext(ETLContext context) throws ETLException {
                        // Should not be called
                    }
                };
                
                SubprocessInterface clean = new SubprocessInterface() {
                    @Override
                    public SubprocessType getType() {
                        return SubprocessType.CLEAN;
                    }
                    
                    @Override
                    public int execute(ETLContext context) throws ETLException {
                        fail("Clean should not be executed after extract failure");
                        return 0;
                    }
                    
                    @Override
                    public void validateContext(ETLContext context) throws ETLException {
                        // Should not be called
                    }
                };
                
                return Arrays.asList(extract, transform, load, validate, clean);
            }
        };
        
        // When
        DailyProcessResult result = testWorkflow.execute(date, config);
        
        // Then
        assertFalse("Day should be failed", result.isSuccess());
        assertEquals("Date should match", date, result.getDate());
        
        Map<String, SubprocessResult> subprocessResults = result.getSubprocessResults();
        assertTrue("Subprocess results should be empty on failure", subprocessResults.isEmpty());
        
        // Verify context is not set in result (should be null)
        assertNull("Context should be null in failed result", result.getContext());
    }
    
    @Test
    public void testCompleteSubprocessSequence_ValidateFailure() throws ETLException {
        // Given
        String date = "20250101";
        ETConfiguration config = createTestConfiguration();
        
        // Create an extended DailyETLWorkflow where validate fails
        DailyETLWorkflow testWorkflow = new DailyETLWorkflow(statusLogger, subprocessExecutor) {
            @Override
            protected List<SubprocessInterface> createSubprocesses() {
                // Create successful extract, transform, load subprocesses
                SubprocessInterface extract = new SubprocessInterface() {
                    @Override
                    public SubprocessType getType() {
                        return SubprocessType.EXTRACT;
                    }
                    
                    @Override
                    public int execute(ETLContext context) throws ETLException {
                        context.setExtractedDataCount(100);
                        context.setExtractedData(Collections.emptyList());
                        return 100;
                    }
                    
                    @Override
                    public void validateContext(ETLContext context) throws ETLException {
                        // Validation passes
                    }
                };
                
                SubprocessInterface transform = new SubprocessInterface() {
                    @Override
                    public SubprocessType getType() {
                        return SubprocessType.TRANSFORM;
                    }
                    
                    @Override
                    public int execute(ETLContext context) throws ETLException {
                        context.setTransformedDataCount(95);
                        context.setTransformedData(new Object());
                        return 95;
                    }
                    
                    @Override
                    public void validateContext(ETLContext context) throws ETLException {
                        // Validation passes
                    }
                };
                
                SubprocessInterface load = new SubprocessInterface() {
                    @Override
                    public SubprocessType getType() {
                        return SubprocessType.LOAD;
                    }
                    
                    @Override
                    public int execute(ETLContext context) throws ETLException {
                        context.setLoadedDataCount(95);
                        return 95;
                    }
                    
                    @Override
                    public void validateContext(ETLContext context) throws ETLException {
                        // Validation passes
                    }
                };
                
                // Create failing validate subprocess
                SubprocessInterface validate = new SubprocessInterface() {
                    @Override
                    public SubprocessType getType() {
                        return SubprocessType.VALIDATE;
                    }
                    
                    @Override
                    public int execute(ETLContext context) throws ETLException {
                        throw new ETLException("VALIDATE", date, "Data validation failed - integrity check");
                    }
                    
                    @Override
                    public void validateContext(ETLContext context) throws ETLException {
                        // Validation passes
                    }
                };
                
                // Create clean subprocess (should not be executed)
                SubprocessInterface clean = new SubprocessInterface() {
                    @Override
                    public SubprocessType getType() {
                        return SubprocessType.CLEAN;
                    }
                    
                    @Override
                    public int execute(ETLContext context) throws ETLException {
                        fail("Clean should not be executed after validate failure");
                        return 0;
                    }
                    
                    @Override
                    public void validateContext(ETLContext context) throws ETLException {
                        // Should not be called
                    }
                };
                
                return Arrays.asList(extract, transform, load, validate, clean);
            }
        };
        
        // When
        DailyProcessResult result = testWorkflow.execute(date, config);
        
        // Then
        assertFalse("Day should be failed", result.isSuccess());
        assertEquals("Date should match", date, result.getDate());
        
        Map<String, SubprocessResult> subprocessResults = result.getSubprocessResults();
        // Extract, transform, load completed but validate failed
        // In current implementation, when a subprocess fails, the entire workflow fails
        // and no results are aggregated (empty map)
        assertTrue("Subprocess results should be empty on failure", subprocessResults.isEmpty());
        
        // Verify context is not set in result (should be null)
        assertNull("Context should be null in failed result", result.getContext());
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