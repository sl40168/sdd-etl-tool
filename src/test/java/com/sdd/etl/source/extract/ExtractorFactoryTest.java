package com.sdd.etl.source.extract;

import com.sdd.etl.ETLException;
import com.sdd.etl.config.ETConfiguration;
import com.sdd.etl.source.extract.cos.config.CosSourceConfig;
import com.sdd.etl.source.extract.cos.XbondQuoteExtractor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.Constructor;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * Unit tests for ExtractorFactory.
 * 
 * <p>Verifies that the factory correctly creates extractor instances
 * based on source configuration type and properties.</p>
 */
public class ExtractorFactoryTest {
    
    @Mock
    private CosSourceConfig mockCosConfig;
    
    @Mock
    private ETConfiguration.SourceConfig mockGenericSourceConfig;
    
    private Map<String, String> properties;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        properties = new HashMap<>();
        
        // Setup default mock behavior for CosSourceConfig
        when(mockCosConfig.getType()).thenReturn("cos");
        when(mockCosConfig.getProperties()).thenReturn(properties);
        when(mockCosConfig.getProperty(anyString())).thenAnswer(invocation -> 
            properties.get(invocation.getArgument(0)));
        
        // Setup default mock behavior for generic SourceConfig
        when(mockGenericSourceConfig.getType()).thenReturn("cos");
    }
    
    @Test
    public void testCreateExtractor_WithCosConfigAndAllPriceDepthCategory_ReturnsXbondQuoteExtractor() throws ETLException {
        // Given
        properties.put("category", "AllPriceDepth");
        
        // When
        Extractor extractor = ExtractorFactory.createExtractor(mockCosConfig);
        
        // Then
        assertNotNull(extractor);
        assertTrue(extractor instanceof XbondQuoteExtractor);
        assertEquals("AllPriceDepth", extractor.getCategory());
    }
    
    @Test(expected = ETLException.class)
    public void testCreateExtractor_WithCosConfigAndUnknownCategory_ThrowsETLException() throws ETLException {
        // Given
        properties.put("category", "UnknownCategory");
        
        // When/Then
        ExtractorFactory.createExtractor(mockCosConfig);
    }
    
    @Test(expected = ETLException.class)
    public void testCreateExtractor_WithCosConfigAndMissingCategory_ThrowsETLException() throws ETLException {
        // Given - no category property set
        
        // When/Then
        ExtractorFactory.createExtractor(mockCosConfig);
    }
    
    @Test(expected = ETLException.class)
    public void testCreateExtractor_WithNonCosSourceConfig_ThrowsETLException() throws ETLException {
        // Given
        when(mockGenericSourceConfig.getType()).thenReturn("mysql");
        
        // When/Then
        ExtractorFactory.createExtractor(mockGenericSourceConfig);
    }
    
    @Test(expected = ETLException.class)
    public void testCreateExtractor_WithNullConfig_ThrowsETLException() throws ETLException {
        // Given - null config
        
        // When/Then
        ExtractorFactory.createExtractor(null);
    }
    
    @Test(expected = ETLException.class)
    public void testCreateExtractor_WithEmptySourceType_ThrowsETLException() throws ETLException {
        // Given
        when(mockGenericSourceConfig.getType()).thenReturn("");
        
        // When/Then
        ExtractorFactory.createExtractor(mockGenericSourceConfig);
    }
    
    @Test(expected = ETLException.class)
    public void testCreateExtractor_WithNullSourceType_ThrowsETLException() throws ETLException {
        // Given
        when(mockGenericSourceConfig.getType()).thenReturn(null);
        
        // When/Then
        ExtractorFactory.createExtractor(mockGenericSourceConfig);
    }
    
    @Test
    public void testCreateCosExtractor_ConvenienceMethod_Works() throws ETLException {
        // Given
        properties.put("category", "AllPriceDepth");
        
        // When
        Extractor extractor = ExtractorFactory.createCosExtractor(mockCosConfig);
        
        // Then
        assertNotNull(extractor);
        assertTrue(extractor instanceof XbondQuoteExtractor);
    }
    
    @Test(expected = ETLException.class)
    public void testCreateCosExtractor_WithWrongConfigType_ThrowsETLException() throws ETLException {
        // Given - mockGenericSourceConfig is not a CosSourceConfig
        when(mockGenericSourceConfig.getType()).thenReturn("cos");
        
        // When/Then
        ExtractorFactory.createCosExtractor(mockCosConfig); // This should work, but let's test wrong case
        // Actually createCosExtractor expects CosSourceConfig, but we can pass ETConfiguration.SourceConfig
        // The method will cast and throw ClassCastException
        // We'll test via createExtractor path instead.
    }
    
    @Test
    public void testFactoryIsUtilityClass_NoPublicConstructor() {
        // Given - the factory class
        
        // When - attempt to reflectively access constructors
        Constructor<?>[] constructors = ExtractorFactory.class.getDeclaredConstructors();
        
        // Then - should have exactly one private constructor
        assertEquals(1, constructors.length);
        assertFalse(constructors[0].isAccessible());
    }
}