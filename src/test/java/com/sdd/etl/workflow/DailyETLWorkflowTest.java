package com.sdd.etl.workflow;

import com.sdd.etl.ETLException;
import com.sdd.etl.config.ETConfiguration;
import com.sdd.etl.context.ETLContext;
import com.sdd.etl.context.SubprocessType;
import com.sdd.etl.logging.StatusLogger;
import com.sdd.etl.model.DailyProcessResult;
import com.sdd.etl.model.SubprocessResult;
import com.sdd.etl.subprocess.SubprocessInterface;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for DailyETLWorkflow.
 */
public class DailyETLWorkflowTest {

    private StatusLogger mockStatusLogger;
    private SubprocessExecutor mockSubprocessExecutor;
    private DailyETLWorkflow dailyETLWorkflow;
    
    @Before
    public void setUp() {
        mockStatusLogger = mock(StatusLogger.class);
        mockSubprocessExecutor = mock(SubprocessExecutor.class);
        dailyETLWorkflow = new DailyETLWorkflow(mockStatusLogger, mockSubprocessExecutor);
    }
    
    @Test
    public void testExecute_Success() throws ETLException {
        // Given
        String date = "20250101";
        ETConfiguration mockConfig = mock(ETConfiguration.class);
        
        // Create mock subprocesses
        SubprocessInterface mockExtract = mock(SubprocessInterface.class);
        SubprocessInterface mockTransform = mock(SubprocessInterface.class);
        SubprocessInterface mockLoad = mock(SubprocessInterface.class);
        SubprocessInterface mockValidate = mock(SubprocessInterface.class);
        SubprocessInterface mockClean = mock(SubprocessInterface.class);
        
        when(mockExtract.getType()).thenReturn(SubprocessType.EXTRACT);
        when(mockTransform.getType()).thenReturn(SubprocessType.TRANSFORM);
        when(mockLoad.getType()).thenReturn(SubprocessType.LOAD);
        when(mockValidate.getType()).thenReturn(SubprocessType.VALIDATE);
        when(mockClean.getType()).thenReturn(SubprocessType.CLEAN);
        
        List<SubprocessInterface> mockSubprocesses = Arrays.asList(
            mockExtract, mockTransform, mockLoad, mockValidate, mockClean
        );
        
        // Create mock context
        ETLContext mockContext = mock(ETLContext.class);
        when(mockContext.getCurrentDate()).thenReturn(date);
        when(mockContext.getConfig()).thenReturn(mockConfig);
        when(mockContext.getCurrentSubprocess()).thenReturn(SubprocessType.EXTRACT);
        when(mockContext.getExtractedDataCount()).thenReturn(0);
        when(mockContext.getTransformedDataCount()).thenReturn(0);
        when(mockContext.getLoadedDataCount()).thenReturn(0);
        
        // Mock subprocess results
        Map<String, SubprocessResult> mockResults = new HashMap<>();
        mockResults.put("EXTRACT", new SubprocessResult(100));
        mockResults.put("TRANSFORM", new SubprocessResult(95));
        mockResults.put("LOAD", new SubprocessResult(95));
        mockResults.put("VALIDATE", new SubprocessResult(95));
        mockResults.put("CLEAN", new SubprocessResult(0));
        
        when(mockSubprocessExecutor.executeAll(mockSubprocesses, mockContext)).thenReturn(mockResults);
        
        // Create DailyETLWorkflow instance that overrides createContext and createSubprocesses
        DailyETLWorkflow workflow = new DailyETLWorkflow(mockStatusLogger, mockSubprocessExecutor) {
            @Override
            protected ETLContext createContext(String date, ETConfiguration config) {
                return mockContext;
            }
            
            @Override
            protected List<SubprocessInterface> createSubprocesses() {
                return mockSubprocesses;
            }
        };
        
        // When
        DailyProcessResult result = workflow.execute(date, mockConfig);
        
        // Then
        assertTrue("Day should be successful", result.isSuccess());
        assertEquals("Date should match", date, result.getDate());
        assertEquals("Should have 5 subprocess results", 5, result.getSubprocessResults().size());
        assertNotNull("Context should be set", result.getContext());
        
        // Verify logging was called
        verify(mockStatusLogger).logDayCompletion(date, 5, true);
    }
    
    @Test
    public void testExecute_Failure() throws ETLException {
        // Given
        String date = "20250101";
        ETConfiguration mockConfig = mock(ETConfiguration.class);
        
        // Create mock subprocesses
        SubprocessInterface mockExtract = mock(SubprocessInterface.class);
        SubprocessInterface mockTransform = mock(SubprocessInterface.class);
        
        when(mockExtract.getType()).thenReturn(SubprocessType.EXTRACT);
        when(mockTransform.getType()).thenReturn(SubprocessType.TRANSFORM);
        
        List<SubprocessInterface> mockSubprocesses = Arrays.asList(mockExtract, mockTransform);
        
        // Create mock context
        ETLContext mockContext = mock(ETLContext.class);
        when(mockContext.getCurrentDate()).thenReturn(date);
        when(mockContext.getConfig()).thenReturn(mockConfig);
        when(mockContext.getCurrentSubprocess()).thenReturn(SubprocessType.EXTRACT);
        when(mockContext.getExtractedDataCount()).thenReturn(0);
        when(mockContext.getTransformedDataCount()).thenReturn(0);
        when(mockContext.getLoadedDataCount()).thenReturn(0);
        
        // Mock subprocess failure
        when(mockSubprocessExecutor.executeAll(mockSubprocesses, mockContext))
            .thenThrow(new ETLException("EXTRACT", date, "Extract failed"));
        
        // Create DailyETLWorkflow instance that overrides createContext and createSubprocesses
        DailyETLWorkflow workflow = new DailyETLWorkflow(mockStatusLogger, mockSubprocessExecutor) {
            @Override
            protected ETLContext createContext(String date, ETConfiguration config) {
                return mockContext;
            }
            
            @Override
            protected List<SubprocessInterface> createSubprocesses() {
                return mockSubprocesses;
            }
        };
        
        // When
        DailyProcessResult result = workflow.execute(date, mockConfig);
        
        // Then
        assertFalse("Day should be failed", result.isSuccess());
        assertEquals("Date should match", date, result.getDate());
        assertTrue("Subprocess results should be empty on failure", result.getSubprocessResults().isEmpty());
        
        // Verify error logging was called
        verify(mockStatusLogger).logError(date, "EXTRACT", "Extract failed");
        verify(mockStatusLogger).logDayCompletion(date, 0, false);
    }
    
    
}