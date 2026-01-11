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
import com.sdd.etl.source.extract.cos.CosClient;
import com.sdd.etl.source.extract.cos.client.CosClientImpl;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Concrete extractor for Xbond Trade data from Tencent COS.
 * 
 * <p>Handles:
 * <ol>
 *   <li>File selection based on category "TradeData" and business date</li>
 *   <li>CSV file parsing using {@link TradeCsvParser}</li>
 *   <li>Conversion of raw records to {@link XbondTradeDataModel} with business rules:
 *     <ul>
 *       <li>Group records by {@code mqOffset}</li>
 *       <li>Map raw trade fields (price, yield, volume, counterparty, trade ID)</li>
 *       <li>Add ".IB" suffix to underlyingSecurityId</li>
 *       <li>Map settlement types (0=T+0, 1=T+1) to settleSpeed</li>
 *     </ul>
 *   </li>
 * </ol>
 * 
 * <p>CSV format expectations:
 * <ul>
 *   <li>Header row with field names</li>
 *   <li>Columns in fixed order (id, underlying_security_id, underlying_settlement_type,
 *       trade_price, trade_yield, trade_yield_type, trade_volume, counterparty, trade_id,
 *       transact_time, mq_offset, recv_time)</li>
 *   <li>UTF-8 encoding</li>
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
     * @return "TradeData"
     */
    @Override
    public String getCategory() {
        return "TradeData";
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
        this.currentBusinessDate = dateStr.substring(0, 4) + "." + dateStr.substring(4, 6) + "." + dateStr.substring(6, 8);
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
     * <p>Business rules:
     * <ol>
     *   <li>Group records by {@code mqOffset}</li>
     *   <li>For each group, create one {@link XbondTradeDataModel} instance</li>
     *   <li>Map raw fields:
     *     <ul>
     *       <li>Add ".IB" suffix to {@code underlyingSecurityId} to form {@code exchProductId}</li>
     *       <li>Map {@code underlyingSettlementType} (0=T+0, 1=T+1) to {@code settleSpeed}</li>
     *       <li>Map {@code tradePrice}, {@code tradeYield}, {@code tradeVolume}, {@code counterparty}, {@code tradeId}</li>
     *       <li>Set {@code eventTime} = {@code transactTime}, {@code receiveTime} = {@code recvTime}</li>
     *     </ul>
     *   </li>
     *   <li>Set {@code businessDate} from context (YYYY.MM.DD format)</li>
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
        
        // Group records by mqOffset
        Map<Long, List<RawTradeRecord>> recordsByMqOffset = rawRecords.stream()
                .filter(record -> record != null && record.getMqOffset() != null)
                .collect(Collectors.groupingBy(RawTradeRecord::getMqOffset));
        
        List<SourceDataModel> result = new ArrayList<>();
        
        for (Map.Entry<Long, List<RawTradeRecord>> entry : recordsByMqOffset.entrySet()) {
            Long mqOffset = entry.getKey();
            List<RawTradeRecord> groupRecords = entry.getValue();
            
            // Create a new XbondTradeDataModel for this group
            XbondTradeDataModel model = new XbondTradeDataModel();
            
            // Process each raw record in the group
            // Note: For trades, typically each mqOffset group contains one trade record
            // but we iterate through all records in the group for consistency with quote pattern
            for (RawTradeRecord record : groupRecords) {
                // Apply business rules to map fields
                mapRawRecordToModel(record, model);
            }
            
            // Set common fields that are the same for all records in group
            // Use the first record for timestamps and security ID
            RawTradeRecord firstRecord = groupRecords.get(0);
            model.setEventTime(firstRecord.getTransactTime());
            model.setReceiveTime(firstRecord.getRecvTime());
            
            // Set business date from stored currentBusinessDate (set by extract method)
            // If not available, fall back to transactTime from first record
            if (currentBusinessDate != null) {
                model.setBusinessDate(currentBusinessDate);
            } else if (firstRecord.getTransactTime() != null) {
                String businessDate = firstRecord.getTransactTime()
                        .format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
                model.setBusinessDate(businessDate);
            }
            
            // Validate the model before adding to result
            if (model.validate()) {
                result.add(model);
            } else {
                LOG.warn("Skipping invalid trade data model for mq_offset={}. Validation failed: {}", mqOffset, model);
                // Continue processing other groups
            }
        }
        
        return result;
    }
    
    /**
     * Maps a single raw record to the appropriate fields in the model.
     * 
     * @param record raw record containing trade data
     * @param model model to update with mapped values
     */
    private void mapRawRecordToModel(RawTradeRecord record, XbondTradeDataModel model) {
        // Add ".IB" suffix to underlyingSecurityId to form exchProductId
        String securityId = record.getUnderlyingSecurityId();
        if (securityId != null && !securityId.endsWith(".IB")) {
            securityId = securityId + ".IB";
        }
        model.setExchProductId(securityId);
        
        // Map settlement type to settleSpeed (0=T+0, 1=T+1)
        Integer settlementType = record.getUnderlyingSettlementType();
        if (settlementType != null) {
            model.setSettleSpeed(settlementType);
        }
        
        // Map trade-specific fields
        model.setTradePrice(record.getTradePrice());
        model.setTradeYield(record.getTradeYield());
        model.setTradeYieldType(record.getTradeYieldType());
        model.setTradeVolume(record.getTradeVolume());
        model.setCounterparty(record.getCounterparty());
        model.setTradeId(record.getTradeId());
    }
}