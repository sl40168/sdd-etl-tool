package com.sdd.etl.subprocess;

import com.sdd.etl.ETLException;
import com.sdd.etl.config.ConfigurationLoader;
import com.sdd.etl.config.ETConfiguration;
import com.sdd.etl.context.ContextManager;
import com.sdd.etl.context.ETLContext;
import com.sdd.etl.loader.model.BondFutureQuoteDataModel;
import com.sdd.etl.loader.model.XbondQuoteDataModel;
import com.sdd.etl.loader.model.XbondTradeDataModel;
import com.sdd.etl.model.TargetDataModel;
import com.sdd.etl.util.DateUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Integration tests for LoadSubprocess with real DolphinDB connection.
 * 
 * <p>This test class connects to a real DolphinDB instance and loads data
 * from the specifications in docs/v2/Plan.md, docs/v4/Plan.md, and docs/v5/Plan.md.</p>
 * 
 * <p>Prerequisites:</p>
 * <ul>
 *   <li>DolphinDB server must be running at the configured host:port</li>
 *   <li>DolphinDB tables must be created (xbond_quote_stream_temp, xbond_trade_stream_temp, bond_future_quote_stream_temp)</li>
 *   <li>Configuration file (src/main/resources/default-config.ini) must have correct DolphinDB settings</li>
 * </ul>
 * 
 * <p>To run these tests:</p>
 * <pre>
 * mvn test -Dtest=LoadSubprocessRealDBIntegrationTest
 * </pre>
 */
@Ignore
public class LoadSubprocessRealDBIntegrationTest {

    private ETConfiguration config;
    private ETLContext context;
    private LoadSubprocess loadSubprocess;

    @Before
    public void setUp() throws ETLException {
        // Load configuration from default-config.ini (uses real DolphinDB settings)
        ConfigurationLoader configLoader = new ConfigurationLoader();
        config = configLoader.load("src/main/resources/default-config.ini");

        // Create context with business date from config
        String businessDateStr = "20260106";
        context = ContextManager.createContext(DateUtils.parseDate(businessDateStr), config);

        // Create LoadSubprocess instance
        loadSubprocess = new LoadSubprocess();
    }

