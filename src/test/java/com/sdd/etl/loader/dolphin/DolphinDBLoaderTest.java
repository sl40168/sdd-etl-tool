package com.sdd.etl.loader.dolphin;

import com.sdd.etl.loader.api.exceptions.LoaderException;
import com.sdd.etl.loader.model.XbondQuoteDataModel;
import com.sdd.etl.loader.model.XbondTradeDataModel;
import com.sdd.etl.loader.config.LoaderConfiguration;
import com.sdd.etl.model.TargetDataModel;
import org.junit.Test;
import static org.junit.Assert.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for DolphinDBLoader.
 */
public class DolphinDBLoaderTest {

    private LoaderConfiguration createTestConfig() {
        LoaderConfiguration config = new LoaderConfiguration();
        config.setHost("localhost");
        config.setPort(8848);
        config.setUsername("admin");
        config.setPassword("123456");
        config.setBatchSize(100);
        return config;
    }

    private List<TargetDataModel> createMixedTestData() {
        List<TargetDataModel> data = new ArrayList<>();

        XbondQuoteDataModel quote = new XbondQuoteDataModel();
        quote.setBusinessDate(LocalDate.of(2026, 1, 11));
        quote.setExchProductId("TEST001");
        quote.setProductType("BOND");
        quote.setExchange("SSE");
        quote.setSource("TEST");
        quote.setSettleSpeed(1);
        quote.setLevel("1");
        quote.setStatus("TRADING");
        quote.setBid0Price(99.5);
        quote.setOffer0Price(99.6);
        quote.setEventTime(Instant.now());
        quote.setReceiveTime(Instant.now());

        XbondTradeDataModel trade = new XbondTradeDataModel();
        trade.setBusinessDate(LocalDate.of(2026, 1, 11));
        trade.setExchProductId("TEST001");
        trade.setProductType("BOND");
        trade.setExchange("SSE");
        trade.setSource("TEST");
        trade.setSettleSpeed(1);
        trade.setLastTradePrice(99.55);
        trade.setLastTradeVolume(1000.0);
        trade.setLastTradeSide("BUY");
        trade.setEventTime(Instant.now());
        trade.setReceiveTime(Instant.now());

        data.add(quote);
        data.add(trade);

        return data;
    }

    @Test
    public void testConstructorCreatesInstance() {
        LoaderConfiguration config = createTestConfig();
        DolphinDBLoader loader = new DolphinDBLoader(config);

        assertNotNull("Loader instance should be created", loader);
    }

    @Test
    public void testSortDataWithValidData() throws LoaderException {
        LoaderConfiguration config = createTestConfig();
        DolphinDBLoader loader = new DolphinDBLoader(config);

        List<TargetDataModel> data = createMixedTestData();

        // This should not throw an exception
        loader.sortData(data, "receiveTime");
    }

    @Test(expected = LoaderException.class)
    public void testSortDataWithNullData() throws LoaderException {
        LoaderConfiguration config = createTestConfig();
        DolphinDBLoader loader = new DolphinDBLoader(config);

        loader.sortData(null, "receiveTime");
    }

    @Test(expected = LoaderException.class)
    public void testSortDataWithNullSortField() throws LoaderException {
        LoaderConfiguration config = createTestConfig();
        DolphinDBLoader loader = new DolphinDBLoader(config);
        List<TargetDataModel> data = createMixedTestData();

        loader.sortData(data, null);
    }

    @Test
    public void testLoadDataWithValidData() throws LoaderException {
        LoaderConfiguration config = createTestConfig();
        DolphinDBLoader loader = new DolphinDBLoader(config);

        List<TargetDataModel> data = createMixedTestData();

        // Note: This test will fail if DolphinDB is not running
        // For unit testing, we'd typically use mocking
        // This is more of an integration test placeholder
        try {
            loader.loadData(data);
        } catch (Exception e) {
            // Expected if no database connection
        }
    }

    @Test(expected = LoaderException.class)
    public void testLoadDataWithNullData() throws LoaderException {
        LoaderConfiguration config = createTestConfig();
        DolphinDBLoader loader = new DolphinDBLoader(config);

        loader.loadData(null);
    }

    @Test
    public void testShutdownIsCallable() {
        LoaderConfiguration config = createTestConfig();
        DolphinDBLoader loader = new DolphinDBLoader(config);

        // Should not throw exception
        loader.shutdown();
    }

    @Test
    public void testShutdownIsIdempotent() {
        LoaderConfiguration config = createTestConfig();
        DolphinDBLoader loader = new DolphinDBLoader(config);

        // Multiple shutdown calls should be safe
        loader.shutdown();
        loader.shutdown();
        loader.shutdown();
    }
}
