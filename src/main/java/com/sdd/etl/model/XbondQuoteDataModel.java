package com.sdd.etl.model;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Concrete implementation of {@link SourceDataModel} for Xbond Quote data.
 * 
 * <p>Represents standardized bond quote records extracted from COS CSV files.
 * Contains bid and offer price levels (0-5) with associated yields, volumes,
 * and timestamps.</p>
 * 
 * <p><strong>Note</strong>: Level 0 fields (bid_0_*, offer_0_*) represent the
 * best quotes in the global market but may not be tradable. Levels 1-5 fields
 * represent tradable quotes with corresponding tradable volumes.</p>
 */
public class XbondQuoteDataModel extends SourceDataModel {
    
    // --- Common Fields ---
    private String businessDate;
    private String exchProductId;
    private String productType;
    private String exchange;
    private String source;
    private Integer settleSpeed;
    private String level;
    private String status;
    
    // --- Timestamps ---
    private LocalDateTime eventTime;
    private LocalDateTime receiveTime;
    
    // --- Level 0 Fields (best quotes, may not be tradable) ---
    private Double bid0Price;
    private Double bid0Yield;
    private String bid0YieldType;
    private Long bid0Volume;
    
    private Double offer0Price;
    private Double offer0Yield;
    private String offer0YieldType;
    private Long offer0Volume;
    
    // --- Level 1 Fields (tradable) ---
    private Double bid1Price;
    private Double bid1Yield;
    private String bid1YieldType;
    private Long bid1TradableVolume;
    
    private Double offer1Price;
    private Double offer1Yield;
    private String offer1YieldType;
    private Long offer1TradableVolume;
    
    // --- Level 2 Fields (tradable) ---
    private Double bid2Price;
    private Double bid2Yield;
    private String bid2YieldType;
    private Long bid2TradableVolume;
    
    private Double offer2Price;
    private Double offer2Yield;
    private String offer2YieldType;
    private Long offer2TradableVolume;
    
    // --- Level 3 Fields (tradable) ---
    private Double bid3Price;
    private Double bid3Yield;
    private String bid3YieldType;
    private Long bid3TradableVolume;
    
    private Double offer3Price;
    private Double offer3Yield;
    private String offer3YieldType;
    private Long offer3TradableVolume;
    
    // --- Level 4 Fields (tradable) ---
    private Double bid4Price;
    private Double bid4Yield;
    private String bid4YieldType;
    private Long bid4TradableVolume;
    
    private Double offer4Price;
    private Double offer4Yield;
    private String offer4YieldType;
    private Long offer4TradableVolume;
    
    // --- Level 5 Fields (tradable) ---
    private Double bid5Price;
    private Double bid5Yield;
    private String bid5YieldType;
    private Long bid5TradableVolume;
    
    private Double offer5Price;
    private Double offer5Yield;
    private String offer5YieldType;
    private Long offer5TradableVolume;
    
    // --- Constructors ---
    
    /**
     * Constructs an empty XbondQuoteDataModel.
     */
    public XbondQuoteDataModel() {
        super();
        // Initialize with defaults for required fields
        this.productType = "BOND";
        this.exchange = "CFETS";
        this.source = "XBOND";
        this.level = "L2";
        this.status = "Normal";
        
        // Initialize Double fields to NaN to indicate unassigned values
        this.bid0Price = Double.NaN;
        this.bid0Yield = Double.NaN;
        this.offer0Price = Double.NaN;
        this.offer0Yield = Double.NaN;
        
        this.bid1Price = Double.NaN;
        this.bid1Yield = Double.NaN;
        this.offer1Price = Double.NaN;
        this.offer1Yield = Double.NaN;
        
        this.bid2Price = Double.NaN;
        this.bid2Yield = Double.NaN;
        this.offer2Price = Double.NaN;
        this.offer2Yield = Double.NaN;
        
        this.bid3Price = Double.NaN;
        this.bid3Yield = Double.NaN;
        this.offer3Price = Double.NaN;
        this.offer3Yield = Double.NaN;
        
        this.bid4Price = Double.NaN;
        this.bid4Yield = Double.NaN;
        this.offer4Price = Double.NaN;
        this.offer4Yield = Double.NaN;
        
        this.bid5Price = Double.NaN;
        this.bid5Yield = Double.NaN;
        this.offer5Price = Double.NaN;
        this.offer5Yield = Double.NaN;
    }
    
