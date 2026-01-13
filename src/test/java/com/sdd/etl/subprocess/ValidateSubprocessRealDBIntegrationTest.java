package com.sdd.etl.subprocess;

import com.sdd.etl.ETLException;
import com.sdd.etl.config.ConfigurationLoader;
import com.sdd.etl.config.ETConfiguration;
import com.sdd.etl.context.ContextManager;
import com.sdd.etl.context.ETLContext;
import com.sdd.etl.loader.config.LoaderConfiguration;
import com.sdd.etl.loader.model.BondFutureQuoteDataModel;
import com.sdd.etl.loader.model.XbondQuoteDataModel;
import com.sdd.etl.loader.model.XbondTradeDataModel;
import com.sdd.etl.loader.dolphin.DolphinDBConnection;
import com.sdd.etl.loader.dolphin.DolphinDBLoader;
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
 * Integration tests for ValidateSubprocess with real DolphinDB connection.
 *
 * <p>This test class connects to a real DolphinDB instance and validates loaded data.</p>
 *
 * <p>Prerequisites:</p>
 * <ul>
 *   <li>DolphinDB server must be running at the configured host:port</li>
 *   <li>DolphinDB tables must be created (xbond_quote_stream_temp, xbond_trade_stream_temp, bond_future_quote_stream_temp)</li>
 *   <li>Configuration file (src/main/resources/default-config.ini) must have correct DolphinDB settings</li>
 *   <li>Data must be loaded first before running validation test</li>
 * </ul>
 *
 * <p>To run these tests:</p>
 * <pre>
 * mvn test -Dtest=ValidateSubprocessRealDBIntegrationTest
 * </pre>
 */
@Ignore
public class ValidateSubprocessRealDBIntegrationTest {

    private ETConfiguration config;
    private ETLContext context;
    private ValidateSubprocess validateSubprocess;
    private LoadSubprocess loadSubprocess;

    @Before
    public void setUp() throws ETLException {
        // Load configuration from default-config.ini (uses real DolphinDB settings)
        ConfigurationLoader configLoader = new ConfigurationLoader();
        config = configLoader.load("src/main/resources/default-config.ini");

        // Create context with business date from config
        String businessDateStr = "20260106";
        context = ContextManager.createContext(DateUtils.parseDate(businessDateStr), config);

        // Create subprocess instances
        loadSubprocess = new LoadSubprocess();
        validateSubprocess = new ValidateSubprocess();
    }

    /**
     * Integration test: Validate Xbond Quote data in real DolphinDB.
     * Loads data first, then validates it.
     */
    @Test
    public void testValidateXbondQuoteWithRealDB() throws ETLException {
        System.out.println("=== Integration Test: Validating Xbond Quote Data ===");

        // Given: Create and load XbondQuote data
        List<TargetDataModel> transformedData = createXbondQuoteTestData();
        context.setTransformedData(transformedData);
        context.setTransformedDataCount(transformedData.size());

        // When: Load data to DolphinDB
        System.out.println("Loading " + transformedData.size() + " records to DolphinDB...");
        int loadedCount = loadSubprocess.execute(context);
        System.out.println("Loaded " + loadedCount + " records");

        // Then: Validate loaded data
        System.out.println("Validating loaded data...");
        int validatedCount = validateSubprocess.execute(context);

        // Then: Verify validation passed
        assertTrue("Validation should pass", context.isValidationPassed());
        assertEquals("Validated count should match transformed count",
                     transformedData.size(), validatedCount);
        assertTrue("Validation errors should be empty", context.getValidationErrors().isEmpty());

        System.out.println("=== Test Passed: Xbond Quote Validation ===\n");
    }

    /**
     * Integration test: Validate Xbond Trade data in real DolphinDB.
     * Loads data first, then validates it.
     */
    @Test
    public void testValidateXbondTradeWithRealDB() throws ETLException {
        System.out.println("=== Integration Test: Validating Xbond Trade Data ===");

        // Given: Create and load XbondTrade data
        List<TargetDataModel> transformedData = createXbondTradeTestData();
        context.setTransformedData(transformedData);
        context.setTransformedDataCount(transformedData.size());

        // When: Load data to DolphinDB
        System.out.println("Loading " + transformedData.size() + " records to DolphinDB...");
        int loadedCount = loadSubprocess.execute(context);
        System.out.println("Loaded " + loadedCount + " records");

        // Then: Validate loaded data
        System.out.println("Validating loaded data...");
        int validatedCount = validateSubprocess.execute(context);

        // Then: Verify validation passed
        assertTrue("Validation should pass", context.isValidationPassed());
        assertEquals("Validated count should match transformed count",
                     transformedData.size(), validatedCount);
        assertTrue("Validation errors should be empty", context.getValidationErrors().isEmpty());

        System.out.println("=== Test Passed: Xbond Trade Validation ===\n");
    }

