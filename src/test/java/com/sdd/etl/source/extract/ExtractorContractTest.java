package com.sdd.etl.source.extract;

import com.sdd.etl.ETLException;
import com.sdd.etl.context.ETLContext;
import com.sdd.etl.model.SourceDataModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Contract test for the {@link Extractor} interface.
 * 
 * <p>Verifies that any implementation of the Extractor interface
 * adheres to the contract defined in the interface documentation.</p>
 * 
 * <p><strong>Contract Requirements</strong>:
 * <ol>
 *   <li>{@link Extractor#getCategory()} returns non-null, non-empty string</li>
 *   <li>{@link Extractor#getName()} returns non-null string</li>
 *   <li>{@link Extractor#setup(ETLContext)} can throw {@link ETLException} for invalid configuration</li>
 *   <li>{@link Extractor#extract(ETLContext)} returns non-null list (may be empty)</li>
 *   <li>{@link Extractor#cleanup()} is idempotent (safe to call multiple times)</li>
 *   <li>{@link Extractor#validate(ETLContext)} can throw {@link ETLException} for invalid context</li>
 * </ol>
 */
public class ExtractorContractTest {

    /** Test implementation of Extractor interface for contract verification */
    private static class TestExtractor implements Extractor {
        private String category;
        private String name;
        private boolean shouldThrowSetupException;
        private boolean shouldThrowValidateException;
        private List<SourceDataModel> extractResult;
        
        TestExtractor(String category, String name) {
            this.category = category;
            this.name = name;
            this.shouldThrowSetupException = false;
            this.shouldThrowValidateException = false;
            this.extractResult = new ArrayList<>();
        }
        
        @Override
        public String getCategory() {
            return category;
        }
        
        @Override
        public String getName() {
            return name;
        }
        
        @Override
        public void setup(ETLContext context) throws ETLException {
            if (shouldThrowSetupException) {
                throw new ETLException("TEST", null, "Setup failed by test");
            }
        }
        
        @Override
        public List<SourceDataModel> extract(ETLContext context) throws ETLException {
            return extractResult;
        }
        
        @Override
        public void cleanup() throws ETLException {
            // No resources to clean up
        }
        
        @Override
        public void validate(ETLContext context) throws ETLException {
            if (shouldThrowValidateException) {
                throw new ETLException("TEST", null, "Validation failed by test");
            }
        }
        
        // Test control methods
        void setShouldThrowSetupException(boolean shouldThrow) {
            this.shouldThrowSetupException = shouldThrow;
        }
        
        void setShouldThrowValidateException(boolean shouldThrow) {
            this.shouldThrowValidateException = shouldThrow;
        }
        
        void setExtractResult(List<SourceDataModel> result) {
            this.extractResult = result != null ? result : new ArrayList<>();
        }
    }
    
    private TestExtractor extractor;
    private ETLContext mockContext;
    
    @Before
    public void setUp() {
        extractor = new TestExtractor("TEST", "TestExtractor");
        mockContext = Mockito.mock(ETLContext.class);
    }
    
    @Test
    public void testGetCategory_ReturnsNonNullNonEmptyString() {
        assertNotNull("Category should not be null", extractor.getCategory());
        assertFalse("Category should not be empty", extractor.getCategory().trim().isEmpty());
    }
    
    @Test
    public void testGetName_ReturnsNonNullString() {
        assertNotNull("Name should not be null", extractor.getName());
    }
    
    @Test
    public void testSetup_ThrowsETLExceptionForInvalidConfiguration() {
        // Test that setup can throw ETLException
        extractor.setShouldThrowSetupException(true);
        
        try {
            extractor.setup(mockContext);
            fail("Expected ETLException to be thrown");
        } catch (ETLException e) {
            // Expected
            assertEquals("Subprocess type should match", "TEST", e.getSubprocessType());
        }
    }
    
    @Test
    public void testExtract_ReturnsNonNullList() throws ETLException {
        List<SourceDataModel> result = extractor.extract(mockContext);
        assertNotNull("Extract result should not be null", result);
    }
    
    @Test
    public void testExtract_ReturnsEmptyListWhenNoData() throws ETLException {
        List<SourceDataModel> result = extractor.extract(mockContext);
        assertTrue("Extract result should be empty when no data", result.isEmpty());
    }
    
    @Test
    public void testExtract_ReturnsProvidedRecords() throws ETLException {
        List<SourceDataModel> expected = new ArrayList<>();
        SourceDataModel mockRecord = mock(SourceDataModel.class);
        expected.add(mockRecord);
        
        extractor.setExtractResult(expected);
        List<SourceDataModel> result = extractor.extract(mockContext);
        
        assertEquals("Extract result should match provided records", expected, result);
    }
    
    @Test
    public void testCleanup_IsIdempotent() throws ETLException {
        // First call should succeed
        extractor.cleanup();
        
        // Second call should also succeed (no exception)
        extractor.cleanup();
        
        // No assertion needed - test passes if no exception
    }
    
    @Test
    public void testValidate_ThrowsETLExceptionForInvalidContext() {
        // Test that validate can throw ETLException
        extractor.setShouldThrowValidateException(true);
        
        try {
            extractor.validate(mockContext);
            fail("Expected ETLException to be thrown");
        } catch (ETLException e) {
            // Expected
            assertEquals("Subprocess type should match", "TEST", e.getSubprocessType());
        }
    }
    
    @Test
    public void testValidate_SucceedsForValidContext() throws ETLException {
        // Default test extractor should not throw exception
        extractor.validate(mockContext);
        
        // No assertion needed - test passes if no exception
    }
}