    // --- Abstract Method Implementations ---
    
    /**
     * Validates data integrity and completeness.
     * 
     * <p>Validation rules:
     * <ol>
     *   <li>businessDate must be non-null and match pattern YYYY.MM.DD</li>
     *   <li>exchProductId must be non-null and end with ".IB"</li>
     *   <li>settleSpeed must be 0 or 1</li>
     *   <li>eventTime must be non-null</li>
     *   <li>receiveTime must be non-null</li>
     *   <li>At least one bid or offer field must be non-null</li>
     * </ol>
     * 
     * @return true if data is valid, false otherwise
     */
    @Override
    public boolean validate() {
        // Validate required common fields
        if (businessDate == null || !businessDate.matches("\\d{4}\\.\\d{2}\\.\\d{2}")) {
            return false;
        }
        
        if (exchProductId == null || !exchProductId.endsWith(".IB")) {
            return false;
        }
        
        if (settleSpeed == null || (settleSpeed != 0 && settleSpeed != 1)) {
            return false;
        }
        
        if (eventTime == null || receiveTime == null) {
            return false;
        }
        
        // Validate at least one quote field is populated (non-null and not NaN)
        boolean hasBidFields = (bid0Price != null && !Double.isNaN(bid0Price)) ||
                              (bid1Price != null && !Double.isNaN(bid1Price)) ||
                              (bid2Price != null && !Double.isNaN(bid2Price)) ||
                              (bid3Price != null && !Double.isNaN(bid3Price)) ||
                              (bid4Price != null && !Double.isNaN(bid4Price)) ||
                              (bid5Price != null && !Double.isNaN(bid5Price));
        boolean hasOfferFields = (offer0Price != null && !Double.isNaN(offer0Price)) ||
                                (offer1Price != null && !Double.isNaN(offer1Price)) ||
                                (offer2Price != null && !Double.isNaN(offer2Price)) ||
                                (offer3Price != null && !Double.isNaN(offer3Price)) ||
                                (offer4Price != null && !Double.isNaN(offer4Price)) ||
                                (offer5Price != null && !Double.isNaN(offer5Price));
        
        if (!hasBidFields && !hasOfferFields) {
            return false;
        }
        
        // Validate price fields are positive
        if (bid0Price != null && !Double.isNaN(bid0Price) && bid0Price <= 0.0) return false;
        if (bid1Price != null && !Double.isNaN(bid1Price) && bid1Price <= 0.0) return false;
        if (bid2Price != null && !Double.isNaN(bid2Price) && bid2Price <= 0.0) return false;
        if (bid3Price != null && !Double.isNaN(bid3Price) && bid3Price <= 0.0) return false;
        if (bid4Price != null && !Double.isNaN(bid4Price) && bid4Price <= 0.0) return false;
        if (bid5Price != null && !Double.isNaN(bid5Price) && bid5Price <= 0.0) return false;
        
        if (offer0Price != null && !Double.isNaN(offer0Price) && offer0Price <= 0.0) return false;
        if (offer1Price != null && !Double.isNaN(offer1Price) && offer1Price <= 0.0) return false;
        if (offer2Price != null && !Double.isNaN(offer2Price) && offer2Price <= 0.0) return false;
        if (offer3Price != null && !Double.isNaN(offer3Price) && offer3Price <= 0.0) return false;
        if (offer4Price != null && !Double.isNaN(offer4Price) && offer4Price <= 0.0) return false;
        if (offer5Price != null && !Double.isNaN(offer5Price) && offer5Price <= 0.0) return false;
        
        // Validate volumes are non-negative
        if (bid0Volume != null && bid0Volume < 0) return false;
        if (bid1TradableVolume != null && bid1TradableVolume < 0) return false;
        if (bid2TradableVolume != null && bid2TradableVolume < 0) return false;
        if (bid3TradableVolume != null && bid3TradableVolume < 0) return false;
        if (bid4TradableVolume != null && bid4TradableVolume < 0) return false;
        if (bid5TradableVolume != null && bid5TradableVolume < 0) return false;
        
        if (offer0Volume != null && offer0Volume < 0) return false;
        if (offer1TradableVolume != null && offer1TradableVolume < 0) return false;
        if (offer2TradableVolume != null && offer2TradableVolume < 0) return false;
        if (offer3TradableVolume != null && offer3TradableVolume < 0) return false;
        if (offer4TradableVolume != null && offer4TradableVolume < 0) return false;
        if (offer5TradableVolume != null && offer5TradableVolume < 0) return false;
        
        return true;
    }
    
