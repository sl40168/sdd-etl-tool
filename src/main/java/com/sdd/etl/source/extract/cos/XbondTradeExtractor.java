package com.sdd.etl.source.extract.cos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sdd.etl.util.DateUtils;
import com.sdd.etl.ETLException;
import com.sdd.etl.context.ETLContext;
import com.sdd.etl.model.SourceDataModel;
import com.sdd.etl.model.XbondTradeDataModel;
import com.sdd.etl.source.extract.cos.config.CosSourceConfig;
import com.sdd.etl.source.extract.cos.model.RawTradeRecord;
import com.sdd.etl.source.extract.cos.client.CosClientImpl;
import com.sdd.etl.source.extract.cos.model.CosFileMetadata;

import java.io.File;

import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Concrete extractor for Xbond Trade data from Tencent COS.
 * 
 * <p>
 * Handles:
 * <ol>
 * <li>File selection based on category "XbondCfetsDeal" and business date</li>
 * <li>CSV file parsing using {@link TradeCsvParser}</li>
 * <li>Conversion of raw records to {@link XbondTradeDataModel} with business
 * rules:
 * <ul>
 * <li>Group records by {@code mqOffset}</li>
 * <li>Map raw trade fields (price, yield, volume, counterparty, trade ID)</li>
 * <li>Add ".IB" suffix to underlyingSecurityId</li>
 * <li>Map settlement types (0=T+0, 1=T+1) to settleSpeed</li>
 * </ul>
 * </li>
 * </ol>
 * 
 * <p>
 * CSV format expectations:
 * <ul>
 * <li>Header row with field names</li>
 * <li>Columns in fixed order (id, underlying_security_id,
 * underlying_settlement_type,
 * trade_price, trade_yield, trade_yield_type, trade_volume, trade_side,
 * trade_id,
 * transact_time, mq_offset, recv_time)</li>
 * <li>UTF-8 encoding</li>
 * </ul>
 */
public class XbondTradeExtractor extends CosExtractor<RawTradeRecord> {

    /** Logger instance */
    private static final Logger LOG = LoggerFactory.getLogger(XbondTradeExtractor.class);

    /** TradeCsvParser instance for parsing CSV files */
    private TradeCsvParser tradeCsvParser;

    /** Current business date in YYYY.MM.DD format */
    private String currentBusinessDate;

    /**
     * Default constructor.
     */
    public XbondTradeExtractor() {
        super();
        this.tradeCsvParser = new TradeCsvParser();
    }

    /**
     * Gets the category identifier for file filtering.
     * 
     * @return "XbondCfetsDeal"
     */
    @Override
    public String getCategory() {
        return "XbondCfetsDeal";
    }

    /**
     * Selects files from COS matching the category and business date pattern.
     * Uses YYYY-MM-DD format for business date as required for Trade data.
     * 
     * @param context ETL context with business date
     * @return list of COS file metadata for selected files
     * @throws ETLException if file listing fails
     */
    @Override
    protected List<CosFileMetadata> selectFiles(ETLContext context) throws ETLException {
        String category = getCategory();
        // Format business date as YYYY-MM-DD for XbondCfetsDeal
        String dateStr = DateUtils.formatDate(context.getCurrentDate());
        String businessDate;
        if (dateStr != null && dateStr.length() == 8) {
            businessDate = dateStr.substring(0, 4) + "-" + dateStr.substring(4, 6) + "-" + dateStr.substring(6, 8);
        } else {
            businessDate = dateStr;
        }

        String prefix = category + "/" + businessDate + "/";

        return cosClient.listObjects(sourceConfig, prefix);
    }

    /**
     * Extracts data from COS, storing current business date for conversion.
     */
    @Override
    public List<SourceDataModel> extract(ETLContext context) throws ETLException {
        // Format business date from YYYYMMDD to YYYY.MM.DD
        String dateStr = DateUtils.formatDate(context.getCurrentDate());
        // Validate context parameters
        if (dateStr == null || dateStr.length() != 8) {
            throw new ETLException("XBOND_TRADE_EXTRACT", dateStr,
                    "Invalid business date format. Expected YYYYMMDD, got: " + dateStr);
        }
        this.currentBusinessDate = dateStr.substring(0, 4) + "." + dateStr.substring(4, 6) + "."
                + dateStr.substring(6, 8);
        return super.extract(context);
    }

    /**
     * Creates a COS client instance.
     * 
     * @param config COS source configuration
     * @return initialized CosClient instance
     * @throws ETLException if client creation fails
     */
    @Override
    protected CosClient createCosClient(CosSourceConfig config) throws ETLException {
        // Use the Tencent COS SDK implementation (same as quote extractor)
        return new CosClientImpl();
    }

