package com.sdd.etl.source.extract.cos;

import com.sdd.etl.ETLException;
import com.sdd.etl.config.ETConfiguration;
import com.sdd.etl.context.ETLContext;
import com.sdd.etl.model.SourceDataModel;
import com.sdd.etl.model.XbondTradeDataModel;
import com.sdd.etl.source.extract.cos.config.CosSourceConfig;
import com.sdd.etl.source.extract.cos.model.RawTradeRecord;
import com.sdd.etl.source.extract.cos.model.CosFileMetadata;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import java.io.File;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for XbondTradeExtractor.
 * 
 * <p>
 * Tests trade-specific extraction logic including CSV parsing delegation
 * and conversion of raw trade records to standardized data models.
 * </p>
 */
public class XbondTradeExtractorTest {

    /** System under test */
    private XbondTradeExtractor extractor;

    /** Mock ETL context */
    private ETLContext mockContext;

    /** Mock COS source configuration */
    private CosSourceConfig mockConfig;

    /** Mock COS client */
    private CosClient mockCosClient;

    /** Mock trade CSV parser */
    private TradeCsvParser mockTradeCsvParser;

    /** Mock ET configuration */
    private ETConfiguration mockEtConfig;

    @Before
    public void setUp() {
        // Create mocks
        mockContext = mock(ETLContext.class);
        mockConfig = mock(CosSourceConfig.class);
        mockCosClient = mock(CosClient.class);
        mockTradeCsvParser = mock(TradeCsvParser.class);
        mockEtConfig = mock(ETConfiguration.class);

        // Setup default mock behavior
        when(mockContext.getConfig()).thenReturn(mockEtConfig);
        when(mockContext.getCurrentDate()).thenReturn(LocalDate.of(2025, 1, 1));

        // Setup sources list with our mock CosSourceConfig
        List<ETConfiguration.SourceConfig> sources = new ArrayList<>();
        sources.add(mockConfig);
        when(mockEtConfig.getSources()).thenReturn(sources);

        // Configure mockConfig to be recognized as COS type
        when(mockConfig.getType()).thenReturn("cos");
        when(mockConfig.isValid()).thenReturn(true);
        when(mockConfig.getMaxFileSizeOrDefault()).thenReturn(100 * 1024 * 1024L); // 100MB

        // Create extractor and inject mock parser via reflection
        extractor = new XbondTradeExtractor();
        injectMockParser(extractor, mockTradeCsvParser);
    }

    /**
     * Injects mock TradeCsvParser into extractor via reflection.
     */
    private void injectMockParser(XbondTradeExtractor extractor, TradeCsvParser mockParser) {
        try {
            java.lang.reflect.Field parserField = XbondTradeExtractor.class.getDeclaredField("tradeCsvParser");
            parserField.setAccessible(true);
            parserField.set(extractor, mockParser);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject mock parser", e);
        }
    }

    @Test
    public void testGetCategory_ReturnsXbondCfetsDeal() {
        assertEquals("Category should be 'XbondCfetsDeal'", "XbondCfetsDeal", extractor.getCategory());
    }

    @Test
    public void testCreateCosClient_ReturnsCosClientImpl() throws ETLException {
        // This is a simple factory method; just verify it returns a non-null CosClient
        CosClient client = extractor.createCosClient(mockConfig);
        assertNotNull("Created CosClient should not be null", client);
        // It should be an instance of CosClientImpl (implementation detail)
        // We can trust it returns a working client; integration tests verify actual
        // behavior
    }

    @Test
    public void testParseCsvFile_DelegatesToTradeCsvParser() throws ETLException {
        // Setup
        File csvFile = new File("test.csv");
        List<RawTradeRecord> expectedRecords = new ArrayList<>();
        RawTradeRecord mockRecord = mock(RawTradeRecord.class);
        expectedRecords.add(mockRecord);

        when(mockTradeCsvParser.parse(csvFile)).thenReturn(expectedRecords);

        // Execute
        List<RawTradeRecord> result = extractor.parseCsvFile(csvFile);

        // Verify
        assertSame("Should return records from TradeCsvParser", expectedRecords, result);
        verify(mockTradeCsvParser).parse(csvFile);
    }

