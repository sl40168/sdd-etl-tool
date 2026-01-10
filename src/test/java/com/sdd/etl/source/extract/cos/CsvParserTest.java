package com.sdd.etl.source.extract.cos;

import com.sdd.etl.ETLException;
import com.sdd.etl.source.extract.cos.model.RawQuoteRecord;
import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for CsvParser utility.
 * Tests streaming CSV parsing for Xbond Quote data files.
 */
public class CsvParserTest {
    
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    
    private CsvParser csvParser;
    private File tempFile;
    
    @Before
    public void setUp() throws IOException {
        csvParser = new CsvParser();
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
     * Creates a standard header row.
     */
    private String createHeader() {
        return "id,underlying_security_id,underlying_settlement_type,underlying_md_entry_type," +
               "underlying_md_entry_px,underlying_md_price_level,underlying_md_entry_size," +
               "underlying_yield_type,underlying_yield,transact_time,mq_offset,recv_time";
    }
    
    /**
     * Creates a valid CSV row.
     */
    private String createValidRow(long id) {
        return String.format("%d,BOND%03d,1,0,101.5,2,1000,YTM,3.5,20250101-09:30:00.000,5000,20250101-09:30:01.000",
                id, id);
    }
    
    /**
     * Creates a row with missing required fields (invalid).
     */
    private String createInvalidRow() {
        return ",,1,0,101.5,2,1000,YTM,3.5,20250101-09:30:00.000,5000,20250101-09:30:01.000";
    }
    
    @Test
    public void testParse_ValidSingleRecord_ReturnsRecord() throws IOException, ETLException {
        // Given: CSV with header and one valid row
        String header = createHeader();
        String row = createValidRow(1);
        createCsvFile(header + "\n" + row);
        
        // When
        List<RawQuoteRecord> records = csvParser.parse(tempFile);
        
        // Then
        assertNotNull("Records list should not be null", records);
        assertEquals("Should parse exactly one record", 1, records.size());
        
        RawQuoteRecord record = records.get(0);
        assertNotNull("Parsed record should not be null", record);
        assertEquals("Record id should match", Long.valueOf(1), record.getId());
        assertEquals("Underlying security id should match", "BOND001", record.getUnderlyingSecurityId());
        assertEquals("Underlying settlement type should match", Integer.valueOf(1), record.getUnderlyingSettlementType());
        assertEquals("Underlying md entry type should match", Integer.valueOf(0), record.getUnderlyingMdEntryType());
        assertEquals("Underlying md entry px should match", Double.valueOf(101.5), record.getUnderlyingMdEntryPx());
        assertEquals("Underlying md price level should match", Integer.valueOf(2), record.getUnderlyingMdPriceLevel());
        assertEquals("Underlying md entry size should match", Long.valueOf(1000), record.getUnderlyingMdEntrySize());
        assertEquals("Underlying yield type should match", "YTM", record.getUnderlyingYieldType());
        assertEquals("Underlying yield should match", Double.valueOf(3.5), record.getUnderlyingYield());
        assertEquals("Transact time should match", 
                     LocalDateTime.parse("2025-01-01T09:30:00"), record.getTransactTime());
        assertEquals("Mq offset should match", Long.valueOf(5000), record.getMqOffset());
        assertEquals("Recv time should match", 
                     LocalDateTime.parse("2025-01-01T09:30:01"), record.getRecvTime());
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
        List<RawQuoteRecord> records = csvParser.parse(tempFile);
        
        // Then
        assertEquals("Should parse exactly 5 records", 5, records.size());
        for (int i = 0; i < 5; i++) {
            RawQuoteRecord record = records.get(i);
            assertEquals("Record id should match", Long.valueOf(i + 1), record.getId());
            assertEquals("Underlying security id should match", 
                         String.format("BOND%03d", i + 1), record.getUnderlyingSecurityId());
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
        List<RawQuoteRecord> records = csvParser.parse(tempFile);
        
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
        List<RawQuoteRecord> records = csvParser.parse(tempFile);
        
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
        List<RawQuoteRecord> records = csvParser.parse(tempFile);
        
        // Then
        assertNotNull("Records list should not be null", records);
        assertTrue("Records list should be empty when only header present", records.isEmpty());
    }
    
    @Test
    public void testParse_MissingColumns_IgnoresExtraColumns() throws IOException, ETLException {
        // Given: CSV with fewer columns than expected (missing last two optional columns)
        // Standard header still has 12 columns, but data row has only 10
        String header = createHeader();
        // Row with required fields: id, underlyingSecurityId, underlyingSettlementType, underlyingMdEntryType,
        // underlyingMdEntryPx, underlyingMdPriceLevel, underlyingMdEntrySize, underlyingYieldType, underlyingYield, transactTime
        // Missing: mq_offset, recv_time (optional)
        String row = "1,BOND001,1,0,101.5,2,1000,YTM,3.5,20250101-09:30:00.000";
        createCsvFile(header + "\n" + row);
        
        // When
        List<RawQuoteRecord> records = csvParser.parse(tempFile);
        
        // Then: Should parse one record, missing optional fields are null
        assertEquals("Should parse one record", 1, records.size());
        RawQuoteRecord record = records.get(0);
        assertEquals("Record id should match", Long.valueOf(1), record.getId());
        assertEquals("Underlying security id should match", "BOND001", record.getUnderlyingSecurityId());
        assertEquals("Underlying md price level should match", Integer.valueOf(2), record.getUnderlyingMdPriceLevel());
        assertEquals("Transact time should match", LocalDateTime.parse("2025-01-01T09:30:00"), record.getTransactTime());
        // Missing optional fields should be null
        assertNull("mqOffset should be null", record.getMqOffset());
        assertNull("recvTime should be null", record.getRecvTime());
    }
    
    @Test
    public void testParse_ExtraColumns_IgnoresExtraColumns() throws IOException, ETLException {
        // Given: CSV with extra columns beyond the expected 12
        String header = createHeader() + ",extra_field1,extra_field2";
        String row = "1,BOND001,1,0,101.5,2,1000,YTM,3.5,20250101-09:30:00.000,5000,20250101-09:30:01.000,extra1,extra2";
        createCsvFile(header + "\n" + row);
        
        // When
        List<RawQuoteRecord> records = csvParser.parse(tempFile);
        
        // Then: Should parse first 12 columns correctly
        assertEquals("Should parse one record", 1, records.size());
        RawQuoteRecord record = records.get(0);
        assertEquals("Record id should match", Long.valueOf(1), record.getId());
        assertEquals("Underlying security id should match", "BOND001", record.getUnderlyingSecurityId());
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
        String row = "1,BOND001,1,0,101.5,2,1000,YTM,3.5,invalid_format,5000,20250101-09:30:01.000";
        createCsvFile(header + "\n" + row);
        
        // When
        List<RawQuoteRecord> records = csvParser.parse(tempFile);
        
        // Then: Invalid timestamp results in null field, record may be invalid
        // The parser's parseDateTime method returns null for invalid formats
        // Since transactTime is required, record.isValid() will return false
        // and the record will not be added to the result list
        assertTrue("Invalid timestamp should result in empty list", records.isEmpty());
    }
    
    @Test
    public void testParse_NumericFieldsWithInvalidFormat_ReturnsNull() throws IOException, ETLException {
        // Given: CSV with invalid numeric format for optional field (underlyingMdEntryPx)
        // All required fields are valid
        String header = createHeader();
        String row = "1,BOND001,1,0,not_a_number,2,1000,YTM,3.5,20250101-09:30:00.000,5000,20250101-09:30:01.000";
        createCsvFile(header + "\n" + row);
        
        // When
        List<RawQuoteRecord> records = csvParser.parse(tempFile);
        
        // Then: Should parse one record, invalid numeric field parsed as null
        assertEquals("Should parse one record", 1, records.size());
        RawQuoteRecord record = records.get(0);
        assertEquals("Record id should match", Long.valueOf(1), record.getId());
        assertEquals("Underlying security id should match", "BOND001", record.getUnderlyingSecurityId());
        assertNull("Invalid numeric underlyingMdEntryPx should be null", record.getUnderlyingMdEntryPx());
        // Required fields should be present
        assertEquals("Underlying md price level should match", Integer.valueOf(2), record.getUnderlyingMdPriceLevel());
        assertEquals("Transact time should match", LocalDateTime.parse("2025-01-01T09:30:00"), record.getTransactTime());
    }
    
    @Test
    public void testParse_EmptyFields_ParsesNull() throws IOException, ETLException {
        // Given: CSV with empty fields
        String header = createHeader();
        String row = ",,1,0,,2,,,3.5,20250101-09:30:00.000,,";
        createCsvFile(header + "\n" + row);
        
        // When
        List<RawQuoteRecord> records = csvParser.parse(tempFile);
        
        // Then: Empty fields should parse as null
        // The record will be invalid because underlyingSecurityId is empty (required)
        // So result list should be empty
        assertTrue("Record with empty required fields should be invalid", records.isEmpty());
    }
    
    @Test
    public void testParse_WhitespaceFields_TrimsWhitespace() throws IOException, ETLException {
        // Given: CSV with whitespace around fields
        String header = createHeader();
        String row = "  1  ,  BOND001  ,  1  ,  0  ,  101.5  ,  2  ,  1000  ,  YTM  ,  3.5  ,  20250101-09:30:00.000  ,  5000  ,  20250101-09:30:01.000  ";
        createCsvFile(header + "\n" + row);
        
        // When
        List<RawQuoteRecord> records = csvParser.parse(tempFile);
        
        // Then: Whitespace should be trimmed
        assertEquals("Should parse one record", 1, records.size());
        RawQuoteRecord record = records.get(0);
        assertEquals("Record id should be trimmed", Long.valueOf(1), record.getId());
        assertEquals("Underlying security id should be trimmed", "BOND001", record.getUnderlyingSecurityId());
    }
    
    @Test
    public void testParse_QuotedFields_HandlesQuotes() throws IOException, ETLException {
        // Given: CSV with quoted fields
        String header = createHeader();
        String row = "\"1\",\"BOND001\",\"1\",\"0\",\"101.5\",\"2\",\"1000\",\"YTM\",\"3.5\",\"20250101-09:30:00.000\",\"5000\",\"20250101-09:30:01.000\"";
        createCsvFile(header + "\n" + row);
        
        // When
        List<RawQuoteRecord> records = csvParser.parse(tempFile);
        
        // Then: Quoted fields should parse correctly
        assertEquals("Should parse one record", 1, records.size());
        RawQuoteRecord record = records.get(0);
        assertEquals("Record id should match", Long.valueOf(1), record.getId());
        assertEquals("Underlying security id should match", "BOND001", record.getUnderlyingSecurityId());
    }
}