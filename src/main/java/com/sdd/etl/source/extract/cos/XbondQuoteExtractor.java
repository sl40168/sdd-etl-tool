package com.sdd.etl.source.extract.cos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sdd.etl.util.DateUtils;
import com.sdd.etl.ETLException;
import com.sdd.etl.context.ETLContext;
import com.sdd.etl.model.SourceDataModel;
import com.sdd.etl.model.XbondQuoteDataModel;
import com.sdd.etl.source.extract.cos.config.CosSourceConfig;
import com.sdd.etl.source.extract.cos.model.RawQuoteRecord;
import com.sdd.etl.source.extract.cos.CosClient;
import com.sdd.etl.source.extract.cos.client.CosClientImpl;

import java.io.File;
import java.time.LocalDateTime;
import cn.hutool.core.date.LocalDateTimeUtil;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Concrete extractor for Xbond Quote data from Tencent COS.
 * 
 * <p>Handles:
 * <ol>
 *   <li>File selection based on category "AllPriceDepth" and business date</li>
 *   <li>CSV file parsing using {@link CsvParser}</li>
 *   <li>Conversion of raw records to {@link XbondQuoteDataModel} with business rules:
 *     <ul>
 *       <li>Group records by {@code mqOffset}</li>
 *       <li>Map price levels (0-5) to bid/offer fields</li>
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
 *       underlying_md_entry_type, underlying_md_entry_px, underlying_md_price_level,
 *       underlying_md_entry_size, underlying_yield_type, underlying_yield,
 *       transact_time, mq_offset, recv_time)</li>
 *   <li>UTF-8 encoding</li>
 * </ul>
 */
public class XbondQuoteExtractor extends CosExtractor<RawQuoteRecord> {
    
    /** Logger instance */
    private static final Logger LOG = LoggerFactory.getLogger(XbondQuoteExtractor.class);
    
    /** CsvParser instance for parsing CSV files */
    private CsvParser csvParser;
    
    /** Current business date in YYYY.MM.DD format */
    private String currentBusinessDate;
    
    /**
     * Default constructor.
     */
    public XbondQuoteExtractor() {
        super();
        this.csvParser = new CsvParser();
    }
    