    /**
     * Integration test: Load Xbond Quote data to real DolphinDB.
     * Uses sample data from docs/v2/Plan.md III.3.
     */
    @Test
    public void testLoadXbondQuoteToRealDB() throws ETLException {
        System.out.println("=== Integration Test: Loading Xbond Quote Data ===");

        // Given: Create XbondQuote target data model matching docs/v2/Plan.md line 66-67
        List<TargetDataModel> transformedData = new ArrayList<>();

        // Sample 1: 092018002.IB
        XbondQuoteDataModel quote1 = new XbondQuoteDataModel();
        quote1.setBusinessDate(LocalDate.of(2026, 1, 6));
        quote1.setExchProductId("092018002.IB");
        quote1.setProductType("BOND");
        quote1.setExchange("CFETS");
        quote1.setSource("XBOND");
        quote1.setSettleSpeed(1);  // T+1
        quote1.setLevel("L2");
        quote1.setStatus("Normal");

        // Level 1 (tradable)
        quote1.setBid1Price(102.1069);
        quote1.setBid1Yield(1.6645);
        quote1.setBid1YieldType("MATURITY");
        quote1.setBid1Volume(30000000);
        quote1.setOffer1Price(102.2136);
        quote1.setOffer1Yield(1.6047);
        quote1.setOffer1YieldType("MATURITY");
        quote1.setOffer1Volume(30000000);

        // Level 2-5
        quote1.setBid2Price(102.097);
        quote1.setBid2Yield(1.6701);
        quote1.setBid2YieldType("MATURITY");
        quote1.setBid2TradableVolume(30000000);
        quote1.setOffer2Price(102.2216);
        quote1.setOffer2Yield(1.6002);
        quote1.setOffer2YieldType("MATURITY");
        quote1.setOffer2TradableVolume(30000000);

        quote1.setBid3Price(102.069);
        quote1.setBid3Yield(1.6858);
        quote1.setBid3YieldType("MATURITY");
        quote1.setBid3TradableVolume(30000000);
        quote1.setOffer3Price(102.2236);
        quote1.setOffer3Yield(1.5991);
        quote1.setOffer3YieldType("MATURITY");
        quote1.setOffer3TradableVolume(30000000);

        quote1.setBid4Price(Double.NaN);
        quote1.setOffer4Price(102.2602);
        quote1.setOffer4Yield(1.5786);
        quote1.setOffer4YieldType("MATURITY");
        quote1.setOffer4TradableVolume(30000000);

        // Timestamps
        quote1.setReceiveTime(LocalDateTime.of(2026, 1, 6, 11, 1, 33, 507000000)
                .toInstant(ZoneOffset.UTC));
        quote1.setEventTime(LocalDateTime.of(2026, 1, 6, 11, 1, 33, 300000000)
                .toInstant(ZoneOffset.UTC));

        System.out.println("Created quote1: " + quote1.getExchProductId());
        transformedData.add(quote1);

        // Sample 2: 09240422.IB
        XbondQuoteDataModel quote2 = new XbondQuoteDataModel();
        quote2.setBusinessDate(LocalDate.of(2026, 1, 6));
        quote2.setExchProductId("09240422.IB");
        quote2.setProductType("BOND");
        quote2.setExchange("CFETS");
        quote2.setSource("XBOND");
        quote2.setSettleSpeed(1);
        quote2.setLevel("L2");
        quote2.setStatus("Normal");

        quote2.setBid1Price(100.1902);
        quote2.setBid1Yield(1.5444);
        quote2.setBid1YieldType("MATURITY");
        quote2.setBid1Volume(30000000);
        quote2.setOffer1Price(100.2182);
        quote2.setOffer1Yield(1.5172);
        quote2.setOffer1YieldType("MATURITY");
        quote2.setOffer1Volume(30000000);

        quote2.setBid2Price(100.1866);
        quote2.setBid2Yield(1.5479);
        quote2.setBid2YieldType("MATURITY");
        quote2.setBid2TradableVolume(50000000);
        quote2.setOffer2Price(100.2205);
        quote2.setOffer2Yield(1.515);
        quote2.setOffer2YieldType("MATURITY");
        quote2.setOffer2TradableVolume(30000000);

        quote2.setBid3Price(100.1844);
        quote2.setBid3Yield(1.55);
        quote2.setBid3YieldType("MATURITY");
        quote2.setBid3TradableVolume(30000000);
        quote2.setOffer3Price(100.2218);
        quote2.setOffer3Yield(1.5137);
        quote2.setOffer3YieldType("MATURITY");
        quote2.setOffer3TradableVolume(30000000);

        quote2.setBid4Price(100.1837);
        quote2.setBid4Yield(1.5507);
        quote2.setBid4YieldType("MATURITY");
        quote2.setBid4TradableVolume(30000000);
        quote2.setOffer4Price(100.2226);
        quote2.setOffer4Yield(1.5129);
        quote2.setOffer4YieldType("MATURITY");
        quote2.setOffer4TradableVolume(30000000);

        quote2.setBid5Price(100.1824);
        quote2.setBid5Yield(1.5519);
        quote2.setBid5YieldType("MATURITY");
        quote2.setBid5TradableVolume(30000000);
        quote2.setOffer5Price(100.2247);
        quote2.setOffer5Yield(1.5109);
        quote2.setOffer5YieldType("MATURITY");
        quote2.setOffer5TradableVolume(30000000);

        quote2.setReceiveTime(LocalDateTime.of(2026, 1, 6, 11, 1, 33, 507000000)
                .toInstant(ZoneOffset.UTC));
        quote2.setEventTime(LocalDateTime.of(2026, 1, 6, 11, 1, 33, 300000000)
                .toInstant(ZoneOffset.UTC));

        System.out.println("Created quote2: " + quote2.getExchProductId());
        transformedData.add(quote2);

        // Set transformed data in context
        context.setTransformedData(transformedData);
        context.setTransformedDataCount(transformedData.size());

        // When: Execute LoadSubprocess
        System.out.println("Loading " + transformedData.size() + " records to DolphinDB...");
        int loadedCount = loadSubprocess.execute(context);

        // Then: Verify loaded count
        System.out.println("Successfully loaded " + loadedCount + " records");
        assertEquals("Should load 2 records", 2, loadedCount);
        assertEquals("Context loaded count should match", 2, context.getLoadedDataCount());
        
        System.out.println("=== Test Passed: Xbond Quote Data ===\n");
    }

