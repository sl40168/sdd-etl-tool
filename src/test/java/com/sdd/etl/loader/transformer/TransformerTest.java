package com.sdd.etl.loader.transformer;

import com.sdd.etl.model.TargetDataModel;
import com.sdd.etl.model.SourceDataModel;
import com.sdd.etl.loader.transformer.exceptions.TransformationException;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Abstract base test class for Transformer implementations.
 *
 * <p>Provides common test utilities and test cases that should be inherited
 * by concrete transformer test classes.</p>
 *
 * @param <S> Source data model type
 * @param <T> Target data model type
 */
public abstract class TransformerTest<S extends SourceDataModel, T extends TargetDataModel> {
    
    /**
     * Creates a new transformer instance to test.
     * 
     * @return new transformer instance
     */
    protected abstract Transformer<S, T> createTransformer();
    
    /**
     * Creates a new source model instance for testing.
     * 
     * @return new source model instance
     */
    protected abstract S createSourceModel();
    
    /**
     * Creates a new target model instance for testing.
     * 
     * @return new target model instance
     */
    protected abstract T createTargetModel();
    
    /**
     * Gets the data type identifier for the transformer being tested.
     * 
     * @return data type identifier
     */
    protected abstract String getDataType();
    
    /**
     * Test transforming an empty list.
     */
    @Test
    public void testTransform_emptyList() throws TransformationException {
        Transformer<S, T> transformer = createTransformer();
        List<S> emptyList = new ArrayList<>();

        List<T> result = transformer.transform(emptyList);

        assertNotNull("Result should not be null", result);
        assertTrue("Result should be empty list", result.isEmpty());
    }

    /**
     * Test transforming with null input (should throw exception).
     */
    @Test(expected = IllegalArgumentException.class)
    public void testTransform_nullInput() throws TransformationException {
        Transformer<S, T> transformer = createTransformer();
        transformer.transform(null);
    }

    /**
     * Test transforming a single record.
     */
    @Test
    public void testTransform_singleRecord() throws TransformationException {
        Transformer<S, T> transformer = createTransformer();
        S source = createSourceModel();

        List<S> sources = new ArrayList<>();
        sources.add(source);

        List<T> result = transformer.transform(sources);

        assertNotNull("Result should not be null", result);
        assertEquals("Result should have one record", 1, result.size());
    }

    /**
     * Test transforming multiple records.
     */
    @Test
    public void testTransform_multipleRecords() throws TransformationException {
        Transformer<S, T> transformer = createTransformer();

        List<S> sources = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            sources.add(createSourceModel());
        }

        List<T> result = transformer.transform(sources);

        assertNotNull("Result should not be null", result);
        assertEquals("Result should have five records", 5, result.size());
    }

    /**
     * Test that null fields in source are handled with sentinel values.
     */
    @Test
    public void testTransform_nullSourceFields() throws TransformationException {
        Transformer<S, T> transformer = createTransformer();
        S source = createNullSourceModel();

        List<S> sources = new ArrayList<>();
        sources.add(source);

        List<T> result = transformer.transform(sources);

        assertNotNull("Result should not be null", result);
        assertEquals("Result should have one record", 1, result.size());

        // Verify null fields are handled (specific implementations override this)
        T target = result.get(0);
        verifyNullFieldHandling(target);
    }
    
    /**
     * Creates a source model with all fields set to null.
     * 
     * @return source model with null fields
     */
    protected S createNullSourceModel() {
        return createSourceModel(); // Subclasses should override if needed
    }
    
    /**
     * Verifies that null fields are correctly handled with sentinel values.
     * 
     * @param target the target model to verify
     */
    protected void verifyNullFieldHandling(T target) {
        // Default implementation does nothing
        // Subclasses should override to verify specific sentinel values
    }
    
    /**
     * Converts a string date in YYYY.MM.DD format to LocalDate.
     * 
     * @param dateStr date string in YYYY.MM.DD format
     * @return LocalDate instance
     */
    protected LocalDate parseDate(String dateStr) {
        if (dateStr == null) {
            return null;
        }
        return LocalDate.parse(dateStr.replace(".", ""));
    }
    
    /**
     * Converts a LocalDateTime to Instant using system timezone.
     * 
     * @param dateTime LocalDateTime to convert
     * @return Instant instance
     */
    protected java.time.Instant toInstant(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atZone(ZoneId.systemDefault()).toInstant();
    }
    
    /**
     * Asserts that two doubles are equal, accounting for NaN values.
     * 
     * @param expected expected value
     * @param actual actual value
     * @param delta tolerance for comparison
     */
    protected void assertEqualsDouble(double expected, double actual, double delta) {
        if (Double.isNaN(expected)) {
            assertTrue("Expected NaN but got: " + actual, Double.isNaN(actual));
        } else if (Double.isNaN(actual)) {
            fail("Actual is NaN but expected: " + expected);
        } else {
            assertEquals("Double values should be equal", expected, actual, delta);
        }
    }
}