    @Test(expected = ETLException.class)
    public void testParseCsvFile_PropagatesETLExceptionFromParser() throws ETLException {
        // Setup
        File csvFile = new File("test.csv");
        when(mockTradeCsvParser.parse(csvFile))
                .thenThrow(new ETLException("CSV_PARSE", csvFile.getName(), "Parser error"));

        // Execute & Verify exception propagation
        extractor.parseCsvFile(csvFile);
    }

    @Test
    public void testConvertRawRecords_EmptyInput_ReturnsEmptyList() throws ETLException {
        // Execute
        List<SourceDataModel> result = extractor.convertRawRecords(new ArrayList<>());

        // Verify
        assertNotNull("Result should not be null", result);
        assertTrue("Result should be empty", result.isEmpty());
    }

    @Test
    public void testConvertRawRecords_NullInput_ReturnsEmptyList() throws ETLException {
        // Execute
        List<SourceDataModel> result = extractor.convertRawRecords(null);

        // Verify
        assertNotNull("Result should not be null", result);
        assertTrue("Result should be empty", result.isEmpty());
    }

    @Test
    public void testConvertRawRecords_SingleValidRecord_CreatesModel() throws ETLException {
        // Setup
        List<RawTradeRecord> rawRecords = new ArrayList<>();
        RawTradeRecord record = new RawTradeRecord();
        record.setId(12345L);
        record.setUnderlyingSecurityId("1021001");
        record.setSetDays("T+1");
        record.setNetPrice(100.5);
        record.setYield(2.5);
        record.setYieldType("YTM");
        record.setDealSize(1000L);
        record.setSide("C001");
        record.setDealTime(LocalDateTime.of(2025, 1, 1, 10, 30, 0));
        record.setRecvTime(LocalDateTime.of(2025, 1, 1, 10, 30, 5));
        rawRecords.add(record);

        // Set current business date via reflection (normally set by extract method)
        setCurrentBusinessDate("2025.01.01");

        // Execute
        List<SourceDataModel> result = extractor.convertRawRecords(rawRecords);

        // Verify
        assertEquals("Should create one model", 1, result.size());
        assertTrue("Result should contain XbondTradeDataModel", result.get(0) instanceof XbondTradeDataModel);

        XbondTradeDataModel model = (XbondTradeDataModel) result.get(0);
        assertEquals("exchProductId should have .IB suffix", "1021001.IB", model.getExchProductId());
        assertEquals("settleSpeed should match settlement type", 1, model.getSettleSpeed());
        assertEquals("tradePrice should match", 100.5, model.getTradePrice(), 0.00001);
        assertEquals("tradeYield should match", 2.5, model.getTradeYield(), 0.00001);
        assertEquals("tradeYieldType should match", "YTM", model.getTradeYieldType());
        assertEquals("tradeVolume should match", 1000, model.getTradeVolume(), 0.1);
        assertEquals("trade side should match", "C001", model.getTradeSide());
        assertEquals("tradeId should match", "12345", model.getTradeId());
        assertEquals("eventTime should match dealTime", LocalDateTime.of(2025, 1, 1, 10, 30, 0),
                model.getEventTime());
        assertEquals("receiveTime should match recvTime", LocalDateTime.of(2025, 1, 1, 10, 30, 5),
                model.getReceiveTime());
        assertEquals("businessDate should be set", "2025.01.01", model.getBusinessDate());

        // Model should be valid
        assertTrue("Created model should be valid", model.validate());
    }