    /**
     * Integration test: Load Xbond Trade data to real DolphinDB.
     * Uses sample data from docs/v4/Plan.md I.6.
     */
    @Test
    public void testLoadXbondTradeToRealDB() throws ETLException {
        System.out.println("=== Integration Test: Loading Xbond Trade Data ===");

        // Given: Create XbondTrade target data model matching docs/v4/Plan.md line 42-44
        List<TargetDataModel> transformedData = new ArrayList<>();

        // Sample 1: 250210.IB
        XbondTradeDataModel trade1 = new XbondTradeDataModel();
        trade1.setBusinessDate(LocalDate.of(2026, 1, 6));
        trade1.setExchProductId("250210.IB");
        trade1.setProductType("BOND");
        trade1.setExchange("CFETS");
        trade1.setSource("XBOND");
        trade1.setSettleSpeed(1);
        trade1.setLastTradePrice(99.912);
        trade1.setLastTradeYield(1.8096);
        trade1.setLastTradeYieldType("MATURITY");
        trade1.setLastTradeVolume(10000000);
        trade1.setLastTradeTurnover(Double.NaN);
        trade1.setLastTradeInterest(Double.NaN);
        trade1.setLastTradeSide("TKN");
        trade1.setEventTime(LocalDateTime.of(2026, 1, 6, 11, 1, 34, 79000000)
                .toInstant(ZoneOffset.UTC));
        trade1.setReceiveTime(LocalDateTime.of(2026, 1, 6, 11, 1, 34, 246000000)
                .toInstant(ZoneOffset.UTC));

        System.out.println("Created trade1: " + trade1.getExchProductId());
        transformedData.add(trade1);

        // Sample 2: 230023.IB
        XbondTradeDataModel trade2 = new XbondTradeDataModel();
        trade2.setBusinessDate(LocalDate.of(2026, 1, 6));
        trade2.setExchProductId("230023.IB");
        trade2.setProductType("BOND");
        trade2.setExchange("CFETS");
        trade2.setSource("XBOND");
        trade2.setSettleSpeed(1);
        trade2.setLastTradePrice(122.8227);
        trade2.setLastTradeYield(1.945);
        trade2.setLastTradeYieldType("MATURITY");
        trade2.setLastTradeVolume(20000000);
        trade2.setLastTradeTurnover(Double.NaN);
        trade2.setLastTradeInterest(Double.NaN);
        trade2.setLastTradeSide("TKN");
        trade2.setEventTime(LocalDateTime.of(2026, 1, 6, 11, 1, 36, 85000000)
                .toInstant(ZoneOffset.UTC));
        trade2.setReceiveTime(LocalDateTime.of(2026, 1, 6, 11, 1, 36, 255000000)
                .toInstant(ZoneOffset.UTC));

        System.out.println("Created trade2: " + trade2.getExchProductId());
        transformedData.add(trade2);

        XbondTradeDataModel trade3 = new XbondTradeDataModel();
        trade3.setBusinessDate(LocalDate.of(2026, 1, 6));
        trade3.setExchProductId("230023.IB");
        trade3.setProductType("BOND");
        trade3.setExchange("CFETS");
        trade3.setSource("XBOND");
        trade3.setSettleSpeed(1);
        trade3.setLastTradePrice(122.8227);
        trade3.setLastTradeYield(1.945);
        trade3.setLastTradeYieldType("MATURITY");
        trade3.setLastTradeVolume(20000000);
        trade3.setLastTradeTurnover(Double.NaN);
        trade3.setLastTradeInterest(Double.NaN);
        trade3.setLastTradeSide("TKN");
        trade3.setEventTime(LocalDateTime.of(2026, 1, 6, 11, 1, 36, 85000000)
                .toInstant(ZoneOffset.UTC));
        trade3.setReceiveTime(LocalDateTime.of(2026, 1, 6, 11, 1, 36, 258000000)
                .toInstant(ZoneOffset.UTC));

        System.out.println("Created trade3: " + trade3.getExchProductId());
        transformedData.add(trade3);

        // Set transformed data in context
        context.setTransformedData(transformedData);
        context.setTransformedDataCount(transformedData.size());

        // When: Execute LoadSubprocess
        System.out.println("Loading " + transformedData.size() + " records to DolphinDB...");
        int loadedCount = loadSubprocess.execute(context);

        // Then: Verify loaded count
        System.out.println("Successfully loaded " + loadedCount + " records");
        assertEquals("Should load 3 records", 3, loadedCount);
        assertEquals("Context loaded count should match", 3, context.getLoadedDataCount());
        
        System.out.println("=== Test Passed: Xbond Trade Data ===\n");
    }

