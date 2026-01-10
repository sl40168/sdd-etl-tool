package com.sdd.etl.source.extract.cos;

import com.sdd.etl.ETLException;
import com.sdd.etl.source.extract.cos.model.RawQuoteRecord;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Streaming CSV parser for Xbond Quote data files.
 * 
 * <p>Uses OpenCSV library for efficient streaming parsing of large CSV files.
 * Provides conversion from CSV row data to {@link RawQuoteRecord} objects.</p>
 * 
 * <p>CSV format expectations:
 * <ul>
 *   <li>Header row with field names</li>
 *   <li>Data rows starting from row 2</li>
 *   <li>UTF-8 encoding</li>
 *   <li>Standard comma separator</li>
 *   <li>Optional double quotes around fields</li>
 * </ul>
 */
public class CsvParser {
    
    /** Date format for timestamp fields in CSV */
    private static final DateTimeFormatter TIME_FORMATTER = 
            DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss.SSS");
    
    /** CSV header field names */
    private static final String FIELD_ID = "id";
    private static final String FIELD_UNDERLYING_SECURITY_ID = "underlying_security_id";
    private static final String FIELD_UNDERLYING_SETTLEMENT_TYPE = "underlying_settlement_type";
    private static final String FIELD_UNDERLYING_MD_ENTRY_TYPE = "underlying_md_entry_type";
    private static final String FIELD_UNDERLYING_MD_ENTRY_PX = "underlying_md_entry_px";
    private static final String FIELD_UNDERLYING_MD_PRICE_LEVEL = "underlying_md_price_level";
    private static final String FIELD_UNDERLYING_MD_ENTRY_SIZE = "underlying_md_entry_size";
    private static final String FIELD_UNDERLYING_YIELD_TYPE = "underlying_yield_type";
    private static final String FIELD_UNDERLYING_YIELD = "underlying_yield";
    private static final String FIELD_TRANSACT_TIME = "transact_time";
    private static final String FIELD_MQ_OFFSET = "mq_offset";
    private static final String FIELD_RECV_TIME = "recv_time";
    
    /**
     * Parses a CSV file and converts to RawQuoteRecord objects.
     * 
     * <p>Method uses streaming parser to handle large files efficiently.
     * Each row is parsed, validated, and converted to a record object.</p>
     * 
     * @param csvFile CSV file to parse
     * @return list of RawQuoteRecord objects
     * @throws ETLException if file cannot be read or parsing fails
     */
    public List<RawQuoteRecord> parse(File csvFile) throws ETLException {
        List<RawQuoteRecord> records = new ArrayList<>();
        
        try (InputStream inputStream = new FileInputStream(csvFile);
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            // Create CSV reader with OpenCSV, skip header row
            CSVReader reader = new CSVReaderBuilder(inputStreamReader)
                    .withSkipLines(1)
                    .build();
            
            // Read and parse CSV rows
            String[] nextLine;
            try {
                while ((nextLine = reader.readNext()) != null) {
                    try {
                        RawQuoteRecord record = createRecordFromRow(nextLine);
                        
                        // Validate record
                        if (record != null && record.isValid()) {
                            records.add(record);
                        }
                        
                    } catch (Exception e) {
                        throw new ETLException("CSV_PARSE", csvFile.getName(),
                                "Failed to parse CSV row: " + e.getMessage(), e);
                    }
                }
            } catch (Exception e) {
                throw new ETLException("CSV_PARSE", csvFile.getName(),
                        "Failed to read CSV: " + e.getMessage(), e);
            }
            
        } catch (IOException e) {
            throw new ETLException("CSV_PARSE", csvFile.getName(),
                    "Failed to read CSV file: " + e.getMessage(), e);
        }
        
        return records;
    }
    
    /**
     * Creates a RawQuoteRecord from a CSV row array.
     * 
     * <p>Maps CSV field values to record properties based on field names.
     * Handles type conversion (String to Long, String to Integer, String to Double, etc.).
     * Uses safe conversion methods to handle null/empty values.</p>
     * 
     * @param row CSV row as string array
     * @return RawQuoteRecord object, or null if row is invalid
     */
    private RawQuoteRecord createRecordFromRow(String[] row) {
        if (row == null || row.length == 0) {
            return null;
        }
        
        try {
            RawQuoteRecord record = new RawQuoteRecord();
            
            // Map fields by index (assuming fixed column order)
            // Order: id, underlying_security_id, underlying_settlement_type, 
            //        underlying_md_entry_type, underlying_md_entry_px, underlying_md_price_level,
            //        underlying_md_entry_size, underlying_yield_type, underlying_yield,
            //        transact_time, mq_offset, recv_time
            
            int index = 0;
            
            // id
            if (index < row.length) {
                record.setId(parseLong(row[index++]));
            }
            
            // underlying_security_id
            if (index < row.length) {
                record.setUnderlyingSecurityId(parseString(row[index++]));
            }
            
            // underlying_settlement_type
            if (index < row.length) {
                record.setUnderlyingSettlementType(parseInt(row[index++]));
            }
            
            // underlying_md_entry_type
            if (index < row.length) {
                record.setUnderlyingMdEntryType(parseInt(row[index++]));
            }
            
            // underlying_md_entry_px
            if (index < row.length) {
                record.setUnderlyingMdEntryPx(parseDouble(row[index++]));
            }
            
            // underlying_md_price_level
            if (index < row.length) {
                record.setUnderlyingMdPriceLevel(parseInt(row[index++]));
            }
            
            // underlying_md_entry_size
            if (index < row.length) {
                record.setUnderlyingMdEntrySize(parseLong(row[index++]));
            }
            
            // underlying_yield_type
            if (index < row.length) {
                record.setUnderlyingYieldType(parseString(row[index++]));
            }
            
            // underlying_yield
            if (index < row.length) {
                record.setUnderlyingYield(parseDouble(row[index++]));
            }
            
            // transact_time
            if (index < row.length) {
                record.setTransactTime(parseDateTime(row[index++]));
            }
            
            // mq_offset
            if (index < row.length) {
                record.setMqOffset(parseLong(row[index++]));
            }
            
            // recv_time
            if (index < row.length) {
                record.setRecvTime(parseDateTime(row[index++]));
            }
            
            return record;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to create record from CSV row", e);
        }
    }
    
    /**
     * Parses a Long value from string, handling null/empty values.
     * 
     * @param value string value to parse
     * @return Long value, or null if value is null/empty
     */
    private Long parseLong(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Parses an Integer value from string, handling null/empty values.
     * 
     * @param value string value to parse
     * @return Integer value, or null if value is null/empty
     */
    private Integer parseInt(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Parses a Double value from string, handling null/empty values.
     * 
     * @param value string value to parse
     * @return Double value, or null if value is null/empty
     */
    private Double parseDouble(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Parses a string value, handling null values.
     * 
     * @param value string value to parse
     * @return string value, or null if value is null
     */
    private String parseString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
    
    /**
     * Parses a LocalDateTime value from string, handling null/empty values.
     * 
     * <p>Expected format: yyyyMMdd-HH:mm:ss.SSS (e.g., 20250101-10:30:00.000)</p>
     * 
     * @param value string value to parse
     * @return LocalDateTime value, or null if value is null/empty
     */
    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value.trim(), TIME_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }
}