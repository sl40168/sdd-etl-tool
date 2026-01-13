package com.sdd.etl.source.extract.cos;

import com.sdd.etl.ETLException;
import com.sdd.etl.source.extract.cos.model.RawTradeRecord;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import cn.hutool.core.date.LocalDateTimeUtil;
import java.util.ArrayList;
import java.util.List;

/**
 * Streaming CSV parser for Xbond Trade data files.
 * 
 * <p>
 * Uses OpenCSV library for efficient streaming parsing of large CSV files.
 * Provides conversion from CSV row data to {@link RawTradeRecord} objects.
 * </p>
 * 
 * <p>
 * CSV format expectations:
 * <ul>
 * <li>Header row with field names</li>
 * <li>Data rows starting from row 2</li>
 * <li>UTF-8 encoding</li>
 * <li>Standard comma separator</li>
 * <li>Optional double quotes around fields</li>
 * </ul>
 */
public class TradeCsvParser {

    /** Date format for timestamp fields in CSV */
    private static final String TIME_FORMATTER = "yyyy-MM-dd HH:mm:ss.SSS";

    /** Date format for act_dt field in CSV */
    private static final String ACT_DATE_FORMATTER = "yyyyMMdd";

    /** CSV header field names - trade specific (from Plan.md I.4 format) */
    private static final String FIELD_ID = "id";
    private static final String FIELD_BOND_KEY = "bond_key";
    private static final String FIELD_BOND_CODE = "bond_code";
    private static final String FIELD_SYMBOL = "symbol";
    private static final String FIELD_DEAL_TIME = "deal_time";
    private static final String FIELD_ACT_DT = "act_dt";
    private static final String FIELD_ACT_TM = "act_tm";
    private static final String FIELD_PRE_MARKET = "pre_market";
    private static final String FIELD_TRADE_METHOD = "trade_method";
    private static final String FIELD_SIDE = "side";
    private static final String FIELD_NET_PRICE = "net_price";
    private static final String FIELD_SET_DAYS = "set_days";
    private static final String FIELD_YIELD = "yield";
    private static final String FIELD_YIELD_TYPE = "yield_type";
    private static final String FIELD_DEAL_SIZE = "deal_size";
    private static final String FIELD_RECV_TIME = "recv_time";
    private static final String FIELD_HLID = "hlid";

    /**
     * Parses a CSV file and converts to RawTradeRecord objects.
     * 
     * <p>
     * Method uses streaming parser to handle large files efficiently.
     * Each row is parsed, validated, and converted to a record object.
     * </p>
     * 
     * @param csvFile CSV file to parse
     * @return list of RawTradeRecord objects
     * @throws ETLException if file cannot be read or parsing fails
     */
    public List<RawTradeRecord> parse(File csvFile) throws ETLException {
        List<RawTradeRecord> records = new ArrayList<>();

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
                        RawTradeRecord record = createRecordFromRow(nextLine);

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
     * Creates a RawTradeRecord from a CSV row array.
     *
     * <p>
     * Maps CSV field values to record properties based on field names.
     * Handles type conversion (String to Long, String to Integer, String to Double,
     * etc.).
     * Uses safe conversion methods to handle null/empty values.
     * </p>
     *
     * <p>
     * CSV format from Plan.md I.4:
     * id, bond_key, bond_code, symbol, deal_time, act_dt, act_tm, pre_market,
     * trade_method, side, net_price, set_days, yield, yield_type, deal_size,
     * recv_time, hlid
     * </p>
     *
     * @param row CSV row as string array
     * @return RawTradeRecord object, or null if row is invalid
     */
    private RawTradeRecord createRecordFromRow(String[] row) {
        if (row == null || row.length == 0) {
            return null;
        }

        try {
            RawTradeRecord record = new RawTradeRecord();

            int index = 0;

            // id
            if (index < row.length) {
                record.setId(parseLong(row[index++]));
            }

            // bond_key -> underlyingSecurityId
            if (index < row.length) {
                record.setUnderlyingSecurityId(parseString(row[index++]));
            }

            // bond_code
            if (index < row.length) {
                record.setBondCode(parseString(row[index++]));
            }

            // symbol
            if (index < row.length) {
                record.setSymbol(parseString(row[index++]));
            }

            // deal_time -> dealTime
            if (index < row.length) {
                record.setDealTime(parseDateTime(row[index++]));
            }

            // act_dt
            if (index < row.length) {
                record.setActDt(parseString(row[index++]));
            }

            // act_tm
            if (index < row.length) {
                record.setActTm(parseString(row[index++]));
            }

            // pre_market
            if (index < row.length) {
                record.setPreMarket(parseInt(row[index++]));
            }

            // trade_method
            if (index < row.length) {
                record.setTradeMethod(parseInt(row[index++]));
            }

            // side
            if (index < row.length) {
                record.setSide(parseString(row[index++]));
            }

            // net_price
            if (index < row.length) {
                record.setNetPrice(parseDouble(row[index++]));
            }

            // set_days
            if (index < row.length) {
                record.setSetDays(parseString(row[index++]));
            }

            // yield
            if (index < row.length) {
                record.setYield(parseDouble(row[index++]));
            }

            // yield_type
            if (index < row.length) {
                record.setYieldType(parseString(row[index++]));
            }

            // deal_size
            if (index < row.length) {
                record.setDealSize(parseLong(row[index++]));
            }

            // recv_time
            if (index < row.length) {
                record.setRecvTime(parseDateTime(row[index++]));
            }

            // hlid
            if (index < row.length) {
                record.setHlid(parseString(row[index++]));
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
     * <p>
     * Expected format: yyyy-MM-dd HH:mm:ss.SSS (e.g., 2026-01-05 10:07:45.068)
     * </p>
     *
     * @param value string value to parse
     * @return LocalDateTime value, or null if value is null/empty
     */
    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTimeUtil.parse(value.trim(), TIME_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }
}