    @Test
    public void testConvertRawRecords_MultipleRecords_CreatesMultipleModels() throws ETLException {
        // Setup: two records (one-to-one mapping, no grouping)
        List<RawTradeRecord> rawRecords = new ArrayList<>();

        RawTradeRecord record1 = new RawTradeRecord();
        record1.setId(1L);
        record1.setUnderlyingSecurityId("1021001");
        record1.setSetDays("T+1");
        record1.setNetPrice(100.5);
        record1.setYield(2.5);
        record1.setYieldType("YTM");
        record1.setDealSize(1000L);
        record1.setSide("X");
        record1.setDealTime(LocalDateTime.of(2025, 1, 1, 10, 30, 0));
        record1.setRecvTime(LocalDateTime.of(2025, 1, 1, 10, 30, 5));

        RawTradeRecord record2 = new RawTradeRecord();
        record2.setId(2L);
        record2.setUnderlyingSecurityId("1021001");
        record2.setSetDays("T+1");
        record2.setNetPrice(100.6);
        record2.setYield(2.6);
        record2.setYieldType("YTM");
        record2.setDealSize(2000L);
        record2.setSide("Y");
        record2.setDealTime(LocalDateTime.of(2025, 1, 1, 10, 31, 0));
        record2.setRecvTime(LocalDateTime.of(2025, 1, 1, 10, 31, 5));

        rawRecords.add(record1);
        rawRecords.add(record2);

        setCurrentBusinessDate("2025.01.01");

        // Execute
        List<SourceDataModel> result = extractor.convertRawRecords(rawRecords);

        // Verify: two models created (one-to-one mapping)
        assertEquals("Should create two models (one-to-one mapping)", 2, result.size());

        // First model
        XbondTradeDataModel model1 = (XbondTradeDataModel) result.get(0);
        assertEquals("First model exchProductId", "1021001.IB", model1.getExchProductId());
        assertEquals("First model tradePrice", 100.5, model1.getTradePrice(), 0.00001);
        assertEquals("First model tradeSide should be mapped", "TKN", model1.getTradeSide());
        assertEquals("First model tradeId", "1", model1.getTradeId());

        // Second model
        XbondTradeDataModel model2 = (XbondTradeDataModel) result.get(1);
        assertEquals("Second model exchProductId", "1021001.IB", model2.getExchProductId());
        assertEquals("Second model tradePrice", 100.6, model2.getTradePrice(), 0.00001);
        assertEquals("Second model tradeSide should be mapped", "GVN", model2.getTradeSide());
        assertEquals("Second model tradeId", "2", model2.getTradeId());

        assertTrue("First model should be valid", model1.validate());
        assertTrue("Second model should be valid", model2.validate());
    }