    /**
     * Integration test: Load Bond Future Quote data to real DolphinDB.
     * Uses sample data from docs/v5/Plan.md II.7.
     */
    @Test
    public void testLoadBondFutureQuoteToRealDB() throws ETLException {
        System.out.println("=== Integration Test: Loading Bond Future Quote Data ===");

        // Given: Create BondFutureQuote target data model matching docs/v5/Plan.md line 66-67
        List<TargetDataModel> transformedData = new ArrayList<>();

        // Sample 1: TS2512
        BondFutureQuoteDataModel future1 = new BondFutureQuoteDataModel();
        future1.setBusinessDate(LocalDate.of(2026, 1, 6));
        future1.setExchProductId("TS2512");
        future1.setProductType("BOND_FUT");
        future1.setExchange("CFFEX");
        future1.setSource("CFFEX");
        future1.setSettleSpeed(0);
        future1.setLastTradePrice(102.472);
        future1.setLastTradeYield(Double.NaN);
        future1.setLastTradeYieldType(null);
        future1.setLastTradeVolume(Double.NaN);
        future1.setLastTradeTurnover(Double.NaN);
        future1.setLastTradeInterest(Double.NaN);
        future1.setLastTradeSide(null);
        future1.setLevel("L1");
        future1.setStatus("Normal");
        future1.setPreClosePrice(102.468);
        future1.setPreSettlePrice(102.466);
        future1.setPreInterest(67365);
        future1.setOpenPrice(102.476);
        future1.setHighPrice(102.482);
        future1.setLowPrice(102.47);
        future1.setClosePrice(Double.NaN);
        future1.setSettlePrice(0);
        future1.setUpperLimit(102.978);
        future1.setLowerLimit(101.954);
        future1.setTotalVolume(9810);
        future1.setTotalTurnover(2.01059E10);
        future1.setOpenInterest(66148);

        future1.setBid1Price(102.472);
        future1.setBid1Yield(Double.NaN);
        future1.setBid1YieldType(null);
        future1.setBid1TradableVolume(144);
        future1.setOffer1Price(102.474);
        future1.setOffer1Yield(Double.NaN);
        future1.setOffer1YieldType(null);
        future1.setOffer1TradableVolume(125);

        // Levels 2-5 (all zero in sample)
        for (int i = 2; i <= 5; i++) {
            setBidOfferLevel(future1, i, 0, 0);
        }

        // Timestamps
        future1.setEventTimeTrade(LocalDateTime.of(2026, 1, 6, 11, 1, 36, 300000000)
                .toInstant(ZoneOffset.UTC));
        future1.setReceiveTimeTrade(LocalDateTime.of(2026, 1, 6, 11, 1, 36, 469000000)
                .toInstant(ZoneOffset.UTC));
        future1.setCreateTimeTrade(LocalDateTime.of(2026, 1, 6, 11, 1, 36, 785000000)
                .toInstant(ZoneOffset.UTC));
        future1.setEventTimeQuote(LocalDateTime.of(2026, 1, 6, 11, 1, 36, 300000000)
                .toInstant(ZoneOffset.UTC));
        future1.setReceiveTimeQuote(LocalDateTime.of(2026, 1, 6, 11, 1, 36, 469000000)
                .toInstant(ZoneOffset.UTC));
        future1.setCreateTimeQuote(LocalDateTime.of(2026, 1, 6, 11, 1, 36, 785000000)
                .toInstant(ZoneOffset.UTC));
        future1.setTickType("SNAPSHOT");
        future1.setReceiveTime(LocalDateTime.of(2026, 1, 6, 11, 1, 36, 469000000)
                .toInstant(ZoneOffset.UTC));

        System.out.println("Created future1: " + future1.getExchProductId());
        transformedData.add(future1);

        // Sample 2: TS2603
        BondFutureQuoteDataModel future2 = new BondFutureQuoteDataModel();
        future2.setBusinessDate(LocalDate.of(2026, 1, 6));
        future2.setExchProductId("TS2603");
        future2.setProductType("BOND_FUT");
        future2.setExchange("CFFEX");
        future2.setSource("CFFEX");
        future2.setSettleSpeed(0);
        future2.setLastTradePrice(102.424);
        future2.setLastTradeYield(Double.NaN);
        future2.setLastTradeYieldType(null);
        future2.setLastTradeVolume(Double.NaN);
        future2.setLastTradeTurnover(Double.NaN);
        future2.setLastTradeInterest(Double.NaN);
        future2.setLastTradeSide(null);
        future2.setLevel("L1");
        future2.setStatus("Normal");
        future2.setPreClosePrice(102.416);
        future2.setPreSettlePrice(102.416);
        future2.setPreInterest(16441);
        future2.setOpenPrice(102.43);
        future2.setHighPrice(102.434);
        future2.setLowPrice(102.418);
        future2.setClosePrice(Double.NaN);
        future2.setSettlePrice(0);
        future2.setUpperLimit(102.928);
        future2.setLowerLimit(101.904);
        future2.setTotalVolume(3035);
        future2.setTotalTurnover(6.21719E9);
        future2.setOpenInterest(16813);

        future2.setBid1Price(102.422);
        future2.setBid1TradableVolume(39);
        future2.setOffer1Price(102.424);
        future2.setOffer1TradableVolume(20);

        // Levels 2-5 (all zero in sample)
        for (int i = 2; i <= 5; i++) {
            setBidOfferLevel(future2, i, 0, 0);
        }

        future2.setEventTimeTrade(LocalDateTime.of(2026, 1, 6, 11, 1, 36, 300000000)
                .toInstant(ZoneOffset.UTC));
        future2.setReceiveTimeTrade(LocalDateTime.of(2026, 1, 6, 11, 1, 36, 469000000)
                .toInstant(ZoneOffset.UTC));
        future2.setCreateTimeTrade(LocalDateTime.of(2026, 1, 6, 11, 1, 36, 787000000)
                .toInstant(ZoneOffset.UTC));
        future2.setEventTimeQuote(LocalDateTime.of(2026, 1, 6, 11, 1, 36, 300000000)
                .toInstant(ZoneOffset.UTC));
        future2.setReceiveTimeQuote(LocalDateTime.of(2026, 1, 6, 11, 1, 36, 469000000)
                .toInstant(ZoneOffset.UTC));
        future2.setCreateTimeQuote(LocalDateTime.of(2026, 1, 6, 11, 1, 36, 787000000)
                .toInstant(ZoneOffset.UTC));
        future2.setTickType("SNAPSHOT");
        future2.setReceiveTime(LocalDateTime.of(2026, 1, 6, 11, 1, 36, 469000000)
                .toInstant(ZoneOffset.UTC));

        System.out.println("Created future2: " + future2.getExchProductId());
        transformedData.add(future2);

        // Set transformed data in context
        context.setTransformedData(transformedData);
        context.setTransformedDataCount(transformedData.size());

        // When: Execute LoadSubprocess
        System.out.println("Loading " + transformedData.size() + " records to DolphinDB...");
        int loadedCount = loadSubprocess.execute(context);

        // Then: Verify loaded count
        System.out.println("Successfully loaded " + loadedCount + " records");
        assertEquals("Should load 2 records", 2, loadedCount);
        assertEquals("Context loaded count should match", 2, context.getLoadedDataCount());
        
        System.out.println("=== Test Passed: Bond Future Quote Data ===\n");
    }

