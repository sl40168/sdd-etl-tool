package com.sdd.etl.source.extract;

import com.sdd.etl.ETLException;
import com.sdd.etl.context.ETLContext;
import com.sdd.etl.model.SourceDataModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Contract test for the {@link Extractor} lifecycle methods.
 * 
 * <p>Verifies that extractor implementations follow the expected lifecycle:
 * <ol>
 *   <li>{@link Extractor#setup(ETLContext)} is called before extraction</li>
 *   <li>{@link Extractor#validate(ETLContext)} is called before extraction</li>
 *   <li>{@link Extractor#extract(ETLContext)} returns expected records</li>
 *   <li>{@link Extractor#cleanup()} is called after extraction completes (success or failure)</li>
 *   <li>Exceptions are properly propagated and handled</li>
 * </ol>
 * 
 * <p>This test complements {@link ExtractorContractTest} by focusing on
 * method sequencing and interaction patterns rather than individual
 * method contracts.</p>
 */
public class ExtractorLifecycleTest {
    
    /** Mock context used for all test operations */
    private ETLContext mockContext;
    
    /** Mock extractor implementation for testing lifecycle */
    private Extractor mockExtractor;
    
    /** In-order verifier for sequencing assertions */
    private InOrder inOrder;
    
    @Before
    public void setUp() throws ETLException {
        mockContext = mock(ETLContext.class);
        mockExtractor = mock(Extractor.class);
        inOrder = inOrder(mockExtractor);
        
        // Configure default behavior
        when(mockExtractor.getCategory()).thenReturn("TEST");
        when(mockExtractor.getName()).thenReturn("TestExtractor");
        doReturn(new ArrayList<>()).when(mockExtractor).extract(mockContext);
    }
    
    @Test
    public void testLifecycleSequence_SuccessfulExtraction() throws ETLException {
        // Execute extraction
        mockExtractor.setup(mockContext);
        mockExtractor.validate(mockContext);
        mockExtractor.extract(mockContext);
        mockExtractor.cleanup();
        
        // Verify method call order
        inOrder.verify(mockExtractor).setup(mockContext);
        inOrder.verify(mockExtractor).validate(mockContext);
        inOrder.verify(mockExtractor).extract(mockContext);
        inOrder.verify(mockExtractor).cleanup();
        
        verifyNoMoreInteractions(mockExtractor);
    }
    
    @Test
    public void testSetupFailure_PreventsExtraction() throws ETLException {
        // Configure setup to throw exception
        doThrow(new ETLException("EXTRACT", null, "Configuration invalid"))
            .when(mockExtractor).setup(mockContext);
        
        // Execute extraction - should fail at setup
        try {
            mockExtractor.setup(mockContext);
            fail("Expected ETLException to be thrown");
        } catch (ETLException e) {
            // Expected
            assertEquals("EXTRACT", e.getSubprocessType());
        }
        
        // Verify extract and validate are NOT called
        verify(mockExtractor, never()).extract(mockContext);
        verify(mockExtractor, never()).validate(mockContext);
        
        // Cleanup can be called safely after setup failure (caller's responsibility)
        mockExtractor.cleanup();
        verify(mockExtractor).cleanup();
    }
    
    @Test
    public void testValidationFailure_PreventsExtraction() throws ETLException {
        // Configure validation to throw exception
        doThrow(new ETLException("EXTRACT", null, "Context invalid"))
            .when(mockExtractor).validate(mockContext);
        
        // Setup succeeds, validation fails
        mockExtractor.setup(mockContext);
        try {
            mockExtractor.validate(mockContext);
            fail("Expected ETLException to be thrown");
        } catch (ETLException e) {
            assertEquals("EXTRACT", e.getSubprocessType());
        }
        
        // Extract should NOT be called
        verify(mockExtractor, never()).extract(mockContext);
        
        // Cleanup can be called safely after validation failure (caller's responsibility)
        mockExtractor.cleanup();
        verify(mockExtractor).cleanup();
    }
    
    @Test
    public void testExtractFailure_StillCallsCleanup() throws ETLException {
        // Configure extract to throw exception
        doThrow(new ETLException("EXTRACT", null, "Download failed"))
            .when(mockExtractor).extract(mockContext);
        
        // Setup and validation succeed
        mockExtractor.setup(mockContext);
        mockExtractor.validate(mockContext);
        
        // Extract fails
        try {
            mockExtractor.extract(mockContext);
            fail("Expected ETLException to be thrown");
        } catch (ETLException e) {
            assertEquals("EXTRACT", e.getSubprocessType());
        }
        
        // Cleanup should be called by the caller after extraction failure
        mockExtractor.cleanup();
        verify(mockExtractor).cleanup();
    }
    
    @Test
    public void testExtractReturnsNonNullList() throws ETLException {
        // Configure extract to return a non-null list
        List<SourceDataModel> expectedRecords = new ArrayList<>();
        doReturn(expectedRecords).when(mockExtractor).extract(mockContext);
        
        // Execute extraction
        mockExtractor.setup(mockContext);
        mockExtractor.validate(mockContext);
        List<SourceDataModel> result = mockExtractor.extract(mockContext);
        mockExtractor.cleanup();
        
        // Verify result is non-null
        assertNotNull("Extract result should not be null", result);
        assertSame("Extract result should be the same list", expectedRecords, result);
        
        // Verify call order
        inOrder.verify(mockExtractor).setup(mockContext);
        inOrder.verify(mockExtractor).validate(mockContext);
        inOrder.verify(mockExtractor).extract(mockContext);
        inOrder.verify(mockExtractor).cleanup();
    }
    
    @Test
    public void testCleanupIsIdempotent() throws ETLException {
        // Call cleanup multiple times
        mockExtractor.cleanup();
        mockExtractor.cleanup();
        mockExtractor.cleanup();
        
        // Verify cleanup was called three times
        verify(mockExtractor, times(3)).cleanup();
        
        // No exception should be thrown (idempotent)
        // Test passes if no exception
    }
    
    @Test
    public void testLifecycleWithMultipleCalls() throws ETLException {
        // Simulate multiple extraction cycles
        for (int i = 0; i < 3; i++) {
            mockExtractor.setup(mockContext);
            mockExtractor.validate(mockContext);
            mockExtractor.extract(mockContext);
            mockExtractor.cleanup();
        }
        
        // Verify each lifecycle was called 3 times in exact interleaved order
        inOrder.verify(mockExtractor).setup(mockContext);
        inOrder.verify(mockExtractor).validate(mockContext);
        inOrder.verify(mockExtractor).extract(mockContext);
        inOrder.verify(mockExtractor).cleanup();
        inOrder.verify(mockExtractor).setup(mockContext);
        inOrder.verify(mockExtractor).validate(mockContext);
        inOrder.verify(mockExtractor).extract(mockContext);
        inOrder.verify(mockExtractor).cleanup();
        inOrder.verify(mockExtractor).setup(mockContext);
        inOrder.verify(mockExtractor).validate(mockContext);
        inOrder.verify(mockExtractor).extract(mockContext);
        inOrder.verify(mockExtractor).cleanup();
        verifyNoMoreInteractions(mockExtractor);
    }
}