    @Test
    public void testConvertRawRecords_MultipleRecordsDifferentMqOffset_CreatesMultipleModels() throws ETLException {
        // Setup: two records with different values
        List<RawTradeRecord> rawRecords = new ArrayList<>();

        RawTradeRecord record1 = new RawTradeRecord();
        record1.setId(1L);
        record1.setUnderlyingSecurityId("1021001");
        record1.setSetDays("T+1");
        record1.setNetPrice(100.5);
        record1.setYield(2.5);
        record1.setYieldType("YTM");
        record1.setDealSize(1000L);
        record1.setSide("C001");
        record1.setDealTime(LocalDateTime.of(2025, 1, 1, 10, 30, 0));
        record1.setRecvTime(LocalDateTime.of(2025, 1, 1, 10, 30, 5));

        RawTradeRecord record2 = new RawTradeRecord();
        record2.setId(2L);
        record2.setUnderlyingSecurityId("1021002");
        record2.setSetDays("T+0");
        record2.setNetPrice(101.0);
        record2.setYield(3.0);
        record2.setYieldType("YTC");
        record2.setDealSize(1500L);
        record2.setSide("C003");
        record2.setDealTime(LocalDateTime.of(2025, 1, 1, 10, 35, 0));
        record2.setRecvTime(LocalDateTime.of(2025, 1, 1, 10, 35, 5));

        rawRecords.add(record1);
        rawRecords.add(record2);

        setCurrentBusinessDate("2025.01.01");

        // Execute
        List<SourceDataModel> result = extractor.convertRawRecords(rawRecords);

        // Verify: two models created
        assertEquals("Should create two models for different mqOffset", 2, result.size());

        // First model
        XbondTradeDataModel model1 = (XbondTradeDataModel) result.get(0);
        assertEquals("First model exchProductId", "1021001.IB", model1.getExchProductId());
        assertEquals("First model settleSpeed", 1, model1.getSettleSpeed());
        assertEquals("First model tradePrice", 100.5, model1.getTradePrice(), 0.00001);
        assertEquals("First model tradeId", "1", model1.getTradeId());

        // Second model
        XbondTradeDataModel model2 = (XbondTradeDataModel) result.get(1);
        assertEquals("Second model exchProductId", "1021002.IB", model2.getExchProductId());
        assertEquals("Second model settleSpeed", 0, model2.getSettleSpeed());
        assertEquals("Second model tradePrice", 101.0, model2.getTradePrice(), 0.00001);
        assertEquals("Second model tradeId", "2", model2.getTradeId());

        // Both should be valid
        assertTrue("First model should be valid", model1.validate());
        assertTrue("Second model should be valid", model2.validate());
    }

    @Test
    public void testConvertRawRecords_InvalidRecord_SkipsInvalidModel() throws ETLException {
        // Setup: one valid record and one invalid record
        List<RawTradeRecord> rawRecords = new ArrayList<>();

        // Valid record
        RawTradeRecord validRecord = new RawTradeRecord();
        validRecord.setId(1L);
        validRecord.setUnderlyingSecurityId("1021001");
        validRecord.setSetDays("T+1");
        validRecord.setNetPrice(100.5);
        validRecord.setYield(2.5);
        validRecord.setYieldType("YTM");
        validRecord.setDealSize(1000L);
        validRecord.setSide("X");
        validRecord.setDealTime(LocalDateTime.of(2025, 1, 1, 10, 30, 0));
        validRecord.setRecvTime(LocalDateTime.of(2025, 1, 1, 10, 30, 5));

        // Invalid record (missing required dealTime)
        RawTradeRecord invalidRecord = new RawTradeRecord();
        invalidRecord.setId(2L);
        invalidRecord.setUnderlyingSecurityId("1021001");
        invalidRecord.setSetDays("T+1");
        invalidRecord.setNetPrice(101.0);
        invalidRecord.setYield(3.0);
        invalidRecord.setYieldType("YTC");
        invalidRecord.setDealSize(1500L);
        invalidRecord.setSide(null);
        // Missing dealTime
        invalidRecord.setRecvTime(LocalDateTime.of(2025, 1, 1, 10, 35, 5));

        rawRecords.add(validRecord);
        rawRecords.add(invalidRecord);

        setCurrentBusinessDate("2025.01.01");

        // Execute
        List<SourceDataModel> result = extractor.convertRawRecords(rawRecords);

        // Verify: only valid model is created, invalid one is skipped
        assertEquals("Should create one model (invalid skipped)", 1, result.size());

        XbondTradeDataModel model = (XbondTradeDataModel) result.get(0);
        assertEquals("Should be the valid record", "1", model.getTradeId());
    }