    /**
     * Integration test: Load mixed data types to real DolphinDB.
     * Tests loading all three data types together.
     */
    @Test
    public void testLoadMixedDataTypesToRealDB() throws ETLException {
        System.out.println("=== Integration Test: Loading Mixed Data Types ===");

        // Given: Mix of Xbond Quote, Xbond Trade, and Bond Future Quote data
        List<TargetDataModel> transformedData = new ArrayList<>();

        // Add Xbond Quote
        XbondQuoteDataModel quote = new XbondQuoteDataModel();
        quote.setBusinessDate(LocalDate.of(2026, 1, 6));
        quote.setExchProductId("092018002.IB");
        quote.setProductType("BOND");
        quote.setExchange("CFETS");
        quote.setSource("XBOND");
        quote.setSettleSpeed(1);
        quote.setLevel("L2");
        quote.setStatus("Normal");
        quote.setBid1Price(102.1069);
        quote.setOffer1Price(102.2136);
        quote.setReceiveTime(LocalDateTime.of(2026, 1, 6, 11, 1, 33, 507000000)
                .toInstant(ZoneOffset.UTC));

        System.out.println("Created quote: " + quote.getExchProductId());
        transformedData.add(quote);

        // Add Xbond Trade
        XbondTradeDataModel trade = new XbondTradeDataModel();
        trade.setBusinessDate(LocalDate.of(2026, 1, 6));
        trade.setExchProductId("250210.IB");
        trade.setProductType("BOND");
        trade.setExchange("CFETS");
        trade.setSource("XBOND");
        trade.setSettleSpeed(1);
        trade.setLastTradePrice(99.912);
        trade.setLastTradeVolume(10000000);
        trade.setLastTradeSide("TKN");
        trade.setReceiveTime(LocalDateTime.of(2026, 1, 6, 11, 1, 34, 246000000)
                .toInstant(ZoneOffset.UTC));

        System.out.println("Created trade: " + trade.getExchProductId());
        transformedData.add(trade);

        // Add Bond Future Quote
        BondFutureQuoteDataModel future = new BondFutureQuoteDataModel();
        future.setBusinessDate(LocalDate.of(2026, 1, 6));
        future.setExchProductId("TS2512");
        future.setProductType("BOND_FUT");
        future.setExchange("CFFEX");
        future.setSource("CFFEX");
        future.setSettleSpeed(0);
        future.setLastTradePrice(102.472);
        future.setLevel("L1");
        future.setStatus("Normal");
        future.setReceiveTime(LocalDateTime.of(2026, 1, 6, 11, 1, 36, 469000000)
                .toInstant(ZoneOffset.UTC));

        System.out.println("Created future: " + future.getExchProductId());
        transformedData.add(future);

        // Set transformed data in context
        context.setTransformedData(transformedData);
        context.setTransformedDataCount(transformedData.size());

        // When: Execute LoadSubprocess
        System.out.println("Loading " + transformedData.size() + " records to DolphinDB...");
        int loadedCount = loadSubprocess.execute(context);

        // Then: Verify loaded count
        System.out.println("Successfully loaded " + loadedCount + " records");
        assertEquals("Should load 3 records", 3, loadedCount);
        assertEquals("Context loaded count should match", 3, context.getLoadedDataCount());
        
        System.out.println("=== Test Passed: Mixed Data Types ===\n");
    }

