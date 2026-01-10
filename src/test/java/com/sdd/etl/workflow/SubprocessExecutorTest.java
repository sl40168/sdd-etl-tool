package com.sdd.etl.workflow;

import com.sdd.etl.ETLException;
import com.sdd.etl.util.DateUtils;
import java.time.LocalDate;
import com.sdd.etl.context.ETLContext;
import com.sdd.etl.context.SubprocessType;
import com.sdd.etl.logging.StatusLogger;
import com.sdd.etl.model.SubprocessResult;
import com.sdd.etl.subprocess.SubprocessInterface;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for SubprocessExecutor.
 */
public class SubprocessExecutorTest {

    private StatusLogger mockStatusLogger;
    private SubprocessExecutor subprocessExecutor;
    
    @Before
    public void setUp() {
        mockStatusLogger = mock(StatusLogger.class);
        subprocessExecutor = new SubprocessExecutor(mockStatusLogger);
    }
    
    @Test
    public void testExecute_Success() throws ETLException {
        // Given
        SubprocessInterface mockSubprocess = mock(SubprocessInterface.class);
        ETLContext mockContext = mock(ETLContext.class);
        
        when(mockSubprocess.getType()).thenReturn(SubprocessType.EXTRACT);
        when(mockSubprocess.execute(mockContext)).thenReturn(100);
        
        // When
        SubprocessResult result = subprocessExecutor.execute(mockSubprocess, mockContext);
        
        // Then
        assertTrue("Result should be successful", result.isSuccess());
        assertEquals("Data count should be 100", 100, result.getDataCount());
        verify(mockContext).setCurrentSubprocess(SubprocessType.EXTRACT);
        verify(mockSubprocess).validateContext(mockContext);
    }
    
    @Test
    public void testExecute_Failure() throws ETLException {
        // Given
        SubprocessInterface mockSubprocess = mock(SubprocessInterface.class);
        ETLContext mockContext = mock(ETLContext.class);
        
        when(mockSubprocess.getType()).thenReturn(SubprocessType.EXTRACT);
        when(mockSubprocess.execute(mockContext)).thenThrow(new ETLException("EXTRACT", "20250101", "Extract failed"));
        
        // When
        SubprocessResult result = subprocessExecutor.execute(mockSubprocess, mockContext);
        
        // Then
        assertFalse("Result should be failure", result.isSuccess());
        assertNotNull("Error message should not be null", result.getErrorMessage());
        verify(mockContext).setCurrentSubprocess(SubprocessType.EXTRACT);
        verify(mockSubprocess).validateContext(mockContext);
    }
    
    @Test
    public void testExecuteAll_Success() throws ETLException {
        // Given
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
        
        // Simulate each subprocess updating context
        // We'll use a spy on a real context to track state changes
        ETLContext realContext = new ETLContext();
        ETLContext spyContext = spy(realContext);
        when(spyContext.getCurrentDate()).thenReturn(DateUtils.parseDate("20250101"));
        
        // Set up execute behaviors to update context
        when(mockExtract.execute(spyContext)).thenAnswer(invocation -> {
            spyContext.setExtractedDataCount(100);
            spyContext.setExtractedData(new Object());
            return 100;
        });
        
        when(mockTransform.execute(spyContext)).thenAnswer(invocation -> {
            spyContext.setTransformedDataCount(95);
            spyContext.setTransformedData(new Object());
            return 95;
        });
        
        when(mockLoad.execute(spyContext)).thenAnswer(invocation -> {
            spyContext.setLoadedDataCount(95);
            return 95;
        });
        
        when(mockValidate.execute(spyContext)).thenAnswer(invocation -> {
            spyContext.setValidationPassed(true);
            return 95;
        });
        
        when(mockClean.execute(spyContext)).thenAnswer(invocation -> {
            spyContext.setCleanupPerformed(true);
            return 0;
        });
        
        List<SubprocessInterface> subprocesses = Arrays.asList(
            mockExtract, mockTransform, mockLoad, mockValidate, mockClean
        );
        
        // When
        Map<String, SubprocessResult> results = subprocessExecutor.executeAll(subprocesses, spyContext);
        
        // Then
        assertEquals("Should have 5 results", 5, results.size());
        assertTrue("Extract result should be successful", results.get("EXTRACT").isSuccess());
        assertEquals("Extract data count should be 100", 100, results.get("EXTRACT").getDataCount());
        
        // Verify logging was called for each subprocess
        verify(mockStatusLogger).logSubprocessCompletion("20250101", "EXTRACT", results.get("EXTRACT"));
        verify(mockStatusLogger).logSubprocessCompletion("20250101", "TRANSFORM", results.get("TRANSFORM"));
        verify(mockStatusLogger).logSubprocessCompletion("20250101", "LOAD", results.get("LOAD"));
        verify(mockStatusLogger).logSubprocessCompletion("20250101", "VALIDATE", results.get("VALIDATE"));
        verify(mockStatusLogger).logSubprocessCompletion("20250101", "CLEAN", results.get("CLEAN"));
    }
    
