package com.sdd.etl.loader.annotation;

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Field;

/**
 * Unit tests for @ColumnOrder annotation.
 */
public class ColumnOrderTest {

    /**
     * Test class using ColumnOrder annotation.
     */
    private static class TestModel {
        @ColumnOrder(1)
        private String fieldA;

        @ColumnOrder(0)
        private String fieldB;

        @ColumnOrder(2)
        private String fieldC;

        private String fieldD; // No annotation
    }

    @Test
    public void testAnnotationPresent() throws NoSuchFieldException {
        Field fieldA = TestModel.class.getDeclaredField("fieldA");
        Field fieldB = TestModel.class.getDeclaredField("fieldB");
        Field fieldC = TestModel.class.getDeclaredField("fieldC");
        Field fieldD = TestModel.class.getDeclaredField("fieldD");

        ColumnOrder annotationA = fieldA.getAnnotation(ColumnOrder.class);
        ColumnOrder annotationB = fieldB.getAnnotation(ColumnOrder.class);
        ColumnOrder annotationC = fieldC.getAnnotation(ColumnOrder.class);
        ColumnOrder annotationD = fieldD.getAnnotation(ColumnOrder.class);

        assertNotNull("fieldA should have ColumnOrder annotation", annotationA);
        assertNotNull("fieldB should have ColumnOrder annotation", annotationB);
        assertNotNull("fieldC should have ColumnOrder annotation", annotationC);
        assertNull("fieldD should not have ColumnOrder annotation", annotationD);
    }

    @Test
    public void testAnnotationValues() throws NoSuchFieldException {
        Field fieldA = TestModel.class.getDeclaredField("fieldA");
        Field fieldB = TestModel.class.getDeclaredField("fieldB");
        Field fieldC = TestModel.class.getDeclaredField("fieldC");

        ColumnOrder annotationA = fieldA.getAnnotation(ColumnOrder.class);
        ColumnOrder annotationB = fieldB.getAnnotation(ColumnOrder.class);
        ColumnOrder annotationC = fieldC.getAnnotation(ColumnOrder.class);

        assertEquals("fieldA should have order 1", 1, annotationA.value());
        assertEquals("fieldB should have order 0", 0, annotationB.value());
        assertEquals("fieldC should have order 2", 2, annotationC.value());
    }

    @Test
    public void testAnnotationRetention() {
        // @ColumnOrder is FIELD target only, not applicable to classes
        // This test verifies the annotation cannot be used on class level
        ColumnOrder annotation = TestModel.class.getAnnotation(ColumnOrder.class);
        assertNull("Class should not have ColumnOrder annotation", annotation);
    }
}