    /**
     * Helper method to set bid/offer levels with zero values.
     */
    private void setBidOfferLevel(BondFutureQuoteDataModel model, int level,
                                 double bidPrice, double offerPrice) {
        switch (level) {
            case 2:
                model.setBid2Price(bidPrice);
                model.setBid2Yield(Double.NaN);
                model.setBid2YieldType(null);
                model.setBid2TradableVolume(0);
                model.setOffer2Price(offerPrice);
                model.setOffer2Yield(Double.NaN);
                model.setOffer2YieldType(null);
                model.setOffer2TradableVolume(0);
                break;
            case 3:
                model.setBid3Price(bidPrice);
                model.setBid3Yield(Double.NaN);
                model.setBid3YieldType(null);
                model.setBid3TradableVolume(0);
                model.setOffer3Price(offerPrice);
                model.setOffer3Yield(Double.NaN);
                model.setOffer3YieldType(null);
                model.setOffer3TradableVolume(0);
                break;
            case 4:
                model.setBid4Price(bidPrice);
                model.setBid4Yield(Double.NaN);
                model.setBid4YieldType(null);
                model.setBid4TradableVolume(0);
                model.setOffer4Price(offerPrice);
                model.setOffer4Yield(Double.NaN);
                model.setOffer4YieldType(null);
                model.setOffer4TradableVolume(0);
                break;
            case 5:
                model.setBid5Price(bidPrice);
                model.setBid5Yield(Double.NaN);
                model.setBid5YieldType(null);
                model.setBid5TradableVolume(0);
                model.setOffer5Price(offerPrice);
                model.setOffer5Yield(Double.NaN);
                model.setOffer5YieldType(null);
                model.setOffer5TradableVolume(0);
                break;
        }
    }
}