    @Test
    public void testConvertRawRecords_AllRecordsProcessed() throws ETLException {
        // Setup: records are processed
        List<RawTradeRecord> rawRecords = new ArrayList<>();

        RawTradeRecord record1 = new RawTradeRecord();
        record1.setId(1L);
        record1.setUnderlyingSecurityId("1021001");
        record1.setSetDays("T+1");
        record1.setNetPrice(100.5);
        record1.setYield(2.5);
        record1.setYieldType("YTM");
        record1.setDealSize(1000L);
        record1.setSide("X");
        record1.setDealTime(LocalDateTime.of(2025, 1, 1, 10, 30, 0));
        record1.setRecvTime(LocalDateTime.of(2025, 1, 1, 10, 30, 5));

        RawTradeRecord record2 = new RawTradeRecord();
        record2.setId(2L);
        record2.setUnderlyingSecurityId("1021002");
        record2.setSetDays("T+1");
        record2.setNetPrice(101.0);
        record2.setYield(2.6);
        record2.setYieldType("YTM");
        record2.setDealSize(1500L);
        record2.setSide("Y");
        record2.setDealTime(LocalDateTime.of(2025, 1, 1, 10, 31, 0));
        record2.setRecvTime(LocalDateTime.of(2025, 1, 1, 10, 31, 5));

        rawRecords.add(record1);
        rawRecords.add(record2);

        setCurrentBusinessDate("2025.01.01");

        // Execute
        List<SourceDataModel> result = extractor.convertRawRecords(rawRecords);

        // Verify: both records are processed (one-to-one mapping)
        assertEquals("All records should be processed", 2, result.size());
    }

    @Test
    public void testExtract_SetsCurrentBusinessDate() throws ETLException {
        // Setup
        when(mockContext.getCurrentDate()).thenReturn(LocalDate.of(2025, 1, 1));
        when(mockContext.getConfig()).thenReturn(mockEtConfig);

        // Create spy and inject mock CosClient
        XbondTradeExtractor spyExtractor = spy(extractor);
        doReturn(mockCosClient).when(spyExtractor).createCosClient(any());

        // Mock file selection and downloading
        List<CosFileMetadata> mockFiles = new ArrayList<>();
        CosFileMetadata mockFile = mock(CosFileMetadata.class);
        when(mockFile.getKey()).thenReturn("XbondCfetsDeal/2025-01-01/file1.csv");
        when(mockFile.getSize()).thenReturn(1024L);
        when(mockFile.getLastModified()).thenReturn(Instant.now());
        mockFiles.add(mockFile);

        when(mockCosClient.listObjects(any(CosSourceConfig.class), anyString())).thenReturn(mockFiles);

        File mockDownloadedFile = new File("downloaded.csv");
        // Stub downloadFiles to avoid actual download
        doReturn(Arrays.asList(mockDownloadedFile)).when(spyExtractor).downloadFiles(mockFiles);

        // Mock parsing and conversion
        List<RawTradeRecord> mockRawRecords = new ArrayList<>();
        List<SourceDataModel> mockConvertedRecords = new ArrayList<>();
        when(mockTradeCsvParser.parse(mockDownloadedFile)).thenReturn(mockRawRecords);

        // Setup extractor first (initializes cosClient)
        spyExtractor.setup(mockContext);

        // Execute
        spyExtractor.extract(mockContext);

        // Verify: currentBusinessDate should be set to "2025.01.01"
        // We'll check via reflection
        String currentBusinessDate = getCurrentBusinessDate(spyExtractor);
        assertEquals("currentBusinessDate should be formatted YYYY.MM.DD", "2025.01.01", currentBusinessDate);
    }

    /**
     * Helper to set currentBusinessDate via reflection.
     */
    private void setCurrentBusinessDate(String date) {
        try {
            java.lang.reflect.Field field = XbondTradeExtractor.class.getDeclaredField("currentBusinessDate");
            field.setAccessible(true);
            field.set(extractor, date);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set currentBusinessDate", e);
        }
    }

    /**
     * Helper to get currentBusinessDate via reflection.
     */
    private String getCurrentBusinessDate(XbondTradeExtractor extractor) {
        try {
            java.lang.reflect.Field field = XbondTradeExtractor.class.getDeclaredField("currentBusinessDate");
            field.setAccessible(true);
            return (String) field.get(extractor);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get currentBusinessDate", e);
        }
    }
}