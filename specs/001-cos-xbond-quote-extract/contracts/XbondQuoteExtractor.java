package com.sdd.etl.source.extract.cos;

import com.sdd.etl.ETLException;
import com.sdd.etl.context.ETLContext;
import com.sdd.etl.model.SourceDataModel;
import com.sdd.etl.source.extract.cos.model.RawQuoteRecord;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Concrete extractor for Xbond Quote data from COS.
 * 
 * <p>Extracts CSV files from the {@code AllPriceDepth} category and converts
 * raw quote records into standardized {@link SourceDataModel} format with
 * bid/offer price levels for bond trading data.</p>
 * 
 * <p><strong>Category</strong>: {@code AllPriceDepth}</p>
 * <p><strong>File Pattern</strong>: {@code /AllPriceDepth/{YYYYMMDD}/*.csv}</p>
 * <p><strong>Output Model</strong>: {@link XbondQuoteDataModel} (concrete implementation of {@link SourceDataModel})</p>
 */
public class XbondQuoteExtractor extends CosExtractor {
    
    /** Category identifier for file filtering */
    private static final String CATEGORY = "AllPriceDepth";
    
    /** Date format for COS file paths */
    private static final String DATE_FORMAT = "YYYYMMDD";
    
    /** Formatter for business date in file paths */
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    /** Formatter for output business date (YYYY.MM.DD) */
    private final DateTimeFormatter outputDateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    
    /**
     * Gets the category identifier for this extractor.
     * 
     * @return "AllPriceDepth"
     */
    @Override
    public String getCategory() {
        return CATEGORY;
    }
    
    /**
     * Gets the date format used in COS file paths.
     * 
     * @return "YYYYMMDD"
     */
    @Override
    protected String getBusinessDateFormat() {
        return DATE_FORMAT;
    }
    
    /**
     * Converts raw quote records to standardized Xbond quote data models.
     * 
     * <p>Processing steps:
     * <ol>
     *   <li>Group raw records by {@code mqOffset}</li>
     *   <li>For each group, create a new {@link XbondQuoteDataModel} instance</li>
     *   <li>Map raw fields based on price level and entry type</li>
     *   <li>Set constant values (product_type, exchange, source, level, status)</li>
     *   <li>Format dates and apply business rules (add ".IB" suffix, map settlement types)</li>
     * </ol>
     * 
     * <p><strong>Mapping Logic</strong>:
     * <ul>
     *   <li>Price level 1 (bid/offer) → Level 0 fields (bid_0_*, offer_0_*)</li>
     *   <li>Price level 2 (bid/offer) → Level 1 fields (bid_1_*, offer_1_*) with tradable_volume</li>
     *   <li>Price level 3-6 → Level 2-5 fields with tradable_volume</li>
     *   <li>Entry type 0 → bid fields</li>
     *   <li>Entry type 1 → offer fields</li>
     * </ul>
     * 
     * @param rawRecords list of raw records from all processed files
     * @return list of converted {@link SourceDataModel} records
     * @throws ETLException if conversion fails (data validation errors, etc.)
     */
    @Override
    protected List<SourceDataModel> convertRawRecords(List<RawQuoteRecord> rawRecords) 
            throws ETLException {
        
        // Group records by mq_offset (primary grouping key)
        Map<Long, List<RawQuoteRecord>> groupedByOffset = rawRecords.stream()
                .collect(Collectors.groupingBy(RawQuoteRecord::getMqOffset));
        
        List<SourceDataModel> result = new ArrayList<>();
        
        for (Map.Entry<Long, List<RawQuoteRecord>> group : groupedByOffset.entrySet()) {
            XbondQuoteDataModel dataModel = convertGroup(group.getValue());
            result.add(dataModel);
        }
        
        return result;
    }
    
    /**
     * Converts a group of raw records (same mq_offset) to a single data model.
     * 
     * @param groupRecords list of raw records with the same mq_offset
     * @return converted data model
     * @throws ETLException if conversion fails
     */
    private XbondQuoteDataModel convertGroup(List<RawQuoteRecord> groupRecords) throws ETLException {
        if (groupRecords == null || groupRecords.isEmpty()) {
            throw new ETLException("XbondQuoteExtractor", null,
                    "Cannot convert empty record group");
        }
        
        // Use first record for common fields
        RawQuoteRecord firstRecord = groupRecords.get(0);
        
        // Create data model instance
        XbondQuoteDataModel dataModel = new XbondQuoteDataModel();
        
        // Set metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source_type", "cos");
        metadata.put("category", CATEGORY);
        metadata.put("record_count", groupRecords.size());
        dataModel.setMetadata(metadata);
        
        // Set common fields (from first record, but validate consistency)
        setCommonFields(dataModel, firstRecord);
        
        // Map price level fields
        for (RawQuoteRecord record : groupRecords) {
            mapPriceLevelFields(dataModel, record);
        }
        
        // Validate the converted model
        if (!dataModel.validate()) {
            throw new ETLException("XbondQuoteExtractor", dataModel.getBusinessDate(),
                    "Converted data model failed validation for mq_offset: " + 
                    groupRecords.get(0).getMqOffset());
        }
        
        return dataModel;
    }
    
    /**
     * Sets common fields that are the same for all records in the group.
     * 
     * @param dataModel target data model to populate
     * @param record source raw record
     */
    private void setCommonFields(XbondQuoteDataModel dataModel, RawQuoteRecord record) {
        // Format business date from context (should be provided by caller)
        // In real implementation, this would come from ETLContext
        LocalDate businessDate = record.getTransactTime().toLocalDate();
        dataModel.setBusinessDate(businessDate.format(outputDateFormatter));
        
        // Set exch_product_id with ".IB" suffix if missing
        String securityId = record.getUnderlyingSecurityId();
        if (securityId != null && !securityId.endsWith(".IB")) {
            securityId = securityId + ".IB";
        }
        dataModel.setExchProductId(securityId);
        
        // Constant fields
        dataModel.setProductType("BOND");
        dataModel.setExchange("CFETS");
        dataModel.setSource("XBOND");
        
        // Map settlement type: 1→0 (T+0), 2→1 (T+1)
        Integer settlementType = record.getUnderlyingSettlementType();
        if (settlementType != null) {
            dataModel.setSettleSpeed(settlementType == 1 ? 0 : 1);
        }
        
        // Constant level and status
        dataModel.setLevel("L2");
        dataModel.setStatus("Normal");
        
        // Timestamps
        dataModel.setEventTime(record.getTransactTime());
        dataModel.setReceiveTime(record.getRecvTime());
    }
    
    /**
     * Maps raw record fields based on price level and entry type.
     * 
     * @param dataModel target data model to populate
     * @param record source raw record
     */
    private void mapPriceLevelFields(XbondQuoteDataModel dataModel, RawQuoteRecord record) {
        Integer priceLevel = record.getUnderlyingMdPriceLevel();
        Integer entryType = record.getUnderlyingMdEntryType();
        BigDecimal price = record.getUnderlyingMdEntryPx();
        BigDecimal yield = record.getUnderlyingYield();
        String yieldType = record.getUnderlyingYieldType();
        Long volume = record.getUnderlyingMdEntrySize();
        
        if (priceLevel == null || entryType == null || price == null) {
            // Skip records with missing required fields
            return;
        }
        
        // Map based on price level and entry type
        switch (priceLevel) {
            case 1: // Level 0 (best quote, may not be tradable)
                if (entryType == 0) { // Bid
                    dataModel.setBid0Price(price);
                    dataModel.setBid0Yield(yield);
                    dataModel.setBid0YieldType(yieldType);
                    dataModel.setBid0Volume(volume);
                } else if (entryType == 1) { // Offer
                    dataModel.setOffer0Price(price);
                    dataModel.setOffer0Yield(yield);
                    dataModel.setOffer0YieldType(yieldType);
                    dataModel.setOffer0Volume(volume);
                }
                break;
                
            case 2: // Level 1 (tradable)
                if (entryType == 0) { // Bid
                    dataModel.setBid1Price(price);
                    dataModel.setBid1Yield(yield);
                    dataModel.setBid1YieldType(yieldType);
                    dataModel.setBid1TradableVolume(volume);
                } else if (entryType == 1) { // Offer
                    dataModel.setOffer1Price(price);
                    dataModel.setOffer1Yield(yield);
                    dataModel.setOffer1YieldType(yieldType);
                    dataModel.setOffer1TradableVolume(volume);
                }
                break;
                
            case 3: // Level 2
                if (entryType == 0) {
                    dataModel.setBid2Price(price);
                    dataModel.setBid2Yield(yield);
                    dataModel.setBid2YieldType(yieldType);
                    dataModel.setBid2TradableVolume(volume);
                } else if (entryType == 1) {
                    dataModel.setOffer2Price(price);
                    dataModel.setOffer2Yield(yield);
                    dataModel.setOffer2YieldType(yieldType);
                    dataModel.setOffer2TradableVolume(volume);
                }
                break;
                
            case 4: // Level 3
                if (entryType == 0) {
                    dataModel.setBid3Price(price);
                    dataModel.setBid3Yield(yield);
                    dataModel.setBid3YieldType(yieldType);
                    dataModel.setBid3TradableVolume(volume);
                } else if (entry_type == 1) {
                    dataModel.setOffer3Price(price);
                    dataModel.setOffer3Yield(yield);
                    dataModel.setOffer3YieldType(yieldType);
                    dataModel.setOffer3TradableVolume(volume);
                }
                break;
                
            case 5: // Level 4
                if (entryType == 0) {
                    dataModel.setBid4Price(price);
                    dataModel.setBid4Yield(yield);
                    dataModel.setBid4YieldType(yieldType);
                    dataModel.setBid4TradableVolume(volume);
                } else if (entryType == 1) {
                    dataModel.setOffer4Price(price);
                    dataModel.setOffer4Yield(yield);
                    dataModel.setOffer4YieldType(yieldType);
                    dataModel.setOffer4TradableVolume(volume);
                }
                break;
                
            case 6: // Level 5
                if (entryType == 0) {
                    dataModel.setBid5Price(price);
                    dataModel.setBid5Yield(yield);
                    dataModel.setBid5YieldType(yieldType);
                    dataModel.setBid5TradableVolume(volume);
                } else if (entryType == 1) {
                    dataModel.setOffer5Price(price);
                    dataModel.setOffer5Yield(yield);
                    dataModel.setOffer5YieldType(yieldType);
                    dataModel.setOffer5TradableVolume(volume);
                }
                break;
                
            default:
                // Ignore unexpected price levels
                break;
        }
    }
    
    /**
     * Provides a descriptive name for this extractor instance.
     * 
     * @return descriptive name including category
     */
    @Override
    public String getName() {
        return String.format("XbondQuoteExtractor[%s]", CATEGORY);
    }
    
    /**
     * Validates extractor configuration and context.
     * 
     * <p>Additional validation for Xbond Quote specific requirements.</p>
     */
    @Override
    public void validate(ETLContext context) throws ETLException {
        // Call parent validation
        super.validate(context);
        
        // Validate business date format compatibility
        try {
            LocalDate businessDate = context.getCurrentDate();
            // Ensure date can be formatted as expected
            String formattedDate = businessDate.format(dateFormatter);
            // Basic validation of format
            if (formattedDate.length() != 8) {
                throw new ETLException("XbondQuoteExtractor", businessDate,
                        "Business date format must be YYYYMMDD, got: " + formattedDate);
            }
        } catch (Exception e) {
            throw new ETLException("XbondQuoteExtractor", context.getCurrentDate(),
                    "Invalid business date format: " + e.getMessage(), e);
        }
    }
}