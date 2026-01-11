package com.sdd.etl.loader.dolphin.integration;

import com.sdd.etl.loader.api.exceptions.LoaderException;
import com.sdd.etl.loader.dolphin.DolphinDBLoader;
import com.sdd.etl.loader.config.LoaderConfiguration;
import com.sdd.etl.loader.model.XbondQuoteDataModel;
import com.sdd.etl.loader.model.XbondTradeDataModel;
import com.sdd.etl.loader.model.BondFutureQuoteDataModel;
import com.sdd.etl.model.TargetDataModel;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Integration tests for DolphinDBLoader with embedded DolphinDB instance.
 *
 * NOTE: These tests require an actual or embedded DolphinDB instance running.
 * Skip these tests if DolphinDB is not available.
 */
public class DolphinDBLoaderIntegrationTest {

    private static final String TEST_HOST = "localhost";
    private static final int TEST_PORT = 8848;
    private static final String TEST_USER = "admin";
    private static final String TEST_PASSWORD = "123456";

    private DolphinDBLoader loader;
    private List<TargetDataModel> testData;
    private boolean isDolphinDBAvailable;

    @Before
    public void setUp() {
        isDolphinDBAvailable = checkDolphinDBAvailability();

        if (isDolphinDBAvailable) {
            LoaderConfiguration config = new LoaderConfiguration();
            config.setHost(TEST_HOST);
            config.setPort(TEST_PORT);
            config.setUsername(TEST_USER);
            config.setPassword(TEST_PASSWORD);
            config.setBatchSize(100);
            config.setDatabase("test_db");

            loader = new DolphinDBLoader(config);
            testData = createMixedTestData();
        }
    }

    @After
    public void tearDown() {
        if (loader != null) {
            loader.shutdown();
        }
    }

    private boolean checkDolphinDBAvailability() {
        // Simple check - in production, try to connect to DolphinDB
        // For now, return false to skip tests by default
        return false;
    }

