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
    private int settleSpeed;
    private String level;
    private String status;
    
    // --- Timestamps ---
    private LocalDateTime eventTime;
    private LocalDateTime receiveTime;
    
    // --- Level 0 Fields (best quotes, may not be tradable) ---
    private double bid0Price;
    private double bid0Yield;
    private String bid0YieldType;
    private double bid0Volume;
    
    private double offer0Price;
    private double offer0Yield;
    private String offer0YieldType;
    private double offer0Volume;
    
    // --- Level 1 Fields (tradable) ---
    private double bid1Price;
    private double bid1Yield;
    private String bid1YieldType;
    private double bid1TradableVolume;
    
    private double offer1Price;
    private double offer1Yield;
    private String offer1YieldType;
    private double offer1TradableVolume;
    
    // --- Level 2 Fields (tradable) ---
    private double bid2Price;
    private double bid2Yield;
    private String bid2YieldType;
    private double bid2TradableVolume;
    
    private double offer2Price;
    private double offer2Yield;
    private String offer2YieldType;
    private double offer2TradableVolume;
    
    // --- Level 3 Fields (tradable) ---
    private double bid3Price;
    private double bid3Yield;
    private String bid3YieldType;
    private double bid3TradableVolume;
    
    private double offer3Price;
    private double offer3Yield;
    private String offer3YieldType;
    private double offer3TradableVolume;
    
    // --- Level 4 Fields (tradable) ---
    private double bid4Price;
    private double bid4Yield;
    private String bid4YieldType;
    private double bid4TradableVolume;
    
    private double offer4Price;
    private double offer4Yield;
    private String offer4YieldType;
    private double offer4TradableVolume;
    
    // --- Level 5 Fields (tradable) ---
    private double bid5Price;
    private double bid5Yield;
    private String bid5YieldType;
    private double bid5TradableVolume;
    
    private double offer5Price;
    private double offer5Yield;
    private String offer5YieldType;
    private double offer5TradableVolume;
    
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
        
        // Initialize double fields to NaN to indicate unassigned values
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
        
        if ((settleSpeed != 0 && settleSpeed != 1)) {
            return false;
        }
        
        if (eventTime == null || receiveTime == null) {
            return false;
        }
        
        // Validate at least one quote field is populated (non-null and not NaN)
        boolean hasBidFields = (!Double.isNaN(bid0Price)) ||
                              (!Double.isNaN(bid1Price)) ||
                              (!Double.isNaN(bid2Price)) ||
                              (!Double.isNaN(bid3Price)) ||
                              (!Double.isNaN(bid4Price)) ||
                              (!Double.isNaN(bid5Price));
        boolean hasOfferFields = (!Double.isNaN(offer0Price)) ||
                                (!Double.isNaN(offer1Price)) ||
                                (!Double.isNaN(offer2Price)) ||
                                (!Double.isNaN(offer3Price)) ||
                                (!Double.isNaN(offer4Price)) ||
                                (!Double.isNaN(offer5Price));
        
        if (!hasBidFields && !hasOfferFields) {
            return false;
        }
        
        // Validate price fields are positive
        if (!Double.isNaN(bid0Price) && bid0Price <= 0.0) return false;
        if (!Double.isNaN(bid1Price) && bid1Price <= 0.0) return false;
        if (!Double.isNaN(bid2Price) && bid2Price <= 0.0) return false;
        if (!Double.isNaN(bid3Price) && bid3Price <= 0.0) return false;
        if (!Double.isNaN(bid4Price) && bid4Price <= 0.0) return false;
        if (!Double.isNaN(bid5Price) && bid5Price <= 0.0) return false;
        
        if (!Double.isNaN(offer0Price) && offer0Price <= 0.0) return false;
        if (!Double.isNaN(offer1Price) && offer1Price <= 0.0) return false;
        if (!Double.isNaN(offer2Price) && offer2Price <= 0.0) return false;
        if (!Double.isNaN(offer3Price) && offer3Price <= 0.0) return false;
        if (!Double.isNaN(offer4Price) && offer4Price <= 0.0) return false;
        if (!Double.isNaN(offer5Price) && offer5Price <= 0.0) return false;
        
        // Validate volumes are non-negative
        if (bid0Volume < 0) return false;
        if (bid1TradableVolume < 0) return false;
        if (bid2TradableVolume < 0) return false;
        if (bid3TradableVolume < 0) return false;
        if (bid4TradableVolume < 0) return false;
        if (bid5TradableVolume < 0) return false;
        
        if (offer0Volume < 0) return false;
        if (offer1TradableVolume < 0) return false;
        if (offer2TradableVolume < 0) return false;
        if (offer3TradableVolume < 0) return false;
        if (offer4TradableVolume < 0) return false;
        if (offer5TradableVolume < 0) return false;
        
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
    
    public int getSettleSpeed() { return settleSpeed; }
    public void setSettleSpeed(int settleSpeed) { this.settleSpeed = settleSpeed; }
    
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
    public double getBid0Price() { return bid0Price; }
    public void setBid0Price(double bid0Price) { this.bid0Price = bid0Price; }
    
    public double getBid0Yield() { return bid0Yield; }
    public void setBid0Yield(double bid0Yield) { this.bid0Yield = bid0Yield; }
    
    public String getBid0YieldType() { return bid0YieldType; }
    public void setBid0YieldType(String bid0YieldType) { this.bid0YieldType = bid0YieldType; }
    
    public double getBid0Volume() { return bid0Volume; }
    public void setBid0Volume(double bid0Volume) { this.bid0Volume = bid0Volume; }
    
    public double getOffer0Price() { return offer0Price; }
    public void setOffer0Price(double offer0Price) { this.offer0Price = offer0Price; }
    
    public double getOffer0Yield() { return offer0Yield; }
    public void setOffer0Yield(double offer0Yield) { this.offer0Yield = offer0Yield; }
    
    public String getOffer0YieldType() { return offer0YieldType; }
    public void setOffer0YieldType(String offer0YieldType) { this.offer0YieldType = offer0YieldType; }
    
    public double getOffer0Volume() { return offer0Volume; }
    public void setOffer0Volume(double offer0Volume) { this.offer0Volume = offer0Volume; }
    
    // Level 1 Fields
    public double getBid1Price() { return bid1Price; }
    public void setBid1Price(double bid1Price) { this.bid1Price = bid1Price; }
    
    public double getBid1Yield() { return bid1Yield; }
    public void setBid1Yield(double bid1Yield) { this.bid1Yield = bid1Yield; }
    
    public String getBid1YieldType() { return bid1YieldType; }
    public void setBid1YieldType(String bid1YieldType) { this.bid1YieldType = bid1YieldType; }
    
    public double getBid1TradableVolume() { return bid1TradableVolume; }
    public void setBid1TradableVolume(double bid1TradableVolume) { this.bid1TradableVolume = bid1TradableVolume; }
    
    public double getOffer1Price() { return offer1Price; }
    public void setOffer1Price(double offer1Price) { this.offer1Price = offer1Price; }
    
    public double getOffer1Yield() { return offer1Yield; }
    public void setOffer1Yield(double offer1Yield) { this.offer1Yield = offer1Yield; }
    
    public String getOffer1YieldType() { return offer1YieldType; }
    public void setOffer1YieldType(String offer1YieldType) { this.offer1YieldType = offer1YieldType; }
    
    public double getOffer1TradableVolume() { return offer1TradableVolume; }
    public void setOffer1TradableVolume(double offer1TradableVolume) { this.offer1TradableVolume = offer1TradableVolume; }
    
    // Level 2 Fields
    public double getBid2Price() { return bid2Price; }
    public void setBid2Price(double bid2Price) { this.bid2Price = bid2Price; }
    
    public double getBid2Yield() { return bid2Yield; }
    public void setBid2Yield(double bid2Yield) { this.bid2Yield = bid2Yield; }
    
    public String getBid2YieldType() { return bid2YieldType; }
    public void setBid2YieldType(String bid2YieldType) { this.bid2YieldType = bid2YieldType; }
    
    public double getBid2TradableVolume() { return bid2TradableVolume; }
    public void setBid2TradableVolume(double bid2TradableVolume) { this.bid2TradableVolume = bid2TradableVolume; }
    
    public double getOffer2Price() { return offer2Price; }
    public void setOffer2Price(double offer2Price) { this.offer2Price = offer2Price; }
    
    public double getOffer2Yield() { return offer2Yield; }
    public void setOffer2Yield(double offer2Yield) { this.offer2Yield = offer2Yield; }
    
    public String getOffer2YieldType() { return offer2YieldType; }
    public void setOffer2YieldType(String offer2YieldType) { this.offer2YieldType = offer2YieldType; }
    
    public double getOffer2TradableVolume() { return offer2TradableVolume; }
    public void setOffer2TradableVolume(double offer2TradableVolume) { this.offer2TradableVolume = offer2TradableVolume; }
    
    // Level 3 Fields
    public double getBid3Price() { return bid3Price; }
    public void setBid3Price(double bid3Price) { this.bid3Price = bid3Price; }
    
    public double getBid3Yield() { return bid3Yield; }
    public void setBid3Yield(double bid3Yield) { this.bid3Yield = bid3Yield; }
    
    public String getBid3YieldType() { return bid3YieldType; }
    public void setBid3YieldType(String bid3YieldType) { this.bid3YieldType = bid3YieldType; }
    
    public double getBid3TradableVolume() { return bid3TradableVolume; }
    public void setBid3TradableVolume(double bid3TradableVolume) { this.bid3TradableVolume = bid3TradableVolume; }
    
    public double getOffer3Price() { return offer3Price; }
    public void setOffer3Price(double offer3Price) { this.offer3Price = offer3Price; }
    
    public double getOffer3Yield() { return offer3Yield; }
    public void setOffer3Yield(double offer3Yield) { this.offer3Yield = offer3Yield; }
    
    public String getOffer3YieldType() { return offer3YieldType; }
    public void setOffer3YieldType(String offer3YieldType) { this.offer3YieldType = offer3YieldType; }
    
    public double getOffer3TradableVolume() { return offer3TradableVolume; }
    public void setOffer3TradableVolume(double offer3TradableVolume) { this.offer3TradableVolume = offer3TradableVolume; }
    
    // Level 4 Fields
    public double getBid4Price() { return bid4Price; }
    public void setBid4Price(double bid4Price) { this.bid4Price = bid4Price; }
    
    public double getBid4Yield() { return bid4Yield; }
    public void setBid4Yield(double bid4Yield) { this.bid4Yield = bid4Yield; }
    
    public String getBid4YieldType() { return bid4YieldType; }
    public void setBid4YieldType(String bid4YieldType) { this.bid4YieldType = bid4YieldType; }
    
    public double getBid4TradableVolume() { return bid4TradableVolume; }
    public void setBid4TradableVolume(double bid4TradableVolume) { this.bid4TradableVolume = bid4TradableVolume; }
    
    public double getOffer4Price() { return offer4Price; }
    public void setOffer4Price(double offer4Price) { this.offer4Price = offer4Price; }
    
    public double getOffer4Yield() { return offer4Yield; }
    public void setOffer4Yield(double offer4Yield) { this.offer4Yield = offer4Yield; }
    
    public String getOffer4YieldType() { return offer4YieldType; }
    public void setOffer4YieldType(String offer4YieldType) { this.offer4YieldType = offer4YieldType; }
    
    public double getOffer4TradableVolume() { return offer4TradableVolume; }
    public void setOffer4TradableVolume(double offer4TradableVolume) { this.offer4TradableVolume = offer4TradableVolume; }
    
    // Level 5 Fields
    public double getBid5Price() { return bid5Price; }
    public void setBid5Price(double bid5Price) { this.bid5Price = bid5Price; }
    
    public double getBid5Yield() { return bid5Yield; }
    public void setBid5Yield(double bid5Yield) { this.bid5Yield = bid5Yield; }
    
    public String getBid5YieldType() { return bid5YieldType; }
    public void setBid5YieldType(String bid5YieldType) { this.bid5YieldType = bid5YieldType; }
    
    public double getBid5TradableVolume() { return bid5TradableVolume; }
    public void setBid5TradableVolume(double bid5TradableVolume) { this.bid5TradableVolume = bid5TradableVolume; }
    
    public double getOffer5Price() { return offer5Price; }
    public void setOffer5Price(double offer5Price) { this.offer5Price = offer5Price; }
    
    public double getOffer5Yield() { return offer5Yield; }
    public void setOffer5Yield(double offer5Yield) { this.offer5Yield = offer5Yield; }
    
    public String getOffer5YieldType() { return offer5YieldType; }
    public void setOffer5YieldType(String offer5YieldType) { this.offer5YieldType = offer5YieldType; }
    
    public double getOffer5TradableVolume() { return offer5TradableVolume; }
    public void setOffer5TradableVolume(double offer5TradableVolume) { this.offer5TradableVolume = offer5TradableVolume; }
}