    /**
     * Integration test: Validate Bond Future Quote data in real DolphinDB.
     * Loads data first, then validates it.
     */
    @Test
    public void testValidateBondFutureQuoteWithRealDB() throws ETLException {
        System.out.println("=== Integration Test: Validating Bond Future Quote Data ===");

        // Given: Create and load BondFutureQuote data
        List<TargetDataModel> transformedData = createBondFutureQuoteTestData();
        context.setTransformedData(transformedData);
        context.setTransformedDataCount(transformedData.size());

        // When: Load data to DolphinDB
        System.out.println("Loading " + transformedData.size() + " records to DolphinDB...");
        int loadedCount = loadSubprocess.execute(context);
        System.out.println("Loaded " + loadedCount + " records");

        // Then: Validate loaded data
        System.out.println("Validating loaded data...");
        int validatedCount = validateSubprocess.execute(context);

        // Then: Verify validation passed
        assertTrue("Validation should pass", context.isValidationPassed());
        assertEquals("Validated count should match transformed count",
                     transformedData.size(), validatedCount);
        assertTrue("Validation errors should be empty", context.getValidationErrors().isEmpty());

        System.out.println("=== Test Passed: Bond Future Quote Validation ===\n");
    }

    /**
     * Integration test: Validate mixed data types in real DolphinDB.
     * Loads data first, then validates it.
     */
    @Test
    public void testValidateMixedDataWithRealDB() throws ETLException {
        System.out.println("=== Integration Test: Validating Mixed Data Types ===");

        // Given: Create and load mixed data
        List<TargetDataModel> transformedData = new ArrayList<>();
        transformedData.addAll(createXbondQuoteTestData());
        transformedData.addAll(createXbondTradeTestData());
        transformedData.addAll(createBondFutureQuoteTestData());

        context.setTransformedData(transformedData);
        context.setTransformedDataCount(transformedData.size());

        // When: Load data to DolphinDB
        System.out.println("Loading " + transformedData.size() + " records to DolphinDB...");
        int loadedCount = loadSubprocess.execute(context);
        System.out.println("Loaded " + loadedCount + " records");

        // Then: Validate loaded data
        System.out.println("Validating loaded data...");
        int validatedCount = validateSubprocess.execute(context);

        // Then: Verify validation passed
        assertTrue("Validation should pass", context.isValidationPassed());
        assertEquals("Validated count should match transformed count",
                     transformedData.size(), validatedCount);
        assertTrue("Validation errors should be empty", context.getValidationErrors().isEmpty());

        System.out.println("=== Test Passed: Mixed Data Validation ===\n");
    }

    /**
     * Integration test: Test validation failure scenario.
     * Loads fewer records than reported, should cause validation failure.
     */
    @Test(expected = ETLException.class)
    public void testValidateFailureWithRealDB() throws ETLException {
        System.out.println("=== Integration Test: Validation Failure Scenario ===");

        // Given: Create data but report higher count than actual
        List<TargetDataModel> transformedData = createXbondQuoteTestData();
        context.setTransformedData(transformedData);

        // Report more than actual loaded (simulate mismatch)
        context.setTransformedDataCount(1000); // Much higher than actual

        // When: Load only 1 record
        context.setTransformedData(transformedData.subList(0, 1));
        loadSubprocess.execute(context);

        // Then: Validate should fail
        System.out.println("Validating (expecting failure)...");
        try {
            validateSubprocess.execute(context);
            fail("Validation should have thrown ETLException");
        } catch (ETLException e) {
            System.out.println("Validation failed as expected: " + e.getMessage());
            assertTrue("Error message should mention mismatch",
                     e.getMessage().contains("mismatch"));
            throw e;
        }
    }

    /**
     * Creates test data for XbondQuote.
     */
    private List<TargetDataModel> createXbondQuoteTestData() {
        List<TargetDataModel> data = new ArrayList<>();

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
        data.add(quote);

        return data;
    }

    /**
     * Creates test data for XbondTrade.
     */
    private List<TargetDataModel> createXbondTradeTestData() {
        List<TargetDataModel> data = new ArrayList<>();

        XbondTradeDataModel trade = new XbondTradeDataModel();
        trade.setBusinessDate(LocalDate.of(2026, 1, 6));
        trade.setExchProductId("250210.IB");
        trade.setProductType("BOND");
        trade.setExchange("CFETS");
        trade.setSource("XBOND");
        trade.setSettleSpeed(1);
        trade.setLastTradePrice(99.912);
        trade.setLastTradeYield(1.8096);
        trade.setLastTradeYieldType("MATURITY");
        trade.setLastTradeVolume(10000000);
        trade.setLastTradeSide("TKN");
        trade.setEventTime(LocalDateTime.of(2026, 1, 6, 11, 1, 34, 79000000)
                .toInstant(ZoneOffset.UTC));
        trade.setReceiveTime(LocalDateTime.of(2026, 1, 6, 11, 1, 34, 246000000)
                .toInstant(ZoneOffset.UTC));

        System.out.println("Created trade: " + trade.getExchProductId());
        data.add(trade);

        return data;
    }

    /**
     * Creates test data for BondFutureQuote.
     */
    private List<TargetDataModel> createBondFutureQuoteTestData() {
        List<TargetDataModel> data = new ArrayList<>();

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
        data.add(future);

        return data;
    }
}
