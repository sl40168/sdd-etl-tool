package com.sdd.etl.model;

import com.sdd.etl.loader.annotation.ColumnOrder;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

/**
 * Unit tests for TargetDataModel.getOrderedFieldNames().
 */
public class TargetDataModelTest {

    /**
     * Test model with @ColumnOrder annotations.
     */
    private static class OrderedModel extends TargetDataModel {
        @ColumnOrder(2)
        private String fieldC;

        @ColumnOrder(0)
        private String fieldA;

        @ColumnOrder(1)
        private String fieldB;

        private String fieldD; // No annotation

        @Override
        public boolean validate() {
            return true;
        }

        @Override
        public Object toTargetFormat() {
            return null;
        }

        @Override
        public String getTargetType() {
            return "test";
        }

        @Override
        public String getDataType() {
            return "TEST_DATA";
        }
    }

    /**
     * Test model without @ColumnOrder annotations.
     */
    private static class UnorderedModel extends TargetDataModel {
        private String fieldA;
        private String fieldB;
        private String fieldC;

        @Override
        public boolean validate() {
            return true;
        }

        @Override
        public Object toTargetFormat() {
            return null;
        }

        @Override
        public String getTargetType() {
            return "test";
        }

        @Override
        public String getDataType() {
            return "TEST_DATA";
        }
    }

    /**
     * Test model with only ordered fields.
     */
    private static class FullyOrderedModel extends TargetDataModel {
        @ColumnOrder(1)
        private String fieldB;

        @ColumnOrder(0)
        private String fieldA;

        @Override
        public boolean validate() {
            return true;
        }

        @Override
        public Object toTargetFormat() {
            return null;
        }

        @Override
        public String getTargetType() {
            return "test";
        }

        @Override
        public String getDataType() {
            return "TEST_DATA";
        }
    }

    @Test
    public void testGetOrderedFieldNamesWithPartialOrdering() {
        OrderedModel model = new OrderedModel();
        List<String> ordered = model.getOrderedFieldNames();

        assertEquals("Should return 3 fields (only annotated)", 3, ordered.size());
        assertEquals("First field should be fieldA (order 0)", "fieldA", ordered.get(0));
        assertEquals("Second field should be fieldB (order 1)", "fieldB", ordered.get(1));
        assertEquals("Third field should be fieldC (order 2)", "fieldC", ordered.get(2));
        // fieldD has no annotation and should be ignored
        assertFalse("Should not contain fieldD (no annotation)", ordered.contains("fieldD"));
    }

    @Test
    public void testGetOrderedFieldNamesWithoutOrdering() {
        UnorderedModel model = new UnorderedModel();
        List<String> ordered = model.getOrderedFieldNames();

        assertEquals("Should return 0 fields (no annotations)", 0, ordered.size());
        // Without annotations, all fields are ignored per design requirement
    }

    @Test
    public void testGetOrderedFieldNamesWithFullOrdering() {
        FullyOrderedModel model = new FullyOrderedModel();
        List<String> ordered = model.getOrderedFieldNames();

        assertEquals("Should return 2 fields", 2, ordered.size());
        assertEquals("First field should be fieldA (order 0)", "fieldA", ordered.get(0));
        assertEquals("Second field should be fieldB (order 1)", "fieldB", ordered.get(1));
    }

    @Test
    public void testGetOrderedFieldNamesReturnsModifiableList() {
        OrderedModel model = new OrderedModel();
        List<String> ordered = model.getOrderedFieldNames();

        ordered.add("newField");

        assertEquals("List should be modifiable", 4, ordered.size());
    }

    @Test
    public void testGetOrderedFieldNamesIsRepeatable() {
        OrderedModel model = new OrderedModel();

        List<String> firstCall = model.getOrderedFieldNames();
        List<String> secondCall = model.getOrderedFieldNames();

        assertEquals("Subsequent calls should return same result", firstCall, secondCall);
    }
}