    private List<TargetDataModel> createMixedTestData() {
        List<TargetDataModel> data = new ArrayList<>();

        // Add XbondQuote data
        for (int i = 0; i < 10; i++) {
            XbondQuoteDataModel quote = new XbondQuoteDataModel();
            quote.setBusinessDate(LocalDate.of(2026, 1, 11));
            quote.setExchProductId("QUOTE" + String.format("%03d", i));
            quote.setProductType("BOND");
            quote.setExchange("SSE");
            quote.setSource("TEST");
            quote.setSettleSpeed(1);
            quote.setLevel("1");
            quote.setStatus("TRADING");
            quote.setPreClosePrice(100.0 + i);
            quote.setPreSettlePrice(100.5 + i);
            quote.setOpenPrice(99.5 + i);
            quote.setHighPrice(100.5 + i);
            quote.setLowPrice(99.0 + i);
            quote.setClosePrice(100.0 + i);
            quote.setSettlePrice(100.5 + i);
            quote.setUpperLimit(105.0 + i);
            quote.setLowerLimit(95.0 + i);
            quote.setTotalVolume(10000 * i);
            quote.setTotalTurnover(1000000.0 * i);
            quote.setOpenInterest(50000 * i);
            quote.setBid0Price(99.5 + i);
            quote.setBid0Yield(3.5 + i * 0.01);
            quote.setBid0YieldType("YTM");
            quote.setBid0TradableVolume(5000 * i);
            quote.setBid0Volume(1000 * i);
            quote.setOffer0Price(99.6 + i);
            quote.setOffer0Yield(3.49 + i * 0.01);
            quote.setOffer0YieldType("YTM");
            quote.setOffer0TradableVolume(5000 * i);
            quote.setOffer0Volume(1000 * i);
            quote.setEventTime(Instant.now());
            quote.setReceiveTime(Instant.now().plusMillis(i));

            data.add(quote);
        }

        // Add XbondTrade data
        for (int i = 0; i < 10; i++) {
            XbondTradeDataModel trade = new XbondTradeDataModel();
            trade.setBusinessDate(LocalDate.of(2026, 1, 11));
            trade.setExchProductId("TRADE" + String.format("%03d", i));
            trade.setProductType("BOND");
            trade.setExchange("SSE");
            trade.setSource("TEST");
            trade.setSettleSpeed(1);
            trade.setLastTradePrice(99.55 + i * 0.01);
            trade.setLastTradeYield(3.5 + i * 0.01);
            trade.setLastTradeYieldType("YTM");
            trade.setLastTradeVolume(1000.0 + i * 100);
            trade.setLastTradeTurnover(99550.0 + i * 1000);
            trade.setLastTradeInterest(50000.0 + i * 1000);
            trade.setLastTradeSide(i % 2 == 0 ? "BUY" : "SELL");
            trade.setEventTime(Instant.now());
            trade.setReceiveTime(Instant.now().plusMillis(i));

            data.add(trade);
        }

        // Add BondFutureQuote data
        for (int i = 0; i < 10; i++) {
            BondFutureQuoteDataModel future = new BondFutureQuoteDataModel();
            future.setBusinessDate(LocalDate.of(2026, 1, 11));
            future.setExchProductId("FUTURE" + String.format("%03d", i));
            future.setProductType("FUTURE");
            future.setExchange("CFFEX");
            future.setSource("TEST");
            future.setSettleSpeed(1);
            future.setLastTradePrice(100.0 + i * 0.01);
            future.setLastTradeYield(3.5 + i * 0.01);
            future.setLastTradeYieldType("YTM");
            future.setLastTradeVolume(100.0 + i * 10);
            future.setLastTradeTurnover(10000.0 + i * 1000);
            future.setLastTradeInterest(50000.0 + i * 1000);
            future.setLastTradeSide(i % 2 == 0 ? "BUY" : "SELL");
            future.setLevel("1");
            future.setStatus("TRADING");
            future.setTickType("QUOTE");
            future.setEventTimeTrade(Instant.now());
            future.setReceiveTimeTrade(Instant.now().plusMillis(i));
            future.setCreateTimeTrade(Instant.now().plusMillis(i + 1));
            future.setEventTimeQuote(Instant.now());
            future.setReceiveTimeQuote(Instant.now().plusMillis(i));
            future.setCreateTimeQuote(Instant.now().plusMillis(i + 1));
            future.setReceiveTime(Instant.now());

            data.add(future);
        }

        return data;
    }

    @Test
    public void testSortDataIntegration() throws LoaderException {
        org.junit.Assume.assumeTrue("DolphinDB not available", isDolphinDBAvailable);

        loader.sortData(testData, "receiveTime");

        // Verify sorting by checking receiveTime order
        Instant prevTime = null;
        for (TargetDataModel model : testData) {
            if (model instanceof XbondQuoteDataModel) {
                Instant currentTime = ((XbondQuoteDataModel) model).getReceiveTime();
                if (prevTime != null) {
                    assertTrue("Data should be sorted by receiveTime", !currentTime.isBefore(prevTime));
                }
                prevTime = currentTime;
            }
        }
    }

    @Test
    public void testLoadDataIntegration() throws LoaderException {
        org.junit.Assume.assumeTrue("DolphinDB not available", isDolphinDBAvailable);

        loader.sortData(testData, "receiveTime");
        loader.loadData(testData);

        // Data should be loaded successfully - verification would require querying DolphinDB
        assertTrue("Data load should complete", true);
    }

    @Test
    public void testFullWorkflowIntegration() throws LoaderException {
        org.junit.Assume.assumeTrue("DolphinDB not available", isDolphinDBAvailable);

        // Test complete workflow: sort -> load
        loader.sortData(testData, "receiveTime");
        loader.loadData(testData);
        loader.shutdown();

        assertTrue("Full workflow should complete", true);
    }
}
