package com.sdd.etl.source.extract.db.quote;

import com.sdd.etl.model.SourceDataModel;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Concrete implementation of {@link SourceDataModel} for Bond Future Quote
 * data.
 * 
 * <p>
 * Represents standardized bond future quote records extracted from database.
 * Contains price, volume, limit, and Level 1 market depth information.
 * </p>
 */
public class BondFutureQuoteDataModel extends SourceDataModel {

    // --- Common Fields ---
    private String businessDate;
    private String exchProductId;
    private String productType;
    private String exchange;
    private String source;
    private int settleSpeed;
    private String level;
    private String status;

    // --- Price Fields ---
    private double lastTradePrice;
    private double openPrice;
    private double highPrice;
    private double lowPrice;
    private double closePrice;
    private double settlePrice;

    // --- Limit Fields ---
    private double upperLimit;
    private double lowerLimit;

    // --- Volume Fields ---
    private double totalVolume;
    private double totalTurnover;
    private double openInterest;

    // --- Level 1 Market Depth ---
    private double bid1Price;
    private double bid1Volume;
    private double offer1Price;
    private double offer1Volume;

    // --- Timestamps ---
    private LocalDateTime eventTime;
    private LocalDateTime receiveTime;

    /**
     * Constructs a new BondFutureQuoteDataModel with default values.
     */
    public BondFutureQuoteDataModel() {
        super();
        this.productType = "BOND_FUT";
        this.exchange = "CFFEX";
        this.source = "CFFEX";
        this.settleSpeed = 0;
        this.level = "L1";
        this.status = "Normal";

        // Initialize doubles to NaN
        this.lastTradePrice = Double.NaN;
        this.openPrice = Double.NaN;
        this.highPrice = Double.NaN;
        this.lowPrice = Double.NaN;
        this.closePrice = Double.NaN;
        this.settlePrice = Double.NaN;
        this.upperLimit = Double.NaN;
        this.lowerLimit = Double.NaN;
        this.totalTurnover = Double.NaN;

        this.bid1Price = Double.NaN;
        this.offer1Price = Double.NaN;
    }

    /**
     * Validates data integrity and completeness.
     * 
     * <p>
     * Validation rules:
     * <ol>
     * <li>businessDate must be non-null and match pattern YYYY.MM.DD</li>
     * <li>exchProductId must be non-null</li>
     * <li>eventTime must be non-null</li>
     * <li>lastTradePrice must be non-null (warn if NaN but record kept)</li>
     * </ol>
     * 
     * @return true if data is valid, false otherwise
     */
    @Override
    public boolean validate() {
        if (businessDate == null || !businessDate.matches("\\d{4}\\.\\d{2}\\.\\d{2}")) {
            return false;
        }

        if (exchProductId == null || exchProductId.trim().isEmpty()) {
            return false;
        }

        if (eventTime == null) {
            return false;
        }

        return true;
    }

    /**
     * Gets primary key value for this data model.
     * 
     * <p>
     * Primary key: businessDate:exchProductId:eventTime
     * </p>
     * 
     * @return composite primary key string
     */
    @Override
    public Object getPrimaryKey() {
        if (businessDate == null || exchProductId == null || eventTime == null) {
            return null;
        }
        return String.format("%s:%s:%s", businessDate, exchProductId,
                eventTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    /**
     * Gets source type identifier.
     * 
     * @return "bond_future_quote_db"
     */
    @Override
    public String getSourceType() {
        return "bond_future_quote_db";
    }

    // --- Getters and Setters ---

    public String getBusinessDate() {
        return businessDate;
    }

    public void setBusinessDate(String businessDate) {
        this.businessDate = businessDate;
    }

    public String getExchProductId() {
        return exchProductId;
    }

    public void setExchProductId(String exchProductId) {
        this.exchProductId = exchProductId;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getSettleSpeed() {
        return settleSpeed;
    }

    public void setSettleSpeed(int settleSpeed) {
        this.settleSpeed = settleSpeed;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getLastTradePrice() {
        return lastTradePrice;
    }

    public void setLastTradePrice(double lastTradePrice) {
        this.lastTradePrice = lastTradePrice;
    }

    public double getOpenPrice() {
        return openPrice;
    }

    public void setOpenPrice(double openPrice) {
        this.openPrice = openPrice;
    }

    public double getHighPrice() {
        return highPrice;
    }

    public void setHighPrice(double highPrice) {
        this.highPrice = highPrice;
    }

    public double getLowPrice() {
        return lowPrice;
    }

    public void setLowPrice(double lowPrice) {
        this.lowPrice = lowPrice;
    }

    public double getClosePrice() {
        return closePrice;
    }

    public void setClosePrice(double closePrice) {
        this.closePrice = closePrice;
    }

    public double getSettlePrice() {
        return settlePrice;
    }

    public void setSettlePrice(double settlePrice) {
        this.settlePrice = settlePrice;
    }

    public double getUpperLimit() {
        return upperLimit;
    }

    public void setUpperLimit(double upperLimit) {
        this.upperLimit = upperLimit;
    }

    public double getLowerLimit() {
        return lowerLimit;
    }

    public void setLowerLimit(double lowerLimit) {
        this.lowerLimit = lowerLimit;
    }

    public double getTotalVolume() {
        return totalVolume;
    }

    public void setTotalVolume(double totalVolume) {
        this.totalVolume = totalVolume;
    }

    public double getTotalTurnover() {
        return totalTurnover;
    }

    public void setTotalTurnover(double totalTurnover) {
        this.totalTurnover = totalTurnover;
    }

    public double getOpenInterest() {
        return openInterest;
    }

    public void setOpenInterest(double openInterest) {
        this.openInterest = openInterest;
    }

    public double getBid1Price() {
        return bid1Price;
    }

    public void setBid1Price(double bid1Price) {
        this.bid1Price = bid1Price;
    }

    public double getBid1Volume() {
        return bid1Volume;
    }

    public void setBid1Volume(double bid1Volume) {
        this.bid1Volume = bid1Volume;
    }

    public double getOffer1Price() {
        return offer1Price;
    }

    public void setOffer1Price(double offer1Price) {
        this.offer1Price = offer1Price;
    }

    public double getOffer1Volume() {
        return offer1Volume;
    }

    public void setOffer1Volume(double offer1Volume) {
        this.offer1Volume = offer1Volume;
    }

    public LocalDateTime getEventTime() {
        return eventTime;
    }

    public void setEventTime(LocalDateTime eventTime) {
        this.eventTime = eventTime;
    }

    public LocalDateTime getReceiveTime() {
        return receiveTime;
    }

    public void setReceiveTime(LocalDateTime receiveTime) {
        this.receiveTime = receiveTime;
    }
}