    @Test(expected = ETLException.class)
    public void testExecuteAll_Failure() throws ETLException {
        // Given
        SubprocessInterface mockExtract = mock(SubprocessInterface.class);
        SubprocessInterface mockTransform = mock(SubprocessInterface.class);
        
        when(mockExtract.getType()).thenReturn(SubprocessType.EXTRACT);
        when(mockTransform.getType()).thenReturn(SubprocessType.TRANSFORM);
        
        when(mockExtract.execute(any(ETLContext.class))).thenReturn(100);
        when(mockTransform.execute(any(ETLContext.class))).thenThrow(new ETLException("TRANSFORM", "20250101", "Transform failed"));
        
        List<SubprocessInterface> subprocesses = Arrays.asList(mockExtract, mockTransform);
        
        ETLContext mockContext = mock(ETLContext.class);
        when(mockContext.getCurrentDate()).thenReturn(DateUtils.parseDate("20250101"));
        // Mock context state after extract
        when(mockContext.getExtractedDataCount()).thenReturn(100);
        when(mockContext.getExtractedData()).thenReturn(new Object());
        
        // When
        subprocessExecutor.executeAll(subprocesses, mockContext);
        
        // Then - expected exception
    }
    
    @Test
    public void testExecuteAll_SequenceOrder() throws ETLException {
        // Given
        SubprocessInterface mockExtract = mock(SubprocessInterface.class);
        SubprocessInterface mockTransform = mock(SubprocessInterface.class);
        SubprocessInterface mockLoad = mock(SubprocessInterface.class);
        
        when(mockExtract.getType()).thenReturn(SubprocessType.EXTRACT);
        when(mockTransform.getType()).thenReturn(SubprocessType.TRANSFORM);
        when(mockLoad.getType()).thenReturn(SubprocessType.LOAD);
        
        // Use a spy context to track execution order
        ETLContext realContext = new ETLContext();
        ETLContext spyContext = spy(realContext);
        when(spyContext.getCurrentDate()).thenReturn(DateUtils.parseDate("20250101"));
        
        // Set up execute behaviors
        when(mockExtract.execute(spyContext)).thenAnswer(invocation -> {
            spyContext.setExtractedDataCount(100);
            return 100;
        });
        
        when(mockTransform.execute(spyContext)).thenAnswer(invocation -> {
            spyContext.setTransformedDataCount(95);
            return 95;
        });
        
        when(mockLoad.execute(spyContext)).thenAnswer(invocation -> {
            spyContext.setLoadedDataCount(95);
            return 95;
        });
        
        List<SubprocessInterface> subprocesses = Arrays.asList(mockExtract, mockTransform, mockLoad);
        
        // When
        subprocessExecutor.executeAll(subprocesses, spyContext);
        
        // Then - verify execution order using InOrder
        org.mockito.InOrder inOrder = inOrder(mockExtract, mockTransform, mockLoad);
        inOrder.verify(mockExtract).execute(spyContext);
        inOrder.verify(mockTransform).execute(spyContext);
        inOrder.verify(mockLoad).execute(spyContext);
    }
    
    @Test
    public void testExtractFailureStopsTransform() throws ETLException {
        // Given
        SubprocessInterface mockExtract = mock(SubprocessInterface.class);
        SubprocessInterface mockTransform = mock(SubprocessInterface.class);
        
        when(mockExtract.getType()).thenReturn(SubprocessType.EXTRACT);
        when(mockTransform.getType()).thenReturn(SubprocessType.TRANSFORM);
        
        when(mockExtract.execute(any(ETLContext.class))).thenThrow(new ETLException("EXTRACT", "20250101", "Extract failed"));
        
        List<SubprocessInterface> subprocesses = Arrays.asList(mockExtract, mockTransform);
        
        ETLContext mockContext = mock(ETLContext.class);
        when(mockContext.getCurrentDate()).thenReturn(DateUtils.parseDate("20250101"));
        
        // When
        try {
            subprocessExecutor.executeAll(subprocesses, mockContext);
            fail("Should have thrown ETLException");
        } catch (ETLException e) {
            // Then - verify transform was NOT executed
            verify(mockExtract).execute(mockContext);
            verify(mockTransform, never()).execute(mockContext);
        }
    }
    
    @Test
    public void testValidateFailureStopsClean() throws ETLException {
        // Given
        SubprocessInterface mockValidate = mock(SubprocessInterface.class);
        SubprocessInterface mockClean = mock(SubprocessInterface.class);
        
        when(mockValidate.getType()).thenReturn(SubprocessType.VALIDATE);
        when(mockClean.getType()).thenReturn(SubprocessType.CLEAN);
        
        when(mockValidate.execute(any(ETLContext.class))).thenThrow(new ETLException("VALIDATE", "20250101", "Validate failed"));
        
        List<SubprocessInterface> subprocesses = Arrays.asList(mockValidate, mockClean);
        
        ETLContext mockContext = mock(ETLContext.class);
        when(mockContext.getCurrentDate()).thenReturn(DateUtils.parseDate("20250101"));
        // Mock that load completed
        when(mockContext.getLoadedDataCount()).thenReturn(95);
        
        // When
        try {
            subprocessExecutor.executeAll(subprocesses, mockContext);
            fail("Should have thrown ETLException");
        } catch (ETLException e) {
            // Then - verify clean was NOT executed
            verify(mockValidate).execute(mockContext);
            verify(mockClean, never()).execute(mockContext);
        }
    }
}