    /**
     * Gets the category identifier for file filtering.
     * 
     * @return "AllPriceDepth"
     */
    @Override
    public String getCategory() {
        return "AllPriceDepth";
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
            throw new ETLException("XBOND_QUOTE_EXTRACT", dateStr,
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
        // Use the Tencent COS SDK implementation
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
    protected List<RawQuoteRecord> parseCsvFile(File csvFile) throws ETLException {
        // Delegate to CsvParser utility
        return csvParser.parse(csvFile);
    }
    
    /**
     * Converts raw records to standardized {@link SourceDataModel} records.
     * 
     * <p>Business rules:
     * <ol>
     *   <li>Group records by {@code mqOffset}</li>
     *   <li>For each group, create one {@link XbondQuoteDataModel} instance</li>
     *   <li>Map raw fields based on {@code underlyingMdEntryType} (0=bid, 1=offer)
     *       and {@code underlyingMdPriceLevel} (0-5) to corresponding bid/offer fields</li>
     *   <li>Add ".IB" suffix to {@code underlyingSecurityId} to form {@code exchProductId}</li>
     *   <li>Map {@code underlyingSettlementType} (0=T+0, 1=T+1) to {@code settleSpeed}</li>
     *   <li>Set {@code eventTime} = {@code transactTime}, {@code receiveTime} = {@code recvTime}</li>
     *   <li>Set {@code businessDate} from context (YYYY.MM.DD format)</li>
     * </ol>
     * 
     * @param rawRecords list of raw records from all processed files
     * @return list of converted {@link SourceDataModel} records
     * @throws ETLException if conversion fails (data validation errors, etc.)
     */
    @Override
    protected List<SourceDataModel> convertRawRecords(List<RawQuoteRecord> rawRecords) throws ETLException {
        if (rawRecords == null || rawRecords.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Group records by mqOffset
        Map<Long, List<RawQuoteRecord>> recordsByMqOffset = rawRecords.stream()
                .filter(record -> record != null && record.getMqOffset() != null)
                .collect(Collectors.groupingBy(RawQuoteRecord::getMqOffset));
        
        List<SourceDataModel> result = new ArrayList<>();
        
        for (Map.Entry<Long, List<RawQuoteRecord>> entry : recordsByMqOffset.entrySet()) {
            Long mqOffset = entry.getKey();
            List<RawQuoteRecord> groupRecords = entry.getValue();
            
            // Create a new XbondQuoteDataModel for this group
            XbondQuoteDataModel model = new XbondQuoteDataModel();
            
            // Process each raw record in the group
            for (RawQuoteRecord record : groupRecords) {
                // Apply business rules to map fields
                mapRawRecordToModel(record, model);
            }
            
            // Set common fields that are the same for all records in group
            // Use the first record for timestamps and security ID
            RawQuoteRecord firstRecord = groupRecords.get(0);
            model.setEventTime(firstRecord.getTransactTime());
            model.setReceiveTime(firstRecord.getRecvTime());
            
            // Set business date from stored currentBusinessDate (set by extract method)
            // If not available, fall back to transactTime from first record
            if (currentBusinessDate != null) {
                model.setBusinessDate(currentBusinessDate);
            } else if (firstRecord.getTransactTime() != null) {
                String businessDate = LocalDateTimeUtil.format(firstRecord.getTransactTime(), "yyyy.MM.dd");
                model.setBusinessDate(businessDate);
            }
            
            // Validate the model before adding to result
            if (model.validate()) {
                result.add(model);
            } else {
                LOG.warn("Skipping invalid data model for mq_offset={}. Validation failed: {}", mqOffset, model);
                // Continue processing other groups
            }
        }
        
        return result;
    }
    
    /**
     * Maps a single raw record to the appropriate fields in the model.
     * 
     * @param record raw record containing price/yield/volume data
     * @param model model to update with mapped values
     */
    private void mapRawRecordToModel(RawQuoteRecord record, XbondQuoteDataModel model) {
        // Add ".IB" suffix to underlyingSecurityId to form exchProductId
        String securityId = record.getUnderlyingSecurityId();
        if (securityId != null && !securityId.endsWith(".IB")) {
            securityId = securityId + ".IB";
        }
        model.setExchProductId(securityId);
        
        // Map settlement type to settleSpeed (1=T+0, 2=T+1) -> (0, 1)
        // According to Plan.md: 1->0, 2->1
        Integer settlementType = record.getUnderlyingSettlementType();
        if (settlementType != null) {
            // Map 1->0, 2->1, 3->2, etc. (subtract 1)
            model.setSettleSpeed(settlementType - 1);
        }
        
        // Get price level (1-6 from source, needs to map to 0-5 in output)
        Integer priceLevel = record.getUnderlyingMdPriceLevel();
        if (priceLevel == null) {
            return;
        }
        
        // Get entry type (0=bid, 1=offer)
        Integer entryType = record.getUnderlyingMdEntryType();
        if (entryType == null) {
            return;
        }
        
        // Convert source price level (1-6) to output level (0-5)
        // source_level 1 -> output_level 0
        // source_level 2 -> output_level 1
        // source_level 3 -> output_level 2
        // source_level 4 -> output_level 3
        // source_level 5 -> output_level 4
        // source_level 6 -> output_level 5
        int outputLevel = priceLevel - 1;
        
        // Map based on entry type and output level
        if (entryType == 0) { // bid
            mapBidFields(record, model, outputLevel);
        } else if (entryType == 1) { // offer
            mapOfferFields(record, model, outputLevel);
        }
    }
    
    /**
     * Maps bid fields to appropriate level in model.
     * 
     * @param record raw record
     * @param model model to update
     * @param priceLevel 0-5
     */
    private void mapBidFields(RawQuoteRecord record, XbondQuoteDataModel model, int priceLevel) {
        switch (priceLevel) {
            case 0:
                model.setBid0Price(record.getUnderlyingMdEntryPx());
                model.setBid0Yield(record.getUnderlyingYield());
                model.setBid0YieldType(record.getUnderlyingYieldType());
                model.setBid0Volume(record.getUnderlyingMdEntrySize());
                break;
            case 1:
                model.setBid1Price(record.getUnderlyingMdEntryPx());
                model.setBid1Yield(record.getUnderlyingYield());
                model.setBid1YieldType(record.getUnderlyingYieldType());
                model.setBid1TradableVolume(record.getUnderlyingMdEntrySize());
                break;
            case 2:
                model.setBid2Price(record.getUnderlyingMdEntryPx());
                model.setBid2Yield(record.getUnderlyingYield());
                model.setBid2YieldType(record.getUnderlyingYieldType());
                model.setBid2TradableVolume(record.getUnderlyingMdEntrySize());
                break;
            case 3:
                model.setBid3Price(record.getUnderlyingMdEntryPx());
                model.setBid3Yield(record.getUnderlyingYield());
                model.setBid3YieldType(record.getUnderlyingYieldType());
                model.setBid3TradableVolume(record.getUnderlyingMdEntrySize());
                break;
            case 4:
                model.setBid4Price(record.getUnderlyingMdEntryPx());
                model.setBid4Yield(record.getUnderlyingYield());
                model.setBid4YieldType(record.getUnderlyingYieldType());
                model.setBid4TradableVolume(record.getUnderlyingMdEntrySize());
                break;
            case 5:
                model.setBid5Price(record.getUnderlyingMdEntryPx());
                model.setBid5Yield(record.getUnderlyingYield());
                model.setBid5YieldType(record.getUnderlyingYieldType());
                model.setBid5TradableVolume(record.getUnderlyingMdEntrySize());
                break;
            default:
                // Ignore invalid price level
                break;
        }
    }
    
    /**
     * Maps offer fields to appropriate level in model.
     * 
     * @param record raw record
     * @param model model to update
     * @param priceLevel 0-5
     */
    private void mapOfferFields(RawQuoteRecord record, XbondQuoteDataModel model, int priceLevel) {
        switch (priceLevel) {
            case 0:
                model.setOffer0Price(record.getUnderlyingMdEntryPx());
                model.setOffer0Yield(record.getUnderlyingYield());
                model.setOffer0YieldType(record.getUnderlyingYieldType());
                model.setOffer0Volume(record.getUnderlyingMdEntrySize());
                break;
            case 1:
                model.setOffer1Price(record.getUnderlyingMdEntryPx());
                model.setOffer1Yield(record.getUnderlyingYield());
                model.setOffer1YieldType(record.getUnderlyingYieldType());
                model.setOffer1TradableVolume(record.getUnderlyingMdEntrySize());
                break;
            case 2:
                model.setOffer2Price(record.getUnderlyingMdEntryPx());
                model.setOffer2Yield(record.getUnderlyingYield());
                model.setOffer2YieldType(record.getUnderlyingYieldType());
                model.setOffer2TradableVolume(record.getUnderlyingMdEntrySize());
                break;
            case 3:
                model.setOffer3Price(record.getUnderlyingMdEntryPx());
                model.setOffer3Yield(record.getUnderlyingYield());
                model.setOffer3YieldType(record.getUnderlyingYieldType());
                model.setOffer3TradableVolume(record.getUnderlyingMdEntrySize());
                break;
            case 4:
                model.setOffer4Price(record.getUnderlyingMdEntryPx());
                model.setOffer4Yield(record.getUnderlyingYield());
                model.setOffer4YieldType(record.getUnderlyingYieldType());
                model.setOffer4TradableVolume(record.getUnderlyingMdEntrySize());
                break;
            case 5:
                model.setOffer5Price(record.getUnderlyingMdEntryPx());
                model.setOffer5Yield(record.getUnderlyingYield());
                model.setOffer5YieldType(record.getUnderlyingYieldType());
                model.setOffer5TradableVolume(record.getUnderlyingMdEntrySize());
                break;
            default:
                // Ignore invalid price level
                break;
        }
    }
}