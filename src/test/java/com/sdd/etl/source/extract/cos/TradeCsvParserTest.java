package com.sdd.etl.source.extract.cos;

import com.sdd.etl.ETLException;
import com.sdd.etl.source.extract.cos.model.RawTradeRecord;
import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for TradeCsvParser utility.
 * Tests streaming CSV parsing for Xbond Trade data files.
 */
public class TradeCsvParserTest {
    
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    
    private TradeCsvParser csvParser;
    private File tempFile;
    
    @Before
    public void setUp() throws IOException {
        csvParser = new TradeCsvParser();
        tempFile = temporaryFolder.newFile("test.csv");
    }
    
    /**
     * Creates a CSV file with the given content.
     * Content should include header row.
     */
    private void createCsvFile(String content) throws IOException {
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(content);
        }
    }
    
    /**
     * Creates a standard header row for trade CSV.
     */
    private String createHeader() {
        return "id,underlying_security_id,underlying_settlement_type,trade_price,trade_yield,trade_yield_type,trade_volume,counterparty,trade_id,transact_time,mq_offset,recv_time";
    }
    
    /**
     * Creates a valid CSV row for trade data.
     */
    private String createValidRow(long id) {
        return String.format("%d,102100%d,1,100.%d,2.5,YTM,1000,C%03d,T20250101-00%d,20250101-10:30:00.000,%d,20250101-10:30:05.000",
                id, id, id, id, id, id * 100L);
    }
    
    /**
     * Creates a row with missing required fields (invalid).
     */
    private String createInvalidRow() {
        return ",,1,,,YTM,,,20250101-10:30:00.000,,";
    }
    
    @Test
    public void testParse_ValidSingleRecord_ReturnsRecord() throws IOException, ETLException {
        // Given: CSV with header and one valid row
        String header = createHeader();
        String row = createValidRow(1);
        createCsvFile(header + "\n" + row);
        
        // When
        List<RawTradeRecord> records = csvParser.parse(tempFile);
        
        // Then
        assertNotNull("Records list should not be null", records);
        assertEquals("Should parse exactly one record", 1, records.size());
        
        RawTradeRecord record = records.get(0);
        assertNotNull("Parsed record should not be null", record);
        assertEquals("Record id should match", Long.valueOf(1), record.getId());
        assertEquals("Underlying security id should match", "1021001", record.getUnderlyingSecurityId());
        assertEquals("Underlying settlement type should match", Integer.valueOf(1), record.getUnderlyingSettlementType());
        assertEquals("Trade price should match", Double.valueOf(100.1), record.getTradePrice());
        assertEquals("Trade yield should match", Double.valueOf(2.5), record.getTradeYield());
        assertEquals("Trade yield type should match", "YTM", record.getTradeYieldType());
        assertEquals("Trade volume should match", Long.valueOf(1000), record.getTradeVolume());
        assertEquals("Counterparty should match", "C001", record.getCounterparty());
        assertEquals("Trade id should match", "T20250101-001", record.getTradeId());
        assertEquals("Transact time should match", 
                     LocalDateTime.parse("2025-01-01T10:30:00"), record.getTransactTime());
        assertEquals("Mq offset should match", Long.valueOf(100), record.getMqOffset());
        assertEquals("Recv time should match", 
                     LocalDateTime.parse("2025-01-01T10:30:05"), record.getRecvTime());
    }
    
    @Test
    public void testParse_MultipleValidRecords_ReturnsAll() throws IOException, ETLException {
        // Given: CSV with header and multiple valid rows
        String header = createHeader();
        StringBuilder content = new StringBuilder(header);
        for (int i = 1; i <= 5; i++) {
            content.append("\n").append(createValidRow(i));
        }
        createCsvFile(content.toString());
        
        // When
        List<RawTradeRecord> records = csvParser.parse(tempFile);
        
        // Then
        assertEquals("Should parse exactly 5 records", 5, records.size());
        for (int i = 0; i < 5; i++) {
            RawTradeRecord record = records.get(i);
            assertEquals("Record id should match", Long.valueOf(i + 1), record.getId());
            assertEquals("Underlying security id should match", 
                         String.format("102100%d", i + 1), record.getUnderlyingSecurityId());
        }
    }
    
    @Test
    public void testParse_MixedValidAndInvalid_ReturnsOnlyValid() throws IOException, ETLException {
        // Given: CSV with header, valid rows, and invalid rows
        String header = createHeader();
        String valid1 = createValidRow(1);
        String invalid = createInvalidRow();
        String valid2 = createValidRow(2);
        
        createCsvFile(header + "\n" + valid1 + "\n" + invalid + "\n" + valid2);
        
        // When
        List<RawTradeRecord> records = csvParser.parse(tempFile);
        
        // Then
        assertEquals("Should parse only 2 valid records", 2, records.size());
        assertEquals("First record id should be 1", Long.valueOf(1), records.get(0).getId());
        assertEquals("Second record id should be 2", Long.valueOf(2), records.get(1).getId());
    }
    
    @Test
    public void testParse_EmptyFile_ReturnsEmptyList() throws IOException, ETLException {
        // Given: Empty file
        createCsvFile("");
        
        // When
        List<RawTradeRecord> records = csvParser.parse(tempFile);
        
        // Then
        assertNotNull("Records list should not be null", records);
        assertTrue("Records list should be empty", records.isEmpty());
    }
    
    @Test
    public void testParse_OnlyHeader_ReturnsEmptyList() throws IOException, ETLException {
        // Given: CSV with only header row
        String header = createHeader();
        createCsvFile(header);
        
        // When
        List<RawTradeRecord> records = csvParser.parse(tempFile);
        
        // Then
        assertNotNull("Records list should not be null", records);
        assertTrue("Records list should be empty when only header present", records.isEmpty());
    }
    
    @Test
    public void testParse_MissingColumns_IgnoresExtraColumns() throws IOException, ETLException {
        // Given: CSV with fewer columns than expected (missing last two optional columns)
        // Standard header still has 12 columns, but data row has only 10
        String header = createHeader();
        // Row with required fields: id, underlyingSecurityId, underlyingSettlementType, tradePrice,
        // tradeYield, tradeYieldType, tradeVolume, counterparty, tradeId, transactTime
        // Missing: mq_offset, recv_time (optional)
        String row = "1,1021001,1,100.5,2.5,YTM,1000,C001,T20250101-001,20250101-10:30:00.000";
        createCsvFile(header + "\n" + row);
        
        // When
        List<RawTradeRecord> records = csvParser.parse(tempFile);
        
        // Then: Should parse one record, missing optional fields are null
        assertEquals("Should parse one record", 1, records.size());
        RawTradeRecord record = records.get(0);
        assertEquals("Record id should match", Long.valueOf(1), record.getId());
        assertEquals("Underlying security id should match", "1021001", record.getUnderlyingSecurityId());
        assertEquals("Trade price should match", Double.valueOf(100.5), record.getTradePrice());
        assertEquals("Trade id should match", "T20250101-001", record.getTradeId());
        assertEquals("Transact time should match", LocalDateTime.parse("2025-01-01T10:30:00"), record.getTransactTime());
        // Missing optional fields should be null
        assertNull("mqOffset should be null", record.getMqOffset());
        assertNull("recvTime should be null", record.getRecvTime());
    }
    
    @Test
    public void testParse_ExtraColumns_IgnoresExtraColumns() throws IOException, ETLException {
        // Given: CSV with extra columns beyond the expected 12
        String header = createHeader() + ",extra_field1,extra_field2";
        String row = "1,1021001,1,100.5,2.5,YTM,1000,C001,T20250101-001,20250101-10:30:00.000,100,20250101-10:30:05.000,extra1,extra2";
        createCsvFile(header + "\n" + row);
        
        // When
        List<RawTradeRecord> records = csvParser.parse(tempFile);
        
        // Then: Should parse first 12 columns correctly
        assertEquals("Should parse one record", 1, records.size());
        RawTradeRecord record = records.get(0);
        assertEquals("Record id should match", Long.valueOf(1), record.getId());
        assertEquals("Underlying security id should match", "1021001", record.getUnderlyingSecurityId());
        assertEquals("Trade price should match", Double.valueOf(100.5), record.getTradePrice());
        assertEquals("Trade id should match", "T20250101-001", record.getTradeId());
    }
    
    @Test(expected = ETLException.class)
    public void testParse_NonExistentFile_ThrowsETLException() throws ETLException {
        // Given: Non-existent file
        File nonExistentFile = new File("non_existent.csv");
        
        // When/Then: Should throw ETLException
        csvParser.parse(nonExistentFile);
    }
    
    @Test
    public void testParse_NullFile_ThrowsNullPointerException() {
        // Given: Null file
        // When/Then: Should throw NullPointerException
        try {
            csvParser.parse(null);
            fail("Expected NullPointerException");
        } catch (ETLException e) {
            // Should not be ETLException
            fail("Expected NullPointerException, got ETLException: " + e.getMessage());
        } catch (NullPointerException e) {
            // Expected
        }
    }
    
    @Test
    public void testParse_InvalidTimestampFormat_ParsesNull() throws IOException, ETLException {
        // Given: CSV with invalid timestamp format
        String header = createHeader();
        String row = "1,1021001,1,100.5,2.5,YTM,1000,C001,T20250101-001,invalid_format,100,20250101-10:30:05.000";
        createCsvFile(header + "\n" + row);
        
        // When
        List<RawTradeRecord> records = csvParser.parse(tempFile);
        
        // Then: Invalid timestamp results in null field, record may be invalid
        // Since transactTime is required, record.isValid() will return false
        // and the record will not be added to the result list
        assertTrue("Invalid timestamp should result in empty list", records.isEmpty());
    }
    
    @Test
    public void testParse_NumericFieldsWithInvalidFormat_ReturnsNull() throws IOException, ETLException {
        // Given: CSV with invalid numeric format for required field (trade_price)
        // All required fields are otherwise valid
        String header = createHeader();
        String row = "1,1021001,1,not_a_number,2.5,YTM,1000,C001,T20250101-001,20250101-10:30:00.000,100,20250101-10:30:05.000";
        createCsvFile(header + "\n" + row);
        
        // When
        List<RawTradeRecord> records = csvParser.parse(tempFile);
        
        // Then: Should parse one record, invalid numeric field parsed as null
        assertEquals("Should parse one record", 1, records.size());
        RawTradeRecord record = records.get(0);
        assertEquals("Record id should match", Long.valueOf(1), record.getId());
        assertEquals("Underlying security id should match", "1021001", record.getUnderlyingSecurityId());
        assertNull("Invalid numeric trade_price should be null", record.getTradePrice());
        // Required fields should be present
        assertEquals("Trade id should match", "T20250101-001", record.getTradeId());
        assertEquals("Transact time should match", LocalDateTime.parse("2025-01-01T10:30:00"), record.getTransactTime());
    }
    
    @Test
    public void testParse_EmptyFields_ParsesNull() throws IOException, ETLException {
        // Given: CSV with empty fields
        String header = createHeader();
        String row = ",,1,,,YTM,,,20250101-10:30:00.000,,";
        createCsvFile(header + "\n" + row);
        
        // When
        List<RawTradeRecord> records = csvParser.parse(tempFile);
        
        // Then: Empty fields should parse as null
        // The record will be invalid because underlyingSecurityId is empty (required)
        // So result list should be empty
        assertTrue("Record with empty required fields should be invalid", records.isEmpty());
    }
    
    @Test
    public void testParse_WhitespaceFields_TrimsWhitespace() throws IOException, ETLException {
        // Given: CSV with whitespace around fields
        String header = createHeader();
        String row = "  1  ,  1021001  ,  1  ,  100.5  ,  2.5  ,  YTM  ,  1000  ,  C001  ,  T20250101-001  ,  20250101-10:30:00.000  ,  100  ,  20250101-10:30:05.000  ";
        createCsvFile(header + "\n" + row);
        
        // When
        List<RawTradeRecord> records = csvParser.parse(tempFile);
        
        // Then: Whitespace should be trimmed
        assertEquals("Should parse one record", 1, records.size());
        RawTradeRecord record = records.get(0);
        assertEquals("Record id should be trimmed", Long.valueOf(1), record.getId());
        assertEquals("Underlying security id should be trimmed", "1021001", record.getUnderlyingSecurityId());
        assertEquals("Trade price should be trimmed", Double.valueOf(100.5), record.getTradePrice());
    }
    
    @Test
    public void testParse_QuotedFields_HandlesQuotes() throws IOException, ETLException {
        // Given: CSV with quoted fields
        String header = createHeader();
        String row = "\"1\",\"1021001\",\"1\",\"100.5\",\"2.5\",\"YTM\",\"1000\",\"C001\",\"T20250101-001\",\"20250101-10:30:00.000\",\"100\",\"20250101-10:30:05.000\"";
        createCsvFile(header + "\n" + row);
        
        // When
        List<RawTradeRecord> records = csvParser.parse(tempFile);
        
        // Then: Quoted fields should parse correctly
        assertEquals("Should parse one record", 1, records.size());
        RawTradeRecord record = records.get(0);
        assertEquals("Record id should match", Long.valueOf(1), record.getId());
        assertEquals("Underlying security id should match", "1021001", record.getUnderlyingSecurityId());
        assertEquals("Trade price should match", Double.valueOf(100.5), record.getTradePrice());
        assertEquals("Trade id should match", "T20250101-001", record.getTradeId());
    }
    
    @Test
    public void testParse_TradeYieldOnly_NoPrice_Valid() throws IOException, ETLException {
        // Given: CSV with only trade yield (no price, but volume present)
        String header = createHeader();
        String row = "1,1021001,1,,2.5,YTM,1000,C001,T20250101-001,20250101-10:30:00.000,100,20250101-10:30:05.000";
        createCsvFile(header + "\n" + row);
        
        // When
        List<RawTradeRecord> records = csvParser.parse(tempFile);
        
        // Then: Should parse one record, tradePrice null (will be NaN), tradeYield set
        assertEquals("Should parse one record", 1, records.size());
        RawTradeRecord record = records.get(0);
        assertEquals("Record id should match", Long.valueOf(1), record.getId());
        assertNull("Trade price should be null", record.getTradePrice());
        assertEquals("Trade yield should match", Double.valueOf(2.5), record.getTradeYield());
        assertEquals("Trade volume should match", Long.valueOf(1000), record.getTradeVolume());
        // Record should be valid because tradeYield and tradeVolume are populated
        assertTrue("Record with yield and volume should be valid", record.isValid());
    }
    
    @Test
    public void testParse_TradeVolumeZero_Invalid() throws IOException, ETLException {
        // Given: CSV with trade volume = 0 (business validation will reject, but raw parsing succeeds)
        String header = createHeader();
        String row = "1,1021001,1,100.5,2.5,YTM,0,C001,T20250101-001,20250101-10:30:00.000,100,20250101-10:30:05.000";
        createCsvFile(header + "\n" + row);
        
        // When
        List<RawTradeRecord> records = csvParser.parse(tempFile);
        
        // Then: Record should be parsed (raw validation passes), but will fail business validation later
        assertEquals("Should parse one record (raw validation passes)", 1, records.size());
        RawTradeRecord record = records.get(0);
        assertEquals("Trade volume should be 0", Long.valueOf(0), record.getTradeVolume());
    }
    
    @Test
    public void testParse_SettlementTypeZero_Valid() throws IOException, ETLException {
        // Given: CSV with settlement type 0 (T+0)
        String header = createHeader();
        String row = "1,1021001,0,100.5,2.5,YTM,1000,C001,T20250101-001,20250101-10:30:00.000,100,20250101-10:30:05.000";
        createCsvFile(header + "\n" + row);
        
        // When
        List<RawTradeRecord> records = csvParser.parse(tempFile);
        
        // Then: Should parse one record with settlement type 0
        assertEquals("Should parse one record", 1, records.size());
        RawTradeRecord record = records.get(0);
        assertEquals("Settlement type should be 0", Integer.valueOf(0), record.getUnderlyingSettlementType());
    }
}