    /**
     * Gets primary key value for this data model.
     * 
     * <p>Primary key consists of: businessDate + ":" + exchProductId + ":" + eventTime</p>
     * 
     * @return composite primary key string, or null if required fields missing
     */
    @Override
    public Object getPrimaryKey() {
        if (businessDate == null || exchProductId == null || eventTime == null) {
            return null;
        }
        return String.format("%s:%s:%s", businessDate, exchProductId, 
                eventTime.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }
    
    /**
     * Gets source type identifier.
     * 
     * @return "xbond_quote_cos"
     */
    @Override
    public String getSourceType() {
        return "xbond_quote_cos";
    }
    
    // --- Getters and Setters ---
    
    // Common Fields
    public String getBusinessDate() { return businessDate; }
    public void setBusinessDate(String businessDate) { this.businessDate = businessDate; }
    
    public String getExchProductId() { return exchProductId; }
    public void setExchProductId(String exchProductId) { this.exchProductId = exchProductId; }
    
    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }
    
    public String getExchange() { return exchange; }
    public void setExchange(String exchange) { this.exchange = exchange; }
    
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    
    public Integer getSettleSpeed() { return settleSpeed; }
    public void setSettleSpeed(Integer settleSpeed) { this.settleSpeed = settleSpeed; }
    
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    // Timestamps
    public LocalDateTime getEventTime() { return eventTime; }
    public void setEventTime(LocalDateTime eventTime) { this.eventTime = eventTime; }
    
    public LocalDateTime getReceiveTime() { return receiveTime; }
    public void setReceiveTime(LocalDateTime receiveTime) { this.receiveTime = receiveTime; }
    
    // Level 0 Fields
    public Double getBid0Price() { return bid0Price; }
    public void setBid0Price(Double bid0Price) { this.bid0Price = bid0Price; }
    
    public Double getBid0Yield() { return bid0Yield; }
    public void setBid0Yield(Double bid0Yield) { this.bid0Yield = bid0Yield; }
    
    public String getBid0YieldType() { return bid0YieldType; }
    public void setBid0YieldType(String bid0YieldType) { this.bid0YieldType = bid0YieldType; }
    
    public Long getBid0Volume() { return bid0Volume; }
    public void setBid0Volume(Long bid0Volume) { this.bid0Volume = bid0Volume; }
    
    public Double getOffer0Price() { return offer0Price; }
    public void setOffer0Price(Double offer0Price) { this.offer0Price = offer0Price; }
    
    public Double getOffer0Yield() { return offer0Yield; }
    public void setOffer0Yield(Double offer0Yield) { this.offer0Yield = offer0Yield; }
    
    public String getOffer0YieldType() { return offer0YieldType; }
    public void setOffer0YieldType(String offer0YieldType) { this.offer0YieldType = offer0YieldType; }
    
    public Long getOffer0Volume() { return offer0Volume; }
    public void setOffer0Volume(Long offer0Volume) { this.offer0Volume = offer0Volume; }
    
    // Level 1 Fields
    public Double getBid1Price() { return bid1Price; }
    public void setBid1Price(Double bid1Price) { this.bid1Price = bid1Price; }
    
    public Double getBid1Yield() { return bid1Yield; }
    public void setBid1Yield(Double bid1Yield) { this.bid1Yield = bid1Yield; }
    
    public String getBid1YieldType() { return bid1YieldType; }
    public void setBid1YieldType(String bid1YieldType) { this.bid1YieldType = bid1YieldType; }
    
    public Long getBid1TradableVolume() { return bid1TradableVolume; }
    public void setBid1TradableVolume(Long bid1TradableVolume) { this.bid1TradableVolume = bid1TradableVolume; }
    
    public Double getOffer1Price() { return offer1Price; }
    public void setOffer1Price(Double offer1Price) { this.offer1Price = offer1Price; }
    
    public Double getOffer1Yield() { return offer1Yield; }
    public void setOffer1Yield(Double offer1Yield) { this.offer1Yield = offer1Yield; }
    
