package com.sdd.etl.loader.api;

import com.sdd.etl.loader.api.exceptions.LoaderException;
import com.sdd.etl.model.TargetDataModel;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for Loader interface contract.
 */
public class LoaderTest {

    /**
     * Mock implementation of Loader for testing.
     */
    private static class MockLoader implements Loader {
        private boolean shutdownCalled = false;
        private String lastSortField = null;
        private int loadCallCount = 0;

        @Override
        public void sortData(List<? extends TargetDataModel> data, String sortFieldName) throws LoaderException {
            this.lastSortField = sortFieldName;
        }

        @Override
        public void loadData(List<? extends TargetDataModel> data) throws LoaderException {
            this.loadCallCount++;
        }

        @Override
        public void shutdown() {
            this.shutdownCalled = true;
        }

        public boolean isShutdownCalled() {
            return shutdownCalled;
        }

        public String getLastSortField() {
            return lastSortField;
        }

        public int getLoadCallCount() {
            return loadCallCount;
        }
    }

    /**
     * Mock TargetDataModel for testing.
     */
    private static class MockDataModel extends TargetDataModel {
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
    public void testSortDataPassesSortField() throws LoaderException {
        MockLoader loader = new MockLoader();
        List<TargetDataModel> data = new ArrayList<>();

        loader.sortData(data, "timestamp");

        assertEquals("sortField should be passed correctly", "timestamp", loader.getLastSortField());
    }

    @Test
    public void testLoadDataCanBeCalledMultipleTimes() throws LoaderException {
        MockLoader loader = new MockLoader();
        List<TargetDataModel> data = new ArrayList<>();

        loader.loadData(data);
        loader.loadData(data);
        loader.loadData(data);

        assertEquals("loadData should be callable multiple times", 3, loader.getLoadCallCount());
    }

    @Test
    public void testShutdownIsCallable() {
        MockLoader loader = new MockLoader();
        loader.shutdown();

        assertTrue("shutdown should be callable", loader.isShutdownCalled());
    }

    @Test
    public void testShutdownIsIdempotent() {
        MockLoader loader = new MockLoader();
        loader.shutdown();
        loader.shutdown();
        loader.shutdown();

        assertTrue("shutdown should be idempotent", loader.isShutdownCalled());
    }

    @Test(expected = LoaderException.class)
    public void testSortDataCanThrowException() throws LoaderException {
        Loader loader = new Loader() {
            @Override
            public void sortData(List<? extends TargetDataModel> data, String sortFieldName) throws LoaderException {
                throw new LoaderException("Test exception");
            }

            @Override
            public void loadData(List<? extends TargetDataModel> data) throws LoaderException {
            }

            @Override
            public void shutdown() {
            }
        };

        loader.sortData(new ArrayList<TargetDataModel>(), "timestamp");
    }
}
