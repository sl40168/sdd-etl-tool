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
     * Creates a standard header row for trade CSV (Plan.md I.4 format).
     */
    private String createHeader() {
        return "id,bond_key,bond_code,symbol,deal_time,act_dt,act_tm,pre_market,trade_method,side,net_price,set_days,yield,yield_type,deal_size,recv_time,hlid";
    }

    /**
     * Creates a valid CSV row for trade data (Plan.md I.4 format).
     */
    private String createValidRow(long id) {
        return String.format(
                "%d,250210.IB,250210,25国开10,2026-01-05 10:07:45.068,20260105,100745068,0,3,Y,98.4289,T+1,1.9875,1,5000,2026-01-05 10:07:45.102,4455380029616468",
                id, id, id, id, id);
    }

    /**
     * Creates a row with missing required fields (invalid).
     */
    private String createInvalidRow() {
        return ",,,,,,,,,,T+1,,,";
    }

    @Test
    public void testParse_ValidSingleRecord_ReturnsRecord() throws IOException, ETLException {
        // Given: CSV with header and one valid row (Plan.md I.4 format)
        String header = createHeader();
        String row = "11568725,250210.IB,250210,25国开10,2026-01-05 10:07:45.068,20260105,100745068,0,3,Y,98.4289,T+1,1.9875,1,5000,2026-01-05 10:07:45.102,4455380029616468";
        createCsvFile(header + "\n" + row);

        // When
        List<RawTradeRecord> records = csvParser.parse(tempFile);

        // Then
        assertNotNull("Records list should not be null", records);
        assertEquals("Should parse exactly one record", 1, records.size());

        RawTradeRecord record = records.get(0);
        assertNotNull("Parsed record should not be null", record);
        assertEquals("Record id should match", Long.valueOf(11568725), record.getId());
        assertEquals("Bond key should match", "250210.IB", record.getUnderlyingSecurityId());
        assertEquals("Net price should match", Double.valueOf(98.4289), record.getNetPrice());
        assertEquals("Yield should match", Double.valueOf(1.9875), record.getYield());
        assertEquals("Yield type should match", "1", record.getYieldType());
        assertEquals("Deal size should match", Long.valueOf(5000), record.getDealSize());
        assertEquals("Side should match", "Y", record.getSide());
        assertEquals("Deal time should match",
                LocalDateTime.parse("2026-01-05T10:07:45.068"), record.getDealTime());
        assertEquals("Recv time should match",
                LocalDateTime.parse("2026-01-05T10:07:45.102"), record.getRecvTime());
    }

    @Test
    public void testParse_MultipleValidRecords_ReturnsAll() throws IOException, ETLException {
        // Given: CSV with header and multiple valid rows (Plan.md I.4 format)
        String header = createHeader();
        StringBuilder content = new StringBuilder(header);
        content.append("\n11568725,250210.IB,250210,25国开10,2026-01-05 10:07:45.068,20260105,100745068,0,3,Y,98.4289,T+1,1.9875,1,5000,2026-01-05 10:07:45.102,4455380029616468");
        content.append("\n11577382,250210.IB,250210,25国开10,2026-01-05 13:57:55.352,20260105,135755352,0,3,Y,98.4082,T+1,1.99,1,5000,2026-01-05 13:57:55.384,4455380029893492");
        content.append("\n11590145,250210.IB,250210,25国开10,2026-01-05 15:50:54.350,20260105,155054350,0,3,Y,98.3668,T+1,1.995,1,3000,2026-01-05 15:50:54.385,4455380030301908");
        createCsvFile(content.toString());

        // When
        List<RawTradeRecord> records = csvParser.parse(tempFile);

        // Then
        assertEquals("Should parse exactly 3 records", 3, records.size());
    }

    @Test
    public void testParse_MixedValidAndInvalid_ReturnsOnlyValid() throws IOException, ETLException {
        // Given: CSV with header, valid rows, and invalid rows
        String header = createHeader();
        String valid1 = "11568725,250210.IB,250210,25国开10,2026-01-05 10:07:45.068,20260105,100745068,0,3,Y,98.4289,T+1,1.9875,1,5000,2026-01-05 10:07:45.102,4455380029616468";
        String invalid = ",,,,,,,,,,T+1,,,";
        String valid2 = "11577382,250210.IB,250210,25国开10,2026-01-05 13:57:55.352,20260105,135755352,0,3,Y,98.4082,T+1,1.99,1,5000,2026-01-05 13:57:55.384,4455380029893492";

        createCsvFile(header + "\n" + valid1 + "\n" + invalid + "\n" + valid2);

        // When
        List<RawTradeRecord> records = csvParser.parse(tempFile);

        // Then
        assertEquals("Should parse only 2 valid records", 2, records.size());
        assertEquals("First record id should be 11568725", Long.valueOf(11568725), records.get(0).getId());
        assertEquals("Second record id should be 11577382", Long.valueOf(11577382), records.get(1).getId());
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
        String row = "11568725,250210.IB,250210,25国开10,invalid_format,20260105,100745068,0,3,Y,98.4289,T+1,1.9875,1,5000,2026-01-05 10:07:45.102,4455380029616468";
        createCsvFile(header + "\n" + row);

        // When
        List<RawTradeRecord> records = csvParser.parse(tempFile);

        // Then: Invalid timestamp results in null field, record may be invalid
        // Since dealTime is required, record.isValid() will return false
        // and the record will not be added to the result list
        assertTrue("Invalid timestamp should result in empty list", records.isEmpty());
    }

    @Test
    public void testParse_NumericFieldsWithInvalidFormat_ReturnsNull() throws IOException, ETLException {
        // Given: CSV with invalid numeric format for required field (net_price)
        // All required fields are otherwise valid
        String header = createHeader();
        String row = "11568725,250210.IB,250210,25国开10,2026-01-05 10:07:45.068,20260105,100745068,0,3,Y,not_a_number,T+1,1.9875,1,5000,2026-01-05 10:07:45.102,4455380029616468";
        createCsvFile(header + "\n" + row);

        // When
        List<RawTradeRecord> records = csvParser.parse(tempFile);

        // Then: Should parse one record, invalid numeric field parsed as null
        assertEquals("Should parse one record", 1, records.size());
        RawTradeRecord record = records.get(0);
        assertEquals("Record id should match", Long.valueOf(11568725), record.getId());
        assertEquals("Bond key should match", "250210.IB", record.getUnderlyingSecurityId());
        assertNull("Invalid numeric net_price should be null", record.getNetPrice());
        // Required fields should be present
        assertEquals("Deal time should match", LocalDateTime.parse("2026-01-05T10:07:45.068"),
                record.getDealTime());
    }

    @Test
    public void testParse_EmptyFields_ParsesNull() throws IOException, ETLException {
        // Given: CSV with empty fields
        String header = createHeader();
        String row = ",,,,,,,,,,T+1,,,";
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
        String row = "  11568725  ,  250210.IB  ,  250210  ,  25国开10  ,  2026-01-05 10:07:45.068  ,  20260105  ,  100745068  ,  0  ,  3  ,  Y  ,  98.4289  ,  T+1  ,  1.9875  ,  1  ,  5000  ,  2026-01-05 10:07:45.102  ,  4455380029616468  ";
        createCsvFile(header + "\n" + row);

        // When
        List<RawTradeRecord> records = csvParser.parse(tempFile);

        // Then: Whitespace should be trimmed
        assertEquals("Should parse one record", 1, records.size());
        RawTradeRecord record = records.get(0);
        assertEquals("Record id should be trimmed", Long.valueOf(11568725), record.getId());
        assertEquals("Bond key should be trimmed", "250210.IB", record.getUnderlyingSecurityId());
        assertEquals("Net price should be trimmed", Double.valueOf(98.4289), record.getNetPrice());
    }

    @Test
    public void testParse_QuotedFields_HandlesQuotes() throws IOException, ETLException {
        // Given: CSV with quoted fields
        String header = createHeader();
        String row = "\"11568725\",\"250210.IB\",\"250210\",\"25国开10\",\"2026-01-05 10:07:45.068\",\"20260105\",\"100745068\",\"0\",\"3\",\"Y\",\"98.4289\",\"T+1\",\"1.9875\",\"1\",\"5000\",\"2026-01-05 10:07:45.102\",\"4455380029616468\"";
        createCsvFile(header + "\n" + row);

        // When
        List<RawTradeRecord> records = csvParser.parse(tempFile);

        // Then: Quoted fields should parse correctly
        assertEquals("Should parse one record", 1, records.size());
        RawTradeRecord record = records.get(0);
        assertEquals("Record id should match", Long.valueOf(11568725), record.getId());
        assertEquals("Bond key should match", "250210.IB", record.getUnderlyingSecurityId());
        assertEquals("Net price should match", Double.valueOf(98.4289), record.getNetPrice());
    }

    /**
     * Test parsing CSV format from Plan.md I.4 section.
     * This test uses the actual CSV format defined in Plan.md for Xbond Trade data.
     * 
     * CSV format from Plan.md:
     * id | bond_key | bond_code | symbol | deal_time | act_dt | act_tm | pre_market | 
     * trade_method | side | net_price | set_days | yield | yield_type | deal_size | recv_time | hlid
     */
    @Test
    public void testParse_PlanMdFormat_CsvFormat() throws IOException, ETLException {
        // Given: CSV in format from Plan.md I.4 section
        String header = "id,bond_key,bond_code,symbol,deal_time,act_dt,act_tm,pre_market,trade_method,side,net_price,set_days,yield,yield_type,deal_size,recv_time,hlid";
        String row1 = "11568725,250210.IB,250210,25国开10,2026-01-05 10:07:45.068,20260105,100745068,0,3,Y,98.4289,T+1,1.9875,1,5000,2026-01-05 10:07:45.102,4455380029616468";
        String row2 = "11577382,250210.IB,250210,25国开10,2026-01-05 13:57:55.352,20260105,135755352,0,3,Y,98.4082,T+1,1.99,1,5000,2026-01-05 13:57:55.384,4455380029893492";
        String row3 = "11590145,250210.IB,250210,25国开10,2026-01-05 15:50:54.350,20260105,155054350,0,3,Y,98.3668,T+1,1.995,1,3000,2026-01-05 15:50:54.385,4455380030301908";

        createCsvFile(header + "\n" + row1 + "\n" + row2 + "\n" + row3);

        // When
        List<RawTradeRecord> records = csvParser.parse(tempFile);

        // Then: Should parse all 3 records correctly
        assertNotNull("Records list should not be null", records);
        assertEquals("Should parse exactly 3 records", 3, records.size());

        // Verify first record
        RawTradeRecord record1 = records.get(0);
        assertEquals("Record 1 id should match", Long.valueOf(11568725), record1.getId());
        assertEquals("Record 1 bond_key should match", "250210.IB", record1.getUnderlyingSecurityId());
        assertEquals("Record 1 net_price should match", Double.valueOf(98.4289), record1.getNetPrice());
        assertEquals("Record 1 set_days (T+1) should be parsed", "T+1", record1.getSetDays());
        assertEquals("Record 1 yield should match", Double.valueOf(1.9875), record1.getYield());
        assertEquals("Record 1 yield_type should match", "1", record1.getYieldType());
        assertEquals("Record 1 deal_size should match", Long.valueOf(5000), record1.getDealSize());
        assertEquals("Record 1 side should match", "Y", record1.getSide());
        assertEquals("Record 1 deal_time should match", 
                LocalDateTime.parse("2026-01-05T10:07:45.068"), record1.getDealTime());
        assertEquals("Record 1 recv_time should match", 
                LocalDateTime.parse("2026-01-05T10:07:45.102"), record1.getRecvTime());

        // Verify second record
        RawTradeRecord record2 = records.get(1);
        assertEquals("Record 2 id should match", Long.valueOf(11577382), record2.getId());
        assertEquals("Record 2 bond_key should match", "250210.IB", record2.getUnderlyingSecurityId());
        assertEquals("Record 2 net_price should match", Double.valueOf(98.4082), record2.getNetPrice());
        assertEquals("Record 2 set_days (T+1) should be parsed", "T+1", record2.getSetDays());
        assertEquals("Record 2 yield should match", Double.valueOf(1.99), record2.getYield());
        assertEquals("Record 2 yield_type should match", "1", record2.getYieldType());
        assertEquals("Record 2 deal_size should match", Long.valueOf(5000), record2.getDealSize());
        assertEquals("Record 2 side should match", "Y", record2.getSide());
        assertEquals("Record 2 deal_time should match", 
                LocalDateTime.parse("2026-01-05T13:57:55.352"), record2.getDealTime());
        assertEquals("Record 2 recv_time should match", 
                LocalDateTime.parse("2026-01-05T13:57:55.384"), record2.getRecvTime());

        // Verify third record
        RawTradeRecord record3 = records.get(2);
        assertEquals("Record 3 id should match", Long.valueOf(11590145), record3.getId());
        assertEquals("Record 3 bond_key should match", "250210.IB", record3.getUnderlyingSecurityId());
        assertEquals("Record 3 net_price should match", Double.valueOf(98.3668), record3.getNetPrice());
        assertEquals("Record 3 set_days (T+1) should be parsed", "T+1", record3.getSetDays());
        assertEquals("Record 3 yield should match", Double.valueOf(1.995), record3.getYield());
        assertEquals("Record 3 yield_type should match", "1", record3.getYieldType());
        assertEquals("Record 3 deal_size should match", Long.valueOf(3000), record3.getDealSize());
        assertEquals("Record 3 side should match", "Y", record3.getSide());
        assertEquals("Record 3 deal_time should match", 
                LocalDateTime.parse("2026-01-05T15:50:54.350"), record3.getDealTime());
        assertEquals("Record 3 recv_time should match", 
                LocalDateTime.parse("2026-01-05T15:50:54.385"), record3.getRecvTime());
    }
}