    public String getOffer1YieldType() { return offer1YieldType; }
    public void setOffer1YieldType(String offer1YieldType) { this.offer1YieldType = offer1YieldType; }
    
    public Long getOffer1TradableVolume() { return offer1TradableVolume; }
    public void setOffer1TradableVolume(Long offer1TradableVolume) { this.offer1TradableVolume = offer1TradableVolume; }
    
    // Level 2 Fields
    public Double getBid2Price() { return bid2Price; }
    public void setBid2Price(Double bid2Price) { this.bid2Price = bid2Price; }
    
    public Double getBid2Yield() { return bid2Yield; }
    public void setBid2Yield(Double bid2Yield) { this.bid2Yield = bid2Yield; }
    
    public String getBid2YieldType() { return bid2YieldType; }
    public void setBid2YieldType(String bid2YieldType) { this.bid2YieldType = bid2YieldType; }
    
    public Long getBid2TradableVolume() { return bid2TradableVolume; }
    public void setBid2TradableVolume(Long bid2TradableVolume) { this.bid2TradableVolume = bid2TradableVolume; }
    
    public Double getOffer2Price() { return offer2Price; }
    public void setOffer2Price(Double offer2Price) { this.offer2Price = offer2Price; }
    
    public Double getOffer2Yield() { return offer2Yield; }
    public void setOffer2Yield(Double offer2Yield) { this.offer2Yield = offer2Yield; }
    
    public String getOffer2YieldType() { return offer2YieldType; }
    public void setOffer2YieldType(String offer2YieldType) { this.offer2YieldType = offer2YieldType; }
    
    public Long getOffer2TradableVolume() { return offer2TradableVolume; }
    public void setOffer2TradableVolume(Long offer2TradableVolume) { this.offer2TradableVolume = offer2TradableVolume; }
    
    // Level 3 Fields
    public Double getBid3Price() { return bid3Price; }
    public void setBid3Price(Double bid3Price) { this.bid3Price = bid3Price; }
    
    public Double getBid3Yield() { return bid3Yield; }
    public void setBid3Yield(Double bid3Yield) { this.bid3Yield = bid3Yield; }
    
    public String getBid3YieldType() { return bid3YieldType; }
    public void setBid3YieldType(String bid3YieldType) { this.bid3YieldType = bid3YieldType; }
    
    public Long getBid3TradableVolume() { return bid3TradableVolume; }
    public void setBid3TradableVolume(Long bid3TradableVolume) { this.bid3TradableVolume = bid3TradableVolume; }
    
    public Double getOffer3Price() { return offer3Price; }
    public void setOffer3Price(Double offer3Price) { this.offer3Price = offer3Price; }
    
    public Double getOffer3Yield() { return offer3Yield; }
    public void setOffer3Yield(Double offer3Yield) { this.offer3Yield = offer3Yield; }
    
    public String getOffer3YieldType() { return offer3YieldType; }
    public void setOffer3YieldType(String offer3YieldType) { this.offer3YieldType = offer3YieldType; }
    
    public Long getOffer3TradableVolume() { return offer3TradableVolume; }
    public void setOffer3TradableVolume(Long offer3TradableVolume) { this.offer3TradableVolume = offer3TradableVolume; }
    
    // Level 4 Fields
    public Double getBid4Price() { return bid4Price; }
    public void setBid4Price(Double bid4Price) { this.bid4Price = bid4Price; }
    
    public Double getBid4Yield() { return bid4Yield; }
    public void setBid4Yield(Double bid4Yield) { this.bid4Yield = bid4Yield; }
    
    public String getBid4YieldType() { return bid4YieldType; }
    public void setBid4YieldType(String bid4YieldType) { this.bid4YieldType = bid4YieldType; }
    
    public Long getBid4TradableVolume() { return bid4TradableVolume; }
    public void setBid4TradableVolume(Long bid4TradableVolume) { this.bid4TradableVolume = bid4TradableVolume; }
    
    public Double getOffer4Price() { return offer4Price; }
    public void setOffer4Price(Double offer4Price) { this.offer4Price = offer4Price; }
    
    public Double getOffer4Yield() { return offer4Yield; }
    public void setOffer4Yield(Double offer4Yield) { this.offer4Yield = offer4Yield; }
    