    /**
     * Parses a CSV file into raw records.
     * 
     * @param csvFile CSV file to parse
     * @return list of raw records from the file
     * @throws ETLException if parsing fails
     */
    @Override
    protected List<RawTradeRecord> parseCsvFile(File csvFile) throws ETLException {
        // Delegate to TradeCsvParser utility
        return tradeCsvParser.parse(csvFile);
    }

    /**
     * Converts raw records to standardized {@link SourceDataModel} records.
     * 
     * <p>
     * Business rules:
     * <ol>
     * <li>Group records by {@code mqOffset}</li>
     * <li>For each group, create one {@link XbondTradeDataModel} instance</li>
     * <li>Map raw fields:
     * <ul>
     * <li>Add ".IB" suffix to {@code underlyingSecurityId} to form
     * {@code exchProductId}</li>
     * <li>Map {@code underlyingSettlementType} (0=T+0, 1=T+1) to
     * {@code settleSpeed}</li>
     * <li>Map {@code tradePrice}, {@code tradeYield}, {@code tradeVolume},
     * {@code tradeSide}, {@code tradeId}</li>
     * <li>Set {@code eventTime} = {@code transactTime}, {@code receiveTime} =
     * {@code recvTime}</li>
     * </ul>
     * </li>
     * <li>Set {@code businessDate} from context (YYYY.MM.DD format)</li>
     * </ol>
     * 
     * @param rawRecords list of raw records from all processed files
     * @return list of converted {@link SourceDataModel} records
     * @throws ETLException if conversion fails (data validation errors, etc.)
     */
    @Override
    protected List<SourceDataModel> convertRawRecords(List<RawTradeRecord> rawRecords) throws ETLException {
        if (rawRecords == null || rawRecords.isEmpty()) {
            return new ArrayList<>();
        }

        List<SourceDataModel> result = new ArrayList<>();

        // Convert each raw record to a data model (one-to-one mapping)
        for (RawTradeRecord record : rawRecords) {
            if (record == null) {
                continue;
            }

            // Create a new XbondTradeDataModel for each record
            XbondTradeDataModel model = new XbondTradeDataModel();

            // Map all fields from raw record to model
            mapRawRecordToModel(record, model);

            // Set timestamps
            model.setEventTime(record.getDealTime());

            // Set receiveTime: use recvTime if available, otherwise copy from dealTime
            if (record.getRecvTime() != null) {
                model.setReceiveTime(record.getRecvTime());
            } else {
                model.setReceiveTime(record.getDealTime());
            }

            // Set business date from stored currentBusinessDate (set by extract method)
            // If not available, fall back to dealTime
            if (currentBusinessDate != null) {
                model.setBusinessDate(currentBusinessDate);
            } else if (record.getDealTime() != null) {
                String businessDate = record.getDealTime()
                        .format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
                model.setBusinessDate(businessDate);
            }

            // Validate the model before adding to result
            if (model.validate()) {
                result.add(model);
            } else {
                LOG.warn("Skipping invalid trade data model. Validation failed: {}", model);
            }
        }

        return result;
    }

    /**
     * Maps a single raw record to the appropriate fields in the model.
     * 
     * @param record raw record containing trade data
     * @param model  model to update with mapped values
     */
    private void mapRawRecordToModel(RawTradeRecord record, XbondTradeDataModel model) {
        // Add ".IB" suffix to underlyingSecurityId to form exchProductId
        String securityId = record.getUnderlyingSecurityId();
        if (securityId != null && !securityId.endsWith(".IB")) {
            securityId = securityId + ".IB";
        }
        model.setExchProductId(securityId);

        // Map settlement type to settleSpeed (map from set_days string to integer)
        String setDays = record.getSetDays();
        if (setDays != null) {
            if (setDays.equals("T+1")) {
                model.setSettleSpeed(1);
            } else if (setDays.equals("T+0")) {
                model.setSettleSpeed(0);
            }
        }

        // Map trade-specific fields
        model.setTradePrice(record.getNetPrice());
        model.setTradeYield(record.getYield());
        model.setTradeYieldType(record.getYieldType());
        model.setTradeVolume(record.getDealSize());
        model.setTradeSide(mapTradeSide(record.getSide()));
        // Use CSV 'id' field as tradeId since both represent unique transaction identifier
        if (record.getId() != null) {
            model.setTradeId(String.valueOf(record.getId()));
        }
    }

    /**
     * Maps raw trade side values from CSV to standardized values.
     * 
     * @param rawSide raw side value from CSV (X/Y/Z/D)
     * @return mapped side value (TKN/GVN/TRD/DONE), or null if input is null
     */
    private String mapTradeSide(String rawSide) {
        if (rawSide == null) {
            return null;
        }

        switch (rawSide.trim().toUpperCase()) {
            case "X":
                return "TKN"; // Taken
            case "Y":
                return "GVN"; // Given
            case "Z":
                return "TRD"; // Trade
            case "D":
                return "DONE"; // Done
            default:
                // Return as-is if unknown value
                return rawSide;
        }
    }
}