    public String getOffer4YieldType() { return offer4YieldType; }
    public void setOffer4YieldType(String offer4YieldType) { this.offer4YieldType = offer4YieldType; }
    
    public Long getOffer4TradableVolume() { return offer4TradableVolume; }
    public void setOffer4TradableVolume(Long offer4TradableVolume) { this.offer4TradableVolume = offer4TradableVolume; }
    
    // Level 5 Fields
    public Double getBid5Price() { return bid5Price; }
    public void setBid5Price(Double bid5Price) { this.bid5Price = bid5Price; }
    
    public Double getBid5Yield() { return bid5Yield; }
    public void setBid5Yield(Double bid5Yield) { this.bid5Yield = bid5Yield; }
    
    public String getBid5YieldType() { return bid5YieldType; }
    public void setBid5YieldType(String bid5YieldType) { this.bid5YieldType = bid5YieldType; }
    
    public Long getBid5TradableVolume() { return bid5TradableVolume; }
    public void setBid5TradableVolume(Long bid5TradableVolume) { this.bid5TradableVolume = bid5TradableVolume; }
    
    public Double getOffer5Price() { return offer5Price; }
    public void setOffer5Price(Double offer5Price) { this.offer5Price = offer5Price; }
    
    public Double getOffer5Yield() { return offer5Yield; }
    public void setOffer5Yield(Double offer5Yield) { this.offer5Yield = offer5Yield; }
    
    public String getOffer5YieldType() { return offer5YieldType; }
    public void setOffer5YieldType(String offer5YieldType) { this.offer5YieldType = offer5YieldType; }
    
    public Long getOffer5TradableVolume() { return offer5TradableVolume; }
    public void setOffer5TradableVolume(Long offer5TradableVolume) { this.offer5TradableVolume = offer5TradableVolume; }
    
    // --- Utility Methods ---
    
    /**
     * Gets a summary of quote levels with non-null prices.
     * 
     * @return map of level -> bid/offer price summary
     */
    public Map<String, String> getQuoteSummary() {
        Map<String, String> summary = new java.util.HashMap<>();
        
        if (bid0Price != null) summary.put("bid_0", String.valueOf(bid0Price));
        if (offer0Price != null) summary.put("offer_0", String.valueOf(offer0Price));
        if (bid1Price != null) summary.put("bid_1", String.valueOf(bid1Price));
        if (offer1Price != null) summary.put("offer_1", String.valueOf(offer1Price));
        if (bid2Price != null) summary.put("bid_2", String.valueOf(bid2Price));
        if (offer2Price != null) summary.put("offer_2", String.valueOf(offer2Price));
        if (bid3Price != null) summary.put("bid_3", String.valueOf(bid3Price));
        if (offer3Price != null) summary.put("offer_3", String.valueOf(offer3Price));
        if (bid4Price != null) summary.put("bid_4", String.valueOf(bid4Price));
        if (offer4Price != null) summary.put("offer_4", String.valueOf(offer4Price));
        if (bid5Price != null) summary.put("bid_5", String.valueOf(bid5Price));
        if (offer5Price != null) summary.put("offer_5", String.valueOf(offer5Price));
        
        return summary;
    }
    
    /**
     * Gets the total tradable volume across all bid levels.
     * 
     * @return sum of bid1-bid5 tradable volumes, or 0 if none
     */
    public Long getTotalBidTradableVolume() {
        long total = 0;
        if (bid1TradableVolume != null) total += bid1TradableVolume;
        if (bid2TradableVolume != null) total += bid2TradableVolume;
        if (bid3TradableVolume != null) total += bid3TradableVolume;
        if (bid4TradableVolume != null) total += bid4TradableVolume;
        if (bid5TradableVolume != null) total += bid5TradableVolume;
        return total;
    }
    
    /**
     * Gets the total tradable volume across all offer levels.
     * 
     * @return sum of offer1-offer5 tradable volumes, or 0 if none
     */
    public Long getTotalOfferTradableVolume() {
        long total = 0;
        if (offer1TradableVolume != null) total += offer1TradableVolume;
        if (offer2TradableVolume != null) total += offer2TradableVolume;
        if (offer3TradableVolume != null) total += offer3TradableVolume;
        if (offer4TradableVolume != null) total += offer4TradableVolume;
        if (offer5TradableVolume != null) total += offer5TradableVolume;
        return total;
    }
}